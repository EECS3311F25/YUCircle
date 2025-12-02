package main.utility;

import java.io.Serializable;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserInteractionId implements Serializable {

    private String fromUsername;
    private String toUsername;

    public UserInteractionId() {}

    public UserInteractionId(String fromUsername, String toUsername) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInteractionId)) return false;
        UserInteractionId that = (UserInteractionId) o;
        return fromUsername.equals(that.fromUsername) &&
                toUsername.equals(that.toUsername);
    }

    @Override
    public int hashCode() {
        return fromUsername.hashCode() + toUsername.hashCode();
    }

    public String getFromUsername() {
        return this.fromUsername;
    }

    public String getToUsername() {
        return this.toUsername;
    }
}
