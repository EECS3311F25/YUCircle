package main.service;

import main.entity.Notification;
import main.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public Notification createNotification(String recipientUsername,
                                           String actorUsername,
                                           String type,
                                           String message,
                                           Long postId) {
        Notification n = new Notification();
        n.setRecipientUsername(recipientUsername);
        n.setActorUsername(actorUsername);
        n.setType(type);
        n.setMessage(message);
        n.setPostId(postId);
        n.setReadFlag(false);
        n.setTimestamp(Instant.now());
        return repo.save(n);
    }

    public List<Notification> getNotificationsForUser(String username) {
        return repo.findByRecipientUsernameOrderByTimestampDesc(username);
    }

    public List<Notification> getUnreadNotificationsForUser(String username) {
        return repo.findByRecipientUsernameAndReadFlagFalseOrderByTimestampDesc(username);
    }

    public long countUnread(String username) {
        return repo.countByRecipientUsernameAndReadFlagFalse(username);
    }

    @Transactional
    public void markAsRead(Long id) {
        repo.findById(id).ifPresent(n -> {
            n.setReadFlag(true);
            // saved automatically at transaction commit
        });
    }

    @Transactional
    public void markAllReadForUser(String username) {
        List<Notification> unread = repo.findByRecipientUsernameAndReadFlagFalseOrderByTimestampDesc(username);
        for (Notification n : unread) n.setReadFlag(true);
    }
}
