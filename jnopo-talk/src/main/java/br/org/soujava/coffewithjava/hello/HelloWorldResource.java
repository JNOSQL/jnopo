package br.org.soujava.coffewithjava.hello;

import br.org.soujava.coffewithjava.jnopo.core.Player;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("hello")
public class HelloWorldResource {

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Object hello(@QueryParam("name") String name) {
		if ((name == null) || name.trim().isEmpty())  {
			return Player.of(name,"world");
		}
		return new Hello(name);
	}
}