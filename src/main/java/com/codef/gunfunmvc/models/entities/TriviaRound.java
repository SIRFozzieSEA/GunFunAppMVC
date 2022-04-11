package com.codef.gunfunmvc.models.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString(includeFieldNames = true)
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trivia_rounds")
public class TriviaRound {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "round_pk")
	private long roundPk;

	@Column(name = "user", length = 20)
	private String roundUser;

	@Column(name = "round_no_of_questions")
	private long roundNoOfQuestions;

	@Column(name = "round_no_of_questions_correct")
	private long roundNoOfQuestionsCorrect;

	@Column(name = "round_score")
	private BigDecimal roundScore;

	@Column(name = "round_played_date")
	private java.sql.Date roundPlayedDate;

}
