package br.com.moraesit.productservice.config;

import br.com.moraesit.productservice.producer.ProductViewEventProducer;
import br.com.moraesit.productservice.producer.event.ProductViewEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Sinks;
import reactor.kafka.sender.SenderOptions;

@Configuration
public class KafkaProducerConfig {
    private static final String PRODUCT_VIEW_TOPIC = "product-view-events";

    @Bean
    public SenderOptions<String, ProductViewEvent> senderOptions(KafkaProperties properties) {
        return SenderOptions.create(properties.buildProducerProperties());
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, ProductViewEvent> producerTemplate(
            SenderOptions<String, ProductViewEvent> senderOptions) {
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }

    @Bean
    public ProductViewEventProducer productViewEventProducer(ReactiveKafkaProducerTemplate<String, ProductViewEvent> template) {
        var sink = Sinks.many().unicast().<ProductViewEvent>onBackpressureBuffer();
        var flux = sink.asFlux();
        var eventProducer = new ProductViewEventProducer(template, sink, flux, PRODUCT_VIEW_TOPIC);
        eventProducer.subscribe();
        return eventProducer;
    }
}
