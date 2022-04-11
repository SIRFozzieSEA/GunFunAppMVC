package com.codef.gunfunmvc.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codef.gunfunmvc.models.entities.TriviaQuestionTemplate;

@Repository
public interface TriviaQuestionTemplateRepo extends JpaRepository<TriviaQuestionTemplate, Long> {
}
