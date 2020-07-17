package br.slamine.webflux.repository;

import br.slamine.webflux.domain.MyUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MyUserRepository extends ReactiveCrudRepository<MyUser, Integer> {
    Mono<MyUser> findByUsername(String username);
}
