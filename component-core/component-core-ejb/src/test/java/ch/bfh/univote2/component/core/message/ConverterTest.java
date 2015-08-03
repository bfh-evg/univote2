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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 *
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
public class ConverterTest {

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
				= Converter.unmarshal(ElectionDefinition.class,
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
}
