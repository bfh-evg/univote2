package ch.bfh.univote.admin.data;

import java.util.List;

public class CumulationRule extends ElectionRule {

    public CumulationRule() {
    }

    public CumulationRule(Integer id, List<Integer> optionIds, Integer lowerBound, Integer upperBound) {
	super(id, optionIds, lowerBound, upperBound);
    }
}
