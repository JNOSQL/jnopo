package br.org.soujava.coffewithjava.jokenpo;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static br.org.soujava.coffewithjava.jokenpo.Movement.PAPER;
import static br.org.soujava.coffewithjava.jokenpo.Movement.ROCK;
import static br.org.soujava.coffewithjava.jokenpo.Movement.SCISSORS;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class GameTest {

    @Test
    void testHappyScenario() {

        Player player1 = Player.of("player1");
        Player player2 = Player.of("player2");

        var game = new Game();

        var gameState = game.newGame(player1);

        assertSoftly(softly -> {
            softly.assertThat(gameState)
                    .as("whenever newGame(Player) is called then a non null game state should return")
                    .isNotNull();
            softly.assertThat(gameState)
                    .as("when it's missing the required players then game's state should be waiting the player")
                    .isInstanceOf(WaitingPlayers.class);
        });

        var gameId = gameState.gameId();

        var players1 = game.playersByGame(gameId);

        assertSoftly(softly -> {
            softly.assertThat(players1)
                    .as("whenever playesOf(gameId) is called then a non null result should return")
                    .isNotNull();
            softly.assertThat(players1)
                    .as("players is correct")
                    .hasSameElementsAs(Set.of(player1));
        });

        var gameState2 = game.newGame(player1);

        assertSoftly(softly -> {
            softly.assertThat(gameState2)
                    .as("whenever the same player request a new it's missing the required players " +
                            "then game's state should be waiting the player")
                    .isEqualTo(gameState);
        });


        var gameState3 = game.newGame(player2);

        assertSoftly(softly -> {
            softly.assertThat(gameState3)
                    .as("when it's provided all required players then game's state should be " + GameReady.class.getName())
                    .isInstanceOf(GameReady.class);
            softly.assertThat(gameState3.gameId())
                    .as("the game state should have the same gameId")
                    .isEqualTo(gameId);
        });

        var players2 = game.playersByGame(gameId);

        assertSoftly(softly -> {
            softly.assertThat(players2)
                    .as("whenever playesOf(gameId) is called then a non null result should return")
                    .isNotNull();
            softly.assertThat(players2)
                    .as("players is correct")
                    .hasSameElementsAs(Set.of(player1, player2));
        });


        var gameState4 = game.playGame(gameId, player1, PAPER);

        assertSoftly(softly -> {
            softly.assertThat(gameState4)
                    .as("whenever one player plays then game's state should be " + GameRunning.class.getName())
                    .isInstanceOf(GameRunning.class);
            softly.assertThat(gameState4.gameId())
                    .as("the game state should have the same gameId")
                    .isEqualTo(gameId);
        });


        var gameState5 = game.playGame(gameId, player1, ROCK);

        assertSoftly(softly -> {
            softly.assertThat(gameState5)
                    .as("whenever the same player plays then game's state should keep stay as " + GameRunning.class.getName())
                    .isInstanceOf(GameRunning.class);
            softly.assertThat(gameState5.gameId())
                    .as("the game state should have the same gameId")
                    .isEqualTo(gameId);

        });

        var gameState6 = game.playGame(gameId, player2, ROCK);

        assertSoftly(softly -> {
            softly.assertThat(gameState6)
                    .as("whenever both players play their movements then game's state should be " + GameOver.class.getName())
                    .isInstanceOf(GameOver.class);
            softly.assertThat(gameState6.gameId())
                    .as("the game state should have the same gameId")
                    .isEqualTo(gameId);
        });


        var players3 = game.playersByGame(gameId);

        assertSoftly(softly -> {
            softly.assertThat(players3)
                    .as("whenever playesOf(gameId) is called then a non null result should return")
                    .isNotNull();
            softly.assertThat(players3)
                    .as("players is correct")
                    .isEmpty();
        });

        var gameState7 = game.playGame(gameId, player1, SCISSORS);

        assertSoftly(softly -> {
            softly.assertThat(gameState7)
                    .as("whenever some player plays their movements to an game that it's over then game's state should be " + InvalidGame.class.getName())
                    .isInstanceOf(InvalidGame.class);
            softly.assertThat(gameState7.gameId())
                    .as("the game state should have the same gameId")
                    .isEqualTo(gameId);
        });

        var invalidGameId = UUID.randomUUID().toString();
        var gameState8 = game.playGame(invalidGameId, player1, ROCK);

        assertSoftly(softly -> {
            softly.assertThat(gameState8)
                    .as("whenever some player plays their movements to an invalid game then game's state should be " + InvalidGame.class.getName())
                    .isInstanceOf(InvalidGame.class);
            softly.assertThat(gameState8.gameId())
                    .as("the game state should have the same gameId")
                    .isEqualTo(invalidGameId);
        });
    }

}
