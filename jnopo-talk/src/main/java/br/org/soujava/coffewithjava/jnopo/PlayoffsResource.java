package br.org.soujava.coffewithjava.jnopo;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;

@Path("/playoffs")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class PlayoffsResource {
    @Inject
    @Database(DatabaseType.DOCUMENT)
    Playoffs playoffs;

    @GET
    @Path("/ranking")
    @WithSpan
    public Ranking getWinnerRanking(){
        return playoffs.winnerRanking();
    }
}
