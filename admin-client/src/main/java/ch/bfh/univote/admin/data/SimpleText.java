package ch.bfh.univote.admin.data;

/* @XmlDiscriminatorValue("simple") */
public class SimpleText extends Text {

    protected String value;

    public SimpleText(String value) {
	this.value = value;
    }

    public SimpleText() {
    }

    public String getValue() {
	return value;
    }

    public void setValue(String value) {
	this.value = value;
    }
}
