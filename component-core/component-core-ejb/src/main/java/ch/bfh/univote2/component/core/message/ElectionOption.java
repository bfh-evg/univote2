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

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <pre>
 * {
 *	"$schema": "http://json-schema.org/draft-04/schema",
 *	"title": "UniVote2: Schema of an election option",
 *	"type": "object",
 *	"properties": {
 *		"id": {
 *			"description": "Option identifier",
 *			"type": "integer"
 *		},
 *		"type": {
 *			"description": "Option type (e.g. candidate, list, answer)",
 *			"type": "string"
 *		}
 *	},
 *	"required": ["id", "type"],
 *	"additionalProperties": true
 * }
 *
 * </pre>
 * Note: Has additional components since the <code>additionalProperties</code> flag is set to true.
 * Thus, marshalling may not yield the expected result.
 *
* @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
@XmlType(propOrder={"id", "type", "other"})
public class ElectionOption {
	private Integer id;
	private String type;
	private List<Object> other;

	public ElectionOption() {
	}

	public ElectionOption(Integer id, String type, List<Object> other) {
		this.id = id;
		this.type = type;
		this.other = other;
	}

	@XmlElement(required=true)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlElement(required=true)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Object> getOther() {
		return other;
	}

	public void setOther(List<Object> other) {
		this.other = other;
	}
}
