package br.com.moraesit.analyticsservice.config;

import br.com.moraesit.analyticsservice.event.ProductViewEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.List;

@Configuration
public class KafkaConsumerConfig {

    private static final String PRODUCT_VIEW_TOPIC = "product-view-events";

    @Bean
    public ReceiverOptions<String, ProductViewEvent> receiverOptions(KafkaProperties properties) {
        return ReceiverOptions.<String, ProductViewEvent>create(properties.buildProducerProperties())
                .consumerProperty(JsonDeserializer.VALUE_DEFAULT_TYPE, ProductViewEvent.class)
                .consumerProperty(JsonDeserializer.USE_TYPE_INFO_HEADERS, false)
                .subscription(List.of(PRODUCT_VIEW_TOPIC));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, ProductViewEvent> kafkaConsumerTemplate(
            ReceiverOptions<String, ProductViewEvent> receiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }
}
