package com.peti.service;


import com.peti.constants.Role;
import com.peti.engine.PetiEngine;
import com.peti.model.Report;
import com.peti.model.User;
import com.peti.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final PetiEngine petiEngine;

    public Report saveReport(Report report) {
        User user=petiEngine.getLoggedInUser();
        report.setUser(user);
        return reportRepository.save(report);
    }

    public List<Report> getAllReports() {
        User user=petiEngine.getLoggedInUser();
        if(user.getRole().equals(Role.USER)) {
            return reportRepository.findAllByUser(user);
        }
        return reportRepository.findAll();
    }

    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }
}