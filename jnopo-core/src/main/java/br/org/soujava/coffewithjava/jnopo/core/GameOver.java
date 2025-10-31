package br.org.soujava.coffewithjava.jnopo.core;

import java.util.Objects;
import java.util.Optional;

public record GameOver(String gameId,
        Player playerA,
        Player playerB,
        Movement playerAMovement,
        Movement playerBMovement) implements GameState {

    public GameOver {
        Objects.requireNonNull(gameId, "game id is required");
        Objects.requireNonNull(playerA, "player 'A' is required");
        Objects.requireNonNull(playerB, "player 'B' is required");
        Objects.requireNonNull(playerAMovement, "player 'A' movement is required");
        Objects.requireNonNull(playerBMovement, "player 'B' movement is required");
    }

    public boolean isTied() {
        return playerAMovement.equals(playerBMovement);
    }

    public Optional<Player> winner() {
        if (isTied()) {
            return Optional.empty();
        }
        return playerAMovement.beats(playerBMovement) ? Optional.of(playerA) : Optional.of(playerB);
    }

    public Optional<Player> loser() {
        if (isTied()) {
            return Optional.empty();
        }
        return playerAMovement.beats(playerBMovement) ? Optional.of(playerB) : Optional.of(playerA);
    }

    public Optional<Movement> winnerMovement() {
        if (isTied()) {
            return Optional.empty();
        }
        return playerAMovement.beats(playerBMovement) ? Optional.of(playerAMovement) : Optional.of(playerBMovement);
    }

    public Optional<Movement> loserMovement() {
        if (isTied()) {
            return Optional.empty();
        }
        return playerAMovement.beats(playerBMovement) ? Optional.of(playerBMovement) : Optional.of(playerAMovement);
    }

    public Optional<GameOverLoserInfo> loserInfo() {
        return loser().map(player -> new GameOverLoserInfo(this.gameId, player, loserMovement().orElseThrow()));
    }

    public Optional<GameOverWinnerInfo> winnerInfo() {
        return winner().map(player -> new GameOverWinnerInfo(this.gameId, player, winnerMovement().orElseThrow()));
    }

    public GameOverPlayerInfo playerAInfo() {
        return new GameOverPlayerInfo(this.gameId, this.playerA, this.playerAMovement);
    }

    public GameOverPlayerInfo playerBInfo() {
        return new GameOverPlayerInfo(this.gameId, this.playerB, this.playerBMovement);
    }
}
