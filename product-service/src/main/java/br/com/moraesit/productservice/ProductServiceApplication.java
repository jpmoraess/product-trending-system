package br.com.moraesit.productservice;

import br.com.moraesit.productservice.entity.Product;
import br.com.moraesit.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class ProductServiceApplication implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        List<Product> productList = List.of(
                new Product("ab72ffb8-0e1c-11ee-be56-0242ac120001", "product-1", new BigDecimal("11.25")),
                new Product("ab72ffb8-0e1c-11ee-be56-0242ac120002", "product-2", new BigDecimal("23.99")),
                new Product("ab72ffb8-0e1c-11ee-be56-0242ac120003", "product-3", new BigDecimal("12.50")),
                new Product("ab72ffb8-0e1c-11ee-be56-0242ac120004", "product-4", new BigDecimal("9.34")),
                new Product("ab72ffb8-0e1c-11ee-be56-0242ac120005", "product-5", new BigDecimal("21.22"))
        );

        productRepository.saveAll(productList).blockLast();
    }
}
