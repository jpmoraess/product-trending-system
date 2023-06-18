package br.com.moraesit.productservice;

import br.com.moraesit.productservice.entity.Product;
import br.com.moraesit.productservice.producer.event.ProductViewEvent;
import br.com.moraesit.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureWebTestClient
class ProductServiceApplicationTests extends AbstractIntegrationTest {
    private static final String PRODUCT1 = "005fbdd2-0dfe-11ee-be56-0242ac120001";
    private static final String PRODUCT2 = "005fbdd2-0dfe-11ee-be56-0242ac120002";

    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        var products = List.of(
                new Product(PRODUCT1, "product-1", new BigDecimal("10.20")),
                new Product(PRODUCT2, "product-2", new BigDecimal("12.95"))
        );
        productRepository.saveAll(products).blockLast();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll().block();
    }

    @Test
    void productViewAndEventTest() {
        // view products
        viewProductSuccess(PRODUCT1);
        viewProductSuccess(PRODUCT1);
        viewProductError("not-found-id");
        viewProductSuccess(PRODUCT2);

        // check if the events are emitted
        Flux<ReceiverRecord<String, ProductViewEvent>> flux = this.<ProductViewEvent>createReceiver(PRODUCT_VIEW_EVENTS)
                .receive()
                .take(3);

        StepVerifier.create(flux)
                .consumeNextWith(r -> assertEquals(PRODUCT1, r.value().getProductId()))
                .consumeNextWith(r -> assertEquals(PRODUCT1, r.value().getProductId()))
                .consumeNextWith(r -> assertEquals(PRODUCT2, r.value().getProductId()))
                .verifyComplete();
    }

    private void viewProductSuccess(String id) {
        client.get()
                .uri("/v1/products/" + id)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.name").isNotEmpty();
    }

    private void viewProductError(String id) {
        client.get()
                .uri("/v1/products/" + id)
                .exchange()
                .expectStatus().is4xxClientError();
    }
}
