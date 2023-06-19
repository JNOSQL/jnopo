package br.org.soujava.coffewithjava.jnopo;

import br.org.soujava.coffewithjava.jnopo.core.Movement;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;

import java.util.Objects;

@Entity
public class WinnerInfo {
        @Column
        private Player player;
        @Column
        private Movement movement;

        public WinnerInfo() {
        }

        public WinnerInfo(
                @Column
                Player player,
                @Column
                Movement movement
        ) {
                this.player = player;
                this.movement = movement;
        }

        public Movement getMovement() {
                return movement;
        }

        public Player getPlayer() {
                return player;
        }

        public Player player() {
                return player;
        }

        public Movement movement() {
                return movement;
        }

        @Override
        public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (WinnerInfo) obj;
                return Objects.equals(this.player, that.player) &&
                        Objects.equals(this.movement, that.movement);
        }

        @Override
        public int hashCode() {
                return Objects.hash(player, movement);
        }

        @Override
        public String toString() {
                return "WinnerInfo[" +
                        "player=" + player + ", " +
                        "movement=" + movement + ']';
        }

}
