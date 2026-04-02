package com.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {}