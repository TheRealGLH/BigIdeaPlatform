package DatabaseConnector.jpa;

import DatabaseConnector.jpa.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Integer> {
}
