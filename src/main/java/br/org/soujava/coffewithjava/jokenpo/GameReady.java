package br.org.soujava.coffewithjava.jokenpo;

public record GameReady(String gameId,
                 Player playerA,
                 Player playerB) implements GameState {
}
