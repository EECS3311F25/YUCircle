package main.repository;

import main.entity.Like;
import main.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepo extends JpaRepository<Like, Long> {

    Optional<Like> findByPostAndUsername(Post post, String username);
   
    void deleteByPostId(Long postId);


}
