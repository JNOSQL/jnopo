package br.org.soujava.coffewithjava.jokenpo;

public record GameRunning(String gameId,
                   Player playerA,
                   Player playerB,
                   Movement playerAMovement,
                   Movement playerBMovement) implements GameState {
}
