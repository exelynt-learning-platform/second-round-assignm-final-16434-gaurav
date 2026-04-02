package com.ecommerce.service;

import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    @Test
    void placeOrder_shouldCalculateTotal() {

        OrderRepository orderRepo = Mockito.mock(OrderRepository.class);
        CartRepository cartRepo = Mockito.mock(CartRepository.class);

        OrderService service = new OrderService(orderRepo, cartRepo);

        User user = new User();

        Product product = new Product();
        product.setPrice(200);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);

        Mockito.when(cartRepo.findByUser(user))
                .thenReturn(List.of(item));

        // 🔥 IMPORTANT FIX
        Mockito.when(orderRepo.save(Mockito.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order order = service.placeOrder(user);

        assertEquals(400, order.getTotalPrice());
    }
}