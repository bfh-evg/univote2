/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.common.message;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <pre>
 * {
 *	"$schema": "http://json-schema.org/draft-04/schema",
 *	"title": "UniVote2: Schema of decrypted votes",
 *	"type": "object",
 *	"properties": {
 *		"decryptedVotes":  {
 *			"type": "array",
 *			"items": { "type": "string"}
 *		}
 *	},
 *	"required": ["decryptedVotes"],
 *	"additionalProperties": false
 * }
 * </pre>
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@XmlType(propOrder = {"decryptedVotes"})
public class DecryptedVotes {

	private List<String> decryptedVotes;

	public DecryptedVotes() {
	}

	public DecryptedVotes(List<String> decryptedVotes) {
		this.decryptedVotes = decryptedVotes;
	}

	@XmlElement(required = true)
	public List<String> getDecryptedVotes() {
		return decryptedVotes;
	}

	public void setDecryptedVotes(List<String> decryptedVotes) {
		this.decryptedVotes = decryptedVotes;
	}

}
