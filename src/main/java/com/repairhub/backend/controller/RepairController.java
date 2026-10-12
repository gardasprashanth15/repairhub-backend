
package com.repairhub.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repairhub.backend.entity.Repair;
import com.repairhub.backend.service.RepairService;

@RestController
@RequestMapping("/api/repairs")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174"
})
public class RepairController {

    private final RepairService repairService;

    public RepairController(RepairService repairService) {
        this.repairService = repairService;
    }

    @GetMapping("/test")
    public String testApi() {
        return "Smart Repair Hub API is working!";
    }

    @GetMapping
    public List<Repair> getAllRepairs() {
        return repairService.getAllRepairs();
    }

    @GetMapping("/{id}")
    public Repair getRepairById(@PathVariable Long id) {
        return repairService.getRepairById(id);
    }

    @PostMapping
    public Repair createRepair(@RequestBody Repair repair) {
        return repairService.createRepair(repair);
    }

    @PutMapping("/{id}")
    public Repair updateRepair(
            @PathVariable Long id,
            @RequestBody Repair repair) {
        return repairService.updateRepair(id, repair);
    }

    @DeleteMapping("/{id}")
    public String deleteRepair(@PathVariable Long id) {
        repairService.deleteRepair(id);
        return "Repair deleted successfully";
    }
}
