package br.org.soujava.coffewithjava.jokenpo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MovementTest {

    public static Stream<Arguments> validMovements() {
        return Stream.of(
                arguments(Movement.ROCK,Movement.SCISSORS),
                arguments(Movement.SCISSORS,Movement.PAPER),
                arguments(Movement.PAPER,Movement.ROCK)
        );
    }

    @ParameterizedTest(name = "{0} should beats {1}")
    @MethodSource("validMovements")
    void testBeats(Movement movementA, Movement movementB){
        assertTrue(movementA.beats(movementB), "%s should beats %s".formatted(movementA,movementB));
    }
}