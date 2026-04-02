package com.ecommerce.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService service;
    private final UserRepository userRepo;

    public CartController(CartService service,
                          UserRepository userRepo) {
        this.service = service;
        this.userRepo = userRepo;
    }

    @PostMapping
    public CartItem add(Authentication auth,
                        @RequestParam Long productId,
                        @RequestParam int qty) {

        User user = userRepo.findByUsername(auth.getName()).orElseThrow();
        return service.add(user, productId, qty);
    }

    @GetMapping
    public List<CartItem> get(Authentication auth) {
        User user = userRepo.findByUsername(auth.getName()).orElseThrow();
        return service.get(user);
    }
}
