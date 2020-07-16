package br.slamine.webflux.util;

import br.slamine.webflux.domain.Anime;

public class AnimeCreator {

    public static Anime createAnimeToBeSaved(){
        return Anime.builder()
                .name("South Park")
                .build();
    }
    public static Anime createValidAnime(){
        return Anime.builder()
                .id(1)
                .name("South Park")
                .build();
    }
    public static Anime createValidUpdatedAnime(){
        return Anime.builder()
                .id(1)
                .name("South Park. Season 2")
                .build();
    }
}
