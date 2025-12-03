package main.controller;

import main.entity.Notification;
import main.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    // Get all notifications for a user
    @GetMapping("/user/{username}")
    public ResponseEntity<List<Notification>> getForUser(@PathVariable String username) {
        return ResponseEntity.ok(service.getNotificationsForUser(username));
    }

    // Get unread count
    @GetMapping("/user/{username}/unread-count")
    public ResponseEntity<Long> unreadCount(@PathVariable String username) {
        return ResponseEntity.ok(service.countUnread(username));
    }

    // Mark single notification as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        service.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    // Mark all as read for a user
    @PatchMapping("/mark-all-read/{username}")
    public ResponseEntity<Void> markAllRead(@PathVariable String username) {
        service.markAllReadForUser(username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Notification> create(@RequestBody Notification n) {
        Notification saved = service.createNotification(n.getRecipientUsername(), n.getActorUsername(), n.getType(), n.getMessage(), n.getPostId());
        return ResponseEntity.ok(saved);
    }
}
