package br.org.soujava.coffewithjava.jnopo;

import br.org.soujava.coffewithjava.jnopo.core.GameOverLoserInfo;
import br.org.soujava.coffewithjava.jnopo.core.GameOverPlayerInfo;
import br.org.soujava.coffewithjava.jnopo.core.GameOverWinnerInfo;
import br.org.soujava.coffewithjava.jnopo.core.Movement;
import br.org.soujava.coffewithjava.jnopo.core.Player;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Objects;

@Entity
public final class GameMatch {
    @Id
    private String id;
    @Column
    private boolean tied;
    @Column
    private PlayerInfo playerA;
    @Column
    private PlayerInfo playerB;
    @Column
    private WinnerInfo winner;
    @Column
    private LoserInfo loser;

    public GameMatch() {
    }

    public GameMatch(
            @Id
            String id,
            @Column
            boolean tied,
            @Column
            PlayerInfo playerA,
            @Column
            PlayerInfo playerB,
            @Column
            WinnerInfo winner,
            @Column
            LoserInfo loser
    ) {
        this.id = id;
        this.tied = tied;
        this.playerA = playerA;
        this.playerB = playerB;
        this.winner = winner;
        this.loser = loser;
    }

    public String id() {
        return id;
    }

    public boolean tied() {
        return tied;
    }

    public PlayerInfo playerA() {
        return playerA;
    }

    public PlayerInfo playerB() {
        return playerB;
    }

    public WinnerInfo winner() {
        return winner;
    }

    public LoserInfo loser() {
        return loser;
    }

    public String getId() {
        return id;
    }

    public boolean isTied() {
        return tied;
    }

    public PlayerInfo getPlayerA() {
        return playerA;
    }

    public PlayerInfo getPlayerB() {
        return playerB;
    }

    public WinnerInfo getWinner() {
        return winner;
    }

    public LoserInfo getLoser() {
        return loser;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GameMatch) obj;
        return Objects.equals(this.id, that.id) &&
                this.tied == that.tied &&
                Objects.equals(this.playerA, that.playerA) &&
                Objects.equals(this.playerB, that.playerB) &&
                Objects.equals(this.winner, that.winner) &&
                Objects.equals(this.loser, that.loser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tied, playerA, playerB, winner, loser);
    }

    @Override
    public String toString() {
        return "GameMatch[" +
                "id=" + id + ", " +
                "tied=" + tied + ", " +
                "playerA=" + playerA + ", " +
                "playerB=" + playerB + ", " +
                "winner=" + winner + ", " +
                "loser=" + loser + ']';
    }

}
