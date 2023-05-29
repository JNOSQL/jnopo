package br.org.soujava.coffewithjava.jokenpo;

import java.util.Set;

public record GameAbandoned(String gameId, Set<Player> players) implements GameState {
}
