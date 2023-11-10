package br.org.soujava.coffewithjava.jnopo;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/playoffs")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class PlayoffsResource {
    @Inject
    @Database(DatabaseType.DOCUMENT)
    Playoffs playoffs;

    @GET
    @Path("/tied")
    @WithSpan
    public List<GameMatch> tiedPlayoffs() {
        return playoffs.listTiedPlayoffs();
    }

    @GET
    @Path("/winners")
    @WithSpan
    public Set<String> getWinners(
            @QueryParam("name") String name
    ) {
        if (name != null)
            return playoffs.findByWinnerNameLike(name)
                    .stream()
                    .map(g -> g.winner().name())
                    .collect(Collectors.toSet());
        return playoffs.listPlayoffsWithWinnerAndLoser()
                .stream()
                .map(g -> g.winner().name())
                .collect(Collectors.toSet());
    }

    @GET
    @Path("/losers")
    @WithSpan
    public Set<String> getLosers(
            @QueryParam("name") String name
    ) {
        if (name != null)
            return playoffs.findByLoserNameLike(name)
                    .stream()
                    .map(g -> g.loser().name())
                    .collect(Collectors.toSet());
        return playoffs.listPlayoffsWithWinnerAndLoser()
                .stream()
                .map(g -> g.loser().name())
                .collect(Collectors.toSet());
    }

    @GET
    @Path("/ranking")
    @WithSpan
    public Ranking getRanking(){
        return Ranking.winnerRanking(playoffs);
    }
}
