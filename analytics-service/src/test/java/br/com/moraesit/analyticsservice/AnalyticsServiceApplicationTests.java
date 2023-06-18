package br.com.moraesit.analyticsservice;

import br.com.moraesit.analyticsservice.dto.ProductTrendingDTO;
import br.com.moraesit.analyticsservice.event.ProductViewEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureWebTestClient
class AnalyticsServiceApplicationTests extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Test
    void trendingTest() {

        // emit events
        var events = Flux.just(
                        createEvent("a", 2),
                        createEvent("b", 3),
                        createEvent("c", 3),
                        createEvent("d", 2),
                        createEvent("c", 6),
                        createEvent("a", 8),
                        createEvent("e", 1)
                )
                .flatMap(Flux::fromIterable)
                .map(e -> this.toSenderRecord(PRODUCT_VIEW_EVENTS, e.getProductId(), e));

        Flux<SenderResult<String>> resultFlux = this.<ProductViewEvent>createSender().send(events);

        StepVerifier.create(resultFlux)
                .expectNextCount(25)
                .verifyComplete();

        // verify via treding endpoint
        Mono<List<ProductTrendingDTO>> mono = client
                .get()
                .uri("/v1/trending")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .returnResult(new ParameterizedTypeReference<List<ProductTrendingDTO>>() {
                })
                .getResponseBody()
                .next();

        StepVerifier.create(mono)
                .consumeNextWith(this::validateResult)
                .verifyComplete();
    }

    private void validateResult(List<ProductTrendingDTO> trendingDTOList) {
        assertEquals(5, trendingDTOList.size());
        assertEquals("a", trendingDTOList.get(0).getProductId());
        assertEquals(10, trendingDTOList.get(0).getViewCount());

        assertEquals("e", trendingDTOList.get(4).getProductId());
        assertEquals(1, trendingDTOList.get(4).getViewCount());
    }

    private List<ProductViewEvent> createEvent(String productId, int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> new ProductViewEvent(productId))
                .collect(Collectors.toList());
    }
}
