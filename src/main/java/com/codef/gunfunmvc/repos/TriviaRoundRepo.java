package com.codef.gunfunmvc.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codef.gunfunmvc.models.entities.TriviaRound;

@Repository
public interface TriviaRoundRepo extends JpaRepository<TriviaRound, Long> {
}
