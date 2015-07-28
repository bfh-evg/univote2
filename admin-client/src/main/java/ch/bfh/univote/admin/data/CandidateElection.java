package ch.bfh.univote.admin.data;

import java.util.List;

public class CandidateElection extends ElectionIssue {

    public CandidateElection() {
    }

    public CandidateElection(Integer id, I18nText title, I18nText description, I18nText question, List<Integer> optionIds, List<Integer> ruleIds) {
	super(id, title, description, question, optionIds, ruleIds);
    }
}
