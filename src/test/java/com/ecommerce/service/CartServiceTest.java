package com.ecommerce.service;

import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest {

    @Test
    void add_shouldAddItem() {

        CartRepository cartRepo = Mockito.mock(CartRepository.class);
        ProductRepository productRepo = Mockito.mock(ProductRepository.class);

        CartService service = new CartService(cartRepo, productRepo);

        User user = new User();

        Product product = new Product();
        product.setStock(10);
        product.setPrice(100);

        Mockito.when(productRepo.findById(1L))
                .thenReturn(Optional.of(product));

        Mockito.when(cartRepo.save(Mockito.any(CartItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CartItem item = service.add(user, 1L, 2);

        assertNotNull(item);
        assertEquals(2, item.getQuantity());
        assertEquals(product, item.getProduct());
    }

    @Test
    void add_shouldThrowException_whenStockLow() {

        CartRepository cartRepo = Mockito.mock(CartRepository.class);
        ProductRepository productRepo = Mockito.mock(ProductRepository.class);

        CartService service = new CartService(cartRepo, productRepo);

        User user = new User();

        Product product = new Product();
        product.setStock(1);

        Mockito.when(productRepo.findById(1L))
                .thenReturn(Optional.of(product));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.add(user, 1L, 5);
        });

        assertTrue(exception.getMessage().contains("stock"));
    }

    @Test
    void add_shouldThrowException_whenQuantityInvalid() {

        CartRepository cartRepo = Mockito.mock(CartRepository.class);
        ProductRepository productRepo = Mockito.mock(ProductRepository.class);

        CartService service = new CartService(cartRepo, productRepo);

        User user = new User();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.add(user, 1L, 0);
        });

        assertTrue(exception.getMessage().contains("quantity"));
    }
}