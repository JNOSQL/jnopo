package br.org.soujava.coffewithjava.jnopo;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;

@ApplicationScoped
public class GameEventProcessor {

    @Inject
    @Database(DatabaseType.DOCUMENT)
    Playoffs playoffs;

    @WithSpan
    public void process(@Observes GameEvent event) {
        var game = event.gameover();
        var match = new GameMatch(game.gameId(),
                new PlayerInfo(game.playerA().name(), game.playerAMovement()),
                new PlayerInfo(game.playerB().name(), game.playerBMovement()),
                game.isTied(),
                game.winnerInfo()
                        .map(p -> new PlayerInfo(p.player().name(), p.movement()))
                        .orElse(null),
                game.loserInfo()
                        .map(p -> new PlayerInfo(p.player().name(), p.movement()))
                        .orElse(null)
        );
        playoffs.save(match);
    }

}
