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
 *	"title": "UniVote2: Schema of a mixing request to mix encrypted votes",
 *	"type": "object",
 *	"properties": {
 *		"mixerId": { "type": "string" },
 *		"votesToMix": {
 *			"type": "array",
 *			"items": {
 *				"type": "object",
 *				"properties": {
 *					"firstValue":  { "type": "string" },
 *					"secondValue": { "type": "string" }
 *				},
 *				"required": ["firstValue", "secondValue"],
 *				"additionalProperties": false
 *			}
 *		}
 *	},
 *	"required": ["mixerId", "votesToMix"],
 *	"additionalProperties": false
 * }
 * </pre>
 *
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
@XmlType(propOrder={"mixerId", "votesToMix"})
public class VoteMixingRequest {
	private String mixerId;
	private List<EncryptedVote> votesToMix;

	public VoteMixingRequest() {
	}

	public VoteMixingRequest(String mixerId, List<EncryptedVote> votesToMix) {
		this.mixerId = mixerId;
		this.votesToMix = votesToMix;
	}

	@XmlElement(required=true)
	public String getMixerId() {
		return mixerId;
	}

	public void setMixerId(String mixerId) {
		this.mixerId = mixerId;
	}

	@XmlElement(required=true)
	public List<EncryptedVote> getVotesToMix() {
		return votesToMix;
	}

	public void setVotesToMix(List<EncryptedVote> votesToMix) {
		this.votesToMix = votesToMix;
	}
}