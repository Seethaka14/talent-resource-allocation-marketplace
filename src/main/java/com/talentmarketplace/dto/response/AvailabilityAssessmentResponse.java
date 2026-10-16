package com.talentmarketplace.dto.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.talentmarketplace.entity.EmploymentStatus;

/**
 * Data Transfer Object representing an employee availability assessment over a specific date range.
 *
 * <p><b>CAPACITY & PENDING ALLOCATION POLICY:</b></p>
 * <ul>
 *   <li><b>CONFIRMED Allocations:</b> Hard-reserve capacity. Confirmed allocations represent binding project
 *       commitments and directly reduce an employee's remaining capacity on each overlapping date.</li>
 *   <li><b>PENDING Allocations:</b> Do NOT deplete remaining capacity across other demands. Pending requests
 *       represent manager proposals undergoing review. Depleting capacity for pending requests would result in
 *       "phantom lockouts" where unapproved requests artificially lock up enterprise talent.</li>
 *   <li><b>Demand Deduplication:</b> While PENDING allocations do not consume multi-demand capacity, they strictly
 *       block duplicate candidate requests on the SAME target demand (via {@code existsActiveAllocationForEmployeeAndDemand}).</li>
 *   <li><b>Candidate Ranking:</b> Active PENDING allocations on the target demand flag the candidate with
 *       "Allocation Pending" (disallowing duplicate requests). CONFIRMED allocations flag them as "Already Allocated".</li>
 *   <li><b>Commit-Time Concurrency Protection:</b> When a proposal is confirmed, pessimistic write locks
 *       ({@code SELECT ... FOR UPDATE} on the employee row) serialize confirmations and re-evaluate total confirmed
 *       capacity at commit time to guarantee 100% capacity limits are never breached.</li>
 * </ul>
 */
public class AvailabilityAssessmentResponse {

    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private EmploymentStatus employmentStatus;
    private Integer maxCapacityPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer requestedAllocationPercent;
    private boolean available;
    private Integer peakAllocatedPercent = 0;
    private Integer remainingCapacityPercent;
    private Integer pendingAllocationsCount = 0;
    private Integer pendingAllocationPercent = 0;
    private List<UnavailabilityResponse> overlappingUnavailabilities = new ArrayList<>();
    private String reason;

    public AvailabilityAssessmentResponse() {
    }

    public AvailabilityAssessmentResponse(Long employeeId, String employeeCode, String employeeName,
                                          EmploymentStatus employmentStatus, Integer maxCapacityPercent,
                                          LocalDate startDate, LocalDate endDate,
                                          Integer requestedAllocationPercent, boolean available,
                                          Integer remainingCapacityPercent,
                                          List<UnavailabilityResponse> overlappingUnavailabilities,
                                          String reason) {
        this(employeeId, employeeCode, employeeName, employmentStatus, maxCapacityPercent,
             startDate, endDate, requestedAllocationPercent, available,
             (maxCapacityPercent != null ? Math.max(0, maxCapacityPercent - (remainingCapacityPercent != null ? remainingCapacityPercent : 0)) : 0),
             remainingCapacityPercent, 0, 0, overlappingUnavailabilities, reason);
    }

    public AvailabilityAssessmentResponse(Long employeeId, String employeeCode, String employeeName,
                                          EmploymentStatus employmentStatus, Integer maxCapacityPercent,
                                          LocalDate startDate, LocalDate endDate,
                                          Integer requestedAllocationPercent, boolean available,
                                          Integer peakAllocatedPercent,
                                          Integer remainingCapacityPercent,
                                          Integer pendingAllocationsCount,
                                          Integer pendingAllocationPercent,
                                          List<UnavailabilityResponse> overlappingUnavailabilities,
                                          String reason) {
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.employmentStatus = employmentStatus;
        this.maxCapacityPercent = maxCapacityPercent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.requestedAllocationPercent = requestedAllocationPercent;
        this.available = available;
        this.peakAllocatedPercent = peakAllocatedPercent != null ? peakAllocatedPercent : 0;
        this.remainingCapacityPercent = remainingCapacityPercent;
        this.pendingAllocationsCount = pendingAllocationsCount != null ? pendingAllocationsCount : 0;
        this.pendingAllocationPercent = pendingAllocationPercent != null ? pendingAllocationPercent : 0;
        this.overlappingUnavailabilities = overlappingUnavailabilities != null ? overlappingUnavailabilities : new ArrayList<>();
        this.reason = reason;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public Integer getMaxCapacityPercent() {
        return maxCapacityPercent;
    }

    public void setMaxCapacityPercent(Integer maxCapacityPercent) {
        this.maxCapacityPercent = maxCapacityPercent;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getRequestedAllocationPercent() {
        return requestedAllocationPercent;
    }

    public void setRequestedAllocationPercent(Integer requestedAllocationPercent) {
        this.requestedAllocationPercent = requestedAllocationPercent;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Integer getRemainingCapacityPercent() {
        return remainingCapacityPercent;
    }

    public void setRemainingCapacityPercent(Integer remainingCapacityPercent) {
        this.remainingCapacityPercent = remainingCapacityPercent;
    }

    public List<UnavailabilityResponse> getOverlappingUnavailabilities() {
        return overlappingUnavailabilities;
    }

    public void setOverlappingUnavailabilities(List<UnavailabilityResponse> overlappingUnavailabilities) {
        this.overlappingUnavailabilities = overlappingUnavailabilities;
    }

    public Integer getPeakAllocatedPercent() {
        return peakAllocatedPercent;
    }

    public void setPeakAllocatedPercent(Integer peakAllocatedPercent) {
        this.peakAllocatedPercent = peakAllocatedPercent != null ? peakAllocatedPercent : 0;
    }

    public Integer getPendingAllocationsCount() {
        return pendingAllocationsCount;
    }

    public void setPendingAllocationsCount(Integer pendingAllocationsCount) {
        this.pendingAllocationsCount = pendingAllocationsCount != null ? pendingAllocationsCount : 0;
    }

    public Integer getPendingAllocationPercent() {
        return pendingAllocationPercent;
    }

    public void setPendingAllocationPercent(Integer pendingAllocationPercent) {
        this.pendingAllocationPercent = pendingAllocationPercent != null ? pendingAllocationPercent : 0;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
