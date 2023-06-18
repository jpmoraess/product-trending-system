package br.com.moraesit.analyticsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@EqualsAndHashCode(of = {"productId"})
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_view_count")
public class ProductViewCount {
    @Id
    private String productId;
    private Long count;

    public void setCount(Long count) {
        this.count = count;
    }
}
