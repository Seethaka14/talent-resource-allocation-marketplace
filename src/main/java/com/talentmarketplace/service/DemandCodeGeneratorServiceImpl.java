package com.talentmarketplace.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentmarketplace.entity.DemandCodeSequence;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.repository.DemandCodeSequenceRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;

@Service
public class DemandCodeGeneratorServiceImpl implements DemandCodeGeneratorService {

    private final DemandCodeSequenceRepository sequenceRepository;
    private final ProjectDemandRepository demandRepository;

    public DemandCodeGeneratorServiceImpl(DemandCodeSequenceRepository sequenceRepository,
                                         ProjectDemandRepository demandRepository) {
        this.sequenceRepository = sequenceRepository;
        this.demandRepository = demandRepository;
    }

    @Override
    @Transactional
    public synchronized String generateNextDemandCode() {
        int year = LocalDate.now().getYear();
        String sequenceName = "DEM-" + year;

        DemandCodeSequence sequence = sequenceRepository.findBySequenceNameForUpdate(sequenceName)
                .orElseGet(() -> initializeSequence(sequenceName, year));

        long nextVal = sequence.getCurrentValue();
        String candidateCode;
        do {
            nextVal++;
            candidateCode = String.format("DEM-%d-%04d", year, nextVal);
        } while (demandRepository.existsByDemandCode(candidateCode));

        sequence.setCurrentValue(nextVal);
        sequenceRepository.saveAndFlush(sequence);

        return candidateCode;
    }

    @Override
    @Transactional(readOnly = true)
    public String previewNextDemandCode() {
        int year = LocalDate.now().getYear();
        String sequenceName = "DEM-" + year;

        long currentVal = sequenceRepository.findById(sequenceName)
                .map(DemandCodeSequence::getCurrentValue)
                .orElseGet(() -> computeMaxExistingValue(year));

        long nextVal = currentVal;
        String candidateCode;
        do {
            nextVal++;
            candidateCode = String.format("DEM-%d-%04d", year, nextVal);
        } while (demandRepository.existsByDemandCode(candidateCode));

        return candidateCode;
    }

    private DemandCodeSequence initializeSequence(String sequenceName, int year) {
        long maxExisting = computeMaxExistingValue(year);
        DemandCodeSequence newSeq = new DemandCodeSequence(sequenceName, maxExisting);
        return sequenceRepository.saveAndFlush(newSeq);
    }

    private long computeMaxExistingValue(int year) {
        String prefix = "DEM-" + year + "-";
        List<ProjectDemand> existing = demandRepository.findByDemandCodeStartingWith(prefix);
        long max = 0;
        if (existing != null) {
            for (ProjectDemand d : existing) {
                String code = d.getDemandCode();
                if (code != null && code.startsWith(prefix)) {
                    String numPart = code.substring(prefix.length());
                    try {
                        long val = Long.parseLong(numPart);
                        if (val > max) {
                            max = val;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return max;
    }
}
