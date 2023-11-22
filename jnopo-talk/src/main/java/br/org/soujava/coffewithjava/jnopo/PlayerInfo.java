package br.org.soujava.coffewithjava.jnopo;

import br.org.soujava.coffewithjava.jnopo.core.Movement;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;

@Entity
public record PlayerInfo(
        @Column
        String name,
        @Column
        String movement
) {

        public static final PlayerInfo NOBODY = new PlayerInfo("","");

        public PlayerInfo(String name, Movement movement){
                this(name, movement.name());
        }
}
