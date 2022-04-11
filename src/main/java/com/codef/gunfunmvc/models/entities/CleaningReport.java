package com.codef.gunfunmvc.models.entities;

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
@Table(name = "cleaning_reports")
public class CleaningReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_clean_pk")
	private long reportCleanPk;

	@Column(name = "nickname", length = 20)
	private String nickname;

	@Column(name = "caliber")
	private String caliber;

	@Column(name = "no_of_rounds")
	private long noOfRounds;

	@Column(name = "date_fired")
	private java.sql.Date dateCleaned;

}
