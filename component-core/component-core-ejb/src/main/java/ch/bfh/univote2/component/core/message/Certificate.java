/*
 * UniVote2
 *
 *  UniVote2(tm): An Internet-based, verifiable e-voting system for student elections in Switzerland
 *  Copyright (c) 2015 Bern University of Applied Sciences (BFH),
 *  Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *  Quellgasse 21, CH-2501 Biel, Switzerland
 *
 *  Licensed under Dual License consisting of:
 *  1. GNU Affero General Public License (AGPL) v3
 *  and
 *  2. Commercial license
 *
 *
 *  1. This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  2. Licensees holding valid commercial licenses for UniVote2 may use this file in
 *   accordance with the commercial license agreement provided with the
 *   Software or, alternatively, in accordance with the terms contained in
 *   a written agreement between you and Bern University of Applied Sciences (BFH),
 *   Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *   Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 *   For further information contact <e-mail: univote@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
 */
package ch.bfh.univote2.component.core.message;

import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Represents an X.509 certificate.
 * <pre>
 * {
 *	"$schema": "http://json-schema.org/draft-04/schema",
 *	"title": "UniVote2: Schema of a certificate",
 *	"type":"object",
 *	"properties": {
 *		"commonName": {
 *			"type": "string",
 *			"description": "Common name of the certificate owner"
 *		},
 *		"uniqueIdentifier": {
 *			"type": "string",
 *			"description":  "Unique identifier of the certificate owner"
 *		},
 *		"organisation": {
 *			"type": "string",
 *			"description": "Organisation of the certificate owner"
 *		},
 *		"organisationUnit": {
 *			"type": "string",
 *			"description": "Organisation unit of the certificate owner"
 *		},
 *		"countryName": {
 *			"type": "string",
 *			"description": "Country of the certificate owner"
 *		},
 *		"state": {
 *			"type": "string",
 *			"description": "State of the certificate owner"
 *		},
 *		"locality": {
 *			"type": "string",
 *			"description": "Locality of the certificate owner"
 *		},
 *		"surname": {
 *			"type": "string",
 *			"description": "Surname of the certificate owner"
 *		},
 *		"givenName": {
 *			"type": "string",
 *			"description": "Given name of the certificate owner"
 *		},
 *	   	"issuer": {
 *			"type": "string",
 *			"description": "Issuer of the certificate"
 *		},
 *		"serialNumber": {
 *			"type":"string",
 *			"description": "Serial number of the certificate"
 *		},
 *		"validFrom": {
 *			"type": "string",
 *			"description": "Date when the certificate starts to be valid"
 *		},
 *		"validUntil": {
 *			"type": "string",
 *			"description": "Date when the certificate ends to be valid"
 *		},
 *		"applicationIdentifier": {
 *			"type": "string",
 *			"description": "Application the certificate has been issued for"
 *		},
 *		"roles": {
 *			"type":"array",
 *			"description": "Roles the certificate has been issued for",
 *			"items": { "type":"string" }
 *		},
 *		"identityProvider": {
 *			"type": "string",
 *			"description": "Identity provider used to verify the identity of the certificate owner"
 *		},
 *		"pem": {
 *			"type": "string",
 *			"description": "Certificate in PEM format"
 *		}
 *	},
 *	"required": ["commonName", "issuer", "serialNumber", "validFrom", "validUntil", "identityProvider", "pem"],
 *	"additionalProperties": false
 * }
 * </pre>
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
@XmlType(propOrder = {
	"commonName",
	"uniqueIdentifier",
	"organisation",
	"organisationUnit",
	"countryName",
	"state",
	"locality",
	"surname",
	"givenName",
	"issuer",
	"serialNumber",
	"validFrom",
	"validUntil",
	"applicationIdentifier",
	"roles",
	"identityProvider",
	"pem"})
public class Certificate {

	private String commonName;
	private String uniqueIdentifier;
	private String organisation;
	private String organisationUnit;
	private String countryName;
	private String state;
	private String locality;
	private String surname;
	private String givenName;
	private String issuer;
	private String serialNumber;
	@XmlJavaTypeAdapter(DateAdapter.class)
	private Date validFrom;
	@XmlJavaTypeAdapter(DateAdapter.class)
	private Date validUntil;
	private String applicationIdentifier;
	private List<String> roles;
	private String identityProvider;
	private String pem;

	public Certificate() {
	}

	public Certificate(String commonName, String uniqueIdentifier, String organisation, String countryName,
			String state, String locality, String surname, String givenName, String issuer, String serialNumber,
			Date validFrom, Date validUntil, String applicationIdentifier, List<String> roles, String identityProvider,
			String pem) {
		this.commonName = commonName;
		this.uniqueIdentifier = uniqueIdentifier;
		this.organisation = organisation;
		this.countryName = countryName;
		this.state = state;
		this.locality = locality;
		this.surname = surname;
		this.givenName = givenName;
		this.issuer = issuer;
		this.serialNumber = serialNumber;
		this.validFrom = validFrom;
		this.validUntil = validUntil;
		this.applicationIdentifier = applicationIdentifier;
		this.roles = roles;
		this.identityProvider = identityProvider;
		this.pem = pem;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

	public String getOrganisation() {
		return organisation;
	}

	public void setOrganisation(String organisation) {
		this.organisation = organisation;
	}

	public String getOrganisationUnit() {
		return organisationUnit;
	}

	public void setOrganisationUnit(String organisationUnit) {
		this.organisationUnit = organisationUnit;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	public String getApplicationIdentifier() {
		return applicationIdentifier;
	}

	public void setApplicationIdentifier(String applicationIdentifier) {
		this.applicationIdentifier = applicationIdentifier;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getIdentityProvider() {
		return identityProvider;
	}

	public void setIdentityProvider(String identityProvider) {
		this.identityProvider = identityProvider;
	}

	public String getPem() {
		return pem;
	}

	public void setPem(String pem) {
		this.pem = pem;
	}

}
