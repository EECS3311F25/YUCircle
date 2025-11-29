package main.repository;

import main.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepo extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByTimestampDesc();

    List<Post> findByUsernameOrderByTimestampDesc(String username);

}
