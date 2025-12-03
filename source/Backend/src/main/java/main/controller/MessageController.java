package main.controller;

import main.dto.PrivateMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import main.service.UserInteractionService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserInteractionService interactionService;


    // Store username -> sessionId mapping
    private static final Map<String, String> userSessions = new ConcurrentHashMap<>();

    @MessageMapping("/send")
    public void processMessage(String message) {
        messagingTemplate.convertAndSend("/topic/messages", "Received: " + message);
    }

    @MessageMapping("/register")
    public void registerUser(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        if (principal != null) {
            String username = principal.getName();
            String sessionId = headerAccessor.getSessionId();
            userSessions.put(username, sessionId);
        }
    }
    @GetMapping("/message/getUsers")
    public List<String> getAllMessagedUsers(@RequestParam String username) {
        return interactionService.getAllInteractionPartners(username);
    }


    @MessageMapping("/private")
    public void sendPrivateMessage(@Payload PrivateMessageDTO msg,
                                   Principal principal,
                                   SimpMessageHeaderAccessor headerAccessor) {

        String senderUsername = principal.getName();
        String senderSession = headerAccessor.getSessionId();
        userSessions.putIfAbsent(senderUsername, senderSession);

        interactionService.recordInteraction(senderUsername, msg.getToUser());

        String fullMessage = principal.getName() + ": " + msg.getMessage();

        String recipientSession = userSessions.get(msg.getToUser());

        messagingTemplate.convertAndSend(
                "/queue/messages-user" + recipientSession,
                fullMessage
        );

    }
}