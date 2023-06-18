package br.com.moraesit.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTrendingDTO {
    private String productId;
    private Long viewCount;
}
