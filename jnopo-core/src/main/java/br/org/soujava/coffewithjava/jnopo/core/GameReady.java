package br.org.soujava.coffewithjava.jnopo.core;

import java.util.stream.Stream;

public record GameReady(String gameId,
                        Player playerA,
                        Player playerB) implements GameState {

    public Stream<Player> players() {
        return Stream.of(playerA, playerB);
    }

    PlayerMovement playMovement(Movement movement) {
        return new PlayerMovement(this.gameId, movement);
    }
}
