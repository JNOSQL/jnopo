package br.org.soujava.coffewithjava.jokenpo;


import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class GameOverTest {


    @ParameterizedTest(name = "should error when {0}")
    @CsvSource({
            "player B movement is omitted,1,player1,player2,ROCK,",
            "player A movement is omitted,1,player1,player2,,SCISSORS",
            "player B is omitted,1,player1,,ROCK,SCISSORS",
            "player A movement is omitted,1,,player2,ROCK,SCISSORS",
            "game id is omitted,,player1,player2,ROCK,SCISSORS"
    })
    void shouldErrorWhenInvalidParams(
            String scenario,
            String id,
            @ConvertWith(PlayerConverter.class)
            Player playerA,
            @ConvertWith(PlayerConverter.class)
            Player playerB,
            Movement playerAmovement,
            Movement playerBmovement
    ) {

        assertSoftly(softly -> {

            softly.assertThatThrownBy(() -> {
                        new GameOver(id, playerA, playerB, playerAmovement, playerBmovement);
                    }).as("should error when " + scenario)
                    .isInstanceOfAny(NullPointerException.class);

        });

    }

    @ParameterizedTest(name = "given {0} plays {2} and {1} plays {3} then {4} should be the winner")
    @CsvSource({
            "player1,player2,ROCK,SCISSORS,player1,player2,ROCK,SCISSORS",
            "player1,player2,SCISSORS,PAPER,player1,player2,SCISSORS,PAPER",
            "player1,player2,PAPER,ROCK,player1,player2,PAPER,ROCK",
            "player1,player2,SCISSORS,ROCK,player2,player1,ROCK,SCISSORS",
            "player1,player2,PAPER,SCISSORS,player2,player1,SCISSORS,PAPER",
            "player1,player2,ROCK,PAPER,player2,player1,PAPER,ROCK",
    })
    void shouldHaveAWinnerAndLoser(
            @ConvertWith(PlayerConverter.class)
            Player player1,
            @ConvertWith(PlayerConverter.class)
            Player player2,
            Movement player1Movement,
            Movement player2Movement,
            @ConvertWith(PlayerConverter.class)
            Player expectedWinner,
            @ConvertWith(PlayerConverter.class)
            Player expectedLoser,
            Movement expectedWinnerMovement,
            Movement expectedLoserMovement
    ) {

        GameOver gameOver = new GameOver(UUID.randomUUID().toString(),
                player1,
                player2,
                player1Movement,
                player2Movement
        );

        assertSoftly(softly -> {
            softly.assertThat(gameOver.isTied())
                    .as("it's not be tied")
                    .isFalse();

            softly.assertThat(gameOver.winner().isPresent())
                    .as("winner should exists")
                    .isTrue();

            softly.assertThat(gameOver.winner().orElse(null))
                    .as("winner is correct")
                    .isEqualTo(expectedWinner);

            softly.assertThat(gameOver.loser().orElse(null))
                    .as("loser is correct")
                    .isEqualTo(expectedLoser);

            softly.assertThat(gameOver.winnerMovement().orElse(null))
                    .as("winner movement is correct")
                    .isEqualTo(expectedWinnerMovement);

            softly.assertThat(gameOver.loserMovement().orElse(null))
                    .as("loser movement is correct")
                    .isEqualTo(expectedLoserMovement);


        });
    }


    @ParameterizedTest(name = "given {0} plays {2} and {1} plays {3} then it's a tied game over")
    @CsvSource({
            "player1,player2,ROCK,ROCK",
            "player1,player2,SCISSORS,SCISSORS",
            "player1,player2,PAPER,PAPER"
    })
    void shouldBeTiedGameOver(
            @ConvertWith(PlayerConverter.class)
            Player player1,
            @ConvertWith(PlayerConverter.class)
            Player player2,
            Movement player1Movement,
            Movement player2Movement
    ) {

        GameOver gameOver = new GameOver(UUID.randomUUID().toString(),
                player1,
                player2,
                player1Movement,
                player2Movement
        );

        assertSoftly(softly -> {

            softly.assertThat(gameOver.isTied())
                    .as("should be a tired game")
                    .isTrue();

            softly.assertThat(gameOver.winner())
                    .as("winner should be an optional instance")
                    .isInstanceOf(Optional.class);

            softly.assertThat(gameOver.winner().isPresent())
                    .as("should no have winner")
                    .isFalse();

            softly.assertThat(gameOver.loser())
                    .as("loser should be an optional instance")
                    .isInstanceOf(Optional.class);

            softly.assertThat(gameOver.loser().isPresent())
                    .as("should no have loser")
                    .isFalse();

            softly.assertThat(gameOver.winnerMovement())
                    .as("winner movement should be an optional instance")
                    .isInstanceOf(Optional.class);

            softly.assertThat(gameOver.winnerMovement().isPresent())
                    .as("should no have winner movement")
                    .isFalse();

            softly.assertThat(gameOver.loserMovement())
                    .as("loser movement should be an optional instance")
                    .isInstanceOf(Optional.class);

            softly.assertThat(gameOver.loserMovement().isPresent())
                    .as("should no have loser movement")
                    .isFalse();
        });

    }


    static class PlayerConverter implements ArgumentConverter {

        @Override
        public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
            if (Objects.isNull(source)) {
                return null;
            }
            if (!(source instanceof String)) {
                throw new IllegalArgumentException(
                        "The argument should be a string: " + source);
            }
            return Player.of((String) source);
        }
    }

}