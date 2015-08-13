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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <pre>
 * {
 * 	"$schema": "http://json-schema.org/draft-04/schema",
 * 	"title": "UniVote2: Schema of an election definition",
 * 	"type": "object",
 * 	"properties": {
 * 		"title": {
 * 			"description": "Title of the election",
 * 			"$ref": "i18nTextSchema.json"
 * 		},
 * 		"administration": {
 * 			"description": "Name of the election administration",
 * 			"$ref": "i18nTextSchema.json"
 * 		},
 * 		"description": {
 * 			"description": "Description of the election",
 * 			"$ref": "i18nTextSchema.json"
 * 		},
 * 		"votingPeriodBegin": {
 * 			"description": "Beginning date and time of the voting period (ISO 8601 format)",
 * 			"type": "string",
 * 			"format": "date-time"
 * 		},
 * 		"votingPeriodEnd": {
 * 			"description": "End date and time of the voting period (ISO 8601 format)",
 * 			"type": "string",
 * 			"format": "date-time"
 * 		}
 * 	},
 * 	"required": ["title", "votingPeriodBegin", "votingPeriodEnd"],
 * 	"additionalProperties": false
 * }
 * </pre>
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
@XmlType(propOrder={"title", "administration" , "description", "votingPeriodBegin", "votingPeriodEnd"})
public class ElectionDefinition {
	private I18nText title;
	private I18nText administration;
	private I18nText description;
	@XmlJavaTypeAdapter(DateAdapter.class)
	private Date votingPeriodBegin;
	@XmlJavaTypeAdapter(DateAdapter.class)
	private Date votingPeriodEnd;

	public ElectionDefinition() {
	}

	public ElectionDefinition(I18nText title, I18nText administration, I18nText description,
			Date votingPeriodBegin, Date votingPeriodEnd) {
		this.title = title;
		this.administration = administration;
		this.description = description;
		this.votingPeriodBegin = votingPeriodBegin;
		this.votingPeriodEnd = votingPeriodEnd;
	}

	@XmlElement(required=true)
	public I18nText getTitle() {
		return title;
	}

	public void setTitle(I18nText title) {
		this.title = title;
	}

	public I18nText getAdministration() {
		return administration;
	}

	public void setAdministration(I18nText administration) {
		this.administration = administration;
	}

	public I18nText getDescription() {
		return description;
	}

	public void setDescription(I18nText description) {
		this.description = description;
	}

	@XmlElement(required=true)
	public Date getVotingPeriodBegin() {
		return votingPeriodBegin;
	}

	public void setVotingPeriodBegin(Date votingPeriodBegin) {
		this.votingPeriodBegin = votingPeriodBegin;
	}

	@XmlElement(required=true)
	public Date getVotingPeriodEnd() {
		return votingPeriodEnd;
	}

	public void setVotingPeriodEnd(Date votingPeriodEnd) {
		this.votingPeriodEnd = votingPeriodEnd;
	}
}
