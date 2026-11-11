package com.jas.sarees.saree;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SareeRepository extends JpaRepository<Saree, Long> {

    List<Saree> findByStatusOrderByCreatedAtDesc(Saree.Status status);

    List<Saree> findAllByOrderByCreatedAtDesc();
}