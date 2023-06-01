package br.org.soujava.coffewithjava.jnopo.core;

public record GameOverPlayerInfo(String gameId, Player player, Movement movement) implements GameState {
    public GameOverPlayerInfo(GameOverWinnerInfo gameOverWinnerInfo) {
        this(gameOverWinnerInfo.gameId(), gameOverWinnerInfo.player(),gameOverWinnerInfo.movement());
    }
    public GameOverPlayerInfo(GameOverLoserInfo gameOverLoserInfo) {
        this(gameOverLoserInfo.gameId(), gameOverLoserInfo.player(),gameOverLoserInfo.movement());
    }
}
