package br.org.soujava.coffewithjava.jnopo;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;

@Entity
public class Player {
    @Column
    private String name;

    public Player(String name) {
        this.name = name;
    }

    public Player() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
