package br.org.soujava.coffewithjava.jnopo;

import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;

import java.util.List;

@Repository
public interface Playoffs extends DataRepository<GameMatch,String> {

    @Save
    GameMatch save(GameMatch gameMatch);

    @Query("from GameMatch g where g.tied = false")
    List<GameMatch> listNonTiedGameMatches();

    default Ranking  winnerRanking(){
        return Ranking.winnerRanking(this);
    }
}
