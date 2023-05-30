package br.org.soujava.coffewithjava.jokenpo;

public class GameDTO {
    private String namePlayerA;
    private String playerAId;
    private String playerAMovement;
    private String namePlayerB;
    private String playerBId;
    private String playerBMovement;

    public GameDTO(String namePlayerA, String playerAId, String playerAMovement, String namePlayerB, String playerBId, String playerBMovement) {
        this.namePlayerA = namePlayerA;
        this.playerAId = playerAId;
        this.playerAMovement = playerAMovement;
        this.namePlayerB = namePlayerB;
        this.playerBId = playerBId;
        this.playerBMovement = playerBMovement;
    }

    public GameDTO() {
    }

    public String getNamePlayerA() {
        return namePlayerA;
    }

    public void setNamePlayerA(String namePlayerA) {
        this.namePlayerA = namePlayerA;
    }

    public String getPlayerAId() {
        return playerAId;
    }

    public void setPlayerAId(String playerAId) {
        this.playerAId = playerAId;
    }

    public String getPlayerAMovement() {
        return playerAMovement;
    }

    public void setPlayerAMovement(String playerAMovement) {
        this.playerAMovement = playerAMovement;
    }

    public String getNamePlayerB() {
        return namePlayerB;
    }

    public void setNamePlayerB(String namePlayerB) {
        this.namePlayerB = namePlayerB;
    }

    public String getPlayerBId() {
        return playerBId;
    }

    public void setPlayerBId(String playerBId) {
        this.playerBId = playerBId;
    }

    public String getPlayerBMovement() {
        return playerBMovement;
    }

    public void setPlayerBMovement(String playerBMovement) {
        this.playerBMovement = playerBMovement;
    }
}
