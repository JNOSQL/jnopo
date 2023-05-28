package br.org.soujava.coffewithjava.jokenpo;

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

        return Optional
                .of(playerAMovement.beats(playerBMovement))
                .filter(Boolean.TRUE::equals)
                .flatMap((v) -> Optional.of(playerA))
                .or(() -> Optional.of(playerB));
    }

    public Optional<Player> loser() {
        if (isTied()) {
            return Optional.empty();
        }
        return Optional
                .of(playerAMovement.beats(playerBMovement))
                .filter(Boolean.TRUE::equals)
                .flatMap((v) -> Optional.of(playerB))
                .or(() -> Optional.of(playerA));
    }

    public Optional<Movement> winnerMovement() {
        if (isTied()) {
            return Optional.empty();
        }
        return Optional
                .of(playerAMovement.beats(playerBMovement))
                .filter(Boolean.TRUE::equals)
                .flatMap((v) -> Optional.of(playerAMovement))
                .or(() -> Optional.of(playerBMovement));
    }

    public Optional<Movement> loserMovement() {
        if (isTied()) {
            return Optional.empty();
        }
        return Optional
                .of(playerAMovement.beats(playerBMovement))
                .filter(Boolean.TRUE::equals)
                .flatMap((v) -> Optional.of(playerBMovement))
                .or(() -> Optional.of(playerAMovement));
    }
}
