package br.org.soujava.coffewithjava.jnopo;

import br.org.soujava.coffewithjava.jnopo.core.GameOver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.nosql.document.DocumentTemplate;

@ApplicationScoped
public class GameService {

    @Inject
    DocumentTemplate template;

    public void save(@Observes GameEvent event) {

        GameOver gameover = event.gameover();

        GameMatch gameMatch = new GameMatch(gameover.gameId(),
                gameover.isTied(),
                PlayerInfo.of(gameover.playerAInfo()),
                PlayerInfo.of(gameover.playerBInfo()),
                PlayerInfo.of(gameover.winnerInfo().orElse(null)),
                PlayerInfo.of(gameover.loserInfo().orElse(null)));

        template.insert(gameMatch);

    }

}
