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
@Table(name = "cleaning_sessions")
public class CleaningSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "clean_pk")
	private long cleanPk;

	@Column(name = "nickname", length = 20)
	private String nickname;

	@Column(name = "date_cleaned")
	private java.sql.Date dateCleaned;
	
	public CleaningSession(String nickname, java.sql.Date dateCleaned) {
		super();
		this.nickname = nickname;
		this.dateCleaned = dateCleaned;
	}

}
