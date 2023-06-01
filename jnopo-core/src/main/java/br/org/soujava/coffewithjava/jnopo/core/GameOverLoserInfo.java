package br.org.soujava.coffewithjava.jnopo.core;

public record GameOverLoserInfo(String gameId, Player player, Movement movement) implements GameState {
}
