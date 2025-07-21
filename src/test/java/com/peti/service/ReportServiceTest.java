package com.peti.service;

import com.peti.constants.Role;
import com.peti.engine.PetiEngine;
import com.peti.model.Report;
import com.peti.model.User;
import com.peti.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PetiEngine petiEngine;

    @InjectMocks
    private ReportService reportService;

    private User user;
    private Report report;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setFirstName("test");
        user.setLastName("user");
        user.setEmail("testuser@example.com");
        user.setRole(Role.USER);

        report = new Report();
        report.setId(1L);
        report.setTitle("Test Report");
        report.setDetails("Test Details");
        report.setUser(user);
    }

    @Test
    void saveReport_ShouldSaveAndReturnReport() {
        when(petiEngine.getLoggedInUser()).thenReturn(user);
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        Report result = reportService.saveReport(report);

        assertEquals(report, result);
        verify(petiEngine, times(1)).getLoggedInUser();
        verify(reportRepository, times(1)).save(report);
    }

    @Test
    void getAllReports_ShouldReturnAllReports() {
        // Arrange
        when(petiEngine.getLoggedInUser()).thenReturn(user); // Mock PetiEngine to return the user
        List<Report> reports = Arrays.asList(report);
        when(reportRepository.findAllByUser(user)).thenReturn(reports); // Mock repository for USER role

        // Act
        List<Report> result = reportService.getAllReports();

        // Assert
        assertEquals(1, result.size());
        assertEquals(report, result.get(0));
        assertNotNull(result.get(0).getUser(), "Report user should not be null");
        verify(petiEngine, times(1)).getLoggedInUser();
        verify(reportRepository, times(1)).findAllByUser(user); // Verify correct method for USER role
    }

    @Test
    void getReportById_WhenReportExists_ShouldReturnReport() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        Optional<Report> result = reportService.getReportById(1L);

        assertTrue(result.isPresent());
        assertEquals(report, result.get());
        verify(reportRepository, times(1)).findById(1L);
    }

    @Test
    void getReportById_WhenReportDoesNotExist_ShouldReturnEmpty() {
        when(reportRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Report> result = reportService.getReportById(1L);

        assertFalse(result.isPresent());
        verify(reportRepository, times(1)).findById(1L);
    }

    @Test
    void deleteReport_ShouldDeleteReport() {
        reportService.deleteReport(1L);

        verify(reportRepository, times(1)).deleteById(1L);
    }
}