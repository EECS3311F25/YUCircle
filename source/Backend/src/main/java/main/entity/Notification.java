package main.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // username who receives this notification
    @Column(nullable = false)
    private String recipientUsername;

    // username who performed the action (liker / commenter / self for profile edit)
    private String actorUsername;

    // "LIKE", "COMMENT", "PROFILE_EDIT" - simple enum-as-string
    @Column(nullable = false)
    private String type;

    // Optional: related post id if applicable
    private Long postId;

    // human readable message shown to user
    @Column(length = 1000)
    private String message;

    // whether user has read it
    private boolean readFlag = false;

    private Instant timestamp = Instant.now();

    public Notification() {}

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecipientUsername() { return recipientUsername; }
    public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }

    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isReadFlag() { return readFlag; }
    public void setReadFlag(boolean readFlag) { this.readFlag = readFlag; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
