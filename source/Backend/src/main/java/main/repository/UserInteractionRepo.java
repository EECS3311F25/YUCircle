package main.repository;

import main.entity.Student;
import main.entity.UserInteraction;
import main.utility.UserInteractionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInteractionRepo extends JpaRepository<UserInteraction, UserInteractionId> {

    List<UserInteraction> findByIdFromUsername(String username);
    List<UserInteraction> findByIdToUsername(String username);

}
