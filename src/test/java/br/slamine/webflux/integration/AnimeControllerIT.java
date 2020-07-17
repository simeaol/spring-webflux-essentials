package br.slamine.webflux.integration;

import br.slamine.webflux.domain.Anime;
import br.slamine.webflux.exception.CustomAttributes;
import br.slamine.webflux.repository.AnimeRepository;
import br.slamine.webflux.service.AnimeService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static br.slamine.webflux.util.AnimeCreator.createAnimeToBeSaved;
import static br.slamine.webflux.util.AnimeCreator.createValidAnime;

@ExtendWith(SpringExtension.class)//Used for JUnit-5
@WebFluxTest//Init the content related to webflux but doesn't scan all package
@Import({AnimeService.class, CustomAttributes.class})
public class AnimeControllerIT {
    /**
     * Integration Test shouldn't use mock. It need to be executed in real scenario (e.g: real database)
     * But for POC purpose, I will use Mockito because I don't have real database just for testing
     */
    @MockBean
    private AnimeRepository animeRepositoryMock;

    @Autowired
    private WebTestClient testClient;

    private final Anime anime = createValidAnime();


    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    public void setUp(){
        BDDMockito.when(animeRepositoryMock.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepositoryMock.save(createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepositoryMock.saveAll(
                List.of(createAnimeToBeSaved(), createAnimeToBeSaved())
            )).thenReturn(Flux.just(anime, anime));

        BDDMockito.when(animeRepositoryMock.delete(ArgumentMatchers.any(Anime.class)))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeRepositoryMock.save(createValidAnime()))
                .thenReturn(Mono.empty());
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

    @Test
    @DisplayName("findById returns Mono with anime when it exists")
    public void findById_ReturnMonoAnime_WhenSuccessful(){
        testClient
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);

    }

    @Test
    @DisplayName("findById returns Mono error when anime does not exists")
    public void findById_ReturnMonoError_WhenEmptyMonoIsReturned(){

        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        testClient
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()//oris4xxClientError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");

    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void save_CreateAnime_WhenSuccessful(){
        Anime animeTobeSaved = createAnimeToBeSaved();

        testClient
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeTobeSaved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("saveBatch creates a list of anime when successful")
    public void saveBatch_CreatesListOfAnime_WhenSuccessful(){
        Anime animeTobeSaved = createAnimeToBeSaved();

        testClient
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeTobeSaved,animeTobeSaved)))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(Anime.class)
                .hasSize(2)
                .contains(anime);
    }

    @Test
    @DisplayName("save returns Mono error with bad request when name is empty")
    public void save_ReturnMonoError_WhenNameIsEmpty(){
        Anime animeTobeSaved = createAnimeToBeSaved().withName("");

        testClient
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeTobeSaved))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("saveBatch returns Mono error with bad request when name is empty")
    public void saveBatch_returnsMonoError_WhenContainsInvalidName(){
        Anime animeToBeSaved = createAnimeToBeSaved();

        BDDMockito.when(animeRepositoryMock.saveAll(ArgumentMatchers.anyIterable()))
                .thenReturn(Flux.just(animeToBeSaved, animeToBeSaved.withName("")));

        testClient
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved,animeToBeSaved.withName(""))))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }


    @Test
    @DisplayName("delete remove the anime when successful")
    public void delete_RemoveAnime_WhenSuccessful() {
        testClient
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("delete return Mono error when anime does not exist")
    public void delete_ReturnMonoError_WhenEmptyMonoIsReturned(){
        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

            testClient
                    .delete()
                    .uri("/animes/{id}", 1)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(404)
                    .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("update save updated anime and returns empty Mono when successful")
    public void update_SaveUpdatedAnime_WhenSuccessful(){
        testClient
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("update returns Mono error when anime does not exists")
    public void update_ReturnMonoError_WhenEmptyMonoIsReturned(){
        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        testClient
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");;
    }

}
