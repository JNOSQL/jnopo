package br.org.soujava.coffewithjava.jnopo.core;

public record WaitingPlayers(String gameId, Player player) implements GameState {
}
