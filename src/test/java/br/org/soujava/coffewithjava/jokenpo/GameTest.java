package br.org.soujava.coffewithjava.jokenpo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static br.org.soujava.coffewithjava.jokenpo.Movement.PAPER;
import static br.org.soujava.coffewithjava.jokenpo.Movement.ROCK;
import static br.org.soujava.coffewithjava.jokenpo.Movement.SCISSORS;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class GameTest {

    @Test
    void shouldErrorNewGameWithInvalidArgs() {
        var game = new Game();
        assertThatThrownBy(
                () -> game.newGame(null))
                .as("null value")
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "should error when {0} on playGame() method")
    @CsvSource({
            "movement is omitted,true,player1,",
            "player is omitted,true,,ROCK",
            "gameId is omitted,false,player1,ROCK",
    })
    void shouldErrorPlayGameWithInvalidArgs(
            String scenario,
            boolean getGameId,
            @ConvertWith(GameOverTest.PlayerConverter.class)
            Player player,
            Movement movement
    ) {

        Player player1 = Player.of("player1");
        Player player2 = Player.of("player2");

        var game = new Game();

        var gameState = game.newGame(player1);
        var gameId = getGameId ? gameState.gameId() : null;

        game.newGame(player2);

        assertSoftly(softly -> {
            softly.assertThatThrownBy(() -> game.playGame(gameId, player, movement))
                    .as("should error when " + scenario)
                    .isInstanceOf(RuntimeException.class);
        });
    }

    @Test
    void shouldGetGameState() {
        Player player1 = Player.of("player1");
        Player player2 = Player.of("player2");

        var game = new Game();

        assertSoftly(softly -> {
            var state = game.getGameState(null);
            softly.assertThat(state)
                    .as("should return a non-null GameState instance ever")
                    .isNotNull();
            softly.assertThat(state)
                    .as("should return a " + GameInvalid.class.getSimpleName() + " instance when the provided gameId is null")
                    .isInstanceOf(GameInvalid.class);

            state = game.getGameState(UUID.randomUUID().toString());
            softly.assertThat(state)
                    .as("should return a non-null GameState instance ever")
                    .isNotNull();
            softly.assertThat(state)
                    .as("should return a " + GameInvalid.class.getSimpleName() + " instance when the provided gameId is not valid")
                    .isInstanceOf(GameInvalid.class);
        });


        var state2 = game.newGame(player1);

        assertSoftly(softly -> {
            var state = game.getGameState(state2.gameId());
            softly.assertThat(state)
                    .as("should return a non-null GameState instance ever")
                    .isNotNull();
            softly.assertThat(state)
                    .as("returned game state should be equals to the same returned from the latest newGame(player) call")
                    .isEqualTo(state2);
        });

        var state3 = game.newGame(player2);

        assertSoftly(softly -> {
            var state = game.getGameState(state3.gameId());
            softly.assertThat(state)
                    .as("should return a non-null GameState instance ever")
                    .isNotNull();
            softly.assertThat(state)
                    .as("returned game state should be equals to the same returned from the latest newGame(player) call")
                    .isEqualTo(state3);
        });

    }


    @Test
    void shouldLeavingGame() {
        Player player1 = Player.of("player1");
        Player player2 = Player.of("player2");

        var game = new Game();

        game.newGame(player1);
        var gameReady = game.newGame(player2);

        assertSoftly(softly -> {
            softly.assertThat(gameReady)
                    .as("should return a non-null GameState instance ever")
                    .isNotNull();
            softly.assertThat(gameReady)
                    .as("should return a " + GameReady.class.getSimpleName() + " instance")
                    .isInstanceOf(GameReady.class);
        });


        var gameAbandoned = game.leavingGame(player2);

        assertSoftly(softly -> {
            softly.assertThat(gameAbandoned)
                    .as("should return a non-null GameState instance ever")
                    .isNotNull();
            softly.assertThat(gameAbandoned)
                    .as("should return a " + GameAbandoned.class.getSimpleName() + " instance")
                    .isInstanceOf(GameAbandoned.class);
        });


        var invalidGame = game.leavingGame(player2);

        assertSoftly(softly -> {
            softly.assertThat(invalidGame)
                    .as("should return a non-null GameState instance ever")
                    .isNotNull();
            softly.assertThat(invalidGame)
                    .as("should return a " + GameInvalid.class.getSimpleName() + " instance when the informed player is not playing any game")
                    .isInstanceOf(GameInvalid.class);
        });


        var invalidGame2 = game.leavingGame(null);

        assertSoftly(softly -> {
            softly.assertThat(invalidGame2)
                    .as("should return a non-null GameState instance ever")
                    .isNotNull();
            softly.assertThat(invalidGame2)
                    .as("should return a " + GameInvalid.class.getSimpleName() + " instance when a null reference is passed as player")
                    .isInstanceOf(GameInvalid.class);
        });

        var invalidGame3 = game.leavingGame(player1);

        assertSoftly(softly -> {
            softly.assertThat(invalidGame3)
                    .as("should return a non-null GameState instance ever")
                    .isNotNull();
            softly.assertThat(invalidGame3)
                    .as("should return a " + GameInvalid.class.getSimpleName() + " instance")
                    .isInstanceOf(GameInvalid.class);
        });

    }


    @Test
    void shouldWaitingRoom() {
        Player player1 = Player.of("player1");
        Player player2 = Player.of("player2");

        var game = new Game();

        assertSoftly(softly -> {
            Stream<Player> waitingRoom = game.getWaitingRoom();
            softly.assertThat(waitingRoom)
                    .as("should return a non-null Stream<Player> instance ever")
                    .isNotNull();

            softly.assertThat(waitingRoom.count())
                    .as("waiting room should be empty")
                    .isEqualTo(0L);
        });

        game.newGame(player1);

        assertSoftly(softly -> {
            Stream<Player> waitingRoom = game.getWaitingRoom();

            softly.assertThat(waitingRoom)
                    .as("should return a non-null Stream<Player> instance ever")
                    .isNotNull();

            var waitingPlayers = waitingRoom.toList();

            softly.assertThat(waitingPlayers)
                    .as("waiting room should be not empty")
                    .isNotEmpty();

            softly.assertThat(waitingPlayers)
                    .as("player1 should be in waiting room")
                    .hasSameElementsAs(List.of(player1));

        });

        game.newGame(player2);

        assertSoftly(softly -> {
            Stream<Player> waitingRoom = game.getWaitingRoom();

            softly.assertThat(waitingRoom)
                    .as("should return a non-null Stream<Player> instance ever")
                    .isNotNull();

            softly.assertThat(waitingRoom.count())
                    .as("waiting room should be empty")
                    .isEqualTo(0L);
        });


    }

    @Test
    void testBehavior() {

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
                    .as("whenever some player plays their movements to an game that it's over then game's state should be " + GameInvalid.class.getName())
                    .isInstanceOf(GameInvalid.class);
            softly.assertThat(gameState7.gameId())
                    .as("the game state should have the same gameId")
                    .isEqualTo(gameId);
        });

        var invalidGameId = UUID.randomUUID().toString();
        var gameState8 = game.playGame(invalidGameId, player1, ROCK);

        assertSoftly(softly -> {
            softly.assertThat(gameState8)
                    .as("whenever some player plays their movements to an invalid game then game's state should be " + GameInvalid.class.getName())
                    .isInstanceOf(GameInvalid.class);
            softly.assertThat(gameState8.gameId())
                    .as("the game state should have the same gameId")
                    .isEqualTo(invalidGameId);
        });
    }


}
