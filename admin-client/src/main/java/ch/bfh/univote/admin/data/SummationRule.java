package ch.bfh.univote.admin.data;

import java.util.List;

public class SummationRule extends ElectionRule {

    public SummationRule() {
    }

    public SummationRule(Integer id, List<Integer> optionIds, Integer lowerBound, Integer upperBound) {
	super(id, optionIds, lowerBound, upperBound);
    }
}
