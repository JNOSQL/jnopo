package br.org.soujava.coffewithjava.jokenpo;

public record WaitingPlayers(String gameId, Player player) implements GameState {
}
