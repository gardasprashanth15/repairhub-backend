
package com.repairhub.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.repairhub.backend.entity.Repair;
import com.repairhub.backend.repository.RepairRepository;

@Service
public class RepairService {

    private final RepairRepository repairRepository;

    public RepairService(RepairRepository repairRepository) {
        this.repairRepository = repairRepository;
    }

    public List<Repair> getAllRepairs() {
        return repairRepository.findAll();
    }

    public Repair getRepairById(Long id) {
        return repairRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Repair not found with ID: " + id));
    }

    public Repair createRepair(Repair repair) {
        return repairRepository.save(repair);
    }

    public Repair saveRepair(Repair repair) {
        return repairRepository.save(repair);
    }

    public Repair updateRepair(Long id, Repair repair) {
        Repair existingRepair = getRepairById(id);

        repair.setId(existingRepair.getId());

        return repairRepository.save(repair);
    }

    public void deleteRepair(Long id) {
        repairRepository.deleteById(id);
    }
}
