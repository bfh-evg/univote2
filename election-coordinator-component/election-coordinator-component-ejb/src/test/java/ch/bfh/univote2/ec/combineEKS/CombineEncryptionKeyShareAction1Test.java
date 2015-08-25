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
package ch.bfh.univote2.ec.combineEKS;

import ch.bfh.uniboard.data.AttributesDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.unicrypt.crypto.keygenerator.interfaces.KeyPairGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.classes.PlainPreimageProofSystem;
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.crypto.CryptoProvider;
import ch.bfh.univote2.component.core.message.AccessRight;
import ch.bfh.univote2.component.core.message.CryptoSetting;
import ch.bfh.univote2.component.core.message.EncryptionKey;
import ch.bfh.univote2.component.core.message.EncryptionKeyShare;
import ch.bfh.univote2.component.core.message.JSONConverter;
import ch.bfh.univote2.component.core.message.SigmaProof;
import ch.bfh.univote2.component.core.query.GroupEnum;
import ch.bfh.univote2.ec.ActionManagerMock;
import ch.bfh.univote2.ec.InformationServiceMock;
import ch.bfh.univote2.ec.TenantManagerMock;
import ch.bfh.univote2.ec.UniboardServiceMock;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import sun.security.provider.DSAPublicKey;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@RunWith(Arquillian.class)
public class CombineEncryptionKeyShareAction1Test {

	public CombineEncryptionKeyShareAction1Test() {
	}

	/**
	 * Helper method for building the in-memory variant of a deployable unit. See Arquillian for more information.
	 *
	 * @return a Java archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive ja = ShrinkWrap.create(WebArchive.class)
				.addClass(TestableCombineEncryptionKeyShareAction1.class)
				.addClass(UniboardServiceMock.class)
				.addClass(InformationServiceMock.class)
				.addClass(ActionManagerMock.class)
				.addClass(TenantManagerMock.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		//System.out.println(ja.toString(true));
		return ja;
	}

	@EJB
	private UniboardServiceMock uniboardServiceMock;

	@EJB
	private TestableCombineEncryptionKeyShareAction1 combineEKSAction;

	@EJB
	private ActionManagerMock actionManagerMock;

	@EJB
	private TenantManagerMock tenantManagerMock;

	/**
	 * Test computeAndPostKey working
	 */
	@Test
	public void testComputeAndPostKey1() throws InvalidKeyException, UnivoteException {
		String tenant = "computeAndPostKey";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		CombineEncryptionKeyShareActionContext actionContext = new CombineEncryptionKeyShareActionContext(ack);

		CryptoSetting cs = CryptoProvider.getCryptoSetting(0);
		actionContext.setCryptoSetting(cs);
		CyclicGroup cyclicGroup
				= CryptoProvider.getEncryptionSetup(actionContext.getCryptoSetting().getEncryptionSetting());

		actionContext.getKeyShares().put("tallier1", cyclicGroup.getElementFrom(new BigInteger("38")));
		actionContext.getKeyShares().put("tallier2", cyclicGroup.getElementFrom(new BigInteger("11")));
		actionContext.getKeyShares().put("tallier3", cyclicGroup.getElementFrom(new BigInteger("61")));
		actionContext.getKeyShares().put("tallier4", cyclicGroup.getElementFrom(new BigInteger("77")));

		this.tenantManagerMock.setPublicKey(
				new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE));

		this.combineEKSAction.computeAndPostKey(actionContext);

		PostDTO arPost = this.uniboardServiceMock.getPost();
		assertNotNull(arPost);
		AccessRight ar = JSONConverter.unmarshal(AccessRight.class, arPost.getMessage());
		assertEquals("1", ar.getCrypto().getPublickey());
		assertEquals(GroupEnum.ENCRYPTION_KEY.getValue(), ar.getGroup());

		PostDTO encKeyPost = this.uniboardServiceMock.getPost();
		assertNotNull(encKeyPost);

		EncryptionKey encKey = JSONConverter.unmarshal(EncryptionKey.class, encKeyPost.getMessage());
		assertEquals("94", encKey.getEncryptionKey());

	}

	/**
	 * Test validateAndAddKeyShare working
	 */
	@Test
	public void testValidateAndAddKeyShare() throws UnivoteException {
		String tenant = "validateAndAddKeyShare";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		CombineEncryptionKeyShareActionContext actionContext = new CombineEncryptionKeyShareActionContext(ack);

		CryptoSetting cs = CryptoProvider.getCryptoSetting(0);
		actionContext.setCryptoSetting(cs);

		CyclicGroup cyclicGroup
				= CryptoProvider.getEncryptionSetup(actionContext.getCryptoSetting().getEncryptionSetting());
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(cyclicGroup);
		KeyPairGenerator keyPairGen = elGamal.getKeyPairGenerator();
		Function proofFunction = keyPairGen.getPublicKeyGenerationFunction();
		PlainPreimageProofSystem pg = PlainPreimageProofSystem.getInstance(proofFunction);
		Pair keypair = keyPairGen.generateKeyPair();
		Triple proofTriple = pg.generate(keypair.getFirst(), keypair.getSecond());

		EncryptionKeyShare encryptionKeyShare = new EncryptionKeyShare();
		SigmaProof proof = new SigmaProof();
		proof.setCommitment(proofTriple.getFirst().convertToString());
		proof.setChallenge(proofTriple.getSecond().convertToString());
		proof.setResponse(proofTriple.getThird().convertToString());
		encryptionKeyShare.setProof(proof);
		encryptionKeyShare.setKeyShare(keypair.getSecond().convertToString());

		byte[] message = JSONConverter.marshal(encryptionKeyShare).getBytes();
		AttributesDTO alpha = new AttributesDTO();
		alpha.getAttribute().add(new AttributesDTO.AttributeDTO("publickey", new StringValueDTO(tenant)));
		PostDTO post = new PostDTO(message, alpha, null);

		this.combineEKSAction.validateAndAddKeyShare(actionContext, post);

		assertEquals(1, actionContext.getKeyShares().size());
		assertTrue(actionContext.getKeyShares().containsKey(tenant));
		assertEquals(keypair.getSecond(), actionContext.getKeyShares().get(tenant));

	}

}
