package com.talentmarketplace.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentmarketplace.dto.request.CreateUnavailabilityRequest;
import com.talentmarketplace.dto.request.UpdateUnavailabilityRequest;
import com.talentmarketplace.dto.response.UnavailabilityResponse;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeUnavailability;
import com.talentmarketplace.exception.BusinessRuleValidationException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeUnavailabilityRepository;

@Service
@Transactional
public class EmployeeUnavailabilityServiceImpl implements EmployeeUnavailabilityService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeUnavailabilityRepository unavailabilityRepository;

    public EmployeeUnavailabilityServiceImpl(EmployeeRepository employeeRepository,
                                            EmployeeUnavailabilityRepository unavailabilityRepository) {
        this.employeeRepository = employeeRepository;
        this.unavailabilityRepository = unavailabilityRepository;
    }

    @Override
    public UnavailabilityResponse createUnavailability(Long employeeId, CreateUnavailabilityRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleValidationException("Start date cannot be after end date");
        }

        List<EmployeeUnavailability> overlapping = unavailabilityRepository.findOverlappingUnavailabilities(
                employeeId, request.getStartDate(), request.getEndDate()
        );

        if (!overlapping.isEmpty()) {
            EmployeeUnavailability conflict = overlapping.get(0);
            throw new BusinessRuleValidationException(String.format(
                    "Employee '%s' already has an overlapping unavailability period from %s to %s (%s)",
                    employee.getEmployeeCode(), conflict.getStartDate(), conflict.getEndDate(), conflict.getType()
            ));
        }

        EmployeeUnavailability unavailability = new EmployeeUnavailability(
                employee,
                request.getStartDate(),
                request.getEndDate(),
                request.getType(),
                request.getReason() != null ? request.getReason().trim() : null
        );

        EmployeeUnavailability saved = unavailabilityRepository.save(unavailability);
        return UnavailabilityResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnavailabilityResponse> getEmployeeUnavailabilities(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        return unavailabilityRepository.findByEmployeeIdOrderByStartDateAsc(employeeId)
                .stream()
                .map(UnavailabilityResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UnavailabilityResponse getUnavailabilityById(Long employeeId, Long id) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        EmployeeUnavailability unavailability = unavailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unavailability", "id", id));

        if (!unavailability.getEmployee().getId().equals(employeeId)) {
            throw new ResourceNotFoundException("Unavailability", "id", id);
        }

        return UnavailabilityResponse.fromEntity(unavailability);
    }

    @Override
    public UnavailabilityResponse updateUnavailability(Long employeeId, Long id, UpdateUnavailabilityRequest request) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        EmployeeUnavailability unavailability = unavailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unavailability", "id", id));

        if (!unavailability.getEmployee().getId().equals(employeeId)) {
            throw new ResourceNotFoundException("Unavailability", "id", id);
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleValidationException("Start date cannot be after end date");
        }

        List<EmployeeUnavailability> overlapping = unavailabilityRepository.findOverlappingUnavailabilitiesExcludingId(
                employeeId, id, request.getStartDate(), request.getEndDate()
        );

        if (!overlapping.isEmpty()) {
            EmployeeUnavailability conflict = overlapping.get(0);
            throw new BusinessRuleValidationException(String.format(
                    "Employee already has an overlapping unavailability period from %s to %s (%s)",
                    conflict.getStartDate(), conflict.getEndDate(), conflict.getType()
            ));
        }

        unavailability.setStartDate(request.getStartDate());
        unavailability.setEndDate(request.getEndDate());
        unavailability.setType(request.getType());
        unavailability.setReason(request.getReason() != null ? request.getReason().trim() : null);

        EmployeeUnavailability updated = unavailabilityRepository.save(unavailability);
        return UnavailabilityResponse.fromEntity(updated);
    }

    @Override
    public void deleteUnavailability(Long employeeId, Long id) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        EmployeeUnavailability unavailability = unavailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unavailability", "id", id));

        if (!unavailability.getEmployee().getId().equals(employeeId)) {
            throw new ResourceNotFoundException("Unavailability", "id", id);
        }

        unavailabilityRepository.delete(unavailability);
    }
}
