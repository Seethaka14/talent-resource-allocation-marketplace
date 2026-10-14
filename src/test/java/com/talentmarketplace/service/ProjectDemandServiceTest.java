package com.talentmarketplace.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.talentmarketplace.dto.request.CreateDemandRequest;
import com.talentmarketplace.dto.request.DemandSkillRequest;
import com.talentmarketplace.dto.request.UpdateDemandRequest;
import com.talentmarketplace.dto.response.DemandResponse;
import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.entity.ProficiencyLevel;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.SkillCategory;
import com.talentmarketplace.exception.BusinessRuleValidationException;
import com.talentmarketplace.exception.DuplicateResourceException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.DemandSkillRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.SkillRepository;

@ExtendWith(MockitoExtension.class)
class ProjectDemandServiceTest {

    @Mock
    private ProjectDemandRepository demandRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private DemandSkillRepository demandSkillRepository;

    @InjectMocks
    private ProjectDemandServiceImpl demandService;

    private Skill skillJava;
    private ProjectDemand sampleDemand;

    @BeforeEach
    void setUp() {
        skillJava = new Skill("Java", SkillCategory.BACKEND, "Java Core");
        skillJava.setId(10L);

        sampleDemand = new ProjectDemand(
                "DEM-001",
                "E-Commerce Platform",
                "Backend Engineer",
                "Engineering",
                "Bangalore",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30),
                100,
                DemandPriority.HIGH,
                DemandStatus.OPEN,
                3.0
        );
        sampleDemand.setId(1L);
    }

    @Test
    @DisplayName("createDemand: successfully creates demand with valid fields and required skills")
    void createDemand_Success() {
        CreateDemandRequest request = new CreateDemandRequest(
                "DEM-001",
                "E-Commerce Platform",
                "Backend Engineer",
                "Engineering",
                "Bangalore",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30),
                100,
                DemandPriority.HIGH,
                3.0,
                List.of(new DemandSkillRequest(10L, true, ProficiencyLevel.ADVANCED))
        );

        when(demandRepository.existsByDemandCode("DEM-001")).thenReturn(false);
        when(skillRepository.findById(10L)).thenReturn(Optional.of(skillJava));
        when(demandRepository.save(any(ProjectDemand.class))).thenReturn(sampleDemand);

        DemandResponse response = demandService.createDemand(request);

        assertNotNull(response);
        assertEquals("DEM-001", response.getDemandCode());
        assertEquals("Backend Engineer", response.getRole());
        assertEquals(DemandStatus.OPEN, response.getStatus());
        verify(demandRepository).save(any(ProjectDemand.class));
    }

    @Test
    @DisplayName("createDemand: rejects when start date is after end date (TC-06-02)")
    void createDemand_InvalidDates_ThrowsException() {
        CreateDemandRequest request = new CreateDemandRequest(
                "DEM-002",
                "Project Alpha",
                "Lead Dev",
                "Engineering",
                "Bangalore",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 1, 1), // Invalid!
                100,
                DemandPriority.HIGH,
                2.0,
                List.of()
        );

        assertThrows(BusinessRuleValidationException.class, () ->
                demandService.createDemand(request)
        );
        verify(demandRepository, never()).save(any(ProjectDemand.class));
    }

    @Test
    @DisplayName("createDemand: rejects duplicate demand code")
    void createDemand_DuplicateCode_ThrowsException() {
        CreateDemandRequest request = new CreateDemandRequest(
                "DEM-001",
                "Project Alpha",
                "Lead Dev",
                "Engineering",
                "Bangalore",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 1),
                100,
                DemandPriority.HIGH,
                2.0,
                List.of()
        );

        when(demandRepository.existsByDemandCode("DEM-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                demandService.createDemand(request)
        );
        verify(demandRepository, never()).save(any(ProjectDemand.class));
    }

    @Test
    @DisplayName("createDemand: rejects duplicate required skills for same demand")
    void createDemand_DuplicateSkills_ThrowsException() {
        CreateDemandRequest request = new CreateDemandRequest(
                "DEM-003",
                "Project Beta",
                "Dev",
                "Engineering",
                "Bangalore",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 1),
                100,
                DemandPriority.HIGH,
                2.0,
                List.of(
                        new DemandSkillRequest(10L, true, ProficiencyLevel.ADVANCED),
                        new DemandSkillRequest(10L, false, ProficiencyLevel.BEGINNER) // duplicate!
                )
        );

        when(demandRepository.existsByDemandCode("DEM-003")).thenReturn(false);

        assertThrows(BusinessRuleValidationException.class, () ->
                demandService.createDemand(request)
        );
        verify(demandRepository, never()).save(any(ProjectDemand.class));
    }

    @Test
    @DisplayName("updateDemand: updates demand and validates status transition")
    void updateDemand_Success() {
        UpdateDemandRequest updateReq = new UpdateDemandRequest(
                "E-Commerce Platform",
                "Senior Backend Engineer",
                "Engineering",
                "Bangalore",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30),
                80,
                DemandPriority.CRITICAL,
                DemandStatus.MATCHED,
                4.0,
                new ArrayList<>()
        );

        when(demandRepository.findById(1L)).thenReturn(Optional.of(sampleDemand));
        when(demandRepository.save(any(ProjectDemand.class))).thenReturn(sampleDemand);

        DemandResponse response = demandService.updateDemand(1L, updateReq);

        assertNotNull(response);
        verify(demandRepository).save(sampleDemand);
    }

    @Test
    @DisplayName("updateDemand: rejects modification when demand is CANCELLED")
    void updateDemand_CancelledDemand_ThrowsException() {
        sampleDemand.setStatus(DemandStatus.CANCELLED);

        UpdateDemandRequest updateReq = new UpdateDemandRequest(
                "E-Commerce Platform", "Lead", "Eng", "Bangalore",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30),
                100, DemandPriority.HIGH, DemandStatus.OPEN, 3.0, List.of()
        );

        when(demandRepository.findById(1L)).thenReturn(Optional.of(sampleDemand));

        assertThrows(BusinessRuleValidationException.class, () ->
                demandService.updateDemand(1L, updateReq)
        );
    }

    @Test
    @DisplayName("cancelDemand: sets status to CANCELLED")
    void cancelDemand_Success() {
        when(demandRepository.findById(1L)).thenReturn(Optional.of(sampleDemand));
        when(demandRepository.save(any(ProjectDemand.class))).thenReturn(sampleDemand);

        DemandResponse response = demandService.cancelDemand(1L, "Budget cuts");

        assertNotNull(response);
        assertEquals(DemandStatus.CANCELLED, sampleDemand.getStatus());
        verify(demandRepository).save(sampleDemand);
    }
}
