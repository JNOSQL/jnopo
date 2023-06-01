package br.org.soujava.coffewithjava.jokenpo;

public record Player(String id, String name) {
    public static Player of(String id, String username) {
        return new Player(id, username);
    }
}





