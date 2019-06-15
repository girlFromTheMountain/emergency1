package pl.oskarpolak.emergency.model;

import lombok.Data;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Data
public class User {
    private String nickname;
    private WebSocketSession webSocketSession;

    public User(WebSocketSession webSocketSession){
        this.webSocketSession = webSocketSession;
    }

    public void sendMessage(String message) throws IOException {
        webSocketSession.sendMessage(new TextMessage(message));
    }
}
