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
 *	"title": "UniVote2: Schema of election details",
 *	"type": "object",
 *	"properties": {
 *		"options": {
 *			"description": "Election options",
 *			"type": "array",
 *			"items": { "$ref": "electionOption.jsd" }
 *		},
 *		"rules": {
 *			"description": "Election rules",
 *			"type": "array",
 *			"items": { "$ref": "electionRule.jsd" }
 *		},
 *		"issues": {
 *			"description": "Election issues",
 *			"type": "array",
 *			"items": { "$ref": "electionIssue.jsd" }
 *		},
 *		"ballotEncoding": {
 *			"description": "Identifier of the ballot encoding scheme",
 *			"type": "string"
 *		}
 *	},
 *	"required": ["options", "rules", "issues", "ballotEncoding"],
 *	"additionalProperties": false
 * }
 * </pre>
 * Note: Contains sub-components having set the <code>additionalProperties</code> flag to true.
 * Thus, marshalling may not yield the expected result.
 *
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
@XmlType(propOrder={"options", "rules", "issues", "ballotEncoding"})
public class ElectionDetails {
	private List<ElectionOption> options;
	private List<Rule> rules;
	private List<ElectionIssue> issues;
	private String ballotEncoding;

	public ElectionDetails() {
	}

	public ElectionDetails(List<ElectionOption> options, List<Rule> rules, List<ElectionIssue> issues,
			String ballotEncoding) {
		this.options = options;
		this.rules = rules;
		this.issues = issues;
		this.ballotEncoding = ballotEncoding;
	}

	@XmlElement(required=true)
	public List<ElectionOption> getOptions() {
		return options;
	}

	public void setOptions(List<ElectionOption> options) {
		this.options = options;
	}

	@XmlElement(required=true)
	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	@XmlElement(required=true)
	public List<ElectionIssue> getIssues() {
		return issues;
	}

	public void setIssues(List<ElectionIssue> issues) {
		this.issues = issues;
	}

	@XmlElement(required=true)
	public String getBallotEncoding() {
		return ballotEncoding;
	}

	public void setBallotEncoding(String ballotEncoding) {
		this.ballotEncoding = ballotEncoding;
	}
}
