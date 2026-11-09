package com.jas.sarees.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SareeOrderRepository extends JpaRepository<SareeOrder, Long> {

    List<SareeOrder> findAllByOrderByCreatedAtDesc();
}