package main.service;

import main.entity.Post;
import main.entity.Like;
import main.entity.Comment;
import main.repository.PostRepo;
import main.repository.LikeRepo;
import main.repository.CommentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostCommandService {

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private LikeRepo likeRepo;

    @Autowired
    private CommentRepo commentRepo;

    public Post createPost(Post post) {
        return postRepo.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepo.findAllByOrderByTimestampDesc();
    }

    // 🔥 NEW — Get posts for a specific user
    public List<Post> getPostsByUser(String username) {
        return postRepo.findByUsernameOrderByTimestampDesc(username);
    }

    // Like/unlike a post
    public Post toggleLike(Long postId, String username) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<Like> existing = likeRepo.findByPostAndUsername(post, username);

        if (existing.isPresent()) {
            likeRepo.delete(existing.get());
            post.setLikes(post.getLikes() - 1);
        } else {
            Like like = new Like();
            like.setPost(post);
            like.setUsername(username);
            likeRepo.save(like);
            post.setLikes(post.getLikes() + 1);
        }

        return postRepo.save(post);
    }

    // Add comment
    public Comment addComment(Long postId, Comment comment) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        comment.setPost(post);
        return commentRepo.save(comment);
    }
    
    //Edit Post
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
    
    //Delete Post
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
