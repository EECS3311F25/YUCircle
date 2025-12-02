package main.service;

import main.entity.UserInteraction;
import main.repository.UserInteractionRepo;
import main.utility.UserInteractionId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserInteractionService {

    @Autowired
    private UserInteractionRepo repo;

    public void recordInteraction(String userA, String userB) {
        // ensure canonical alphabetical order A < B
        String u1 = userA.compareTo(userB) < 0 ? userA : userB;
        String u2 = userA.compareTo(userB) < 0 ? userB : userA;

        UserInteractionId id = new UserInteractionId(u1, u2);

        if (!repo.existsById(id)) {
            repo.save(new UserInteraction(id));
        }
    }

    public List<String> getAllInteractionPartners(String username) {

        List<UserInteraction> sent = repo.findByIdFromUsername(username);
        List<UserInteraction> received = repo.findByIdToUsername(username);

        List<String> result = new ArrayList<>();

        for (UserInteraction ui : sent) {
            result.add(ui.getId().getToUsername());
        }

        for (UserInteraction ui : received) {
            result.add(ui.getId().getFromUsername());
        }

        return result;
    }
}
