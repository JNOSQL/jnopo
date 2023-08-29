package br.org.soujava.coffewithjava.jnopo;

import jakarta.data.repository.PageableRepository;
import jakarta.data.repository.Repository;

@Repository
public interface Playoffs extends PageableRepository<GameMatch,String> {
}
