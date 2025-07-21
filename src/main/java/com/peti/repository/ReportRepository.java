package com.peti.repository;

import com.peti.model.Report;
import com.peti.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByUser(User user);
}