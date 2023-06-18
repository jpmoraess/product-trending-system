package br.com.moraesit.analyticsservice.repository;

import br.com.moraesit.analyticsservice.entity.ProductViewCount;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductViewRepository extends ReactiveMongoRepository<ProductViewCount, String> {
    Flux<ProductViewCount> findTop5ByOrderByCountDesc();
}
