package main.service;

import main.entity.Post;
import main.entity.Like;
import main.entity.Comment;
import main.repository.PostRepo;
import main.repository.LikeRepo;
import main.repository.CommentRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import main.service.NotificationService;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PostCommandService {

    private final PostRepo postRepo;
    private final LikeRepo likeRepo;
    private final CommentRepo commentRepo;
    private final NotificationService notificationService;

    // constructor injection
    public PostCommandService(PostRepo postRepo,
                              LikeRepo likeRepo,
                              CommentRepo commentRepo,
                              NotificationService notificationService) {
        this.postRepo = postRepo;
        this.likeRepo = likeRepo;
        this.commentRepo = commentRepo;
        this.notificationService = notificationService;
    }

    public Post createPost(Post post) {
        return postRepo.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepo.findAllByOrderByTimestampDesc();
    }

    // Get posts for a specific user
    public List<Post> getPostsByUser(String username) {
        return postRepo.findByUsernameOrderByTimestampDesc(username);
    }

    // Like/unlike a post
    public Post toggleLike(Long postId, String username) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<Like> existing = likeRepo.findByPostAndUsername(post, username);

        if (existing.isPresent()) {
            // unlike
            likeRepo.delete(existing.get());
            int likes = post.getLikes(); // primitive int usage
            post.setLikes(Math.max(0, likes - 1));
        } else {
            // add like
            Like like = new Like();
            like.setPost(post);
            like.setUsername(username);
            likeRepo.save(like);
            int likes = post.getLikes(); // primitive int usage
            post.setLikes(likes + 1);

            // create notification for post owner (unless self)
            try {
                String actor = username;
                String recipient = post.getUsername();
                if (recipient != null && actor != null && !recipient.equals(actor)) {
                    String msg = actor + " liked your post.";
                    notificationService.createNotification(recipient, actor, "LIKE", msg, post.getId());
                }
            } catch (Exception e) {
                // avoid breaking the like operation if notification fails
                System.err.println("Failed to create like notification: " + e.getMessage());
            }
        }

        return postRepo.save(post);
    }

    // Add comment
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
            System.err.println("Failed to create comment notification: " + e.getMessage());
        }

        return saved;
    }

    // Edit Post
    public Post editPost(Long postId, String newContent, String newImageUrl) {

        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (newContent != null) {
            post.setContent(newContent);
        }

        if (newImageUrl != null) {
            post.setImageUrl(newImageUrl);
        }

        return postRepo.save(post);
    }

    // Delete Post
    public void deletePost(Long postId) {
        if (!postRepo.existsById(postId)) {
            throw new RuntimeException("Post not found");
        }

        // Delete likes associated with this post
        likeRepo.deleteByPostId(postId);

        // Delete comments associated with this post
        commentRepo.deleteByPostId(postId);

        // Delete the post itself
        postRepo.deleteById(postId);
    }
}
