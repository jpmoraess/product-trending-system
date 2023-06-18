package br.com.moraesit.analyticsservice.api;

import br.com.moraesit.analyticsservice.dto.ProductTrendingDTO;
import br.com.moraesit.analyticsservice.service.ProductTrendingBroadcastService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/v1/trending")
public class TrendingApi {

    private final ProductTrendingBroadcastService productTrendingBroadcastService;

    public TrendingApi(ProductTrendingBroadcastService productTrendingBroadcastService) {
        this.productTrendingBroadcastService = productTrendingBroadcastService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<ProductTrendingDTO>> trending() {
        return productTrendingBroadcastService.getTrends();
    }
}
