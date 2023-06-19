package br.org.soujava.coffewithjava.jnopo;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

@Repository
public interface Playoffs extends CrudRepository<GameMatch,String> {
}
