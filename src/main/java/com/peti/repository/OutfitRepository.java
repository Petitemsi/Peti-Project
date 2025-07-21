package com.peti.repository;

import com.peti.model.Outfit;
import com.peti.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OutfitRepository extends JpaRepository<Outfit, Long> {
    Optional<Outfit> findByIdAndIsActiveTrue(Long id);
    List<Outfit> findAllByIsActiveTrue();

    List<Outfit> findByUserAndIsActiveTrue(User user);

    List<Outfit> findByLastWornDateBetweenAndIsActiveTrue(LocalDate startDate, LocalDate endDate);

    long countByLastWornDateBetweenAndIsActiveTrue(LocalDate startDate, LocalDate endDate);
}