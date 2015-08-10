package ch.bfh.univote.admin.data;

import java.util.List;

public class ListOption extends ElectionOption {

    private Integer id;
    private String number;
    private I18nText listName;
    private I18nText partyName;
    private List<Integer> candidateIds;

    public ListOption() {
    }

    public ListOption(Integer id, String number, I18nText listName, I18nText partyName, List<Integer> candidateIds) {
	this.id = id;
	this.number = number;
	this.listName = listName;
	this.partyName = partyName;
	this.candidateIds = candidateIds;
    }

    public Integer getId() {
	return id;
    }

    public void setId(Integer id) {
	this.id = id;
    }

    public String getNumber() {
	return number;
    }

    public void setNumber(String number) {
	this.number = number;
    }

    public I18nText getListName() {
	return listName;
    }

    public void setListName(I18nText listName) {
	this.listName = listName;
    }

    public I18nText getPartyName() {
	return partyName;
    }

    public void setPartyName(I18nText partyName) {
	this.partyName = partyName;
    }

    public List<Integer> getCandidateIds() {
	return candidateIds;
    }

    public void setCandidateIds(List<Integer> candidateIds) {
	this.candidateIds = candidateIds;
    }
}
