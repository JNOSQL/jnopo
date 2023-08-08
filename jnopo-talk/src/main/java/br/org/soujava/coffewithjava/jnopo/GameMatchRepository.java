package br.org.soujava.coffewithjava.jnopo;


import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;

@Repository
public interface GameMatchRepository extends
        CrudRepository<GameMatch,String> {

    List<GameMatch> findByTiedTrue();

    List<GameMatch> findByWinnerPlayer(String name);

    @Query("select * from GameMatch where loser.player = @name")
    List<GameMatch> findByLoser(@Param("name") String name);

}
