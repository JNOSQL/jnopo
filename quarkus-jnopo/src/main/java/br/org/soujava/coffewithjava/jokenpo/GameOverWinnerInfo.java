package br.org.soujava.coffewithjava.jokenpo;

public record GameOverWinnerInfo(String gameId, Player player, Movement movement) implements GameState {
}
