package main.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import main.utility.UserInteractionId;

@Entity
@Table(name = "user_interaction")
public class UserInteraction {

    @EmbeddedId
    private UserInteractionId id;

    public UserInteraction() {}

    public UserInteraction(UserInteractionId id) {
        this.id = id;
    }

    public UserInteractionId getId() {
        return id;
    }

    public void setId(UserInteractionId id) {
        this.id = id;
    }

}
