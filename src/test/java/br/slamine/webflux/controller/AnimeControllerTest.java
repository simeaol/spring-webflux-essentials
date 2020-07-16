package br.slamine.webflux.controller;

import br.slamine.webflux.domain.Anime;
import br.slamine.webflux.service.AnimeService;
import br.slamine.webflux.util.AnimeCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
public class AnimeControllerTest {
    @InjectMocks//Inject mock is used for target class (with is the class to be tested)
    private AnimeController animeController;

    @Mock
    private AnimeService animeServiceMock;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    public void setup(){
        BDDMockito.when(animeServiceMock.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeServiceMock.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeServiceMock.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeServiceMock.delete(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeServiceMock.update(AnimeCreator.createValidAnime()))
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
    @DisplayName("find All returns a flux of anime")
    public void findAll_ReturnFluxOfAnime_WhenSuccessful(){

        StepVerifier.create(animeController.listAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("find by id returns mono with anime when it exist")
    public void findById_ReturnMonoOfAnime_WhenSuccessful(){

        StepVerifier.create(animeController.findById(1))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void save_CreateAnime_WhenSuccessful(){
        Anime animeTobeSaved = AnimeCreator.createAnimeToBeSaved();
        StepVerifier.create(animeController.save(animeTobeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete remove the anime when successful")
    public void delete_RemoveAnime_WhenSuccessful(){
        StepVerifier.create(animeController.delete(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("update save updated anime and returns empty Mono when successful")
    public void update_SaveUpdatedAnime_WhenSuccessful(){
        StepVerifier.create(animeController.update(1, AnimeCreator.createValidAnime()))
                .expectSubscription()
                .verifyComplete();
    }

    /**
     * Error cases cannot be tested because these behaviors is handled by service tier
     * so we can assume that <<This test should be done in Integration Test>>
     */

}
