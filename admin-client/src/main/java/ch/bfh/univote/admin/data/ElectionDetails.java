package ch.bfh.univote.admin.data;

import java.util.List;

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
