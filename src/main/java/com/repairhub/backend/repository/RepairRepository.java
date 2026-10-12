
package com.repairhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.repairhub.backend.entity.Repair;


public interface RepairRepository extends JpaRepository<Repair, Long> {
}
