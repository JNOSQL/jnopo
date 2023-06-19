package br.org.soujava.coffewithjava.jnopo;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("playoffs")

public class PlayoffsResources {

    @Inject
    Playoffs playoffs;

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<GameMatch> listAll() {
        return playoffs.findAll().toList();
    }


}
