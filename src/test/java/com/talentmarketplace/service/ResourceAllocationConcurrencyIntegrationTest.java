package com.talentmarketplace.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.talentmarketplace.config.TestDatabaseSafetyGuard;
import com.talentmarketplace.dto.request.CreateAllocationRequest;
import com.talentmarketplace.dto.response.AllocationResponse;
import com.talentmarketplace.entity.AllocationStatus;
import com.talentmarketplace.entity.DemandPriority;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmploymentStatus;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.ResourceAllocation;
import com.talentmarketplace.exception.BusinessRuleValidationException;
import com.talentmarketplace.repository.DemandSkillRepository;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.EmployeeUnavailabilityRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.ResourceAllocationRepository;
import com.talentmarketplace.repository.SkillRepository;

@SpringBootTest
public class ResourceAllocationConcurrencyIntegrationTest {

    @Autowired
    private ResourceAllocationService allocationService;

    @Autowired
    private ResourceAllocationRepository allocationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectDemandRepository demandRepository;

    @Autowired
    private DemandSkillRepository demandSkillRepository;

    @Autowired
    private EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    private EmployeeUnavailabilityRepository unavailabilityRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private DataSource dataSource;

    private ExecutorService executorService;
    private Employee testEmployee;
    private ProjectDemand demandA;
    private ProjectDemand demandB;

    @BeforeEach
    void setUp() {
        TestDatabaseSafetyGuard.assertSafeTestDatabase(dataSource);

        allocationRepository.deleteAll();
        demandSkillRepository.deleteAll();
        demandRepository.deleteAll();
        unavailabilityRepository.deleteAll();
        employeeSkillRepository.deleteAll();
        skillRepository.deleteAll();
        employeeRepository.deleteAll();

        executorService = Executors.newFixedThreadPool(4);

        // 1. Create active employee with 100% capacity
        testEmployee = new Employee(
                "EMP-CONCUR-01", "Ada", "Lovelace", "ada.concur@example.com",
                "Engineering", "Principal Engineer", "London",
                LocalDate.of(2025, 1, 1), EmploymentStatus.ACTIVE, 100
        );
        testEmployee = employeeRepository.save(testEmployee);

        // 2. Create two distinct project demands on identical overlapping date ranges
        demandA = new ProjectDemand(
                "DEM-CONCUR-A", "Distributed Consensus", "Backend Architect", "Engineering",
                "London", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                60, DemandPriority.HIGH, DemandStatus.OPEN, 5.0
        );
        demandA = demandRepository.save(demandA);

        demandB = new ProjectDemand(
                "DEM-CONCUR-B", "Data Replication Engine", "Backend Architect", "Engineering",
                "London", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                60, DemandPriority.HIGH, DemandStatus.OPEN, 5.0
        );
        demandB = demandRepository.save(demandB);
    }

    @AfterEach
    void tearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    @DisplayName("TC-06-10: Two simultaneous confirmations for 60% each on overlapping dates: exactly one succeeds, one rejected")
    void testSimultaneousConfirmations_exceedingCapacity_exactlyOneSucceeds() throws Exception {
        runConcurrentConfirmationTest();
    }

    @RepeatedTest(5)
    @DisplayName("TC-06-10 (Repeated): Verify race-condition resilience across multiple iterations")
    void testSimultaneousConfirmations_repeatedRuns_zeroFlakiness() throws Exception {
        runConcurrentConfirmationTest();
    }

    private void runConcurrentConfirmationTest() throws Exception {
        // Create 2 pending allocations: Allocation A (60%) and Allocation B (60%)
        CreateAllocationRequest reqA = new CreateAllocationRequest(
                demandA.getId(), testEmployee.getId(),
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 60, "Staffing Lead"
        );
        CreateAllocationRequest reqB = new CreateAllocationRequest(
                demandB.getId(), testEmployee.getId(),
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 60, "Staffing Lead"
        );

        AllocationResponse allocA = allocationService.createAllocation(reqA);
        AllocationResponse allocB = allocationService.createAllocation(reqB);

        assertThat(allocA.getStatus()).isEqualTo(AllocationStatus.PENDING);
        assertThat(allocB.getStatus()).isEqualTo(AllocationStatus.PENDING);

        // Latches to ensure simultaneous execution
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<AllocationResponse> taskA = () -> {
            readyLatch.countDown();
            if (!startLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timeout waiting for start latch in Task A");
            }
            return allocationService.confirmAllocation(allocA.getId(), "Manager A");
        };

        Callable<AllocationResponse> taskB = () -> {
            readyLatch.countDown();
            if (!startLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timeout waiting for start latch in Task B");
            }
            return allocationService.confirmAllocation(allocB.getId(), "Manager B");
        };

        Future<AllocationResponse> futureA = executorService.submit(taskA);
        Future<AllocationResponse> futureB = executorService.submit(taskB);

        // Wait until both threads are aligned and ready
        boolean ready = readyLatch.await(5, TimeUnit.SECONDS);
        assertThat(ready).as("Both confirmation tasks must be ready").isTrue();

        // Release both tasks at the exact same instant
        startLatch.countDown();

        int successCount = 0;
        int failureCount = 0;
        String failureReason = null;

        try {
            AllocationResponse resA = futureA.get(10, TimeUnit.SECONDS);
            if (resA.getStatus() == AllocationStatus.CONFIRMED) {
                successCount++;
            }
        } catch (ExecutionException e) {
            failureCount++;
            failureReason = e.getCause().getMessage();
            assertThat(e.getCause()).isInstanceOf(BusinessRuleValidationException.class);
        }

        try {
            AllocationResponse resB = futureB.get(10, TimeUnit.SECONDS);
            if (resB.getStatus() == AllocationStatus.CONFIRMED) {
                successCount++;
            }
        } catch (ExecutionException e) {
            failureCount++;
            failureReason = e.getCause().getMessage();
            assertThat(e.getCause()).isInstanceOf(BusinessRuleValidationException.class);
        }

        // Assertion 1: Exactly one confirmation succeeded and exactly one failed
        assertThat(successCount)
                .as("Exactly one allocation confirmation must succeed under concurrency")
                .isEqualTo(1);

        assertThat(failureCount)
                .as("Exactly one allocation confirmation must fail due to capacity exhaustion")
                .isEqualTo(1);

        assertThat(failureReason)
                .as("Failure message must state allocation confirmation failed due to capacity")
                .contains("Allocation confirmation failed")
                .contains("capacity");

        // Assertion 2: Verify database state
        ResourceAllocation freshAllocA = allocationRepository.findById(allocA.getId()).orElseThrow();
        ResourceAllocation freshAllocB = allocationRepository.findById(allocB.getId()).orElseThrow();

        List<AllocationStatus> statuses = List.of(freshAllocA.getStatus(), freshAllocB.getStatus());
        assertThat(statuses).containsExactlyInAnyOrder(AllocationStatus.CONFIRMED, AllocationStatus.PENDING);

        // Assertion 3: Verify the total confirmed capacity in DB never exceeds 100%
        List<ResourceAllocation> confirmedAllocations = allocationRepository.findOverlappingConfirmedAllocations(
                testEmployee.getId(), LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)
        );
        assertThat(confirmedAllocations).hasSize(1);
        int totalConfirmedPercent = confirmedAllocations.stream()
                .mapToInt(ResourceAllocation::getAllocationPercent)
                .sum();
        assertThat(totalConfirmedPercent)
                .as("Total confirmed allocation percentage must not exceed 100%")
                .isEqualTo(60);
    }

    @Test
    @DisplayName("Boundary: Two simultaneous confirmations for 50% each (total 100%) should both succeed")
    void testSimultaneousConfirmations_boundaryExactly100Percent_bothSucceed() throws Exception {
        // Demands requesting 50% each
        ProjectDemand demand50A = demandRepository.save(new ProjectDemand(
                "DEM-50-A", "Service A", "Engineer", "Engineering",
                "London", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                50, DemandPriority.MEDIUM, DemandStatus.OPEN, 3.0
        ));
        ProjectDemand demand50B = demandRepository.save(new ProjectDemand(
                "DEM-50-B", "Service B", "Engineer", "Engineering",
                "London", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                50, DemandPriority.MEDIUM, DemandStatus.OPEN, 3.0
        ));

        CreateAllocationRequest reqA = new CreateAllocationRequest(
                demand50A.getId(), testEmployee.getId(),
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50, "Lead"
        );
        CreateAllocationRequest reqB = new CreateAllocationRequest(
                demand50B.getId(), testEmployee.getId(),
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), 50, "Lead"
        );

        AllocationResponse allocA = allocationService.createAllocation(reqA);
        AllocationResponse allocB = allocationService.createAllocation(reqB);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<AllocationResponse> taskA = () -> {
            readyLatch.countDown();
            startLatch.await(5, TimeUnit.SECONDS);
            return allocationService.confirmAllocation(allocA.getId(), "Manager 1");
        };

        Callable<AllocationResponse> taskB = () -> {
            readyLatch.countDown();
            startLatch.await(5, TimeUnit.SECONDS);
            return allocationService.confirmAllocation(allocB.getId(), "Manager 2");
        };

        Future<AllocationResponse> futureA = executorService.submit(taskA);
        Future<AllocationResponse> futureB = executorService.submit(taskB);

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        AllocationResponse resA = futureA.get(10, TimeUnit.SECONDS);
        AllocationResponse resB = futureB.get(10, TimeUnit.SECONDS);

        assertThat(resA.getStatus()).isEqualTo(AllocationStatus.CONFIRMED);
        assertThat(resB.getStatus()).isEqualTo(AllocationStatus.CONFIRMED);

        List<ResourceAllocation> confirmedAllocations = allocationRepository.findOverlappingConfirmedAllocations(
                testEmployee.getId(), LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)
        );
        assertThat(confirmedAllocations).hasSize(2);
        int totalPercent = confirmedAllocations.stream().mapToInt(ResourceAllocation::getAllocationPercent).sum();
        assertThat(totalPercent).isEqualTo(100);
    }
}
