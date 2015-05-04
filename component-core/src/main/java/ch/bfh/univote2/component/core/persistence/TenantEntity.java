/*
 * UniVote2
 *
 *  UniVote2(tm): An Internet-based, verifiable e-voting system for student elections in Switzerland
 *  Copyright (c) 2015 Bern University of Applied Sciences (BFH),
 *  Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *  Quellgasse 21, CH-2501 Biel, Switzerland
 *
 *  Licensed under Dual License consisting of:
 *  1. GNU Affero General Public License (AGPL) v3
 *  and
 *  2. Commercial license
 *
 *
 *  1. This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  2. Licensees holding valid commercial licenses for UniVote2 may use this file in
 *   accordance with the commercial license agreement provided with the
 *   Software or, alternatively, in accordance with the terms contained in
 *   a written agreement between you and Bern University of Applied Sciences (BFH),
 *   Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *   Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 *   For further information contact <e-mail: univote@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
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
