package br.org.soujava.coffewithjava.jnopo;

import jakarta.inject.Inject;
import jakarta.nosql.Template;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Path("playoffs")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class Playoffs {

    @Inject
    Template template;

    @Inject
    GameMatchRepository repository;

    @GET
    public List<GameMatch> listAll(@QueryParam("winner") @DefaultValue("") String winner,
                                   @QueryParam("loser") @DefaultValue("") String loser) {

        return template.select(GameMatch.class)
                .where("winner.player").like(winner)
                .and("loser.player").like(loser)
                .result();
    }


    @Path("loser")
    @GET
    public List<GameMatch> findByLoser(@QueryParam("name") String name) {
        return Optional.ofNullable(name)
                .map(w -> repository.findByLoser(name))
                .orElseGet(Collections::emptyList);
    }

    @Path("winner")
    @GET
    public List<GameMatch> findByWinner(@QueryParam("name") String name) {
        return Optional.ofNullable(name)
                .map(w -> repository.findByWinnerPlayer(name))
                .orElseGet(Collections::emptyList);
    }


    @Path("tied")
    @GET
    public List<GameMatch> findTiedGames() {
        return repository.findByTiedTrue();
    }

}
