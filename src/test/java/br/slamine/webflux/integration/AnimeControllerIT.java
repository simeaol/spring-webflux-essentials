package br.slamine.webflux.integration;

import br.slamine.webflux.domain.Anime;
import br.slamine.webflux.repository.AnimeRepository;
import br.slamine.webflux.service.AnimeService;
import br.slamine.webflux.util.AnimeCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)//Used for JUnit-5
@WebFluxTest//Init the content related to webflux but doesn't scan all package
@Import(AnimeService.class)
public class AnimeControllerIT {
    /**
     * Integration Test shouldn't use mock. It need to be executed in real scenario (e.g: real database)
     * But for POC purpose, I will use Mockito because I don't have real database just for testing
     */
    @MockBean
    private AnimeRepository animeRepositoryMock;

    @Autowired
    private WebTestClient testClient;

    private final Anime anime =AnimeCreator.createValidAnime();


    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    public void setUp(){
        BDDMockito.when(animeRepositoryMock.findAll())
                .thenReturn(Flux.just(anime));

//        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
//                .thenReturn(Mono.just(anime));
//
//        BDDMockito.when(animeServiceMock.save(AnimeCreator.createAnimeToBeSaved()))
//                .thenReturn(Mono.just(anime));
//
//        BDDMockito.when(animeServiceMock.delete(ArgumentMatchers.anyInt()))
//                .thenReturn(Mono.empty());
//
//        BDDMockito.when(animeServiceMock.update(AnimeCreator.createValidAnime()))
//                .thenReturn(Mono.empty());
    }

    @Test
    public void blockHoundWorks(){
        try{
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);
            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        }catch (Exception e){
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    @DisplayName("listAll returns a flux of anime")
    public void listAll_ReturnFluxOfAnime_WhenSuccessful(){
        testClient
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.[0].id")
                .isEqualTo(anime.getId())
                .jsonPath("$.[0].name")
                .isEqualTo(anime.getName());
    }

    /**
     * ALERT: Don't do this in production
     */
    @Test
    @DisplayName("listAll returns a flux of anime")
    public void listAll_Flavor2_ReturnFluxOfAnime_WhenSuccessful(){
        testClient
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);
    }
}
