package com.talentmarketplace.service;

import java.util.List;

import com.talentmarketplace.dto.request.CreateUnavailabilityRequest;
import com.talentmarketplace.dto.request.UpdateUnavailabilityRequest;
import com.talentmarketplace.dto.response.UnavailabilityResponse;

public interface EmployeeUnavailabilityService {

    UnavailabilityResponse createUnavailability(Long employeeId, CreateUnavailabilityRequest request);

    List<UnavailabilityResponse> getEmployeeUnavailabilities(Long employeeId);

    UnavailabilityResponse getUnavailabilityById(Long employeeId, Long id);

    UnavailabilityResponse updateUnavailability(Long employeeId, Long id, UpdateUnavailabilityRequest request);

    void deleteUnavailability(Long employeeId, Long id);
}
