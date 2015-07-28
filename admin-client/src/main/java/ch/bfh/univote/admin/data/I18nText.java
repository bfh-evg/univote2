package ch.bfh.univote.admin.data;

import javax.xml.bind.annotation.XmlElement;

public class I18nText {

    @XmlElement(name = "default")
    private String defaultText;
    private String german;
    private String french;
    private String italien;
    private String english;

    public I18nText() {
    }

    public I18nText(String defaultText) {
	this.defaultText = defaultText;
    }

    public I18nText(String defaultText, String german, String french, String italien, String english) {
	this.defaultText = defaultText;
	this.german = german;
	this.french = french;
	this.italien = italien;
	this.english = english;
    }

    public String getDefault() {
	return defaultText;
    }

    public void setDefault(String defaultText) {
	this.defaultText = defaultText;
    }

    public String getGerman() {
	return german;
    }

    public void setGerman(String german) {
	this.german = german;
    }

    public String getFrench() {
	return french;
    }

    public void setFrench(String french) {
	this.french = french;
    }

    public String getItalien() {
	return italien;
    }

    public void setItalien(String italien) {
	this.italien = italien;
    }

    public String getEnglish() {
	return english;
    }

    public void setEnglish(String english) {
	this.english = english;
    }
}
