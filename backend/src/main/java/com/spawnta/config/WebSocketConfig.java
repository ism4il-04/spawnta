package com.spawnta.config;

import com.spawnta.security.ConnectionManager;
import com.spawnta.security.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final ConnectionManager connectionManager;

    public WebSocketConfig(JwtService jwtService, ConnectionManager connectionManager) {
        this.jwtService = jwtService;
        this.connectionManager = connectionManager;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Broadcast prefix for clients to subscribe
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix for client-to-server messages
        registry.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific queues (notifications/errors)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                    "http://localhost:4200", 
                    "http://localhost:4300", 
                    "http://localhost:3000",
                    "http://127.0.0.1:4200",
                    "http://127.0.0.1:4300",
                    "http://127.0.0.1:3000"
                )
                .withSockJS();
                
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                    "http://localhost:4200", 
                    "http://localhost:4300", 
                    "http://localhost:3000",
                    "http://127.0.0.1:4200",
                    "http://127.0.0.1:4300",
                    "http://127.0.0.1:3000"
                ); // Standard websocket handshake without SockJS fallback
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    return message;
                }

                String sessionId = accessor.getSessionId();

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        if (jwtService.isValid(token)) {
                            String email = jwtService.extractEmail(token);
                            Date expiryDate = jwtService.extractExpiration(token);
                            LocalDateTime expiration = LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault());

                            // Create simple Principal
                            UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(email, null, List.of());
                            accessor.setUser(authentication);

                            // Register active session
                            connectionManager.registerSession(email, sessionId, expiration);
                        } else {
                            throw new IllegalArgumentException("Invalid JWT token during WebSocket connection");
                        }
                    } else {
                        throw new IllegalArgumentException("Missing or invalid Authorization header during WebSocket connection");
                    }
                } 
                else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    if (destination != null && destination.startsWith("/topic/chats/")) {
                        // Extract chatId from /topic/chats/{chatId}
                        try {
                            String chatIdStr = destination.substring(13);
                            Long chatId = Long.parseLong(chatIdStr);
                            connectionManager.subscribeChat(sessionId, chatId);
                        } catch (Exception ignored) {}
                    }
                } 
                else if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
                    connectionManager.unsubscribeChat(sessionId);
                } 
                else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    connectionManager.unregisterSession(sessionId);
                }

                return message;
            }
        });
    }
}
