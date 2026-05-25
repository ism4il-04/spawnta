import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnInit, OnDestroy, ViewChild, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { ChatResponse, ChatService, MessageResponse } from '../../core/services/chat.service';
import { ProfileService, UserProfile } from '../../core/services/profile.service';
import { AuthService } from '../../core/services/auth.service';
import { MessageFormatterPipe } from '../../core/pipes/message-formatter.pipe';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MessageFormatterPipe],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss'
})
export class ChatComponent implements OnInit, OnDestroy {
  private readonly chatService = inject(ChatService);
  private readonly profileService = inject(ProfileService);
  protected readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  protected chats = signal<ChatResponse[]>([]);
  protected selectedChat = signal<ChatResponse | null>(null);
  protected messages = signal<MessageResponse[]>([]);
  protected currentUserProfile = signal<UserProfile | null>(null);
  protected activeUserId = signal<number | null>(null);

  // Pagination for scroll history loading
  protected currentPage = 0;
  protected hasMoreMessages = true;
  protected isLoadingMessages = false;

  // New message text state
  protected newMessageText = '';
  protected readonly maxMessageLength = 2000;

  // UI Moderation Panel state
  protected showModerationPanel = false;

  private subscription: Subscription = new Subscription();

  ngOnInit(): void {
    // 1. Secure WS connection
    this.chatService.connectWebSocket();
    // Reset unread badge when user opens the chat page
    this.chatService.resetUnread();

    // 2. Load User Profile details
    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.currentUserProfile.set(profile);
        this.activeUserId.set(profile.id);
      },
      error: (err) => console.error('Failed to load user profile:', err)
    });

    // 3. Load user chats list then auto-select first if no query param
    this.loadChats(true);

    // 4. Subscribe to Real-Time WS message stream
    this.subscription.add(
      this.chatService.messageStream$.subscribe({
        next: (event) => this.handleWebSocketEvent(event)
      })
    );

    // 5. Parse query parameters
    this.route.queryParams.subscribe((params) => {
      const chatIdParam = params['id'];
      const targetUserIdParam = params['userId'];

      if (chatIdParam) {
        const chatId = Number(chatIdParam);
        this.selectChatById(chatId);
      } else if (targetUserIdParam) {
        const targetUserId = Number(targetUserIdParam);
        this.startPrivateChat(targetUserId);
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
    // Do not disconnect WebSocket globally here in case the user toggles tabs,
    // let it persist as defined in ConnectionManager unless they logout.
  }

  protected loadChats(autoSelectFirst = false): void {
    this.chatService.getUserChats().subscribe({
      next: (chatsList) => {
        this.chats.set(chatsList);
        // Auto-select first chat if none is selected and no query param
        if (autoSelectFirst && chatsList.length > 0 && !this.selectedChat() && !this.route.snapshot.queryParams['id']) {
          this.selectChat(chatsList[0]);
        }
      },
      error: (err) => console.error('Failed to load conversations:', err)
    });
  }

  protected selectChat(chat: ChatResponse): void {
    this.selectedChat.set(chat);
    this.messages.set([]);
    this.currentPage = 0;
    this.hasMoreMessages = true;
    this.showModerationPanel = false;

    // Notify service which chat is active (for unread tracking)
    this.chatService.setActiveChatId(chat.id);

    // Connect to WebSocket room
    this.chatService.subscribeToChat(chat.id);
    
    // Fetch initial page of history
    this.loadMessages(chat.id);

    // Update query params without reloading route
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { id: chat.id },
      queryParamsHandling: 'merge'
    });
  }

  private selectChatById(chatId: number): void {
    // Wait until chats are loaded
    this.chatService.getUserChats().subscribe((chatsList) => {
      this.chats.set(chatsList);
      const chat = chatsList.find(c => c.id === chatId);
      if (chat) {
        this.selectChat(chat);
      }
    });
  }

  private startPrivateChat(targetUserId: number): void {
    this.chatService.createPrivateChat(targetUserId).subscribe({
      next: (chat) => {
        this.loadChats();
        this.selectChat(chat);
      },
      error: (err) => console.error('Failed to initiate private chat:', err)
    });
  }

  protected loadMessages(chatId: number, append = false): void {
    if (this.isLoadingMessages) return;
    this.isLoadingMessages = true;

    this.chatService.getChatMessages(chatId, this.currentPage).subscribe({
      next: (paginated) => {
        const sortedContent = [...paginated.content].reverse(); // oldest first
        if (append) {
          this.messages.update(current => [...sortedContent, ...current]);
        } else {
          this.messages.set(sortedContent);
          setTimeout(() => this.scrollToBottom(), 50);
        }

        this.hasMoreMessages = !paginated.last;
        this.isLoadingMessages = false;
      },
      error: (err) => {
        console.error('Failed to load messages history:', err);
        this.isLoadingMessages = false;
      }
    });
  }

  protected onScroll(event: Event): void {
    const el = event.target as HTMLElement;
    // When scrolling near the top, load previous messages page
    if (el.scrollTop <= 10 && this.hasMoreMessages && !this.isLoadingMessages) {
      const activeChat = this.selectedChat();
      if (activeChat) {
        this.currentPage++;
        this.loadMessages(activeChat.id, true);
      }
    }
  }

  protected sendMessage(): void {
    const activeChat = this.selectedChat();
    if (!activeChat || !this.newMessageText.trim()) return;

    if (this.newMessageText.length > this.maxMessageLength) {
      alert(`Le message dépasse la limite de ${this.maxMessageLength} caractères`);
      return;
    }

    const text = this.newMessageText;
    this.newMessageText = '';

    // Utiliser l'API REST pour avoir un retour instantané et éviter l'attente
    this.chatService.sendMessageRest(activeChat.id, text).subscribe({
      next: (msg) => {
        // Ajout immédiat (optimistic update)
        this.messages.update(current => {
          if (current.find(m => m.id === msg.id)) return current;
          return [...current, msg];
        });
        setTimeout(() => this.scrollToBottom(), 50);
        
        // Mettre à jour la prévisualisation dans la liste
        this.chats.update(currentChats => {
          return currentChats.map(c => {
            if (c.id === msg.chatId) {
              return {
                ...c,
                lastMessage: msg.content,
                lastMessageTime: msg.createdAt,
                lastMessageSender: msg.senderName
              };
            }
            return c;
          });
        });
      },
      error: (err) => {
        console.error('Erreur d\'envoi', err);
        alert('Erreur lors de l\'envoi du message');
      }
    });
  }

  protected handleWebSocketEvent(event: { type: string; payload: any }): void {
    const activeChat = this.selectedChat();

    if (event.type === 'MESSAGE') {
      const msg = event.payload as MessageResponse;
      if (activeChat && msg.chatId === activeChat.id) {
        // Éviter les doublons avec l'envoi immédiat
        this.messages.update(current => {
          if (current.find(m => m.id === msg.id)) return current;
          return [...current, msg];
        });
        setTimeout(() => this.scrollToBottom(), 50);
      }

      // Update preview in list
      this.chats.update(currentChats => {
        return currentChats.map(c => {
          if (c.id === msg.chatId) {
            return {
              ...c,
              lastMessage: msg.content,
              lastMessageTime: msg.createdAt,
              lastMessageSender: msg.senderName
            };
          }
          return c;
        });
      });
    } 
    else if (event.type === 'MESSAGE_DELETED') {
      const deletedMsgId = Number(event.payload);
      this.messages.update(current => current.filter(m => m.id !== deletedMsgId));
    } 
    else if (event.type === 'PARTICIPANT_UPDATE') {
      // Reload chats list or trigger status change message
      this.loadChats();
    }
    else if (event.type === 'RECONNECTED') {
      if (activeChat) {
        this.currentPage = 0;
        this.loadMessages(activeChat.id);
      }
    }
    else if (event.type === 'AUTH_EXPIRED') {
      alert('Votre session de chat a expiré. Vous allez être redirigé vers la page de connexion.');
      this.router.navigate(['/login']);
    }
  }

  protected deleteMessage(messageId: number): void {
    const activeChat = this.selectedChat();
    if (!activeChat) return;

    if (confirm('Voulez-vous supprimer ce message ?')) {
      this.chatService.deleteMessage(activeChat.id, messageId).subscribe({
        next: () => {
          this.messages.update(current => current.filter(m => m.id !== messageId));
        },
        error: (err) => alert(err.error?.error || 'Erreur lors de la suppression')
      });
    }
  }

  // ─── Moderation Controls ───────────────────────────────────────────────────

  protected isModerator(): boolean {
    const activeChat = this.selectedChat();
    const userId = this.activeUserId();
    if (!activeChat || !userId || activeChat.type !== 'GROUP') return false;
    
    // In group chats, host has moderator privileges
    return true; // The UI details are conditional depending on host status resolved in HTML
  }

  protected toggleNotifications(): void {
    const activeChat = this.selectedChat();
    if (!activeChat) return;

    const newValue = !activeChat.notificationsEnabled;
    this.chatService.toggleNotifications(activeChat.id, newValue).subscribe({
      next: () => {
        activeChat.notificationsEnabled = newValue;
      },
      error: (err) => console.error(err)
    });
  }

  protected blockConversation(): void {
    const activeChat = this.selectedChat();
    if (!activeChat) return;

    if (confirm('Voulez-vous bloquer cette conversation privée ?')) {
      this.chatService.blockChat(activeChat.id).subscribe({
        next: () => {
          activeChat.status = 'BLOCKED';
          alert('Conversation bloquée.');
        },
        error: (err) => alert(err.error?.error || 'Erreur lors du blocage')
      });
    }
  }

  protected unblockConversation(): void {
    const activeChat = this.selectedChat();
    if (!activeChat) return;

    if (confirm('Voulez-vous débloquer cette conversation privée ?')) {
      this.chatService.unblockChat(activeChat.id).subscribe({
        next: () => {
          activeChat.status = 'ACTIVE';
          alert('Conversation débloquée.');
        },
        error: (err) => alert(err.error?.error || 'Erreur lors du déblocage')
      });
    }
  }

  protected leaveConversation(): void {
    const activeChat = this.selectedChat();
    if (!activeChat) return;

    if (confirm('Voulez-vous quitter cette discussion ?')) {
      this.chatService.leaveChat(activeChat.id).subscribe({
        next: () => {
          this.selectedChat.set(null);
          this.loadChats();
        },
        error: (err) => alert(err.error?.error || 'Erreur')
      });
    }
  }

  private scrollToBottom(): void {
    try {
      if (this.scrollContainer) {
        const el = this.scrollContainer.nativeElement;
        el.scrollTop = el.scrollHeight;
      }
    } catch (err) {}
  }
}
