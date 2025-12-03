package main.service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendMessage(String username, String message) {
        messagingTemplate.convertAndSendToUser(username, "/queue/reply", message);
    }

}
