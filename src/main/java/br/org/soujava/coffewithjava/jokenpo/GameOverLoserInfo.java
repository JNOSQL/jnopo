package br.org.soujava.coffewithjava.jokenpo;

public record GameOverLoserInfo(String gameId, Player player, Movement movement) implements GameState {
}
