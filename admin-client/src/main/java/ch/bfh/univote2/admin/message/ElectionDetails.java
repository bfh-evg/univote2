/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniBoard.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote2.admin.message;

import java.util.List;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"options", "rules", "issues", "ballotEncoding"})
public class ElectionDetails {

	private List<ElectionOption> options;
	private List<ElectionRule> rules;
	private List<ElectionIssue> issues;
	private String ballotEncoding;

	public ElectionDetails(List<ElectionOption> options, List<ElectionRule> rules, List<ElectionIssue> issues, String ballotEncoding) {
		this.options = options;
		this.rules = rules;
		this.issues = issues;
		this.ballotEncoding = ballotEncoding;
	}

	public ElectionDetails() {
	}

	public List<ElectionOption> getOptions() {
		return options;
	}

	public void setOptions(List<ElectionOption> options) {
		this.options = options;
	}

	public List<ElectionRule> getRules() {
		return rules;
	}

	public void setRules(List<ElectionRule> rules) {
		this.rules = rules;
	}

	public List<ElectionIssue> getIssues() {
		return issues;
	}

	public void setIssues(List<ElectionIssue> issues) {
		this.issues = issues;
	}

	public String getBallotEncoding() {
		return ballotEncoding;
	}

	public void setBallotEncoding(String ballotEncoding) {
		this.ballotEncoding = ballotEncoding;
	}
}
