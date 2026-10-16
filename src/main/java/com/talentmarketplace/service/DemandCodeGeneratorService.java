package com.talentmarketplace.service;

public interface DemandCodeGeneratorService {

    /**
     * Atomically generates the next unique demand code under pessimistic locking.
     * Guarantees uniqueness even under concurrent demand creation.
     */
    String generateNextDemandCode();

    /**
     * Returns a non-blocking read-only preview of the next available demand code.
     */
    String previewNextDemandCode();
}
