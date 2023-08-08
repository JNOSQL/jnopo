package br.org.soujava.coffewithjava.jnopo;

import br.org.soujava.coffewithjava.jnopo.core.GameOverLoserInfo;
import br.org.soujava.coffewithjava.jnopo.core.GameOverPlayerInfo;
import br.org.soujava.coffewithjava.jnopo.core.GameOverWinnerInfo;
import br.org.soujava.coffewithjava.jnopo.core.Movement;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;

import java.util.Optional;

@Entity
public record PlayerInfo(@Column String player, @Column Movement movement) {
    public static PlayerInfo of(GameOverPlayerInfo gamePlayer) {
        return new PlayerInfo(gamePlayer.player().name(), gamePlayer.movement());
    }

    public static PlayerInfo of(GameOverWinnerInfo winner) {
        return Optional.ofNullable(winner)
                .map(w -> new PlayerInfo(w.player().name(), w.movement()))
                .orElse(null);
    }

    public static PlayerInfo of(GameOverLoserInfo loser) {
        return Optional.ofNullable(loser)
                .map(w -> new PlayerInfo(w.player().name(), w.movement()))
                .orElse(null);
    }
}
