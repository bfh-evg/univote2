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
package ch.bfh.univote2.ec;

import ch.bfh.uniboard.data.AlphaIdentifierDTO;
import ch.bfh.uniboard.data.BetaIdentifierDTO;
import ch.bfh.uniboard.data.ConstraintDTO;
import ch.bfh.uniboard.data.EqualDTO;
import ch.bfh.uniboard.data.IdentifierDTO;
import ch.bfh.uniboard.data.InDTO;
import ch.bfh.uniboard.data.MessageIdentifierDTO;
import ch.bfh.uniboard.data.OrderDTO;
import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.univote2.component.core.query.AlphaEnum;
import ch.bfh.univote2.component.core.query.BetaEnum;
import ch.bfh.univote2.component.core.query.GroupEnum;

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
		InDTO constraint4 = new InDTO();
		constraint4.getElement().add(new StringValueDTO("electionAdministrator"));
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
		InDTO constraint4 = new InDTO();
		constraint4.getElement().add(new StringValueDTO("trustee"));
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
		ConstraintDTO constraint2 = new EqualDTO(identifier, new StringValueDTO(GroupEnum.ADMIN_CERT.getValue()));
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
				= new EqualDTO(identifier, new StringValueDTO(GroupEnum.TRUSTEE_CERTIFICATES.getValue()));
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
		ConstraintDTO constraint2 = new EqualDTO(identifier,
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
		ConstraintDTO constraint2 = new EqualDTO(identifier,
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

}
