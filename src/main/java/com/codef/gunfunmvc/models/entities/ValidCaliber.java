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
@Table(name = "valid_calibers")
public class ValidCaliber {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "valid_caliber_pk")
	private long validCaliberPk;

	@Column(name = "caliber", length = 25)
	private String caliber;

	@Column(name = "shoots_caliber", length = 50)
	private String shootsCaliber;

}
