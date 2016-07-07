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
package ch.bfh.univote2.ec.createVotingData;

import ch.bfh.uniboard.data.AttributeDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.VotingData;
import ch.bfh.univote2.common.query.AlphaEnum;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.ec.ActionManagerMock;
import ch.bfh.univote2.ec.InformationServiceMock;
import ch.bfh.univote2.ec.TenantManagerMock;
import ch.bfh.univote2.ec.UniboardServiceMock1;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import sun.security.provider.DSAPublicKey;

/**
 *
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
@RunWith(Arquillian.class)
public class CreateVotingDataActionTest {

	/**
	 * Helper method for building the in-memory variant of a deployable unit. See Arquillian for more information.
	 *
	 * @return a Java archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive ja = ShrinkWrap.create(WebArchive.class)
				.addClass(SyncCreateVotingDataAction.class)
				.addClass(UniboardServiceMock1.class)
				.addClass(InformationServiceMock.class)
				.addClass(TenantManagerMock.class)
				.addClass(ActionManagerMock.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		//System.out.println(ja.toString(true));
		return ja;
	}

	@EJB
	private UniboardServiceMock1 uniboardServiceMock;

	@EJB
	private NotifiableAction createVotingDataAction;

	@EJB
	private TenantManagerMock tenantManager;

	@EJB
	private ActionManagerMock actionManagerMock;

	/**
	 * Ensure that mock instance of board is reset to its initial states.
	 */
	@After
	public void tearDown() {
		this.uniboardServiceMock.clear();
	}

	public CreateVotingDataActionTest() {
	}

	/**
	 * Test of prepareContext without published voting data.
	 *
	 * @throws Exception if there is an error
	 */
	@Test
	public void testPrepareContext0() throws Exception {
		String tenant = "testPrepareContext0";
		String section = "section";
		ActionContext context = this.createVotingDataAction.prepareContext(tenant, section);
		assertFalse(context.checkPostCondition());
	}

	/**
	 * Test of prepareContext with unpublished voting data.
	 *
	 * @throws Exception if there is an error
	 */
	@Test
	public void testPrepareContext1() throws Exception {
		String tenant = "testPrepareContext1";
		String section = "section";
		List<PostDTO> response1 = new ArrayList();
		response1.add(new PostDTO());
		this.uniboardServiceMock.addResponse(response1, "");
		ActionContext context = this.createVotingDataAction.prepareContext(tenant, section);
		assertFalse(context.checkPostCondition());
		assertTrue(context.getPreconditionQueries().size() > 0);
	}

	/**
	 * Test of definePrecondition (via prepareContext) with unpublished data.
	 *
	 * @throws Exception if there is an error
	 */
	@Test
	public void testPrepareDefinePreconditionForElectionDefinition1() throws Exception {
		String tenant = "testPrepareDefinePreconditionForElectionDefinition1";
		String section = "section";
		ActionContext context = this.createVotingDataAction.prepareContext(tenant, section);
		// None of the expected information for building a voting data recore is on the board:
		List<PreconditionQuery> pcQueries = context.getPreconditionQueries();
		assertNotNull(pcQueries);
		assertEquals(5, pcQueries.size());
	}

	/**
	 * Test of definePrecondition (via prepareContext) with some published data.
	 *
	 * @throws Exception if there is an error
	 */
	@Test
	public void testPrepareDefinePreconditionForElectionDefinition2() throws Exception {
		String tenant = "testPrepareDefinePreconditionForElectionDefinition2";
		String section = "section";

		// Post an election definition...
		String jsonElectionDefinition
				= "{\n"
				+ "	\"title\": {\n"
				+ "		\"default\": \"Universität Bern: Wahlen des SUB-StudentInnenrates\",\n"
				+ "		\"fr\": \"Université de Berne: Élection du conseil des étudiant-e-s SUB\",\n"
				+ "		\"en\": \"University of Bern: SUB Elections\"\n"
				+ "	},\n"
				+ "	\"administration\": {\n"
				+ "		\"default\": \"StudentInnenschaft der Universität Bern (SUB)\",\n"
				+ "		\"fr\": \"Ensemble des étudiants de l'Université de Berne (SUB)\",\n"
				+ "		\"en\": \"Student Body of the University of Bern (SUB)\"\n"
				+ "	},\n"
				+ "	\"votingPeriodBegin\": \"2015-03-08T23:00:00Z\",\n"
				+ "	\"votingPeriodEnd\": \"2015-03-26T11:00:00Z\"\n"
				+ "}";
		byte[] message = jsonElectionDefinition.getBytes(Charset.forName("UTF-8"));
		List<PostDTO> response = new ArrayList<>();
		response.add(new PostDTO(message, null, null));
		// Post message1 to the board.
		this.uniboardServiceMock.addResponse(response, GroupEnum.ELECTION_DEFINITION.getValue());

		ActionContext context = this.createVotingDataAction.prepareContext(tenant, section);

		List<PreconditionQuery> pcQueries = context.getPreconditionQueries();
		assertNotNull(pcQueries);
		assertEquals(4, pcQueries.size());
		CreateVotingDataActionContext cvdContext = (CreateVotingDataActionContext) context;
		assertNotNull(cvdContext.getElectionDefinition());
	}

	/**
	 * Test of notifyAction with some published data.
	 *
	 * @throws Exception if there is an error
	 */
	@Test
	public void testNotifyActionHavingReceivedOneNotificationOnly() throws Exception {
		String tenant = "testNotifyActionHavingReceivedOneNotificationOnly";
		String section = "section";

		// Notify with signature key
		String jsonSignatureGenerator
				= "{\n"
				+ "	\"signatureGenerator\": \"1234567890\"\n"
				+ "}";
		byte[] message5 = jsonSignatureGenerator.getBytes(Charset.forName("UTF-8"));
		PostDTO post = new PostDTO(message5, null, null);
		AttributeDTO attribute = new AttributeDTO(AlphaEnum.GROUP.getValue(), GroupEnum.MIXED_KEYS.getValue(), null);
		List<AttributeDTO> attributes = new ArrayList<>();
		attributes.add(attribute);
		post.setAlpha(attributes);
		List<PostDTO> response5 = new ArrayList();
		response5.add(post);

		// Let's call notifyAction, check its behavior.
		ActionContext context = this.createVotingDataAction.prepareContext(tenant, section);
		this.createVotingDataAction.notifyAction(context, post);

		assertEquals((ResultStatus.RUN_FINISHED), this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test of notifyAction with all required data published.
	 *
	 * @throws Exception if there is an error
	 */
	@Ignore
	public void testNotifyActionHavingReceivedAllNotifications() throws Exception {
		String tenant = "testNotifyActionHavingReceivedAllNotifications";
		String section = "section";

		prepareBoardWithFirstFourMessages();

		ActionContext context = this.createVotingDataAction.prepareContext(tenant, section);
		// Set dummy public key on tenantManager.
		PublicKey publicKey = new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
		this.tenantManager.setPublicKey(publicKey);

		List<PreconditionQuery> pcQueries = context.getPreconditionQueries();
		assertNotNull(pcQueries);
		assertEquals(1, pcQueries.size());
		CreateVotingDataActionContext cvdContext = (CreateVotingDataActionContext) context;
		assertNotNull(cvdContext.getElectionDefinition());
		assertNotNull(cvdContext.getElectionDetails());
		assertNotNull(cvdContext.getCryptoSetting());
		assertNotNull(cvdContext.getEncryptionKey());

		// Notify with signature key
		String jsonSignatureGenerator
				= "{\n"
				+ "	\"signatureGenerator\": \"1234567890\"\n"
				+ "}";
		byte[] message5 = jsonSignatureGenerator.getBytes(Charset.forName("UTF-8"));
		PostDTO post = new PostDTO(message5, null, null);
		AttributeDTO attribute = new AttributeDTO(AlphaEnum.GROUP.getValue(), GroupEnum.MIXED_KEYS.getValue(), null);
		List<AttributeDTO> attributes = new ArrayList<>();
		attributes.add(attribute);
		post.setAlpha(attributes);

		// Let's call notifyAction, check its behavior.
		this.createVotingDataAction.notifyAction(cvdContext, post);

		// Check whether the board received a voting data message.
		PostDTO posted = this.uniboardServiceMock.getPost();

		assertNotNull(posted);
		byte[] postedMessage = posted.getMessage();
		assertNotNull(postedMessage);
		//JsonObject jsonMessage = unmarshal(postedMessage);
		VotingData vd = JSONConverter.unmarshal(VotingData.class, postedMessage);
		assertNotNull(vd.getDefinition());
		assertNotNull(vd.getDetails());
		assertNotNull(vd.getCryptoSetting());
		assertNotNull(vd.getEncryptionKey());
		assertNotNull(vd.getSignatureGenerator());

		assertEquals((ResultStatus.FINISHED), this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test of run with all required data published.
	 *
	 * @throws Exception if there is an error
	 */
	@Ignore
	public void testRunHavingReceivedAllNotifications() throws Exception {
		String tenant = "testRunHavingReceivedAllNotifications";
		String section = "section";

		prepareBoardWithAllMessages();

		ActionContext context = this.createVotingDataAction.prepareContext(tenant, section);
		// Set dummy public key on tenantManager.
		PublicKey publicKey = new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
		this.tenantManager.setPublicKey(publicKey);
		this.createVotingDataAction.run(context);

		assertEquals((ResultStatus.FINISHED), this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test of run with some required data published.
	 *
	 * @throws Exception if there is an error
	 */
	@Test
	public void testRunHavingReceivedFirstFourNotifications() throws Exception {
		String tenant = "testRunHavingReceivedAllNotifications";
		String section = "section";

		prepareBoardWithFirstFourMessages();

		ActionContext context = this.createVotingDataAction.prepareContext(tenant, section);
		// Set dummy public key on tenantManager.
		PublicKey publicKey = new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
		this.tenantManager.setPublicKey(publicKey);
		this.createVotingDataAction.run(context);

		assertEquals((ResultStatus.RUN_FINISHED), this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test of run with all required data published but no permission.
	 *
	 * @throws Exception if there is an error
	 */
	@Ignore
	public void testRunHavingReceivedAllNotificationsButNoPermission() throws Exception {
		String tenant = "testRunHavingReceivedAllNotificationsButNoPermission";
		String section = "section";

		prepareBoardWithAllMessages();

		ActionContext context = this.createVotingDataAction.prepareContext(tenant, section);
		this.createVotingDataAction.run(context);

		assertEquals((ResultStatus.FAILURE), this.actionManagerMock.getResultStatus());
	}

	// Helper
	private void prepareBoardWithAllMessages() {
		prepareBoardWithFirstFourMessages();
		prepareBoardWithFithMessage();
	}

	private void prepareBoardWithFirstFourMessages() {
		// Post an election definition...
		String jsonElectionDefinition
				= "{\n"
				+ "	\"title\": {\n"
				+ "		\"default\": \"Universität Bern: Wahlen des SUB-StudentInnenrates\",\n"
				+ "		\"fr\": \"Université de Berne: Élection du conseil des étudiant-e-s SUB\",\n"
				+ "		\"en\": \"University of Bern: SUB Elections\"\n"
				+ "	},\n"
				+ "	\"administration\": {\n"
				+ "		\"default\": \"StudentInnenschaft der Universität Bern (SUB)\",\n"
				+ "		\"fr\": \"Ensemble des étudiants de l'Université de Berne (SUB)\",\n"
				+ "		\"en\": \"Student Body of the University of Bern (SUB)\"\n"
				+ "	},\n"
				+ "	\"votingPeriodBegin\": \"2015-03-08T23:00:00Z\",\n"
				+ "	\"votingPeriodEnd\": \"2015-03-26T11:00:00Z\"\n"
				+ "}";
		byte[] message1 = jsonElectionDefinition.getBytes(Charset.forName("UTF-8"));
		List<PostDTO> response1 = new ArrayList();
		response1.add(new PostDTO(message1, null, null));
		// Post message1 to the board.
		this.uniboardServiceMock.addResponse(response1, GroupEnum.ELECTION_DEFINITION.getValue());

		// Post election details (origin of sample: admin-client/json-examples)
		String jsonElectionDetails
				= "{\n"
				+ "	\"options\": [\n"
				+ "		{\n"
				+ "			\"id\": 1,\n"
				+ "			\"type\": \"votingOption\",\n"
				+ "			\"answer\": { \"default\": \"Ja\", \"fr\": \"Oui\", \"it\": \"Si\"}\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 2,\n"
				+ "			\"type\": \"votingOption\",\n"
				+ "			\"answer\": { \"default\": \"Nein\", \"fr\": \"Non\", \"it\": \"No\"}\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 3,\n"
				+ "			\"type\": \"votingOption\",\n"
				+ "			\"answer\": { \"default\": \"Enthaltung\", \"fr\": \"Abstention\", \"it\": \"Astensione\"}\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 4,\n"
				+ "			\"type\": \"votingOption\",\n"
				+ "			\"answer\": { \"default\": \"Ja\", \"fr\": \"Oui\", \"it\": \"Si\"}\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 5,\n"
				+ "			\"type\": \"votingOption\",\n"
				+ "			\"answer\": { \"default\": \"Nein\", \"fr\": \"Non\", \"it\": \"No\"}\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 6,\n"
				+ "			\"type\": \"votingOption\",\n"
				+ "			\"answer\": { \"default\": \"Enthaltung\", \"fr\": \"Abstention\", \"it\": \"Astensione\"}\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 7,\n"
				+ "			\"type\": \"votingOption\",\n"
				+ "			\"answer\": { \"default\": \"Ja\", \"fr\": \"Oui\", \"it\": \"Si\"}\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 8,\n"
				+ "			\"type\": \"votingOption\",\n"
				+ "			\"answer\": { \"default\": \"Nein\", \"fr\": \"Non\", \"it\": \"No\"}\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 9,\n"
				+ "			\"type\": \"votingOption\",\n"
				+ "			\"answer\": { \"default\": \"Enthaltung\", \"fr\": \"Abstention\", \"it\": \"Astensione\"}\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 10,\n"
				+ "			\"type\": \"votingOption\",\n"
				+ "			\"answer\": { \"default\": \"Ja\", \"fr\": \"Oui\", \"it\": \"Si\"}\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 11,\n"
				+ "			\"type\": \"votingOption\",\n"
				+ "			\"answer\": { \"default\": \"Nein\", \"fr\": \"Non\", \"it\": \"No\"}\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 12,\n"
				+ "			\"type\": \"votingOption\",\n"
				+ "			\"answer\": { \"default\": \"Enthaltung\", \"fr\": \"Abstention\", \"it\": \"Astensione\"}\n"
				+ "		}\n"
				+ "	],\n"
				+ "	\"rules\": [\n"
				+ "		{\n"
				+ "			\"id\": 1,\n"
				+ "			\"type\": \"summationRule\",\n"
				+ "			\"optionIds\": [1, 2, 3],\n"
				+ "			\"lowerBound\": 0,\n"
				+ "			\"upperBound\": 1\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 2,\n"
				+ "			\"type\": \"cumulationRule\",\n"
				+ "			\"optionIds\": [1, 2, 3],\n"
				+ "			\"lowerBound\": 0,\n"
				+ "			\"upperBound\": 1\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 3,\n"
				+ "			\"type\": \"summationRule\",\n"
				+ "			\"optionIds\": [4, 5, 6],\n"
				+ "			\"lowerBound\": 0,\n"
				+ "			\"upperBound\": 1\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 4,\n"
				+ "			\"type\": \"cumulationRule\",\n"
				+ "			\"optionIds\": [4, 5, 6],\n"
				+ "			\"lowerBound\": 0,\n"
				+ "			\"upperBound\": 1\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 5,\n"
				+ "			\"type\": \"summationRule\",\n"
				+ "			\"optionIds\": [7, 8, 9],\n"
				+ "			\"lowerBound\": 0,\n"
				+ "			\"upperBound\": 1\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 6,\n"
				+ "			\"type\": \"cumulationRule\",\n"
				+ "			\"optionIds\": [7, 8, 9],\n"
				+ "			\"lowerBound\": 0,\n"
				+ "			\"upperBound\": 1\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 7,\n"
				+ "			\"type\": \"summationRule\",\n"
				+ "			\"optionIds\": [10, 11, 12],\n"
				+ "			\"lowerBound\": 0,\n"
				+ "			\"upperBound\": 1\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 8,\n"
				+ "			\"type\": \"cumulationRule\",\n"
				+ "			\"optionIds\": [10, 11, 12],\n"
				+ "			\"lowerBound\": 0,\n"
				+ "			\"upperBound\": 1\n"
				+ "		}\n"
				+ "	],\n"
				+ "	\"issues\": [\n"
				+ "		{\n"
				+ "			\"id\": 1,\n"
				+ "			\"type\": \"vote\",\n"
				+ "			\"title\": {\n"
				+ "				\"default\": \"Präimplantationsdiagnostik\",\n"
				+ "				\"fr\": \"Diagnostic préimplantatoire\",\n"
				+ "				\"it\": \"Diagnosi preimpianto\",\n"
				+ "				\"rm\": \"Diagnostica da preimplantaziun\"\n"
				+ "			},\n"
				+ "			\"question\": {\n"
				+ "				\"default\": \"Wollen Sie den Bundesbeschluss vom 12. Dezember 2014 über die Änderung der Verfassungsbestimmung zur Fortpflanzungsmedizin und Gentechnologie im Humanbereich annehmen?\",\n"
				+ "				\"fr\": \"Acceptez-vous l’arrêté fédéral du 12 décembre 2014 concernant la modification de l’article constitutionnel relatif à la procréation médicalement assistée et au génie génétique dans le domaine humain?\",\n"
				+ "				\"it\": \"Volete accettare il decreto federale del 12 dicembre 2014 concernente la modifica dell’articolo costituzionale relativo alla medicina riproduttiva e all’ingegneria genetica in ambito umano?\",\n"
				+ "				\"rm\": \"Vulais Vus acceptar il Conclus federal dals 12 da december 2014 davart la midada da l’artitgel constituziunal concernent la medischina da reproducziun e la tecnologia da gens sin il sectur uman?\"\n"
				+ "			},\n"
				+ "			\"optionIds\": [1, 2, 3],\n"
				+ "			\"ruleIds\": [1, 2]\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 2,\n"
				+ "			\"type\": \"vote\",\n"
				+ "			\"title\": {\n"
				+ "				\"default\": \"Stipendieninitiative\",\n"
				+ "				\"fr\": \"Initiative sur les bourses d'études\",\n"
				+ "				\"it\": \"Iniziativa sulle borse di studio\",\n"
				+ "				\"rm\": \"Iniziativa davart ils stipendis\"\n"
				+ "			},\n"
				+ "			\"question\": {\n"
				+ "				\"default\": \"Wollen Sie die Volksinitiative «Stipendieninitiative» annehmen?\",\n"
				+ "				\"fr\": \"Acceptez-vous l’initiative populaire «Initiative sur les bourses d’études»?\",\n"
				+ "				\"it\": \"Volete accettare l’iniziativa popolare «Sulle borse di studio»?\",\n"
				+ "				\"rm\": \"Vulais Vus acceptar l’iniziativa dal pievel «Iniziativa davart ils stipendis»?\"\n"
				+ "			},\n"
				+ "			\"optionIds\": [4, 5, 6],\n"
				+ "			\"ruleIds\": [3, 4]\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 3,\n"
				+ "			\"type\": \"vote\",\n"
				+ "			\"title\": {\n"
				+ "				\"default\": \"Erbschaftssteuerreform\",\n"
				+ "				\"fr\": \"Réforme de la fiscalité successorale\",\n"
				+ "				\"it\": \"Riforma dell'imposta sulle successioni\",\n"
				+ "				\"rm\": \"Refurma da la taglia sin l'ierta\"\n"
				+ "			},\n"
				+ "			\"question\": {\n"
				+ "				\"default\": \"Wollen Sie die Volksinitiative «Millionen-Erbschaften besteuern für unsere AHV (Erbschaftssteuerreform)» annehmen?\",\n"
				+ "				\"fr\": \"Acceptez-vous l’initiative populaire «Imposer les successions de plusieurs millions pour financer notre AVS (Réforme de la fiscalité successorale)»?\",\n"
				+ "				\"it\": \"Volete accettare l’iniziativa popolare «Tassare le eredità milionarie per finanziare la nostra AVS (Riforma dell’impostasulle successioni)»?\",\n"
				+ "				\"rm\": \"Vulais Vus acceptar l’iniziativa dal pievel «Far pajar taglias sin iertas da milliuns per finanziar nossa AVS (Refurma da la tagliasin l’ierta)»?\"\n"
				+ "			},\n"
				+ "			\"optionIds\": [7, 8, 9],\n"
				+ "			\"ruleIds\": [5, 6]\n"
				+ "		},\n"
				+ "		{\n"
				+ "			\"id\": 4,\n"
				+ "			\"type\": \"vote\",\n"
				+ "			\"title\": {\n"
				+ "				\"default\": \"Bundesgesetz über Radio und Fernsehen\",\n"
				+ "				\"fr\": \"Loi fédérale sur la radio et la télévision\",\n"
				+ "				\"it\": \"Legge federale sulla radiotelevisione (LRTV)\",\n"
				+ "				\"rm\": \"Lescha federala davart radio e televisiun\"\n"
				+ "			},\n"
				+ "			\"question\": {\n"
				+ "				\"default\": \"Wollen Sie die Änderung vom 26. September 2014 des Bundesgesetzes über Radio und Fernsehen (RTVG) annehmen?\",\n"
				+ "				\"fr\": \"Acceptez-vous la modification du 26 septembre 2014 de la loi fédérale sur la radio et la télévision (LRTV)?\",\n"
				+ "				\"it\": \"Volete accettare la modifica del 26 settembre 2014 della legge federale sulla radiotelevisione (LRTV)?\",\n"
				+ "				\"rm\": \"Vulais Vus acceptar la midada dals 26 da settember 2014 da la Lescha federala davart radio e televisiun (LRTV)?\"\n"
				+ "			},\n"
				+ "			\"optionIds\": [10, 11, 12],\n"
				+ "			\"ruleIds\": [7, 8]\n"
				+ "		}\n"
				+ "	],\n"
				+ "	\"ballotEncoding\": \"E1\"\n"
				+ "}";
		// Post message2 to the board.
		byte[] message2 = jsonElectionDetails.getBytes(Charset.forName("UTF-8"));
		List<PostDTO> response2 = new ArrayList();
		response2.add(new PostDTO(message2, null, null));
		// Post message2 to the board.
		this.uniboardServiceMock.addResponse(response2, GroupEnum.ELECTION_DETAILS.getValue());

		// Post crypto setting
		String jsonCryptoSetting
				= "{\n"
				+ "  \"encryptionSetting\": \"RC1e\",\n"
				+ "  \"signatureSetting\": \"RC1s\",\n"
				+ "  \"hashSetting\": \"H2\"\n"
				+ "}";
		byte[] message3 = jsonCryptoSetting.getBytes(Charset.forName("UTF-8"));
		List<PostDTO> response3 = new ArrayList();
		response3.add(new PostDTO(message3, null, null));
		// Post message3 to the board.
		this.uniboardServiceMock.addResponse(response3, GroupEnum.CRYPTO_SETTING.getValue());

		// Post encryption key
		String jsonEncryptionKey
				= "{\n"
				+ "	\"encryptionKey\": \"1234567890\"\n"
				+ "}";
		byte[] message4 = jsonEncryptionKey.getBytes(Charset.forName("UTF-8"));
		List<PostDTO> response4 = new ArrayList();
		response4.add(new PostDTO(message4, null, null));
		// Post message4 to the board.
		this.uniboardServiceMock.addResponse(response4, GroupEnum.ENCRYPTION_KEY.getValue());
	}

	private void prepareBoardWithFithMessage() {
		// Notify with signature key
		String jsonSignatureGenerator
				= "{\n"
				+ "	\"signatureGenerator\": \"1234567890\"\n"
				+ "}";
		byte[] message5 = jsonSignatureGenerator.getBytes(Charset.forName("UTF-8"));
		List<PostDTO> response5 = new ArrayList();
		response5.add(new PostDTO(message5, null, null));
		// Post message5 to the board.
		this.uniboardServiceMock.addResponse(response5, GroupEnum.MIXED_KEYS.getValue());
	}
}
