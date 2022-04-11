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
@Table(name = "carry_sessions")
public class CarrySession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "carry_pk")
	private long carryPk;

	@Column(name = "nickname", length = 20)
	private String nickname;

	@Column(name = "date_carried")
	private java.sql.Date dateCarried;

	@Column(name = "day_of_week", length = 10)
	private String dayOfWeek;
	
	public CarrySession(String nickname, java.sql.Date dateCarried, String dayOfWeek) {
		super();
		this.nickname = nickname;
		this.dateCarried = dateCarried;
		this.dayOfWeek = dayOfWeek;
	}

}
