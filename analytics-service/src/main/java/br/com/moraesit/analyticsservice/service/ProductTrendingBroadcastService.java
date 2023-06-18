package br.com.moraesit.analyticsservice.service;

import br.com.moraesit.analyticsservice.dto.ProductTrendingDTO;
import br.com.moraesit.analyticsservice.repository.ProductViewRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Service
public class ProductTrendingBroadcastService {

    private final ProductViewRepository productViewRepository;
    private Flux<List<ProductTrendingDTO>> trends;

    public ProductTrendingBroadcastService(ProductViewRepository productViewRepository) {
        this.productViewRepository = productViewRepository;
    }

    @PostConstruct
    private void init() {
        this.trends = productViewRepository.findTop5ByOrderByCountDesc()
                .map(productViewCount -> new ProductTrendingDTO(productViewCount.getProductId(), productViewCount.getCount()))
                .collectList()
                .filter(Predicate.not(List::isEmpty))
                .repeatWhen(x -> x.delayElements(Duration.ofSeconds(3)))
                .distinctUntilChanged()
                .cache(1);
    }

    public Flux<List<ProductTrendingDTO>> getTrends() {
        return this.trends;
    }
}
