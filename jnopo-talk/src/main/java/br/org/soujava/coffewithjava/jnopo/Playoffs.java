package br.org.soujava.coffewithjava.jnopo;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.data.repository.PageableRepository;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;

@Repository
public interface Playoffs extends PageableRepository<GameMatch,String> {
    List<GameMatch> findByWinnerNameLike(String name);

    @Query("select * from GameMatch where tied=@tied")
    @WithSpan
    List<GameMatch> listTiedPlayoffs(@Param("tied") boolean tied);
}
