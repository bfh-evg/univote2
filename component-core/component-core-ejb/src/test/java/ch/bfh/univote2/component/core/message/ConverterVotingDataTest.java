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
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 *
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
public class ConverterVotingDataTest {

	@Test
	public void testConvertJSONVotingData() throws Exception {
		String jsonVotingData
				= "{\n" +
"	\"definition\": {\n" +
"		\"title\": {\n" +
"			\"default\": \"Wahlen des <a href=\\\"http://www.bfh.ch\\\">SUB</a>-StudentInnenrates (3. - 21. Dezember 2015)\",\n" +
"			\"french\": \"Élection du conseil des étudiant-e-s <a href=\\\"http://www.bfh.ch\\\">SUB</a> (3 - 21 décembre 2015)\",\n" +
"			\"english\": \"<a href=\\\"http://www.bfh.ch\\\">SUB</a> Elections (Decembre 3 - 21, 2015)\"\n" +
"		},\n" +
"		\"administration\": {\n" +
"			\"default\": \"StudentInnenschaft der Universität Bern (SUB)\",\n" +
"			\"french\": \"Ensemble des étudiants de l'Université de Berne (SUB)\",\n" +
"			\"english\": \"Student Body of the University of Bern (SUB)\"\n" +
"		},\n" +
"		\"votingPeriodBegin\": \"2015-03-08T23:00:00Z\",\n" +
"		\"votingPeriodEnd\": \"2015-03-26T11:00:00Z\"\n" +
"	},\n" +
"	\"details\": {\n" +
"		\"options\": [\n" +
"			{\n" +
"				\"id\": 1,\n" +
"				\"type\": \"listOption\",\n" +
"				\"number\": \"1\",\n" +
"				\"listName\": \"Tuxpartei\",\n" +
"				\"partyName\": \"tux\",\n" +
"				\"candidateIds\": [2, 2, 3, 3, 4, 4]\n" +
"			},\n" +
"			{\n" +
"				\"id\": 2,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"1.1\",\n" +
"				\"lastName\": \"Vuilleumier\",\n" +
"				\"firstName\": \"Sebastian\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1988,\n" +
"				\"studyBranch\": \"Humanmedizin\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 10,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 3,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"1.2\",\n" +
"				\"lastName\": \"Schmid\",\n" +
"				\"firstName\": \"Luca\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1993,\n" +
"				\"studyBranch\": \"Humanmedizin\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 4,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"1.3\",\n" +
"				\"lastName\": \"Stolz\",\n" +
"				\"firstName\": \"Marcel\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1989,\n" +
"				\"studyBranch\": \"Informatik\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 3,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 5,\n" +
"				\"type\": \"listOption\",\n" +
"				\"number\": \"2\",\n" +
"				\"listName\": \"Junge Grüne Uni Bern\",\n" +
"				\"partyName\": \"jg\",\n" +
"				\"candidateIds\": [6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14, 15, 15, 16, 17, 18, 19, 20, 21, 22]\n" +
"			},\n" +
"			{\n" +
"				\"id\": 6,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.1\",\n" +
"				\"lastName\": \"Oppold\",\n" +
"				\"firstName\": \"Malvin\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Politikwissenschaft\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 5,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 7,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.2\",\n" +
"				\"lastName\": \"Zingg\",\n" +
"				\"firstName\": \"Flavia\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Medizin\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 3,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 8,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.3\",\n" +
"				\"lastName\": \"Stalder\",\n" +
"				\"firstName\": \"Lorenz\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Rechtswissenschaft\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 1,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 9,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.4\",\n" +
"				\"lastName\": \"Schär\",\n" +
"				\"firstName\": \"Rahel\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Rechtswissenschaft\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 5,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 10,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.5\",\n" +
"				\"lastName\": \"Tempelmann\",\n" +
"				\"firstName\": \"Jochen\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Geographie\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 11,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.6\",\n" +
"				\"lastName\": \"Sollberger\",\n" +
"				\"firstName\": \"Sophia\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Biologie\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 12,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.7\",\n" +
"				\"lastName\": \"Seiler\",\n" +
"				\"firstName\": \"Christoph\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Rechtswissenschaft\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 13,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.8\",\n" +
"				\"lastName\": \"Kaita\",\n" +
"				\"firstName\": \"Anna\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Wirtschaftswissenschaften\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 14,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.9\",\n" +
"				\"lastName\": \"Sollberger\",\n" +
"				\"firstName\": \"Leonie\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Rechtswissenschaft\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 15,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.1\",\n" +
"				\"lastName\": \"Seiler\",\n" +
"				\"firstName\": \"Catarina\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Psychologie\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 16,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.11\",\n" +
"				\"lastName\": \"König\",\n" +
"				\"firstName\": \"Emilie\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Politikwissenschaft\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 5,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 17,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.12\",\n" +
"				\"lastName\": \"Schlunegger\",\n" +
"				\"firstName\": \"Lea\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Rechtswissenschaft\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 18,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.13\",\n" +
"				\"lastName\": \"Steiner\",\n" +
"				\"firstName\": \"Samuel\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Soziologie\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 19,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.14\",\n" +
"				\"lastName\": \"Nägler\",\n" +
"				\"firstName\": \"Leonie\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Sozialwissenschaften\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 20,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.15\",\n" +
"				\"lastName\": \"Häni\",\n" +
"				\"firstName\": \"Eugénie\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Politikwissenschaft\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 21,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.16\",\n" +
"				\"lastName\": \"Farner\",\n" +
"				\"firstName\": \"Rebecca\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Religionswissenschaften\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 22,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"2.17\",\n" +
"				\"lastName\": \"Widmer\",\n" +
"				\"firstName\": \"Simone\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 10,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 23,\n" +
"				\"type\": \"listOption\",\n" +
"				\"number\": \"3\",\n" +
"				\"listName\": \"WIR \\u2013 Wirtschaftswissenschaften im Rat\",\n" +
"				\"partyName\": \"wir\",\n" +
"				\"candidateIds\": [24, 24, 25, 25, 26, 26, 27, 27, 28, 28, 29, 29, 30, 30, 31, 31, 32, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53]\n" +
"			},\n" +
"			{\n" +
"				\"id\": 24,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.1\",\n" +
"				\"lastName\": \"Eichenberger\",\n" +
"				\"firstName\": \"Milena\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 25,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.2\",\n" +
"				\"lastName\": \"Collalti\",\n" +
"				\"firstName\": \"Dino\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1993,\n" +
"				\"studyBranch\": \"VWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 5,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 26,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.3\",\n" +
"				\"lastName\": \"Schlittler\",\n" +
"				\"firstName\": \"Katharina\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 27,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.4\",\n" +
"				\"lastName\": \"Affolter\",\n" +
"				\"firstName\": \"Lorenz\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1994,\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 28,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.5\",\n" +
"				\"lastName\": \"Huber\",\n" +
"				\"firstName\": \"Viktoria\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 29,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.6\",\n" +
"				\"lastName\": \"Wirth\",\n" +
"				\"firstName\": \"Lea\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"International and Monetary Economics\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 30,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.7\",\n" +
"				\"lastName\": \"Knecht\",\n" +
"				\"firstName\": \"Thomas\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Business Administration Management\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 12,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 31,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.8\",\n" +
"				\"lastName\": \"Di Raimondo\",\n" +
"				\"firstName\": \"Michele\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"VWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 32,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.9\",\n" +
"				\"lastName\": \"Theurillat\",\n" +
"				\"firstName\": \"Matthias\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 33,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.1\",\n" +
"				\"lastName\": \"Pichler\",\n" +
"				\"firstName\": \"Jan\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 34,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.11\",\n" +
"				\"lastName\": \"Schächteler\",\n" +
"				\"firstName\": \"Julia\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 35,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.12\",\n" +
"				\"lastName\": \"Tochtermann\",\n" +
"				\"firstName\": \"Lucas\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"VWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 36,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.13\",\n" +
"				\"lastName\": \"Hintermeister\",\n" +
"				\"firstName\": \"Fabian\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 37,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.14\",\n" +
"				\"lastName\": \"Arro\",\n" +
"				\"firstName\": \"Milen\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"VWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 38,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.15\",\n" +
"				\"lastName\": \"Heuberger\",\n" +
"				\"firstName\": \"Thomas\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 5,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 39,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.16\",\n" +
"				\"lastName\": \"Schär\",\n" +
"				\"firstName\": \"Stefan\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 40,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.17\",\n" +
"				\"lastName\": \"Kunz\",\n" +
"				\"firstName\": \"Pascal\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 41,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.18\",\n" +
"				\"lastName\": \"Panchal\",\n" +
"				\"firstName\": \"Purav\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 42,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.19\",\n" +
"				\"lastName\": \"Fankhauser\",\n" +
"				\"firstName\": \"Yvonne\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 43,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.2\",\n" +
"				\"lastName\": \"Laubscher\",\n" +
"				\"firstName\": \"Stephanie\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"VWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 44,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.21\",\n" +
"				\"lastName\": \"Streun\",\n" +
"				\"firstName\": \"Rico\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 45,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.22\",\n" +
"				\"lastName\": \"Lendenmann\",\n" +
"				\"firstName\": \"Joel\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 46,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.23\",\n" +
"				\"lastName\": \"Diep\",\n" +
"				\"firstName\": \"Minh Khoa Julien\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 47,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.24\",\n" +
"				\"lastName\": \"Zhung\",\n" +
"				\"firstName\": \"YuanYuan\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 48,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.25\",\n" +
"				\"lastName\": \"Ducrey\",\n" +
"				\"firstName\": \"Teresa\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 49,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.26\",\n" +
"				\"lastName\": \"Wegmüller\",\n" +
"				\"firstName\": \"Nadja\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 50,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.27\",\n" +
"				\"lastName\": \"Denby\",\n" +
"				\"firstName\": \"Natasha Allison\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 51,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.28\",\n" +
"				\"lastName\": \"Schmidt\",\n" +
"				\"firstName\": \"Tim\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 52,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.29\",\n" +
"				\"lastName\": \"Jingle\",\n" +
"				\"firstName\": \"Li\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 53,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"3.3\",\n" +
"				\"lastName\": \"Moser\",\n" +
"				\"firstName\": \"Barbara\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 54,\n" +
"				\"type\": \"listOption\",\n" +
"				\"number\": \"4\",\n" +
"				\"listName\": \"Jungfreisinnige Uni Bern\",\n" +
"				\"partyName\": \"JF\",\n" +
"				\"candidateIds\": [55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 57, 58, 59, 55]\n" +
"			},\n" +
"			{\n" +
"				\"id\": 55,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.1\",\n" +
"				\"lastName\": \"Kilchenmann\",\n" +
"				\"firstName\": \"Paloma\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1990,\n" +
"				\"studyBranch\": \"Rechtswissenschaften\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 56,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.2\",\n" +
"				\"lastName\": \"Schmid\",\n" +
"				\"firstName\": \"Thomas\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1989,\n" +
"				\"studyBranch\": \"Rechtswissenschaften\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 57,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.3\",\n" +
"				\"lastName\": \"Kellenberger\",\n" +
"				\"firstName\": \"Pascal\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1989,\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 12,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 58,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.4\",\n" +
"				\"lastName\": \"Geu\",\n" +
"				\"firstName\": \"Rémy\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1992,\n" +
"				\"studyBranch\": \"Rechtswissenschaften\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 59,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.5\",\n" +
"				\"lastName\": \"Schuler\",\n" +
"				\"firstName\": \"Marcel\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1988,\n" +
"				\"studyBranch\": \"SoWi\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 60,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.6\",\n" +
"				\"lastName\": \"Pfister\",\n" +
"				\"firstName\": \"Simon Tim\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1991,\n" +
"				\"studyBranch\": \"VWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 61,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.7\",\n" +
"				\"lastName\": \"Schoy\",\n" +
"				\"firstName\": \"Michael\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1989,\n" +
"				\"studyBranch\": \"Rechtswissenschaften\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 10,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 62,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.8\",\n" +
"				\"lastName\": \"Ziltener\",\n" +
"				\"firstName\": \"Daniel\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1990,\n" +
"				\"studyBranch\": \"Informatik\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 63,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.9\",\n" +
"				\"lastName\": \"Bachmann\",\n" +
"				\"firstName\": \"Gregor\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1991,\n" +
"				\"studyBranch\": \"Rechtswissenschaften\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 10,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 64,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.1\",\n" +
"				\"lastName\": \"Buff\",\n" +
"				\"firstName\": \"Tobias\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1994,\n" +
"				\"studyBranch\": \"Rechtswissenschaften\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 65,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.11\",\n" +
"				\"lastName\": \"Thomann\",\n" +
"				\"firstName\": \"Viviane\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1989,\n" +
"				\"studyBranch\": \"Rechtswissenschaften\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 66,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.12\",\n" +
"				\"lastName\": \"Studer\",\n" +
"				\"firstName\": \"Fabio\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1989,\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 11,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 67,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.13\",\n" +
"				\"lastName\": \"Kägi\",\n" +
"				\"firstName\": \"Adrian\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1990,\n" +
"				\"studyBranch\": \"Geschichte, Germanistik, Politik\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 68,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.14\",\n" +
"				\"lastName\": \"Hahn\",\n" +
"				\"firstName\": \"Michael Fabian\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1990,\n" +
"				\"studyBranch\": \"Rechtswissenschaften\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 69,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.15\",\n" +
"				\"lastName\": \"Arnaud\",\n" +
"				\"firstName\": \"Robert\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Rechtswissenschaften\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 10,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 70,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.16\",\n" +
"				\"lastName\": \"Serret\",\n" +
"				\"firstName\": \"Attila\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1992,\n" +
"				\"studyBranch\": \"Osteuropastudien\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 71,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.17\",\n" +
"				\"lastName\": \"Walter\",\n" +
"				\"firstName\": \"Aliosha\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Rechtswissenschaften\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 72,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.18\",\n" +
"				\"lastName\": \"Beck\",\n" +
"				\"firstName\": \"Ramona\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1994,\n" +
"				\"studyBranch\": \"Mathematik\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 73,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.19\",\n" +
"				\"lastName\": \"Bigovic\",\n" +
"				\"firstName\": \"Danilo\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1991,\n" +
"				\"studyBranch\": \"BWL/VWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 74,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"4.2\",\n" +
"				\"lastName\": \"Wild\",\n" +
"				\"firstName\": \"Nicolas\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"BWL/VWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 75,\n" +
"				\"type\": \"listOption\",\n" +
"				\"number\": \"5\",\n" +
"				\"listName\": \"Grünliberale Uni Bern\",\n" +
"				\"partyName\": \"glp\",\n" +
"				\"candidateIds\": [76, 76, 77, 77, 78, 78, 79, 79, 80, 80, 81, 81, 82, 82, 83, 83, 84, 84, 85, 85, 86, 86, 87, 87, 88, 88, 89, 89, 90, 90, 91, 91, 92, 92, 93, 94, 95, 96, 97, 98]\n" +
"			},\n" +
"			{\n" +
"				\"id\": 76,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.1\",\n" +
"				\"lastName\": \"Lindgren\",\n" +
"				\"firstName\": \"Maurice\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1987,\n" +
"				\"studyBranch\": \"VWL\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 12,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 77,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.2\",\n" +
"				\"lastName\": \"Gmuender\",\n" +
"				\"firstName\": \"Elvira\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1990,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 10,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 78,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.3\",\n" +
"				\"lastName\": \"Broennimann\",\n" +
"				\"firstName\": \"Lucas\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1993,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 79,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.4\",\n" +
"				\"lastName\": \"Haldemann\",\n" +
"				\"firstName\": \"Fabiana\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1990,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 80,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.5\",\n" +
"				\"lastName\": \"Winkelmann\",\n" +
"				\"firstName\": \"Nicolas\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1991,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 81,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.6\",\n" +
"				\"lastName\": \"Meier\",\n" +
"				\"firstName\": \"Laura\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1991,\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 82,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.7\",\n" +
"				\"lastName\": \"Mathis\",\n" +
"				\"firstName\": \"Stefania\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1993,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 83,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.8\",\n" +
"				\"lastName\": \"Abaecherli\",\n" +
"				\"firstName\": \"Aniko Beatrice\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1992,\n" +
"				\"studyBranch\": \"Sozialwissenschawten\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 10,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 84,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.9\",\n" +
"				\"lastName\": \"Vogler\",\n" +
"				\"firstName\": \"Manuel\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1989,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 10,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 85,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.1\",\n" +
"				\"lastName\": \"Frey\",\n" +
"				\"firstName\": \"Gabrielle\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1990,\n" +
"				\"studyBranch\": \"Biologie\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 86,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.11\",\n" +
"				\"lastName\": \"Muradyan\",\n" +
"				\"firstName\": \"Tigran\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1984,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 87,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.12\",\n" +
"				\"lastName\": \"Vogt\",\n" +
"				\"firstName\": \"Livia\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1992,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 88,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.13\",\n" +
"				\"lastName\": \"Siegrist\",\n" +
"				\"firstName\": \"Simon Stefan\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1991,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 89,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.14\",\n" +
"				\"lastName\": \"Hartmann\",\n" +
"				\"firstName\": \"Loredana\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1992,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 90,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.15\",\n" +
"				\"lastName\": \"Solothurnmann\",\n" +
"				\"firstName\": \"Lorenz\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1986,\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 10,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 91,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.16\",\n" +
"				\"lastName\": \"Stauffer\",\n" +
"				\"firstName\": \"Layla\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1991,\n" +
"				\"studyBranch\": \"VWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 92,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.17\",\n" +
"				\"lastName\": \"Kolly\",\n" +
"				\"firstName\": \"Raphael\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1991,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 93,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.18\",\n" +
"				\"lastName\": \"Koch\",\n" +
"				\"firstName\": \"Markus\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1989,\n" +
"				\"studyBranch\": \"VWL\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 94,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.19\",\n" +
"				\"lastName\": \"Aebischer\",\n" +
"				\"firstName\": \"Patricia\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1991,\n" +
"				\"studyBranch\": \"BWL\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 95,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.2\",\n" +
"				\"lastName\": \"Sommer\",\n" +
"				\"firstName\": \"Yves\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1990,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 10,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 96,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.21\",\n" +
"				\"lastName\": \"Bauer\",\n" +
"				\"firstName\": \"Joel\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1990,\n" +
"				\"studyBranch\": \"Zahnmedizin\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 10,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 97,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.22\",\n" +
"				\"lastName\": \"Turgul\",\n" +
"				\"firstName\": \"Timur Umut\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1992,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 98,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"5.23\",\n" +
"				\"lastName\": \"Zaugg\",\n" +
"				\"firstName\": \"Luca\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1989,\n" +
"				\"studyBranch\": \"Rechtswissenschawten\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 99,\n" +
"				\"type\": \"listOption\",\n" +
"				\"number\": \"6\",\n" +
"				\"listName\": \"Sozialdemokratisches Forum\",\n" +
"				\"partyName\": \"sf\",\n" +
"				\"candidateIds\": [100, 100, 101, 101, 102, 102, 103, 103, 104, 104, 105, 105, 106, 106, 107, 107, 108, 108, 109, 109, 110, 110, 111, 111, 112, 112, 113, 113, 114, 115, 116, 117, 118, 119]\n" +
"			},\n" +
"			{\n" +
"				\"id\": 100,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.1\",\n" +
"				\"lastName\": \"Trafelet\",\n" +
"				\"firstName\": \"Salome\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1992,\n" +
"				\"studyBranch\": \"SOWI\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 101,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.2\",\n" +
"				\"lastName\": \"Liebi\",\n" +
"				\"firstName\": \"Corina\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 102,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.3\",\n" +
"				\"lastName\": \"Strobel\",\n" +
"				\"firstName\": \"Julia\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Kunstgeschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 10,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 103,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.4\",\n" +
"				\"lastName\": \"Fux\",\n" +
"				\"firstName\": \"Deny\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 104,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.5\",\n" +
"				\"lastName\": \"Kräuchi\",\n" +
"				\"firstName\": \"Anna-Daria\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 105,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.6\",\n" +
"				\"lastName\": \"Bieri\",\n" +
"				\"firstName\": \"Julian\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 106,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.7\",\n" +
"				\"lastName\": \"Leimann\",\n" +
"				\"firstName\": \"Aline\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Jus\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 107,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.8\",\n" +
"				\"lastName\": \"Landolt\",\n" +
"				\"firstName\": \"Christoph\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 108,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.9\",\n" +
"				\"lastName\": \"Beeler\",\n" +
"				\"firstName\": \"Kathrin\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 109,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.1\",\n" +
"				\"lastName\": \"Trottmann\",\n" +
"				\"firstName\": \"Jeremy Simeon\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 110,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.11\",\n" +
"				\"lastName\": \"Langenegger\",\n" +
"				\"firstName\": \"Ilaria\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Jus\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 6,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 111,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.12\",\n" +
"				\"lastName\": \"Karst\",\n" +
"				\"firstName\": \"Kevin\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Englisch\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 12,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 112,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.13\",\n" +
"				\"lastName\": \"Hofacher\",\n" +
"				\"firstName\": \"Samuel\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 20,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 113,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.14\",\n" +
"				\"lastName\": \"Brügger\",\n" +
"				\"firstName\": \"Silvan\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"MA\",\n" +
"				\"studySemester\": 12,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 114,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.15\",\n" +
"				\"lastName\": \"Hidalgo Staub\",\n" +
"				\"firstName\": \"Samuel\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 115,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.16\",\n" +
"				\"lastName\": \"Funiciello\",\n" +
"				\"firstName\": \"Tamara\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 116,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.17\",\n" +
"				\"lastName\": \"Rauter\",\n" +
"				\"firstName\": \"Ricarda\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Psychologie\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 117,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.18\",\n" +
"				\"lastName\": \"Zucha\",\n" +
"				\"firstName\": \"Jan Wolfgang\",\n" +
"				\"sex\": \"M\",\n" +
"				\"studyBranch\": \"Erdwissenschaften\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 118,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.19\",\n" +
"				\"lastName\": \"Steiner\",\n" +
"				\"firstName\": \"Michèle\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 119,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"6.2\",\n" +
"				\"lastName\": \"Staub\",\n" +
"				\"firstName\": \"Lisa-Maria\",\n" +
"				\"sex\": \"F\",\n" +
"				\"studyBranch\": \"Geschichte\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 120,\n" +
"				\"type\": \"listOption\",\n" +
"				\"number\": \"7\",\n" +
"				\"listName\": \"Wolke 7\",\n" +
"				\"partyName\": \"w7\",\n" +
"				\"candidateIds\": [121, 121, 122, 122, 123, 123, 124, 124, 125]\n" +
"			},\n" +
"			{\n" +
"				\"id\": 121,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"7.1\",\n" +
"				\"lastName\": \"Reich\",\n" +
"				\"firstName\": \"Samuel\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1991,\n" +
"				\"studyBranch\": \"Geographie\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 8,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 122,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"7.2\",\n" +
"				\"lastName\": \"Hurni\",\n" +
"				\"firstName\": \"Julia Sophia\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1993,\n" +
"				\"studyBranch\": \"Psychologie\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"OLD\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 123,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"7.3\",\n" +
"				\"lastName\": \"Hadorn\",\n" +
"				\"firstName\": \"Jonathan\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1994,\n" +
"				\"studyBranch\": \"Jus\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 124,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"7.4\",\n" +
"				\"lastName\": \"Yared\",\n" +
"				\"firstName\": \"Jonathan\",\n" +
"				\"sex\": \"M\",\n" +
"				\"yearOfBirth\": 1992,\n" +
"				\"studyBranch\": \"Jus\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 4,\n" +
"				\"status\": \"NEW\"\n" +
"			},\n" +
"			{\n" +
"				\"id\": 125,\n" +
"				\"type\": \"candidateOption\",\n" +
"				\"number\": \"7.5\",\n" +
"				\"lastName\": \"Graf\",\n" +
"				\"firstName\": \"Yolanda\",\n" +
"				\"sex\": \"F\",\n" +
"				\"yearOfBirth\": 1994,\n" +
"				\"studyBranch\": \"Psychologie\",\n" +
"				\"studyDegree\": \"BA\",\n" +
"				\"studySemester\": 2,\n" +
"				\"status\": \"NEW\"\n" +
"			}\n" +
"		],\n" +
"		\"rules\": [\n" +
"			{\n" +
"				\"id\": 1,\n" +
"				\"type\": \"summationRule\",\n" +
"				\"optionIds\": [1, 5, 23, 54, 75, 99, 120],\n" +
"				\"lowerBound\": 0,\n" +
"				\"upperBound\": 1\n" +
"			},\n" +
"			{\n" +
"				\"id\": 2,\n" +
"				\"type\": \"cumulationRule\",\n" +
"				\"optionIds\": [1, 5, 23, 54, 75, 99, 120],\n" +
"				\"lowerBound\": 0,\n" +
"				\"upperBound\": 1\n" +
"			},\n" +
"			{\n" +
"				\"id\": 3,\n" +
"				\"type\": \"summationRule\",\n" +
"				\"optionIds\": [2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 121, 122, 123, 124, 125],\n" +
"				\"lowerBound\": 0,\n" +
"				\"upperBound\": 40\n" +
"			},\n" +
"			{\n" +
"				\"id\": 4,\n" +
"				\"type\": \"cumulationRule\",\n" +
"				\"optionIds\": [2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 121, 122, 123, 124, 125],\n" +
"				\"lowerBound\": 0,\n" +
"				\"upperBound\": 3\n" +
"			}\n" +
"		],\n" +
"		\"issues\": [\n" +
"			{\n" +
"				\"id\": 1,\n" +
"				\"type\": \"listElection\",\n" +
"				\"title\": {\n" +
"					\"default\": \"Wahlen des SUB-StudentInnenrates 2015\",\n" +
"					\"french\": \"Élection du conseil des étudiant-e-s SUB 2015\",\n" +
"					\"english\": \"SUB Elections 2015\"\n" +
"				},\n" +
"				\"description\": {\n" +
"					\"default\": \"Der Wahlzettel kann einer Liste zugeordnet werden und maximal 40 Kandidierende aus verschiedenen Listen enthalten. Einzelne Kandidierende können bis zu dreimal aufgeführt werden. Enthält ein Wahlzettel weniger als 40 Kandidierende, so zählen die fehlenden Einträge als Zusatzstimmen für die ausgewählte Liste. Wenn keine Liste angegeben ist, verfallen diese Stimmen.\",\n" +
"					\"french\": \"Le bulletin de vote peut comporter au maximum 40 candidats. Il peut comporter des candidats de listes différentes. Chaque électeur peut cumuler jusqu’à trois suffrages sur un candidat. Le bulletin de vote peut-être attribué à une liste. Si un bulletin de vote contient un nombre de candidats inférieurs à 40, les lignes laissées en blanc sont considérées comme autant de suffrages complémentaires attribués à la liste choisie. Si aucune liste n’est indiquée, ces suffrages  sont considérés comme périmés.\",\n" +
"					\"english\": \"Your ballot paper can include up to 40 candidates from all lists. It can include candidates from different lists. You may vote for each candidate up to three times. You can assign a list to your ballot. If you vote for less than 40 candidates, the missing entries will count as list votes for the chosen list. If you do not assign a list, they will expire and count as empty votes.\"\n" +
"				},\n" +
"				\"optionIds\": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125],\n" +
"				\"ruleIds\": [1, 2, 3, 4]\n" +
"			}\n" +
"		],\n" +
"		\"ballotEncoding\": \"E1\"\n" +
"	},\n" +
"	\"cryptoSetting\": {\n" +
"		\"encryptionSetting\": \"RC2\",\n" +
"		\"signatureSetting\": \"RC2s\",\n" +
"		\"hashSetting\": \"H2\"\n" +
"	},\n" +
"	\"encryptionKey\": \"91119287387482810725299284597751730297358595357967339129245557319362300326317603349124577759531942586879523260357064133075657405043421423278179218133399049314945190759226487413938851037528971806505834896476263628153957882527151512135458357322989509505305049487723315898812780201104384863495612707468258957878\",\n" +
"	\"signatureGenerator\": \"107109962631870964694631290572616741684259433534913193717696669627034744183712064532843948178840692685135901742106546031184882792684386296417476646866306748317314750581351545212887046296410227653636832554555991359342552427316273176036531855263497569544312481810013296540896767718156533429912241745106756662354\"\n" +
"}	\n" +
"";

		VotingData vd
				= Converter.unmarshal(VotingData.class,
						jsonVotingData.getBytes(Charset.forName("UTF-8")));

		assertNotNull(vd.getDefinition());

		assertNotNull(vd.getDetails());
		ElectionDetails electionDetails = vd.getDetails();

		assertNotNull(electionDetails.getOptions());
		List<ElectionOption> electionOptions = electionDetails.getOptions();
		assertEquals(125, electionOptions.size());
		ElectionOption electionOption0 = electionOptions.get(0);
		assertNotNull(electionOption0.getOthers());

		assertNotNull(electionDetails.getRules());
		List<ElectionRule> electionRules = electionDetails.getRules();
		assertEquals(4, electionRules.size());
		ElectionRule electionRule0 = electionRules.get(0);
		assertNotNull(electionRule0.getOthers());

		assertNotNull(electionDetails.getIssues());
		List<ElectionIssue> electionIssues = electionDetails.getIssues();
		assertEquals(1, electionIssues.size());
		ElectionIssue electionIssue0 = electionIssues.get(0);
		// Note: There are no additional properties in the sample JSON string above.
		assertNull(electionIssue0.getOthers());

		assertNotNull(electionDetails.getBallotEncoding());

		assertNotNull(vd.getCryptoSetting());
		assertNotNull(vd.getEncryptionKey());
		assertNotNull(vd.getSignatureGenerator());

		// Now let's check if we obtain the same JSON value when marshalling
		// the VotingData instance.
		String marshalledVotingData
				= Converter.marshal(vd);
		// Guess: We cannot impose strict ordering of fields put in the 'others' Object array
		// of the Java DTO objects. Thus, two 'equal' JSON strings differ in the ordering
		// of their children. To compare (and test of 'equality') we use a helper
		// framework, JSONassert: https://github.com/skyscreamer/JSONassert
		//JSONAssert.assertEquals(jsonVotingData, marshalledVotingData, JSONCompareMode.LENIENT);
		System.out.println(marshalledVotingData);
	}
}
