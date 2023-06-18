package br.com.moraesit.analyticsservice.service;

import br.com.moraesit.analyticsservice.entity.ProductViewCount;
import br.com.moraesit.analyticsservice.event.ProductViewEvent;
import br.com.moraesit.analyticsservice.repository.ProductViewRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductViewEventConsumer {
    private final ReactiveKafkaConsumerTemplate<String, ProductViewEvent> consumerTemplate;
    private final ProductViewRepository productViewRepository;

    public ProductViewEventConsumer(ReactiveKafkaConsumerTemplate<String, ProductViewEvent> consumerTemplate,
                                    ProductViewRepository productViewRepository) {
        this.consumerTemplate = consumerTemplate;
        this.productViewRepository = productViewRepository;
    }

    @PostConstruct
    public void subscribe() {
        consumerTemplate
                .receive()
                .bufferTimeout(1000, Duration.ofSeconds(1))
                .map(this::process)
                .subscribe();
    }

    private Mono<Void> process(List<ReceiverRecord<String, ProductViewEvent>> events) {
        var eventsMap = events.stream()
                .map(r -> r.value().getProductId())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        return productViewRepository.findAllById(eventsMap.keySet())
                .collectMap(ProductViewCount::getProductId)
                .defaultIfEmpty(Collections.emptyMap())
                .map(dbMap -> eventsMap.keySet().stream().map(productId -> updateViewCount(dbMap, eventsMap, productId)).collect(Collectors.toList()))
                .flatMapMany(productViewRepository::saveAll)
                .doOnComplete(() -> events.get(events.size() - 1).receiverOffset().acknowledge())
                .doOnError(ex -> log.error(ex.getMessage()))
                .then();
    }

    private ProductViewCount updateViewCount(Map<String, ProductViewCount> dbMap, Map<String, Long> eventMap, String productId) {
        ProductViewCount productViewCount = dbMap.getOrDefault(productId, new ProductViewCount(productId, 0L));
        productViewCount.setCount(productViewCount.getCount() + eventMap.get(productId));
        return productViewCount;
    }
}
