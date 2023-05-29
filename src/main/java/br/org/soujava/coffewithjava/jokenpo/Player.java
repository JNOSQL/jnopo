package br.org.soujava.coffewithjava.jokenpo;

public record Player(String id) {
    public static Player of(String id) {
        return new Player(id);
    }
}





