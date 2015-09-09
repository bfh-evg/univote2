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
package ch.bfh.univote2.common.message;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <pre>
 * {
 *	"$schema": "http://json-schema.org/draft-04/schema",
 *	"title": "UniVote2: Schema of an election issue",
 *	"type": "object",
 *	"properties": {
 *		"id": {
 *			"description": "Issue identifier",
 *			"type": "integer"
 *		},
 *		"type": {
 *			"description": "Issue type",
 *			"type": "string"
 *		},
 *		"title": {
 *			"description": "Issue title",
 *			"$ref": "i18nText.jsd"
 *		},
 *		"description": {
 *			"description": "Description of the issue",
 *			"$ref": "i18nText.jsd"
 *		},
 *		"optionIds": {
 *			"description": "Identifiers of issue options",
 *			"type": "array",
 *			"items": { "type": "integer" }
 *		},
 *		"ruleIds": {
 *			"description": "Identifiers of issue rules",
 *			"type": "array",
 *			"items": { "type": "integer" }
 *		}
 *	},
 *	"required": ["id", "type", "title", "optionIds", "ruleIds"],
 *	"additionalProperties": true
 * }
 * </pre>
 * Note: Has additional components since the <code>additionalProperties</code> flag is set to true.
 * Thus, marshalling may not yield the expected result.
 *
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
@XmlType(propOrder={"id", "type", "title", "description", "optionIds", "ruleIds"})
public class ElectionIssue {
	private Integer id;
	private String type;
	private I18nText title;
	private I18nText description;
	private List<Integer> optionIds;
	private List<Integer> ruleIds;

	public ElectionIssue() {
	}

	public ElectionIssue(Integer id, String type, I18nText title, I18nText description, List<Integer> optionIds,
			List<Integer> ruleIds) {
		this.id = id;
		this.type = type;
		this.title = title;
		this.description = description;
		this.optionIds = optionIds;
		this.ruleIds = ruleIds;
	}

	@XmlElement(required = true)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlElement(required = true)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(required = true)
	public I18nText getTitle() {
		return title;
	}

	public void setTitle(I18nText title) {
		this.title = title;
	}

	public I18nText getDescription() {
		return description;
	}

	public void setDescription(I18nText description) {
		this.description = description;
	}

	@XmlElement(required = true)
	public List<Integer> getOptionIds() {
		return optionIds;
	}

	public void setOptionIds(List<Integer> optionIds) {
		this.optionIds = optionIds;
	}

	@XmlElement(required = true)
	public List<Integer> getRuleIds() {
		return ruleIds;
	}

	public void setRuleIds(List<Integer> ruleIds) {
		this.ruleIds = ruleIds;
	}
}
