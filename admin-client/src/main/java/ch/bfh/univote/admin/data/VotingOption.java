package ch.bfh.univote.admin.data;

public class VotingOption extends ElectionOption {

    private Integer id;
    private I18nText answer;

    public VotingOption() {
    }

    public VotingOption(Integer id, I18nText answer) {
	this.id = id;
	this.answer = answer;
    }

    public Integer getId() {
	return id;
    }

    public void setId(Integer id) {
	this.id = id;
    }

    public I18nText getAnswer() {
	return answer;
    }

    public void setAnswer(I18nText answer) {
	this.answer = answer;
    }
}
