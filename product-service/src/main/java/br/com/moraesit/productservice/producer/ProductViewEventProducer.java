package br.com.moraesit.productservice.producer;

import br.com.moraesit.productservice.producer.event.ProductViewEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.kafka.sender.SenderRecord;

@Slf4j
public class ProductViewEventProducer {
    private final ReactiveKafkaProducerTemplate<String, ProductViewEvent> producerTemplate;
    private final Sinks.Many<ProductViewEvent> sink;
    private final Flux<ProductViewEvent> flux;
    private final String topic;

    public ProductViewEventProducer(ReactiveKafkaProducerTemplate<String, ProductViewEvent> producerTemplate,
                                    Sinks.Many<ProductViewEvent> sink, Flux<ProductViewEvent> flux, String topic) {
        this.producerTemplate = producerTemplate;
        this.sink = sink;
        this.flux = flux;
        this.topic = topic;
    }

    public void subscribe() {
        var senderRecordFlux = flux
                .map(e -> new ProducerRecord<>(topic, e.getProductId().toString(), e))
                .map(producerRecord -> SenderRecord.create(producerRecord, producerRecord.key()));
        producerTemplate.send(senderRecordFlux)
                .doOnNext(r -> log.info("emitted event: {}", r.correlationMetadata()))
                .subscribe();
    }

    public void emitEvent(ProductViewEvent productViewEvent) {
        sink.tryEmitNext(productViewEvent);
    }
}
