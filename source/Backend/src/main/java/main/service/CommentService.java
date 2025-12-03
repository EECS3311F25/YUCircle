package main.service;

import main.entity.Comment;
import main.entity.Post;
import main.repository.CommentRepo;
import main.repository.PostRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import main.service.NotificationService; // added import

import java.util.List;

@Service
@Transactional
public class CommentService {

    private final CommentRepo commentRepo;
    private final PostRepo postRepo;
    private final NotificationService notificationService;

    // constructor injection
    public CommentService(CommentRepo commentRepo, PostRepo postRepo, NotificationService notificationService) {
        this.commentRepo = commentRepo;
        this.postRepo = postRepo;
        this.notificationService = notificationService;
    }

    public Comment addComment(Long postId, Comment comment) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        comment.setPost(post);
        Comment saved = commentRepo.save(comment);

        // Create notification for post owner (unless the commenter is the owner)
        try {
            String actor = saved.getUsername();
            String recipient = post.getUsername();
            if (recipient != null && actor != null && !recipient.equals(actor)) {
                String content = saved.getContent() == null ? "" : saved.getContent();
                String preview = content.length() > 100 ? content.substring(0, 100) + "…" : content;
                String msg = actor + " commented: \"" + preview + "\"";
                notificationService.createNotification(recipient, actor, "COMMENT", msg, post.getId());
            }
        } catch (Exception e) {
            // Don't break the comment creation if notification fails; log if you have logging.
            System.err.println("Failed to create comment notification: " + e.getMessage());
        }

        return saved;
    }

    public List<Comment> getComments(Long postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return commentRepo.findByPost(post);
    }

    public void deleteComment(Long commentId) {
        if (!commentRepo.existsById(commentId)) {
            throw new RuntimeException("Comment not found");
        }
        commentRepo.deleteById(commentId);
    }

}
