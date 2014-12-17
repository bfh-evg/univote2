/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniVote2.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote2.component.core.persistence;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Entity
public class TenantEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final int BASE = 10;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String name;
	@Column(length = 2000)
	private String salt;
	@Column(length = 2000)
	private String hashValue;
	@Column(length = 2000)
	private String publicKey;
	@Column(length = 2000)
	private String encPrivateKey;
	@Column(length = 2000)
	private String modulus;
	@Column(length = 2000)
	private String orderFactor;
	@Column(length = 2000)
	private String generator;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigInteger getSalt() {
		return new BigInteger(salt, BASE);
	}

	public void setSalt(BigInteger salt) {
		this.salt = salt.toString(BASE);
	}

	public BigInteger getHashValue() {
		return new BigInteger(hashValue, BASE);
	}

	public void setHashValue(BigInteger hashValue) {
		this.hashValue = hashValue.toString(BASE);
	}

	public BigInteger getPublicKey() {
		return new BigInteger(publicKey);
	}

	public void setPublicKey(BigInteger publicKey) {
		this.publicKey = publicKey.toString(BASE);
	}

	public BigInteger getEncPrivateKey() {
		return new BigInteger(encPrivateKey, BASE);
	}

	public void setEncPrivateKey(BigInteger encPrivateKey) {
		this.encPrivateKey = encPrivateKey.toString(BASE);
	}

	public BigInteger getModulus() {
		return new BigInteger(modulus, BASE);
	}

	public void setModulus(BigInteger modulus) {
		this.modulus = modulus.toString(BASE);
	}

	public BigInteger getOrderFactor() {
		return new BigInteger(orderFactor);
	}

	public void setOrderFactor(BigInteger orderFactor) {
		this.orderFactor = orderFactor.toString(BASE);
	}

	public BigInteger getGenerator() {
		return new BigInteger(generator);
	}

	public void setGenerator(BigInteger generator) {
		this.generator = generator.toString(BASE);
	}

}
