package com.talentmarketplace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "demand_code_sequences")
public class DemandCodeSequence {

    @Id
    @Column(name = "sequence_name", length = 30)
    private String sequenceName;

    @Column(name = "current_value", nullable = false)
    private Long currentValue;

    public DemandCodeSequence() {
    }

    public DemandCodeSequence(String sequenceName, Long currentValue) {
        this.sequenceName = sequenceName;
        this.currentValue = currentValue;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public Long getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Long currentValue) {
        this.currentValue = currentValue;
    }
}
