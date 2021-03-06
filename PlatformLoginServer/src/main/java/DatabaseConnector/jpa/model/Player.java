package DatabaseConnector.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
public class Player {

    private @Id
    String name;
    @NotNull
    private String password;
    private Long score = 0L;

    @ManyToMany
    @JoinTable(name = "game_player")
    private Set<Game> game;
    private boolean banned = false;

    public Player(String name, String password, Long score) {
        this.name = name;
        this.password = password;
        this.score = score;
    }

    public Player(String name, String password) {
        this.name = name;
        this.password = password;
        this.score = 0L;
    }

    public Player() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Set<Game> getGame() {
        return game;
    }

    public void setGame(Set<Game> game) {
        this.game = game;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }
}
