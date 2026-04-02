package com.ecommerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public Product save(Product p) {
        if (p.getPrice() <= 0) {
            throw new RuntimeException("Invalid price");
        }
        return repo.save(p);
    }

    public List<Product> getAll() {
        return repo.findAll();
    }
}