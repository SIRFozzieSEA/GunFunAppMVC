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
@Table(name = "registry")
public class Registry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "gun_pk")
	private long gunPk;

	@Column(name = "serial", length = 20)
	private String serial;

	@Column(name = "nickname", length = 20)
	private String nickname;

	@Column(name = "make", length = 30)
	private String make;

	@Column(name = "model", length = 50)
	private String model;

	@Column(name = "caliber", length = 10)
	private String caliber;

	@Column(name = "barrel_length")
	private BigDecimal barrelLength;

	@Column(name = "frame_material", length = 20)
	private String frameMaterial;

	@Column(name = "sighted_date", length = 20)
	private String sightedDate;

	@Column(name = "purchase_date")
	private java.sql.Date purchaseDate;

	@Column(name = "purchase_cost")
	private BigDecimal purchaseCost;

	@Column(name = "market_cost_date")
	private java.sql.Date marketCostDate;

	@Column(name = "market_cost")
	private BigDecimal marketCost;

	@Column(name = "market_url", length = 1000)
	private String marketUrl;
	
	@Column(name = "gun_is_dirty")
	private Boolean gunIsDirty;

}
