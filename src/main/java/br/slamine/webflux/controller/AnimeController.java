package br.slamine.webflux.controller;

import br.slamine.webflux.domain.Anime;
import br.slamine.webflux.service.AnimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

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

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Anime> save(@Valid @RequestBody Anime anime){
        return animeService.save(anime);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<?> update(@Valid @RequestBody Anime anime){
        return animeService.update(anime);
    }
}
