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

import java.nio.charset.Charset;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 *
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
public class JSONConverterTest {

	@Test
	public void testComvertJSONCertificate() throws Exception {
		String jsonCertificate
				= "{\n"
				+ "	\"commonName\": \"hans.muster@bfh.ch\",\n"
				+ "	\"uniqueIdentifier\": \"mth1\",\n"
				+ "	\"organisation\": \"BFH\",\n"
				+ "	\"organisationUnit\": \"BFH-TI\",\n"
				+ "	\"countryName\": \"Switzerland\",\n"
				+ "	\"state\": \"Bern\",\n"
				+ "	\"locality\": \"Biel\",\n"
				+ "	\"surname\": \"Muster\",\n"
				+ "	\"givenName\": \"Hans\",\n"
				+ "	\"issuer\": \"CN=UniCert BFH\",\n"
				+ "	\"serialNumber\": \"765428343514349\",\n"
				+ "	\"validFrom\": \"2015-06-22T12:35:42Z\",\n"
				+ "	\"validUntil\": \"2025-06-22T12:35:42Z\",\n"
				+ "	\"applicationIdentifier\": \"UniVote\",\n"
				+ "	\"roles\": [\"Voter\"],\n"
				+ "	\"identityProvider\": \"SwitchAAI\",\n"
				+ "	\"pem\": \"UsAMBYxFDASBWQWFgNasdVBAMMnCdfZIMB4XDTE1MDYyMjEjEAWUWQyMzU0MloXDTI1MDYyMfjEyMzUigM\"\n"
				+ "}";
		Certificate cert
				= JSONConverter.unmarshal(Certificate.class,
						jsonCertificate.getBytes(Charset.forName("UTF-8")));

		assertEquals("hans.muster@bfh.ch", cert.getCommonName());
		assertEquals("mth1", cert.getUniqueIdentifier());
		assertEquals("BFH", cert.getOrganisation());
		assertEquals("BFH-TI", cert.getOrganisationUnit());
		assertEquals("Switzerland", cert.getCountryName());
		assertEquals("Bern", cert.getState());
		assertEquals("Biel", cert.getLocality());
		assertEquals("Muster", cert.getSurname());
		assertEquals("Hans", cert.getGivenName());
		assertEquals("CN=UniCert BFH", cert.getIssuer());
		assertEquals("765428343514349", cert.getSerialNumber());
		assertEquals(new DateAdapter().unmarshal("2015-06-22T12:35:42Z"), cert.getValidFrom());
		assertEquals(new DateAdapter().unmarshal("2025-06-22T12:35:42Z"), cert.getValidUntil());
		assertEquals("UniVote", cert.getApplicationIdentifier());
		assertEquals("SwitchAAI", cert.getIdentityProvider());
		assertEquals("UsAMBYxFDASBWQWFgNasdVBAMMnCdfZIMB4XDTE1MDYyMjEjEAWUWQyMzU0MloXDTI1MDYyMfjEyMzUigM",
				cert.getPem());
	}

	@Test
	public void testConvertJSONElectionDefinition() throws Exception {
		String jsonElectionDefinition
				= "{\n"
				+ "	\"title\": {\n"
				+ "		\"default\": \"Universität Bern: Wahlen des SUB-StudentInnenrates\",\n"
				+ "		\"french\": \"Université de Berne: Élection du conseil des étudiant-e-s SUB\",\n"
				+ "		\"english\": \"University of Bern: SUB Elections\"\n"
				+ "	},\n"
				+ "	\"administration\": {\n"
				+ "		\"default\": \"StudentInnenschaft der Universität Bern (SUB)\",\n"
				+ "		\"french\": \"Ensemble des étudiants de l'Université de Berne (SUB)\",\n"
				+ "		\"english\": \"Student Body of the University of Bern (SUB)\"\n"
				+ "	},\n"
				+ "	\"votingPeriodBegin\": \"2015-03-08T23:00:00Z\",\n"
				+ "	\"votingPeriodEnd\": \"2015-03-26T11:00:00Z\"\n"
				+ "}";
		ElectionDefinition ed
				= JSONConverter.unmarshal(ElectionDefinition.class,
						jsonElectionDefinition.getBytes(Charset.forName("UTF-8")));

		// Check the content of the ElectionDefinition object.
		I18nText title = ed.getTitle();
		assertNotNull(title);
		assertEquals("Universität Bern: Wahlen des SUB-StudentInnenrates", title.getDefault());
		assertNull(title.getGerman());
		assertEquals("Université de Berne: Élection du conseil des étudiant-e-s SUB", title.getFrench());
		assertNull(title.getItalian());
		assertEquals("University of Bern: SUB Elections", title.getEnglish());

		I18nText administration = ed.getAdministration();
		assertNotNull(administration);
		assertEquals("StudentInnenschaft der Universität Bern (SUB)", administration.getDefault());
		assertNull(administration.getGerman());
		assertEquals("Ensemble des étudiants de l'Université de Berne (SUB)", administration.getFrench());
		assertNull(administration.getItalian());
		assertEquals("Student Body of the University of Bern (SUB)", administration.getEnglish());

		Date votingPeriodBegin = ed.getVotingPeriodBegin();
		assertNotNull(votingPeriodBegin);
		assertEquals(new DateAdapter().unmarshal("2015-03-08T23:00:00Z"), votingPeriodBegin);

		Date votingPeriodEnd = ed.getVotingPeriodEnd();
		assertNotNull(votingPeriodEnd);
		assertEquals(new DateAdapter().unmarshal("2015-03-26T11:00:00Z"), votingPeriodEnd);
	}

	@Test
	public void testConvertJSONElectionIssue() throws Exception {
		String input
				= "{\n"
				+ "	\"id\": \"44\",\n"
				+ "	\"type\": \"Election\",\n"
				+ "	\"title\": {\n"
				+ "		\"default\": \"Election 2015\"\n"
				+ "	},\n"
				+ "	\"description\": {\n"
				+ "		\"default\": \"Some description...\"\n"
				+ "	},\n"
				+ "	\"optionIds\": [6, 10, 56],\n"
				+ "	\"ruleIds\": [18, 5, 88]\n"
				+ "}";

		ElectionIssue dto
				= JSONConverter.unmarshal(ElectionIssue.class,
						input.getBytes(Charset.forName("UTF-8")));

		assertNotNull(dto);

		assertEquals(44, (int) dto.getId());
		assertEquals("Election", dto.getType());
		assertNotNull(dto.getTitle());
		assertEquals("Election 2015", dto.getTitle().getDefault());
		assertNotNull(dto.getDescription());
		assertEquals("Some description...", dto.getDescription().getDefault());

		assertEquals(3, dto.getOptionIds().size());
		assertEquals(6, (int) dto.getOptionIds().get(0));
		assertEquals(10, (int) dto.getOptionIds().get(1));
		assertEquals(56, (int) dto.getOptionIds().get(2));

		assertEquals(3, dto.getRuleIds().size());
		assertEquals(18, (int) dto.getRuleIds().get(0));
		assertEquals(5, (int) dto.getRuleIds().get(1));
		assertEquals(88, (int) dto.getRuleIds().get(2));
	}

	@Test
	public void testConvertJSONEncryptedBallot() throws Exception {
		String jsonBallot
				= "{\n"
				+ "  \"encryptedVote\": {\n"
				+ "    \"firstValue\": \"1234567890\",\n"
				+ "    \"secondValue\": \"9876543210\"\n"
				+ "  },\n"
				+ "  \"proof\": {\n"
				+ "    \"commitment\": \"1234567890\",\n"
				+ "    \"challenge\": \"9876543210\",\n"
				+ "    \"response\": \"1234567890\"\n"
				+ "  }\n"
				+ "}";
		Ballot eb
				= JSONConverter.unmarshal(Ballot.class,
						jsonBallot.getBytes(Charset.forName("UTF-8")));

		EncryptedVote ev = eb.getEncryptedVote();
		assertNotNull(ev);
		assertEquals("1234567890", ev.getFirstValue());
		assertEquals("9876543210", ev.getSecondValue());

		Proof p = eb.getProof();
		assertNotNull(p);
		assertEquals("1234567890", p.getCommitment());
		assertEquals("9876543210", p.getChallenge());
		assertEquals("1234567890", p.getResponse());
	}

	@Test
	public void testConvertJSONEncryptionSetting() throws Exception {
		// Values chosen from Chapt. 7, "Cryptographic Settings"
		String jsonCryptoSetting
				= "{\n"
				+ "  \"encryptionSetting\": \"RC1e\",\n"
				+ "  \"signatureSetting\": \"RC1s\",\n"
				+ "  \"hashSetting\": \"H2\"\n"
				+ "}";
		CryptoSetting cs
				= JSONConverter.unmarshal(CryptoSetting.class,
						jsonCryptoSetting.getBytes(Charset.forName("UTF-8")));

		assertEquals("RC1e", cs.getEncryptionSetting());
		assertEquals("RC1s", cs.getSignatureSetting());
		assertEquals("H2", cs.getHashSetting());
	}

	@Test
	public void testConvertJSONSecurityLevel() throws Exception {
		String jsonSecurityLevel
				= "{\n"
				+ "  \"securityLevel\": 3"
				+ "}";

		SecurityLevel sl
				= JSONConverter.unmarshal(SecurityLevel.class,
						jsonSecurityLevel.getBytes(Charset.forName("UTF-8")));

		assertEquals(Integer.valueOf("3"), sl.getSecurityLevel());
	}

	@Test
	public void testConvertJSONEncryptionKeyShare() throws Exception {
		String jsonBallot
				= "{\n"
				+ "  \"keyShare\": \"1234567890\",\n"
				+ "  \"proof\": {\n"
				+ "    \"commitment\": \"1234567890\",\n"
				+ "    \"challenge\": \"9876543210\",\n"
				+ "    \"response\": \"1234567890\"\n"
				+ "  }\n"
				+ "}";
		EncryptionKeyShare eks
				= JSONConverter.unmarshal(EncryptionKeyShare.class,
						jsonBallot.getBytes(Charset.forName("UTF-8")));

		assertEquals("1234567890", eks.getKeyShare());

		Proof p = eks.getProof();
		assertNotNull(p);
		assertEquals("1234567890", p.getCommitment());
		assertEquals("9876543210", p.getChallenge());
		assertEquals("1234567890", p.getResponse());
	}

	@Test
	public void testConvertJSONAccessRight_1() throws Exception {
		String jsonAccessRight
				= "{\n"
				+ "	\"group\": \"g1\",\n"
				+ "	\"crypto\": {\n"
				+ "		\"type\": \"RSA\",\n"
				+ "		\"publickey\": \"1234567890\"\n"
				+ "	},\n"
				+ "	\"amount\": 2,\n"
				+ "	\"startTime\": \"2015-03-08T23:00:00Z\",\n"
				+ "	\"endTime\": \"2015-03-26T11:00:00Z\"\n"
				+ "}";
		AccessRight ar
				= JSONConverter.unmarshal(AccessRight.class,
						jsonAccessRight.getBytes(Charset.forName("UTF-8")));

		assertNotNull(ar);

		assertEquals("g1", ar.getGroup());

		Crypto crypto = ar.getCrypto();
		assertNotNull(crypto);
		assertTrue(crypto instanceof RSA);
		RSA rsa = (RSA) crypto;
		assertEquals("1234567890", rsa.getPublickey());

		assertEquals(Integer.valueOf(2), ar.getAmount());

		assertEquals(new DateAdapter().unmarshal("2015-03-08T23:00:00Z"), ar.getStartTime());
		assertEquals(new DateAdapter().unmarshal("2015-03-26T11:00:00Z"), ar.getEndTime());
	}

	@Test
	public void testConvertJSONAccessRight_2() throws Exception {
		String jsonAccessRight
				= "{\n"
				+ "	\"group\": \"g1\",\n"
				+ "	\"crypto\": {\n"
				+ "		\"type\": \"DL\",\n"
				+ "		\"p\": \"161931481198\",\n"
				+ "		\"q\": \"6513368382\",\n"
				+ "		\"g\": \"109291242\",\n"
				+ "		\"publickey\": \"1234567890\"\n"
				+ "	},\n"
				+ "	\"amount\": 2,\n"
				+ "	\"startTime\": \"2015-03-08T23:00:00Z\",\n"
				+ "	\"endTime\": \"2015-03-26T11:00:00Z\"\n"
				+ "}";
		AccessRight ar
				= JSONConverter.unmarshal(AccessRight.class,
						jsonAccessRight.getBytes(Charset.forName("UTF-8")));

		assertNotNull(ar);

		assertEquals("g1", ar.getGroup());

		Crypto crypto = ar.getCrypto();
		assertNotNull(crypto);
		assertTrue(crypto instanceof DL);
		DL dl = (DL) crypto;
		assertEquals("161931481198", dl.getP());
		assertEquals("6513368382", dl.getQ());
		assertEquals("109291242", dl.getG());
		assertEquals("1234567890", dl.getPublickey());

		assertEquals(Integer.valueOf(2), ar.getAmount());

		assertEquals(new DateAdapter().unmarshal("2015-03-08T23:00:00Z"), ar.getStartTime());
		assertEquals(new DateAdapter().unmarshal("2015-03-26T11:00:00Z"), ar.getEndTime());
	}

	@Test
	public void testConvertJSONAccessRight_3() throws Exception {
		String jsonAccessRight
				= "{\n"
				+ "	\"group\": \"g1\",\n"
				+ "	\"crypto\": {\n"
				+ "		\"type\": \"ECDL\",\n"
				+ "		\"curve\": \"NIST_xyz\",\n"
				+ "		\"publickey\": \"1234567890\"\n"
				+ "	},\n"
				+ "	\"amount\": 2,\n"
				+ "	\"startTime\": \"2015-03-08T23:00:00Z\",\n"
				+ "	\"endTime\": \"2015-03-26T11:00:00Z\"\n"
				+ "}";
		AccessRight ar
				= JSONConverter.unmarshal(AccessRight.class,
						jsonAccessRight.getBytes(Charset.forName("UTF-8")));

		assertNotNull(ar);

		assertEquals("g1", ar.getGroup());

		Crypto crypto = ar.getCrypto();
		assertNotNull(crypto);
		assertTrue(crypto instanceof ECDL);
		ECDL ecdl = (ECDL) crypto;
		assertEquals("NIST_xyz", ecdl.getCurve());
		assertEquals("1234567890", ecdl.getPublickey());

		assertEquals(Integer.valueOf(2), ar.getAmount());

		assertEquals(new DateAdapter().unmarshal("2015-03-08T23:00:00Z"), ar.getStartTime());
		assertEquals(new DateAdapter().unmarshal("2015-03-26T11:00:00Z"), ar.getEndTime());
	}

	@Test
	public void testConvertJSONTrustees() throws Exception {
		String jsonTrustees
				= "{\n"
				+ "	\"mixerIds\": [\"mixerBaldr\", \"mixerUlla\", \"mixerFrigg\"],\n"
				+ "	\"tallierIds\": [\"tallierBaldr\", \"tallierUlla\", \"tallierFrigg\"]\n"
				+ "}";
		Trustees ts
				= JSONConverter.unmarshal(Trustees.class,
						jsonTrustees.getBytes(Charset.forName("UTF-8")));

		assertNotNull(ts);

		assertNotNull(ts.getMixerIds());
		assertEquals(3, ts.getMixerIds().size());
		assertEquals("mixerBaldr", ts.getMixerIds().get(0));
		assertEquals("mixerUlla", ts.getMixerIds().get(1));
		assertEquals("mixerFrigg", ts.getMixerIds().get(2));

		assertNotNull(ts.getTallierIds());
		assertEquals(3, ts.getTallierIds().size());
		assertEquals("tallierBaldr", ts.getTallierIds().get(0));
		assertEquals("tallierUlla", ts.getTallierIds().get(1));
		assertEquals("tallierFrigg", ts.getTallierIds().get(2));
	}

	@Test
	public void testConvertJSONTrusteeCertificates() throws Exception {
		String jsonTrusteeCertificates
				= "{\n"
				+ "	\"mixerCertificates\": [\n"
				+ "		{\n"
				+ "			\"commonName\": \"XXXXXXXXX\",\n"
				+ "			\"uniqueIdentifier\": \"XXXXXXXXXXX\",\n"
				+ "			\"organisation\": \"bfh.ch\",\n"
				+ "			\"issuer\": \"CN=UniCert BFH\",\n"
				+ "			\"serialNumber\": \"765428343514349\",\n"
				+ "			\"validFrom\": \"2015-06-22T12:35:42.000+0000\",\n"
				+ "			\"validUntil\": \"2025-06-22T12:35:42.000+0000\",\n"
				+ "			\"applicationIdentifier\": \"UniVote\",\n"
				+ "			\"roles\": [ \"Mixer\" ],\n"
				+ "			\"identityProvider\": \"SwitchAAI\",\n"
				+ "			\"pem\": \"UsAMBYxFDASBWQWFgNasd...fjEyMzUigM\"\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"commonName\": \"XXXXXXXXX\",\n"
				+ "			\"uniqueIdentifier\": \"XXXXXXXXXXX\",\n"
				+ "			\"organisation\": \"bfh.ch\",\n"
				+ "			\"issuer\": \"CN=UniCert BFH\",\n"
				+ "			\"serialNumber\": \"765428343514349\",\n"
				+ "			\"validFrom\": \"2015-06-22T12:35:42.000+0000\",\n"
				+ "			\"validUntil\": \"2025-06-22T12:35:42.000+0000\",\n"
				+ "			\"applicationIdentifier\": \"UniVote\",\n"
				+ "			\"roles\": [ \"Mixer\" ],\n"
				+ "			\"identityProvider\": \"SwitchAAI\",\n"
				+ "			\"pem\": \"UsAMBYxFDASBWQWFgNasd...fjEyMzUigM\"\n"
				+ "		}\n"
				+ "	],\n"
				+ "	\"tallierCertificates\": [\n"
				+ "		{\n"
				+ "			\"commonName\": \"XXXXXXXXX\",\n"
				+ "			\"uniqueIdentifier\": \"XXXXXXXXXXX\",\n"
				+ "			\"organisation\": \"bfh.ch\",\n"
				+ "			\"issuer\": \"CN=UniCert BFH\",\n"
				+ "			\"serialNumber\": \"765428343514349\",\n"
				+ "			\"validFrom\": \"2015-06-22T12:35:42.000+0000\",\n"
				+ "			\"validUntil\": \"2025-06-22T12:35:42.000+0000\",\n"
				+ "			\"applicationIdentifier\": \"UniVote\",\n"
				+ "			\"roles\": [ \"Tallier\" ],\n"
				+ "			\"identityProvider\": \"SwitchAAI\",\n"
				+ "			\"pem\": \"UsAMBYxFDASBWQWFgNasd...fjEyMzUigM\"\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"commonName\": \"XXXXXXXXX\",\n"
				+ "			\"uniqueIdentifier\": \"XXXXXXXXXXX\",\n"
				+ "			\"organisation\": \"bfh.ch\",\n"
				+ "			\"issuer\": \"CN=UniCert BFH\",\n"
				+ "			\"serialNumber\": \"765428343514349\",\n"
				+ "			\"validFrom\": \"2015-06-22T12:35:42.000+0000\",\n"
				+ "			\"validUntil\": \"2025-06-22T12:35:42.000+0000\",\n"
				+ "			\"applicationIdentifier\": \"UniVote\",\n"
				+ "			\"roles\": [ \"Talier\" ],\n"
				+ "			\"identityProvider\": \"SwitchAAI\",\n"
				+ "			\"pem\": \"UsAMBYxFDASBWQWFgNasd...fjEyMzUigM\"\n"
				+ "		}\n"
				+ "	]\n"
				+ "}";

		TrusteeCertificates certs
				= JSONConverter.unmarshal(TrusteeCertificates.class,
						jsonTrusteeCertificates.getBytes(Charset.forName("UTF-8")));

		assertNotNull(certs);

		assertNotNull(certs.getMixerCertificates());
		assertEquals(2, certs.getMixerCertificates().size());

		assertNotNull(certs.getTallierCertificates());
		assertEquals(2, certs.getTallierCertificates().size());
	}

	@Test
	public void testConvertJSONEnryptionKey() throws Exception {
		String jsonEncryptionKey
				= "{\n"
				+ "	\"encryptionKey\": \"1234567890\"\n"
				+ "}";

		EncryptionKey ek
				= JSONConverter.unmarshal(EncryptionKey.class,
						jsonEncryptionKey.getBytes(Charset.forName("UTF-8")));

		assertNotNull(ek);
		assertEquals("1234567890", ek.getEncryptionKey());
	}


	@Test
	public void testConvertJSONVoterCertificates() throws Exception {
		String jsonVoterCertificates
				= "{\n"
				+ "	\"voterCertificates\": [\n"
				+ "		{\n"
				+ "			\"commonName\": \"XXXXXXXXX\",\n"
				+ "			\"uniqueIdentifier\": \"XXXXXXXXXXX\",\n"
				+ "			\"organisation\": \"bfh.ch\",\n"
				+ "			\"issuer\": \"CN=UniCert BFH\",\n"
				+ "			\"serialNumber\": \"765428343514349\",\n"
				+ "			\"validFrom\": \"2015-06-22T12:35:42.000+0000\",\n"
				+ "			\"validUntil\": \"2025-06-22T12:35:42.000+0000\",\n"
				+ "			\"applicationIdentifier\": \"UniVote\",\n"
				+ "			\"roles\": [ \"Voter\" ],\n"
				+ "			\"identityProvider\": \"SwitchAAI\",\n"
				+ "			\"pem\": \"UsAMBYxFDASBWQWFgNasd...fjEyMzUigM\"\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"commonName\": \"XXXXXXXXX\",\n"
				+ "			\"uniqueIdentifier\": \"XXXXXXXXXXX\",\n"
				+ "			\"organisation\": \"bfh.ch\",\n"
				+ "			\"issuer\": \"CN=UniCert BFH\",\n"
				+ "			\"serialNumber\": \"765428343514349\",\n"
				+ "			\"validFrom\": \"2015-06-22T12:35:42.000+0000\",\n"
				+ "			\"validUntil\": \"2025-06-22T12:35:42.000+0000\",\n"
				+ "			\"applicationIdentifier\": \"UniVote\",\n"
				+ "			\"roles\": [ \"Voter\" ],\n"
				+ "			\"identityProvider\": \"SwitchAAI\",\n"
				+ "			\"pem\": \"UsAMBYxFDASBWQWFgNasd...fjEyMzUigM\"\n"
				+ "		}\n"
				+ "	]\n"
				+ "}";

		VoterCertificates certs
				= JSONConverter.unmarshal(VoterCertificates.class,
						jsonVoterCertificates.getBytes(Charset.forName("UTF-8")));

		assertNotNull(certs);

		assertNotNull(certs.getVoterCertificates());
		assertEquals(2, certs.getVoterCertificates().size());
	}

	@Test
	public void testConvertJSONKeyMixingRequest() throws Exception {
		String jsonKeyMixingRequest
				= "{\n"
				+ "	\"mixerId\": \"mixer1\",\n"
				+ "	\"keys\": [\"1234\", \"5678\", \"9012\"],\n"
				+ "	\"generator\": \"1234567890\"\n"
				+ "}";

		KeyMixingRequest kmr
				= JSONConverter.unmarshal(KeyMixingRequest.class,
						jsonKeyMixingRequest.getBytes(Charset.forName("UTF-8")));

		assertNotNull(kmr);

		assertEquals("mixer1", kmr.getMixerId());

		assertEquals(3, kmr.getKeys().size());
		assertEquals("1234", kmr.getKeys().get(0));
		assertEquals("5678", kmr.getKeys().get(1));
		assertEquals("9012", kmr.getKeys().get(2));

		assertEquals("1234567890", kmr.getGenerator());

		String marshalledKeyMixingRequest
				= JSONConverter.marshal(kmr);

		JSONAssert.assertEquals(jsonKeyMixingRequest, marshalledKeyMixingRequest, true);
	}

	@Test
	public void testConvertJSONKeyMixingResult() throws Exception {
		String jsonKeyMixingResult
				= "{\n"
				+ "	\"mixedKeys\": [\"1234\", \"5678\", \"9012\"],\n"
				+ "	\"generator\": \"1234567890\",\n"
				+ "	\"proof\": {\n"
				+ "		\"commitment\": \"1234567890\",\n"
				+ "		\"challenge\": \"9876543210\",\n"
				+ "		\"response\": \"1234567890\"\n"
				+ "	}\n"
				+ "}";

		KeyMixingResult mkr
				= JSONConverter.unmarshal(KeyMixingResult.class,
						jsonKeyMixingResult.getBytes(Charset.forName("UTF-8")));

		assertNotNull(mkr);

		assertNotNull(mkr.getMixedKeys());
		assertEquals(3, mkr.getMixedKeys().size());
		assertEquals("1234", mkr.getMixedKeys().get(0));
		assertEquals("5678", mkr.getMixedKeys().get(1));
		assertEquals("9012", mkr.getMixedKeys().get(2));

		assertEquals("1234567890", mkr.getGenerator());

		assertNotNull(mkr.getProof());
		assertEquals("1234567890", mkr.getProof().getCommitment());
		assertEquals("9876543210", mkr.getProof().getChallenge());
		assertEquals("1234567890", mkr.getProof().getResponse());
	}

	@Test
	public void testConvertJSONMixedKeys() throws Exception {
		String input
				= "{\n"
				+ "	\"mixedKeys\": [\"1234\", \"5678\", \"9012\"],\n"
				+ "	\"generator\": \"1234567890\"\n"
				+ "}";

		MixedKeys dto
				= JSONConverter.unmarshal(MixedKeys.class,
						input.getBytes(Charset.forName("UTF-8")));

		assertNotNull(dto);

		assertNotNull(dto.getMixedKeys());
		assertEquals(3, dto.getMixedKeys().size());
		assertEquals("1234", dto.getMixedKeys().get(0));
		assertEquals("5678", dto.getMixedKeys().get(1));
		assertEquals("9012", dto.getMixedKeys().get(2));

		assertEquals("1234567890", dto.getGenerator());

		String output
				= JSONConverter.marshal(dto);

		JSONAssert.assertEquals(input, output, true);
	}

	@Test
	public void testConvertJSONMixedVotes() throws Exception {
		String input
				= "{\n"
				+ "	\"mixedVotes\": [\n"
				+ "		{\n"
				+ "			\"firstValue\": \"1234\",\n"
				+ "			\"secondValue\": \"4321\"\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"firstValue\": \"5678\",\n"
				+ "			\"secondValue\": \"8765\"\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"firstValue\": \"9012\",\n"
				+ "			\"secondValue\": \"2109\"\n"
				+ "		}\n"
				+ "	]\n"
				+ "}";

		MixedVotes dto
				= JSONConverter.unmarshal(MixedVotes.class,
						input.getBytes(Charset.forName("UTF-8")));

		assertNotNull(dto);

		assertNotNull(dto.getMixedVotes());
		assertEquals(3, dto.getMixedVotes().size());
		assertEquals("1234", dto.getMixedVotes().get(0).getFirstValue());
		assertEquals("4321", dto.getMixedVotes().get(0).getSecondValue());
		assertEquals("5678", dto.getMixedVotes().get(1).getFirstValue());
		assertEquals("8765", dto.getMixedVotes().get(1).getSecondValue());
		assertEquals("9012", dto.getMixedVotes().get(2).getFirstValue());
		assertEquals("2109", dto.getMixedVotes().get(2).getSecondValue());

		String output
				= JSONConverter.marshal(dto);

		JSONAssert.assertEquals(input, output, true);
	}

	@Test
	public void testConvertJSONPartiallyDecryptedVotes() throws Exception {
		String input
				= "{\n"
				+ "	\"partiallyDecryptedVotes\": [\n"
				+ "		\"1234\",\n"
				+ "		\"5678\",\n"
				+ "		\"9012\"\n"
				+ "	],\n"
				+ " \"proof\": {\n"
				+ "    \"commitment\": \"1234567890\",\n"
				+ "    \"challenge\": \"9876543210\",\n"
				+ "    \"response\": \"1234567890\"\n"
				+ " }\n"
				+ "}";

		PartialDecryption dto
				= JSONConverter.unmarshal(PartialDecryption.class,
						input.getBytes(Charset.forName("UTF-8")));

		assertNotNull(dto);

		assertNotNull(dto.getPartiallyDecryptedVotes());
		assertEquals(3, dto.getPartiallyDecryptedVotes().size());
		assertEquals("1234", dto.getPartiallyDecryptedVotes().get(0));
		assertEquals("5678", dto.getPartiallyDecryptedVotes().get(1));
		assertEquals("9012", dto.getPartiallyDecryptedVotes().get(2));

		assertNotNull(dto.getProof());
		assertEquals("1234567890", dto.getProof().getCommitment());
		assertEquals("9876543210", dto.getProof().getChallenge());
		assertEquals("1234567890", dto.getProof().getResponse());

		String output
				= JSONConverter.marshal(dto);

		JSONAssert.assertEquals(input, output, true);
	}

	@Test
	public void testIsOfTypeElectionDefinition() throws Exception {
		String message
				= "{\n"
				+ "	\"title\": {\n"
				+ "		\"default\": \"Universität Bern: Wahlen des SUB-StudentInnenrates\",\n"
				+ "		\"french\": \"Université de Berne: Élection du conseil des étudiant-e-s SUB\",\n"
				+ "		\"english\": \"University of Bern: SUB Elections\"\n"
				+ "	},\n"
				+ "	\"administration\": {\n"
				+ "		\"default\": \"StudentInnenschaft der Universität Bern (SUB)\",\n"
				+ "		\"french\": \"Ensemble des étudiants de l'Université de Berne (SUB)\",\n"
				+ "		\"english\": \"Student Body of the University of Bern (SUB)\"\n"
				+ "	},\n"
				+ "	\"votingPeriodBegin\": \"2015-03-08T23:00:00Z\",\n"
				+ "	\"votingPeriodEnd\": \"2015-03-26T11:00:00Z\"\n"
				+ "}";

		JSONConverter converter = new JSONConverter();
		assertTrue(converter.isOfType(ElectionDefinition.class, message.getBytes(Charset.forName("UTF-8"))));
		assertNotNull(converter.getUnmarshalledMessage());
		assertTrue(converter.getUnmarshalledMessage() instanceof ElectionDefinition);

		assertFalse(converter.isOfType(ElectoralRoll.class, message.getBytes(Charset.forName("UTF-8"))));
		assertNull(converter.getUnmarshalledMessage());
	}

	@Test
	public void testIsOfTypeEncryptionKey() throws Exception {
		String message
				= "{\n"
				+ "	\"encryptionKey\": \"1234567890\"\n"
				+ "}";

		JSONConverter converter = new JSONConverter();
		assertTrue(converter.isOfType(EncryptionKey.class, message.getBytes(Charset.forName("UTF-8"))));

		assertFalse(converter.isOfType(ElectoralRoll.class, message.getBytes(Charset.forName("UTF-8"))));
		assertNull(converter.getUnmarshalledMessage());
}

	@Test
	public void testIsOfTypeElectionIssue1() throws Exception {
		String message
				= "{\n"
				+ "	\"id\": 44,\n"
				+ "	\"type\": \"Election\",\n"
				+ "	\"title\": {\n"
				+ "		\"default\": \"Election 2015\"\n"
				+ "	},\n"
				+ "	\"description\": {\n"
				+ "		\"default\": \"Some description...\"\n"
				+ "	},\n"
				+ "	\"optionIds\": [6, 10, 56],\n"
				+ "	\"ruleIds\": [18, 5, 88]\n"
				+ "}";

		JSONConverter converter = new JSONConverter();
		assertTrue(converter.isOfType(ElectionIssue.class, message.getBytes(Charset.forName("UTF-8"))));

		assertFalse(converter.isOfType(ElectoralRoll.class, message.getBytes(Charset.forName("UTF-8"))));
		assertNull(converter.getUnmarshalledMessage());
	}

	@Test
	public void testIsOfTypeElectionIssue2() throws Exception {
		String message
				= "{\n"
				+ "	\"id\": 44,\n"
				+ "	\"type\": \"Election\",\n"
				+ "	\"title\": {\n"
				+ "		\"default\": \"Election 2015\"\n"
				+ "	},\n"
				+ "	\"optionIds\": [6, 10, 56],\n"
				+ "	\"ruleIds\": [18, 5, 88]\n"
				+ "}";

		JSONConverter converter = new JSONConverter();
		assertTrue(converter.isOfType(ElectionIssue.class, message.getBytes(Charset.forName("UTF-8"))));

		assertFalse(converter.isOfType(ElectoralRoll.class, message.getBytes(Charset.forName("UTF-8"))));
		assertNull(converter.getUnmarshalledMessage());
	}

	@Test
	public void testIsOfTypeElectionIssue3() throws Exception {
		String message
				= "{\n"
				+ "	\"type\": \"Election\",\n"
				+ "	\"title\": {\n"
				+ "		\"default\": \"Election 2015\"\n"
				+ "	},\n"
				+ "	\"optionIds\": [6, 10, 56],\n"
				+ "	\"ruleIds\": [18, 5, 88]\n"
				+ "}";

		JSONConverter converter = new JSONConverter();
		assertFalse(converter.isOfType(ElectionIssue.class, message.getBytes(Charset.forName("UTF-8"))));
	}

	@Test
	public void testIsOfTypeElectoralRoll1() throws Exception {
		String message
				= "{\n"
				+ "	\"voterIds\": [ \"1234\", \"5678\", \"9012\" ]\n"
				+ "}";

		JSONConverter converter = new JSONConverter();
		assertTrue(converter.isOfType(ElectoralRoll.class, message.getBytes(Charset.forName("UTF-8"))));
	}

	@Test
	public void testIsOfTypeElectoralRoll2() throws Exception {
		String message
				= "{\n"
				+ "	\"voterIds\": [  ]\n"
				+ "}";

		JSONConverter converter = new JSONConverter();
		assertTrue(converter.isOfType(ElectoralRoll.class, message.getBytes(Charset.forName("UTF-8"))));
	}

	@Test
	public void testIsOfTypeElectoralRoll3() throws Exception {
		String message
				= "{\n"
				+ "}";

		JSONConverter converter = new JSONConverter();
		assertFalse(converter.isOfType(ElectoralRoll.class, message.getBytes(Charset.forName("UTF-8"))));
	}

	@Test
	public void testIsOfTypeElectoralRoll4() throws Exception {
		String message
				= "{\n"
				+ "	\"someIds\": [ \"1234\", \"5678\", \"9012\" ]\n"
				+ "}";

		JSONConverter converter = new JSONConverter();
		assertFalse(converter.isOfType(ElectoralRoll.class, message.getBytes(Charset.forName("UTF-8"))));
	}

	@Test
	public void testConvertJSONVoteMixingRequest() throws Exception {
		String input
				= "{\n"
				+ "	\"mixerId\": \"mixer1\",\n"
				+ "	\"votesToMix\": [\n"
				+ "    { \"firstValue\": \"1234\", \"secondValue\": \"5678\" },\n"
				+ "    { \"firstValue\": \"3456\", \"secondValue\": \"7890\" },\n"
				+ "    { \"firstValue\": \"5678\", \"secondValue\": \"9012\" }\n"
				+ "]\n"
				+ "}";

		VoteMixingRequest dto
				= JSONConverter.unmarshal(VoteMixingRequest.class,
						input.getBytes(Charset.forName("UTF-8")));

		assertNotNull(dto);

		assertEquals("mixer1", dto.getMixerId());

		assertEquals(3, dto.getVotesToMix().size());
		assertEquals("1234", dto.getVotesToMix().get(0).getFirstValue());
		assertEquals("5678", dto.getVotesToMix().get(0).getSecondValue());
		assertEquals("3456", dto.getVotesToMix().get(1).getFirstValue());
		assertEquals("7890", dto.getVotesToMix().get(1).getSecondValue());
		assertEquals("5678", dto.getVotesToMix().get(2).getFirstValue());
		assertEquals("9012", dto.getVotesToMix().get(2).getSecondValue());

		String output
				= JSONConverter.marshal(dto);

		JSONAssert.assertEquals(input, output, true);
	}

	@Test
	public void testConvertJSONVoteMixingResult() throws Exception {
		String input
				= "{\n"
				+ "	\"mixedVotes\": [\n"
				+ "    { \"firstValue\": \"1234\", \"secondValue\": \"5678\" },\n"
				+ "    { \"firstValue\": \"3456\", \"secondValue\": \"7890\" },\n"
				+ "    { \"firstValue\": \"5678\", \"secondValue\": \"9012\" }\n"
				+ "  ],\n"
				+ " \"proof\": {\n"
				+ "    \"commitment\": \"1234567890\",\n"
				+ "    \"challenge\": \"9876543210\",\n"
				+ "    \"response\": \"1234567890\"\n"
				+ " }\n"
				+ "}";

		VoteMixingResult dto
				= JSONConverter.unmarshal(VoteMixingResult.class,
						input.getBytes(Charset.forName("UTF-8")));

		assertNotNull(dto);

		assertEquals(3, dto.getMixedVotes().size());
		assertEquals("1234", dto.getMixedVotes().get(0).getFirstValue());
		assertEquals("5678", dto.getMixedVotes().get(0).getSecondValue());
		assertEquals("3456", dto.getMixedVotes().get(1).getFirstValue());
		assertEquals("7890", dto.getMixedVotes().get(1).getSecondValue());
		assertEquals("5678", dto.getMixedVotes().get(2).getFirstValue());
		assertEquals("9012", dto.getMixedVotes().get(2).getSecondValue());

		assertNotNull(dto.getProof());
		assertEquals("1234567890", dto.getProof().getCommitment());
		assertEquals("9876543210", dto.getProof().getChallenge());
		assertEquals("1234567890", dto.getProof().getResponse());

		String output
				= JSONConverter.marshal(dto);

		JSONAssert.assertEquals(input, output, true);
	}
}
