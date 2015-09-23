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
package ch.bfh.univote2.common.query;

import ch.bfh.uniboard.data.AlphaIdentifierDTO;
import ch.bfh.uniboard.data.BetaIdentifierDTO;
import ch.bfh.uniboard.data.ConstraintDTO;
import ch.bfh.uniboard.data.EqualDTO;
import ch.bfh.uniboard.data.IdentifierDTO;
import ch.bfh.uniboard.data.MessageIdentifierDTO;
import ch.bfh.uniboard.data.OrderDTO;
import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.unicrypt.helper.math.MathUtil;
import ch.bfh.univote2.common.UnivoteException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class QueryFactory {

	public static QueryDTO getQueryFormUniCertForEACert(String name) {
		QueryDTO query = new QueryDTO();
		//from unicert
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO("unicert"));
		query.getConstraint().add(constraint);
		//from certificates
		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2, new StringValueDTO("certificate"));
		query.getConstraint().add(constraint2);
		//Where common name is equal to ea name
		IdentifierDTO identifier3 = new MessageIdentifierDTO();
		identifier3.getPart().add("commonName");
		ConstraintDTO constraint3 = new EqualDTO(identifier3, new StringValueDTO(name));
		query.getConstraint().add(constraint3);
		//Where cert type is trustee
		IdentifierDTO identifier4 = new MessageIdentifierDTO();
		identifier4.getPart().add("roles");
		EqualDTO constraint4 = new EqualDTO(identifier4, new StringValueDTO("ElectionAdministrator"));
		query.getConstraint().add(constraint4);
		//Order by timestamp desc
		IdentifierDTO identifier5 = new BetaIdentifierDTO();
		identifier5.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier5, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryFormUniCertForTrusteeCert(String name) {
		QueryDTO query = new QueryDTO();
		//from unicert
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO("unicert"));
		query.getConstraint().add(constraint);
		//from certificates
		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2, new StringValueDTO("certificate"));
		query.getConstraint().add(constraint2);
		//Where common name is equal to trustee name
		IdentifierDTO identifier3 = new MessageIdentifierDTO();
		identifier3.getPart().add("commonName");
		ConstraintDTO constraint3 = new EqualDTO(identifier3, new StringValueDTO(name));
		query.getConstraint().add(constraint3);
		//Where cert type is trustee
		IdentifierDTO identifier4 = new MessageIdentifierDTO();
		identifier4.getPart().add("roles");
		EqualDTO constraint4 = new EqualDTO(identifier4, new StringValueDTO("Trustee"));
		query.getConstraint().add(constraint4);
		//Order by timestamp desc
		IdentifierDTO identifier5 = new BetaIdentifierDTO();
		identifier5.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier5, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryFormUniCertForVoterCert(String name) {
		QueryDTO query = new QueryDTO();
		//from unicert
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO("unicert"));
		query.getConstraint().add(constraint);
		//from certificates
		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2, new StringValueDTO("certificate"));
		query.getConstraint().add(constraint2);
		//Where common name is equal to ea name
		IdentifierDTO identifier3 = new MessageIdentifierDTO();
		identifier3.getPart().add("commonName");
		ConstraintDTO constraint3 = new EqualDTO(identifier3, new StringValueDTO(name));
		query.getConstraint().add(constraint3);
		//Where cert type is trustee
		IdentifierDTO identifier4 = new MessageIdentifierDTO();
		identifier4.getPart().add("roles");
		EqualDTO constraint4 = new EqualDTO(identifier4, new StringValueDTO("Voter"));
		query.getConstraint().add(constraint4);
		//Order by timestamp desc
		IdentifierDTO identifier5 = new BetaIdentifierDTO();
		identifier5.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier5, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForEACert(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2, new StringValueDTO(GroupEnum.ADMIN_CERT.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForTrusteeCerts(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2
				= new EqualDTO(identifier2, new StringValueDTO(GroupEnum.TRUSTEE_CERTIFICATES.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForTrustees(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2,
				new StringValueDTO(GroupEnum.TRUSTEES.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForElectionDefinition(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2,
				new StringValueDTO(GroupEnum.ELECTION_DEFINITION.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForCryptoSetting(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2,
				new StringValueDTO(GroupEnum.CRYPTO_SETTING.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForSecurityLevel(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2,
				new StringValueDTO(GroupEnum.SECURITY_LEVEL.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForEncryptionKey(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2, new StringValueDTO(GroupEnum.ENCRYPTION_KEY.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForEncryptionKeyShares(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2,
				new StringValueDTO(GroupEnum.ENCRYPTION_KEY_SHARE.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		return query;
	}

	public static QueryDTO getQueryForMixedKeys(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2, new StringValueDTO(GroupEnum.MIXED_KEYS.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForKeyMixingResults(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2
				= new EqualDTO(identifier2, new StringValueDTO(GroupEnum.KEY_MIXING_RESULT.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		return query;
	}

	public static QueryDTO getQueryForElectoralRoll(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2, new StringValueDTO(GroupEnum.ELECTORAL_ROLL.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForVotingData(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2, new StringValueDTO(GroupEnum.VOTING_DATA.getValue()));
		query.getConstraint().add(constraint2);

		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);

		return query;
	}

	public static QueryDTO getQueryForElectionDetails(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2
				= new EqualDTO(identifier2, new StringValueDTO(GroupEnum.ELECTION_DETAILS.getValue()));
		query.getConstraint().add(constraint2);

		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);

		return query;
	}

	public static QueryDTO getQueryForSignatureGenerator(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2
				= new EqualDTO(identifier2, new StringValueDTO(GroupEnum.SIGNATURE_GENERATOR.getValue()));
		query.getConstraint().add(constraint2);

		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);

		return query;
	}

	public static QueryDTO getQueryForLastKeyMixingRequest(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2,
				new StringValueDTO(GroupEnum.KEY_MIXING_REQUEST.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForKeyMixingResultForMixer(String section, PublicKey publicKey)
			throws UnivoteException {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2,
				new StringValueDTO(GroupEnum.KEY_MIXING_REQUEST.getValue()));
		query.getConstraint().add(constraint2);

		IdentifierDTO identifier3 = new AlphaIdentifierDTO();
		identifier3.getPart().add(AlphaEnum.PUBLICKEY.getValue());
		ConstraintDTO constraint3 = new EqualDTO(identifier3,
				new StringValueDTO(computePublicKeyString(publicKey)));
		query.getConstraint().add(constraint3);

		//Order by timestamp desc
		IdentifierDTO identifier4 = new BetaIdentifierDTO();
		identifier4.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier4, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	private static String computePublicKeyString(PublicKey publicKey) throws UnivoteException {
		if (publicKey instanceof DSAPublicKey) {
			DSAPublicKey dsaPubKey = (DSAPublicKey) publicKey;
			return dsaPubKey.getY().toString(10);
		} else if (publicKey instanceof RSAPublicKey) {
			RSAPublicKey rsaPubKey = (RSAPublicKey) publicKey;
			BigInteger unicertRsaPubKey = MathUtil.pair(rsaPubKey.getPublicExponent(), rsaPubKey.getModulus());

			return unicertRsaPubKey.toString(10);
		}
		throw new UnivoteException("Unssuport public key type");
	}

	public static QueryDTO getQueryForAccessRight(String section, PublicKey publicKey, GroupEnum group)
			throws UnivoteException {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier2,
				new StringValueDTO(GroupEnum.ACCESS_RIGHT.getValue()));
		query.getConstraint().add(constraint2);

		IdentifierDTO identifier3 = new MessageIdentifierDTO();
		identifier3.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint3 = new EqualDTO(identifier3,
				new StringValueDTO(group.getValue()));
		query.getConstraint().add(constraint3);

		addConstraint(query, publicKey);
		//Order by timestamp desc
		IdentifierDTO identifier4 = new BetaIdentifierDTO();
		identifier4.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier4, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForPartialDecryptionForTallier(String section, PublicKey publicKey)
			throws UnivoteException {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2
				= new EqualDTO(identifier2, new StringValueDTO(GroupEnum.PARTIAL_DECRYPTION.getValue()));
		query.getConstraint().add(constraint2);

		IdentifierDTO identifier3 = new AlphaIdentifierDTO();
		identifier3.getPart().add(AlphaEnum.PUBLICKEY.getValue());
		ConstraintDTO constraint3
				= new EqualDTO(identifier3, new StringValueDTO(computePublicKeyString(publicKey)));
		query.getConstraint().add(constraint3);
		//Order by timestamp desc
		IdentifierDTO identifier4 = new BetaIdentifierDTO();
		identifier4.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier4, false));
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForVoteMixingResultForMixer(String section, PublicKey publicKey)
			throws UnivoteException {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2
				= new EqualDTO(identifier2, new StringValueDTO(GroupEnum.VOTE_MIXING_RESULT.getValue()));
		query.getConstraint().add(constraint2);

		IdentifierDTO identifier3 = new AlphaIdentifierDTO();
		identifier3.getPart().add(AlphaEnum.PUBLICKEY.getValue());
		ConstraintDTO constraint3
				= new EqualDTO(identifier3, new StringValueDTO(computePublicKeyString(publicKey)));
		query.getConstraint().add(constraint3);
		//Order by timestamp desc
		IdentifierDTO identifier4 = new BetaIdentifierDTO();
		identifier4.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier4, false));
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForVoteMixingRequestForMixer(String section, String tenant)
			throws UnivoteException {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2
				= new EqualDTO(identifier2, new StringValueDTO(GroupEnum.VOTE_MIXING_REQUEST.getValue()));
		query.getConstraint().add(constraint2);

		IdentifierDTO identifier3 = new MessageIdentifierDTO();
		identifier3.getPart().add("mixerId");
		ConstraintDTO constraint3 = new EqualDTO(identifier3, new StringValueDTO(tenant));
		query.getConstraint().add(constraint3);
		//Order by timestamp desc
		IdentifierDTO identifier4 = new BetaIdentifierDTO();
		identifier4.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier4, false));
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForMixedVotes(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier,
				new StringValueDTO(GroupEnum.MIXED_VOTES.getValue()));
		query.getConstraint().add(constraint2);
		//Order by timestamp desc
		IdentifierDTO identifier3 = new BetaIdentifierDTO();
		identifier3.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier3, false));
		//Return only first post
		query.setLimit(1);
		return query;
	}

	private static void addConstraint(QueryDTO query, PublicKey publicKey) throws UnivoteException {
		if (publicKey instanceof RSAPublicKey) {
			RSAPublicKey rsaPubKey = (RSAPublicKey) publicKey;
			BigInteger unicertRsaPubKey = MathUtil.pair(rsaPubKey.getPublicExponent(), rsaPubKey.getModulus());
			IdentifierDTO keyIdent1 = new MessageIdentifierDTO();
			keyIdent1.getPart().add("crypto");
			keyIdent1.getPart().add("publickey");
			ConstraintDTO keyConstraint1 = new EqualDTO(keyIdent1, new StringValueDTO(unicertRsaPubKey.toString(10)));
			query.getConstraint().add(keyConstraint1);
		} else if (publicKey instanceof DSAPublicKey) {
			DSAPublicKey dsaPubKey = (DSAPublicKey) publicKey;
			IdentifierDTO keyIdent1 = new MessageIdentifierDTO();
			keyIdent1.getPart().add("crypto");
			keyIdent1.getPart().add("publickey");
			ConstraintDTO keyConstraint1 = new EqualDTO(keyIdent1, new StringValueDTO(dsaPubKey.getY().toString(10)));
			query.getConstraint().add(keyConstraint1);
			IdentifierDTO keyIdent2 = new MessageIdentifierDTO();
			keyIdent2.getPart().add("crypto");
			keyIdent2.getPart().add("p");
			ConstraintDTO keyConstraint2 = new EqualDTO(keyIdent2,
					new StringValueDTO(dsaPubKey.getParams().getP().toString(10)));
			query.getConstraint().add(keyConstraint2);
			IdentifierDTO keyIdent3 = new MessageIdentifierDTO();
			keyIdent3.getPart().add("crypto");
			keyIdent3.getPart().add("q");
			ConstraintDTO keyConstraint3 = new EqualDTO(keyIdent3,
					new StringValueDTO(dsaPubKey.getParams().getQ().toString(10)));
			query.getConstraint().add(keyConstraint3);
			IdentifierDTO keyIdent4 = new MessageIdentifierDTO();
			keyIdent4.getPart().add("crypto");
			keyIdent4.getPart().add("g");
			ConstraintDTO keyConstraint4 = new EqualDTO(keyIdent4,
					new StringValueDTO(dsaPubKey.getParams().getG().toString(10)));
			query.getConstraint().add(keyConstraint4);

		} else {
			throw new UnivoteException("Unsupported public key: " + publicKey.getClass());
		}
	}

	public static QueryDTO getQueryForEncryptionKeyShareForTallier(String section, PublicKey publicKey)
			throws UnivoteException {

		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2
				= new EqualDTO(identifier2, new StringValueDTO(GroupEnum.ENCRYPTION_KEY_SHARE.getValue()));
		query.getConstraint().add(constraint2);

		IdentifierDTO identifier3 = new AlphaIdentifierDTO();
		identifier3.getPart().add(AlphaEnum.PUBLICKEY.getValue());
		ConstraintDTO constraint3
				= new EqualDTO(identifier3, new StringValueDTO(computePublicKeyString(publicKey)));
		query.getConstraint().add(constraint3);
		//Order by timestamp desc
		IdentifierDTO identifier4 = new BetaIdentifierDTO();
		identifier4.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier4, false));
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForSingleKeyMixingResultsForMixer(String section, PublicKey publicKey)
			throws UnivoteException {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2
				= new EqualDTO(identifier2, new StringValueDTO(GroupEnum.SINGLE_KEY_MIXING_RESULT.getValue()));
		query.getConstraint().add(constraint2);

		IdentifierDTO identifier3 = new AlphaIdentifierDTO();
		identifier3.getPart().add(AlphaEnum.PUBLICKEY.getValue());
		ConstraintDTO constraint3
				= new EqualDTO(identifier3, new StringValueDTO(computePublicKeyString(publicKey)));
		query.getConstraint().add(constraint3);
		//Order by timestamp desc
		IdentifierDTO identifier4 = new BetaIdentifierDTO();
		identifier4.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier4, false));
		return query;
	}

	public static QueryDTO getQueryForKeyMixingRequestForMixer(String section, String tenant)
			throws UnivoteException {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2
				= new EqualDTO(identifier2, new StringValueDTO(GroupEnum.KEY_MIXING_REQUEST.getValue()));
		query.getConstraint().add(constraint2);

		IdentifierDTO identifier3 = new MessageIdentifierDTO();
		identifier3.getPart().add("mixerId");
		ConstraintDTO constraint3 = new EqualDTO(identifier3, new StringValueDTO(tenant));
		query.getConstraint().add(constraint3);
		//Order by timestamp desc
		IdentifierDTO identifier4 = new BetaIdentifierDTO();
		identifier4.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier4, false));
		query.setLimit(1);
		return query;
	}

	public static QueryDTO getQueryForSingleKeyMixingRequestsForMixer(String section, String tenant)
			throws UnivoteException {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2
				= new EqualDTO(identifier2, new StringValueDTO(GroupEnum.SINGLE_KEY_MIXING_REQUEST.getValue()));
		query.getConstraint().add(constraint2);

		IdentifierDTO identifier3 = new MessageIdentifierDTO();
		identifier3.getPart().add("mixerId");
		ConstraintDTO constraint3 = new EqualDTO(identifier3, new StringValueDTO(tenant));
		query.getConstraint().add(constraint3);
		//Order by timestamp desc
		IdentifierDTO identifier4 = new BetaIdentifierDTO();
		identifier4.getPart().add(BetaEnum.TIMESTAMP.getValue());
		query.getOrder().add(new OrderDTO(identifier4, false));
		return query;
	}
}
