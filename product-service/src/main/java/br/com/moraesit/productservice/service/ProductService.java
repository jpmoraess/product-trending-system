package br.com.moraesit.productservice.service;

import br.com.moraesit.productservice.entity.Product;
import br.com.moraesit.productservice.producer.ProductViewEventProducer;
import br.com.moraesit.productservice.producer.event.ProductViewEvent;
import br.com.moraesit.productservice.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductViewEventProducer productViewEventProducer;

    public ProductService(ProductRepository productRepository, ProductViewEventProducer productViewEventProducer) {
        this.productRepository = productRepository;
        this.productViewEventProducer = productViewEventProducer;
    }

    public Mono<Product> save(Product product) {
        return productRepository.save(product);
    }

    public Mono<Product> get(String id) {
        return productRepository.findById(id)
                .doOnNext(e -> productViewEventProducer.emitEvent(new ProductViewEvent(e.getId())));
    }
}
