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
@Table(name = "preferences")
public class Preference {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "preference_pk")
	private long preferencePk;

	@Column(name = "preference_key", length = 50)
	private String preferenceKey;
	
	@Column(name = "preference_value", length = 200)
	private String preferenceValue;

	@Column(name = "preference_type", length = 20)
	private String preferenceName;

}
