package com.talentmarketplace.service;

import java.time.LocalDate;

import com.talentmarketplace.dto.response.AvailabilityAssessmentResponse;
import com.talentmarketplace.entity.Employee;

public interface AvailabilityService {

    AvailabilityAssessmentResponse assessEmployeeAvailability(Long employeeId, LocalDate startDate,
                                                             LocalDate endDate, Integer requestedAllocationPercent);

    AvailabilityAssessmentResponse assessEmployeeAvailability(Employee employee, LocalDate startDate,
                                                             LocalDate endDate, Integer requestedAllocationPercent);

    AvailabilityAssessmentResponse assessEmployeeAvailabilityExcludingAllocation(
            Employee employee, LocalDate startDate, LocalDate endDate,
            Integer requestedAllocationPercent, Long excludeAllocationId);
}
