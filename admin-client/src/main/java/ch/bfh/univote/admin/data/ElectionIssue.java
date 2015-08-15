package ch.bfh.univote.admin.data;

import java.util.List;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlSeeAlso({CandidateElection.class, ListElection.class, Vote.class})
@XmlType(propOrder = {"id", "title", "description", "question", "optionIds", "ruleIds"})
public abstract class ElectionIssue {

	private Integer id;
	private I18nText title;
	private I18nText description;
	private I18nText question;
	private List<Integer> optionIds;
	private List<Integer> ruleIds;

	public ElectionIssue() {
	}

	public ElectionIssue(Integer id, I18nText title, I18nText description, I18nText question, List<Integer> optionIds, List<Integer> ruleIds) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.question = question;
		this.optionIds = optionIds;
		this.ruleIds = ruleIds;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

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

	public I18nText getQuestion() {
		return question;
	}

	public void setQuestion(I18nText question) {
		this.question = question;
	}

	public List<Integer> getOptionIds() {
		return optionIds;
	}

	public void setOptionIds(List<Integer> optionIds) {
		this.optionIds = optionIds;
	}

	public List<Integer> getRuleIds() {
		return ruleIds;
	}

	public void setRuleIds(List<Integer> ruleIds) {
		this.ruleIds = ruleIds;
	}
}
