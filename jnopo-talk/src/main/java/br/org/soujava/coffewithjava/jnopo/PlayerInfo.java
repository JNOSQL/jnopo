package br.org.soujava.coffewithjava.jnopo;


import br.org.soujava.coffewithjava.jnopo.core.Movement;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;

@Entity
public record PlayerInfo(@Column String name,@Column Movement movement) {

}
