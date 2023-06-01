package br.org.soujava.coffewithjava.jnopo.core;

public record GameOverWinnerInfo(String gameId, Player player, Movement movement) implements GameState {
}
