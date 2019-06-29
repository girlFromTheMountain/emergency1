package pl.oskarpolak.emergency.model;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@EnableWebSocket
@Component
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {
    private List<User> sessions = new ArrayList<>();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(this, "/chat").setAllowedOrigins("*");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        User user = new User(session);
        sessions.add(user);

        user.sendMessage("<server>Witaj na naszym chacie! Twoja pierwsza wiadomość będzie Twoim nickiem!");
        sendWelcomeMessageToAllWithoutMe(session);
    }

    private void sendWelcomeMessageToAllWithoutMe(WebSocketSession session) throws IOException {
        for (User user : sessions) {
            if(!user.getWebSocketSession().getId().equals(session.getId())){
                user.sendMessage("<server>Nowy kolega dołączył do chatu!");
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User sender = findUserBySession(session);

        if(sender.getNickname() == null){
            sender.setNickname(message.getPayload());
            sender.sendMessage("<server>Ustawiłeś swój nickname!");
            return;
        }

        for (User user : sessions) {
            String messageAsString = sender.getNickname() + " (" + formatter.format(LocalDateTime.now()) + "): " + message.getPayload();
            user.sendMessage(messageAsString);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            sessions.remove(findUserBySession(session));
    }

    private User findUserBySession(WebSocketSession session){
       return sessions.stream()
                .filter(s -> s.getWebSocketSession().getId().equals(session.getId()))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }
}
