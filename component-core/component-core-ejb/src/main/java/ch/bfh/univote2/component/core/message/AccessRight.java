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
package ch.bfh.univote2.component.core.message;

import java.util.Date;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <pre>
 * {
 *	"$schema": "http://json-schema.org/draft-04/schema",
 *	"title": "accessRight",
 *	"type": "object",
 *	"id": "http://uniboard.bfh.ch/accessRight",
 *	"properties": {
 *		"group": {
 *			"type": "string"
 *		},
 *		"crypto": {
 *			"type": "object",
 *			"oneOf": [
 *				{
 *					"$ref": "#/definitions/RSA"
 *				},
 *				{
 *					"$ref": "#/definitions/DL"
 *				},
 *				{
 *					"$ref": "#/definitions/ECDL"
 *				}
 *			]
 *		},
 *		"amount": {
 *			"type": "integer"
 *		},
 *		"startTime": {
 *			"format": "date-time",
 *			"type": "string"
 *		},
 *		"endTime": {
 *			"format": "date-time",
 *			"type": "string"
 *		}
 *	},
 *	"required": ["group", "crypto"],
 *	"additionalProperties": false,
 *	"definitions": {
 *		"RSA": {
 *			"properties": {
 *				"type": {
 *					"type": "string",
 *					"enum": [
 *						"RSA"
 *					]
 *				},
 *				"publickey": {
 *					"type": "string"
 *				}
 *			},
 *			"required": ["type", "publickey"],
 *			"additionalProperties": false
 *		},
 *		"DL": {
 *			"properties": {
 *				"type": {
 *					"type": "string",
 *					"enum": [
 *						"DL"
 *					]
 *				},
 *				"p": {
 *					"type": "string"
 *				},
 *				"q": {
 *					"type": "string"
 *				},
 *				"g": {
 *					"type": "string"
 *				},
 *				"publickey": {
 *					"type": "string"
 *				}
 *			},
 *			"required": ["type", "p", "q", "g", "publickey"],
 *			"additionalProperties": false
 *		},
 *		"ECDL": {
 *			"properties": {
 *				"type": {
 *					"type": "string",
 *					"enum": [
 *						"ECDL"
 *					]
 *				},
 *				"curve": {
 *					"type": "string"
 *				},
 *				"publickey": {
 *					"type": "string"
 *				}
 *			},
 *			"required": ["type", "curve", "publickey"],
 *			"additionalProperties": false
 *		}
 *	}
 * }
 * </pre>
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
@XmlType(propOrder = {"group", "crypto", "amount", "startTime", "endTime"})
public class AccessRight {

	private String group;
	private Crypto crypto;
	private Integer amount;
	@XmlJavaTypeAdapter(DateAdapter.class)
	private Date startTime;
	@XmlJavaTypeAdapter(DateAdapter.class)
	private Date endTime;

	public AccessRight() {
	}

	public AccessRight(String group, Crypto crypto, Integer amount, Date startTime, Date endTime) {
		this.group = group;
		this.crypto = crypto;
		this.amount = amount;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Crypto getCrypto() {
		return crypto;
	}

	public void setCrypto(Crypto crypto) {
		this.crypto = crypto;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
}
