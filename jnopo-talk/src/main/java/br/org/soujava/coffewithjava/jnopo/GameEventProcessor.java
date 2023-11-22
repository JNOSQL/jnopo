package br.org.soujava.coffewithjava.jnopo;

import br.org.soujava.coffewithjava.jnopo.core.GameOver;
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
        final GameOver game = event.gameover();
        var gameMatch= new GameMatch(
                game.gameId(),
                new PlayerInfo(game.playerA().name(),game.playerAMovement()),
                new PlayerInfo(game.playerB().name(),game.playerBMovement()),
                game.winnerInfo().map(g->new PlayerInfo(g.player().name(),g.movement())).orElse(PlayerInfo.NOBODY),
                game.loserInfo().map(g->new PlayerInfo(g.player().name(),g.movement())).orElse(PlayerInfo.NOBODY),
                game.isTied()
        );
        playoffs.save(gameMatch);
     }

}
