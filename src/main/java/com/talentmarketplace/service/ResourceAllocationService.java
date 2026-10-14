package com.talentmarketplace.service;

import java.util.List;

import com.talentmarketplace.dto.request.CreateAllocationRequest;
import com.talentmarketplace.dto.request.RejectAllocationRequest;
import com.talentmarketplace.dto.response.AllocationResponse;

public interface ResourceAllocationService {

    AllocationResponse createAllocation(CreateAllocationRequest request);

    AllocationResponse confirmAllocation(Long allocationId, String confirmedBy);

    AllocationResponse cancelAllocation(Long allocationId, String reason);

    AllocationResponse rejectAllocation(Long allocationId, RejectAllocationRequest request);

    AllocationResponse getAllocationById(Long allocationId);

    List<AllocationResponse> getAllocationsForEmployee(Long employeeId);

    List<AllocationResponse> getAllocationsForDemand(Long demandId);
}
