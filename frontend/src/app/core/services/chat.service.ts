import { Injectable, inject, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, Subject } from 'rxjs';
import { Client, IMessage } from '@stomp/stompjs';
import { AuthService } from './auth.service';
import { NotificationToastService } from './notification-toast.service';
import { Router } from '@angular/router';

export interface ChatResponse {
  id: number;
  type: 'GROUP' | 'PRIVATE';
  activityId: number | null;
  activityTitle: string | null;
  status: 'ACTIVE' | 'ARCHIVED' | 'BLOCKED';
  createdAt: string;
  title: string;
  avatarUrl: string | null;
  lastMessage: string;
  lastMessageTime: string | null;
  lastMessageSender: string | null;
  notificationsEnabled: boolean;
  blockedByUserId: number | null;
  participantStatus: string;
}

export interface MessageResponse {
  id: number;
  chatId: number;
  senderId: number | null;
  senderName: string;
  senderAvatarUrl: string | null;
  content: string;
  status: 'ACTIVE' | 'DELETED';
  createdAt: string;
}

export interface PaginatedMessages {
  content: MessageResponse[];
  totalPages: number;
  totalElements: number;
  last: boolean;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(NotificationToastService);
  private readonly router = inject(Router);
  private readonly ngZone = inject(NgZone);

  private readonly apiUrl = 'http://localhost:8080/api/chats';
  private stompClient: Client | null = null;
  
  // Real-time events stream for active chat room
  private readonly messageStreamSubject = new Subject<{ type: string; payload: any }>();
  public readonly messageStream$ = this.messageStreamSubject.asObservable();

  // Connection status indicator
  private readonly connectionStatusSubject = new BehaviorSubject<boolean>(false);
  public readonly connectionStatus$ = this.connectionStatusSubject.asObservable();

  // Unread message counter for navbar badge
  private readonly unreadCountSubject = new BehaviorSubject<number>(0);
  public readonly unreadCount$ = this.unreadCountSubject.asObservable();

  // Track the currently active chat to know when NOT to count as unread
  private activeChatId: number | null = null;

  private reconnectAttempts = 0;
  private currentSubscribedChatId: number | null = null;
  private readonly subscribedChats = new Set<number>();
  private isConnecting = false;
  private subscribedToErrorQueue = false;

  /** Set which chat the user is currently viewing (to avoid counting it as unread) */
  public setActiveChatId(id: number | null): void {
    this.activeChatId = id;
    if (id !== null) {
      this.unreadCountSubject.next(0);
    }
  }

  /** Reset unread badge count */
  public resetUnread(): void {
    this.unreadCountSubject.next(0);
  }

  /** Increment unread count for messages arriving in background */
  public incrementUnread(): void {
    this.unreadCountSubject.next(this.unreadCountSubject.value + 1);
  }

  constructor() {
    // Proactively connect if user is already logged in at startup
    if (this.authService.isLoggedIn()) {
      this.connectWebSocket();
    }
  }

  /**
   * Establishes a secure STOMP WebSocket connection with JWT auth & exponential backoff.
   */
  public connectWebSocket(): void {
    const token = this.authService.getAccessToken();
    if (!token) return;

    // Prevent concurrent connection attempts
    if (this.isConnecting) return;
    if (this.stompClient && this.stompClient.connected) return;

    // Deactivate any existing ghost client to stop its event handlers
    if (this.stompClient) {
      try { this.stompClient.deactivate(); } catch (_) {}
      this.stompClient = null;
    }

    this.isConnecting = true;
    this.subscribedToErrorQueue = false;

    // Config Stomp Client
    this.stompClient = new Client({
      brokerURL: 'ws://localhost:8080/ws', // Native WebSocket endpoint
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      heartbeatIncoming: 30000, // Heartbeat every 30s (Requirement 9.4)
      heartbeatOutgoing: 30000,
      reconnectDelay: 0, // We handle reconnection manually
      debug: (str) => {
        // Only log non-heartbeat messages to reduce noise
        if (!str.includes('>>> PING') && !str.includes('<<< PONG')) {
          console.debug('[STOMP]', str);
        }
      }
    });

    this.stompClient.onConnect = () => {
      this.ngZone.run(() => {
        this.isConnecting = false;
        this.reconnectAttempts = 0;
        this.connectionStatusSubject.next(true);
        console.log('STOMP Connected successfully!');
        
        this.subscribedChats.clear(); // Clear local cache on fresh connect

        // Subscribe to personal error/auth queue ONCE per connection
        if (this.stompClient && !this.subscribedToErrorQueue) {
          this.subscribedToErrorQueue = true;
          this.stompClient.subscribe('/user/queue/errors', (message: IMessage) => {
            this.ngZone.run(() => {
              try {
                const body = JSON.parse(message.body);
                if (body.type === 'AUTH_EXPIRED') {
                  console.error('Session JWT expired event received!');
                  this.authService.logout();
                  this.messageStreamSubject.next({ type: 'AUTH_EXPIRED', payload: body });
                }
              } catch (ignored) {}
            });
          });
        }

        // Fetch all chats and subscribe to them for global background notifications
        this.getUserChats().subscribe({
          next: (chats) => {
            chats.forEach(c => this.subscribeToChat(c.id));
          }
        });

        // Sync missing messages by telling listeners to refresh (Requirement 9.3)
        this.messageStreamSubject.next({ type: 'RECONNECTED', payload: null });
      });
    };

    this.stompClient.onDisconnect = () => {
      this.ngZone.run(() => {
        this.isConnecting = false;
        this.connectionStatusSubject.next(false);
        console.warn('STOMP Disconnected');
      });
    };

    this.stompClient.onWebSocketClose = () => {
      this.ngZone.run(() => {
        this.isConnecting = false;
        this.connectionStatusSubject.next(false);
        this.reconnectAttempts++;

        // Exponential Backoff Reconnection limit to max 3 times (Requirement 9.1 & 9.2)
        if (this.reconnectAttempts <= 3) {
          const backoffDelay = Math.pow(2, this.reconnectAttempts) * 1000 + 1000; // 3s, 5s, 9s delay
          console.log(`WebSocket closed. Retrying connection in ${backoffDelay}ms (Attempt ${this.reconnectAttempts}/3)...`);
          
          setTimeout(() => {
            this.connectWebSocket();
          }, backoffDelay);
        } else {
          console.error('STOMP WebSocket: Max reconnection attempts reached.');
        }
      });
    };

    this.stompClient.onStompError = (frame) => {
      this.ngZone.run(() => {
        this.isConnecting = false;
        console.error('STOMP protocol error:', frame.headers['message']);
      });
    };

    this.stompClient.activate();
  }

  /**
   * Cleanly disconnects the WebSocket.
   */
  public disconnectWebSocket(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
      this.connectionStatusSubject.next(false);
      this.isConnecting = false;
      this.subscribedToErrorQueue = false;
    }
  }

  /**
   * Subscribes to a specific chat room topic to listen to real-time events.
   */
  public subscribeToChat(chatId: number): void {
    this.currentSubscribedChatId = chatId;

    if (!this.stompClient || !this.stompClient.connected) {
      console.warn('Cannot subscribe. STOMP is not connected yet.');
      return;
    }

    if (this.subscribedChats.has(chatId)) {
      return; // Already subscribed
    }
    
    this.subscribedChats.add(chatId);

    const destination = `/topic/chats/${chatId}`;
    
    this.stompClient.subscribe(destination, (message: IMessage) => {
      try {
        const body = JSON.parse(message.body);
        this.messageStreamSubject.next(body);
        
        // Increment unread badge if the message is for a different chat
        if (body.type === 'MESSAGE') {
          const msgChatId = body.payload?.chatId;
          const msg = body.payload as MessageResponse;
          
          if (msgChatId !== undefined && msgChatId !== this.activeChatId) {
            this.incrementUnread();
            
            // Afficher une notification toast
            this.toastService.showMessage(
              msg.senderName || 'Nouveau message',
              msg.content,
              msg.senderAvatarUrl || undefined,
              () => {
                // Naviguer vers le chat quand on clique sur la notification
                this.router.navigate(['/chat'], { queryParams: { id: msgChatId } });
              }
            );
          }
        }
      } catch (err) {
        console.error('Failed to parse WebSocket event payload', err);
      }
    });

    // Also subscribe to secure personal notifications/errors queue
    this.stompClient.subscribe('/user/queue/errors', (message: IMessage) => {
      try {
        const body = JSON.parse(message.body);
        if (body.type === 'AUTH_EXPIRED') {
          console.error('Session JWT expired event received!');
          this.authService.logout();
          this.messageStreamSubject.next({ type: 'AUTH_EXPIRED', payload: body });
        }
      } catch (ignored) {}
    });
  }

  /**
   * Sends a chat message in real time.
   */
  public sendStompMessage(chatId: number, content: string): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({
        destination: `/app/chats/${chatId}/send`,
        body: JSON.stringify({ content })
      });
    } else {
      // Fallback to REST API if WebSocket is down
      console.warn('STOMP WebSocket disconnected. Falling back to REST API...');
      this.sendMessageRest(chatId, content).subscribe({
        next: (res) => this.messageStreamSubject.next({ type: 'MESSAGE', payload: res }),
        error: (err) => console.error('REST fallback send message failed:', err)
      });
    }
  }

  // ─── REST APIs ─────────────────────────────────────────────────────────────

  public getUserChats(): Observable<ChatResponse[]> {
    return this.http.get<ChatResponse[]>(this.apiUrl);
  }

  public getChatMessages(chatId: number, page = 0, size = 50): Observable<PaginatedMessages> {
    return this.http.get<PaginatedMessages>(`${this.apiUrl}/${chatId}/messages`, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  public createPrivateChat(targetUserId: number): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.apiUrl}/private`, { targetUserId });
  }

  public sendMessageRest(chatId: number, content: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/${chatId}/messages`, { content });
  }

  public deleteMessage(chatId: number, messageId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${chatId}/messages/${messageId}`);
  }

  public muteParticipant(chatId: number, userId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${chatId}/participants/${userId}/mute`, {});
  }

  public kickParticipant(chatId: number, userId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${chatId}/participants/${userId}/kick`, {});
  }

  public toggleNotifications(chatId: number, enabled: boolean): Observable<any> {
    return this.http.put(`${this.apiUrl}/${chatId}/notifications`, { enabled });
  }

  public leaveChat(chatId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${chatId}/leave`);
  }

  public blockChat(chatId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${chatId}/block`, {});
  }

  public unblockChat(chatId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${chatId}/unblock`, {});
  }

  public deleteConversation(chatId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${chatId}/conversation`);
  }
}
