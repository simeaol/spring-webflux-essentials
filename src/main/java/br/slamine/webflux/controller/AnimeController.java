package br.slamine.webflux.controller;

import br.slamine.webflux.domain.Anime;
import br.slamine.webflux.service.AnimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("animes")
@Slf4j
public class AnimeController {
    private final AnimeService animeService;

    @GetMapping
    public Flux<Anime> listAll(){
        log.info("Requesting all animes");
        return animeService.findAll();

    }

    @GetMapping(path = "{id}")
    public Mono<Anime> listAll(@PathVariable int id){
        log.info("Requesting anime for id={}",id);
        return animeService.findById(id);

    }
}
