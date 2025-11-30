package main.controller;

import main.entity.Comment;
import main.entity.Post;
import main.repository.CommentRepo;
import main.repository.PostRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "http://localhost:5173")
public class CommentController {

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private PostRepo postRepo;

    // Get all comments for a specific post
    @GetMapping("/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId) {
        Post post = postRepo.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }

        List<Comment> comments = commentRepo.findByPost(post);
        return ResponseEntity.ok(comments);
    }

    // Add a new comment to a specific post
    @PostMapping("/{postId}")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId, @RequestBody Comment comment) {
        // Find the post being commented on
        Post post = postRepo.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }

        // Create a fresh Comment object to avoid Hibernate “unsaved-value mapping” issues
        Comment newComment = new Comment();
        newComment.setUsername(comment.getUsername());
        newComment.setContent(comment.getContent());
        newComment.setPost(post);

        // Save the comment
        Comment saved = commentRepo.save(newComment);
        return ResponseEntity.ok(saved);
    }

    // Delete a specific comment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        if (!commentRepo.existsById(commentId)) {
            return ResponseEntity.notFound().build();
        }
        commentRepo.deleteById(commentId);
        return ResponseEntity.noContent().build();
    }
}
