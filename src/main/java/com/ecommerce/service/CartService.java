package com.ecommerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;

    public CartService(CartRepository cartRepo,
                       ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    public CartItem add(User user, Long productId, int qty) {

        if (qty <= 0) {
            throw new RuntimeException("Invalid quantity");
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStock() < qty) {
            throw new RuntimeException("Insufficient stock");
        }

        CartItem item = new CartItem();
        item.setUser(user);
        item.setProduct(product);
        item.setQuantity(qty);

        return cartRepo.save(item);
    }

    public List<CartItem> get(User user) {
        return cartRepo.findByUser(user);
    }
}