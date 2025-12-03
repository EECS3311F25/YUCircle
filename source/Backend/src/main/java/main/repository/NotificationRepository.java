package main.repository;

import main.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientUsernameOrderByTimestampDesc(String recipientUsername);
    List<Notification> findByRecipientUsernameAndReadFlagFalseOrderByTimestampDesc(String recipientUsername);
    long countByRecipientUsernameAndReadFlagFalse(String recipientUsername);
}
