package br.org.soujava.coffewithjava.jnopo;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;

import java.util.List;
import java.util.Optional;

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
        return playoffs.listTiedPlayoffs(true);
    }

    @GET
    @WithSpan
    public List<GameMatch> winners(
            @QueryParam("winner") String winnerName,
            @QueryParam("loser") String loserName
    ) {
        if (winnerName != null)
            return playoffs.findByWinnerNameLike(winnerName);
        return playoffs.findAll().toList();
    }

}
