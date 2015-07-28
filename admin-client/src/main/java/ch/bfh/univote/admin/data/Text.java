package ch.bfh.univote.admin.data;

import javax.xml.bind.annotation.XmlSeeAlso;

/* @XmlDiscriminatorNode("@classifier") */
@XmlSeeAlso({SimpleText.class, I18nText.class})
public abstract class Text {
}
