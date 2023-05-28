package br.org.soujava.coffewithjava.jokenpo;

public record Player(String id) {
    static Player of(String id) {
        return new Player(id);
    }
}





