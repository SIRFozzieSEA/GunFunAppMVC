package com.codef.gunfunmvc.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codef.gunfunmvc.models.entities.CleaningReport;

@Repository
public interface CleaningReportRepo extends JpaRepository<CleaningReport, Long> {
}
