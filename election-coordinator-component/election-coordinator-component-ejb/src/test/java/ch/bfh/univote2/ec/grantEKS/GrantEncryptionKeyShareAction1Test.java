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
package ch.bfh.univote2.ec.grantEKS;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultDTO;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.message.Converter;
import ch.bfh.univote2.component.core.message.TrusteeCertificates;
import ch.bfh.univote2.ec.ActionManagerMock;
import ch.bfh.univote2.ec.InformationServiceMock;
import ch.bfh.univote2.ec.UniboardServiceMock;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import sun.security.provider.DSAPublicKey;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@RunWith(Arquillian.class)
public class GrantEncryptionKeyShareAction1Test {

	public GrantEncryptionKeyShareAction1Test() {
	}

	/**
	 * Helper method for building the in-memory variant of a deployable unit. See Arquillian for more information.
	 *
	 * @return a Java archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive ja = ShrinkWrap.create(WebArchive.class)
				.addClass(TestableGrantEncryptionKeyShareAction1.class)
				.addClass(UniboardServiceMock.class)
				.addClass(InformationServiceMock.class)
				.addClass(ActionManagerMock.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		//System.out.println(ja.toString(true));
		return ja;
	}

	@EJB
	UniboardServiceMock uniboardServiceMock;

	@EJB
	TestableGrantEncryptionKeyShareAction1 grantEKSAction;

	@EJB
	ActionManagerMock actionManagerMock;

	/**
	 * Test grantAccessRight working
	 */
	@Test
	public void testGrantAccessRight1() throws InvalidKeyException {
		String tenant = "grantAccessRight1";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		ActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		PublicKey publicKey = new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
		assertTrue(this.grantEKSAction.grantAccessRight(actionContext, publicKey));
	}

	/**
	 * Test checkAccessRight with no accessRight
	 */
	@Test
	public void testCheckAccessRight1() throws InvalidKeyException, InterruptedException {
		String tenant = "checkAccessRight1";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		ActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		PublicKey publicKey = new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);

		ResultDTO response1 = new ResultDTO();
		this.uniboardServiceMock.addResponse(response1);
		try {
			assertFalse(this.grantEKSAction.checkAccessRight(actionContext, publicKey));
		} catch (UnivoteException ex) {
			fail();
		}
	}

	/**
	 * Test checkAccessRight with accessRight
	 */
	@Test
	public void testCheckAccessRight2() throws InvalidKeyException, InterruptedException {
		String tenant = "checkAccessRight2";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		ActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		PublicKey publicKey = new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);

		ResultDTO response1 = new ResultDTO();
		response1.getPost().add(new PostDTO("".getBytes(), null, null));
		this.uniboardServiceMock.addResponse(response1);
		try {
			assertTrue(this.grantEKSAction.checkAccessRight(actionContext, publicKey));
		} catch (UnivoteException ex) {
			fail();
		}
	}

	/**
	 * Test parseTrusteeCerts with a valid message
	 */
	@Test
	public void testParseTrusteeCerts1() throws Exception {
		try {
			String tenant = "parseTrusteeCerts1";
			String section = "section";
			ActionContextKey ack = new ActionContextKey("Test", tenant, section);
			GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);

			String messageString = "{\"tallierCertificates\": [{\"pem\": \"-----BEGIN CERTIFICATE-----\\n"
					+ "MIIEeTCCAmGgAwIBAgIGAUo507HRMA0GCSqGSIb3DQEBBQUAMBYxFDASBgNVBAMM"
					+ "C1VuaUNlcnQgQkZIMB4XDTE0MTIxMTE0NDk0MloXDTI0MTIxMTE0NDk0MlowXjEY"
					+ "MBYGA1UEAwwPNDgyMzExODZAYmZoLmNoMR8wHQYKCZImiZPyLGQBAQwPNDgyMzEx"
					+ "ODZAYmZoLmNoMQ8wDQYDVQQKDAZiZmguY2gxEDAOBgNVBAsMB1Vua25vd24wggFg"
					+ "MIHWBgcqhkjOOAQBMIHKAoGBAOaZHAJeQHkEW2oSnNbZs3Dky9EfTEeZ9DQzsNmZ"
					+ "pmLnNnfhACPBSwu9fs6wIzdkDAZXoddPYeYcDU6QTIdtFhdmsSKRNiqgLn2qwzFh"
					+ "Ksw0FKLL/ZrTVcyGV2jCOpdbkcLFLfCD/nUROCRKpDKBDwh67vqMjdjBAdzcpmjs"
					+ "UL4pAiEAkABbzpVQUFwuF+cdeQOv3uuQ8QuGZ78GHwQYACIKghUCIQCQAFvOlVBQ"
					+ "XC4X5x15A6/e65DxC4ZnvwYfBBgAIgqCFQOBhAACgYA4kI2sBKseQpoUnTQYQA6n"
					+ "/SXfRTnlFPCH0J5aTT3mjkJmUPTz6qPncmgUyuDMwTrJ0mEISlJ4RIpKUY2Un7gi"
					+ "7OSRaQVkt4ltCOfLFTMgUUHxqYftKAmeLnERcOb5pr2rgpvC61H3qBuTBnmnRH4n"
					+ "8NBmnzthl7IYSW7fVUKjfqNHMEUwEwYKKwYBBAHneQFlAwQFVm90ZXIwFQYKKwYB"
					+ "BAHneQFlAgQHVW5pVm90ZTAXBgorBgEEAed5AWUBBAlTd2l0Y2hBQUkwDQYJKoZI"
					+ "hvcNAQEFBQADggIBALQ61h6lceb9WYybJ5pdCUG8YKsgzKuLP1FIJM9gTeRJsRMU"
					+ "a2s6Hu6qdgtpGkiFBWK/bhBGyqBVm7x6kP/EtLfnlkuzjjlNIWi+oQL21TkyGHOf"
					+ "zqjH9PKnH74v1Ulq+FXncR9C5s1kn+RV8SPKkrLliypFGiKKFMaLobQNxkuZbvF9"
					+ "4lQnlT6W5PkcN92lgp9+Qm0k9KbiJwC4eRI8c0/ghVp4ixIQlPSlmb3nskMcDpHd"
					+ "RsMdQwtfVOSjGMddYrelq2E7jU15LkBjAs/hEbr3uWuhJc9HtOK2kWPCMZmyw9Me"
					+ "PQKn4PJKzZCiGuphrvPnjY0a+U3L6GhH9Qmi01W7JujoCsPpfAx4QiKZ9Anz6sgt"
					+ "pl3m12Um/n0rM4UDkrV80ztt7CneN06KmD1RisuVf8styoG8xgS8ENU0CeyWIClv"
					+ "aBK2mmBQrwH3Sir7HR9Ji0DXGDa2fWJp8EnAXnG5tp/+aJplswIHGUhQ3b0DfoS1"
					+ "wydKaZ/KMKXrkz1qPpB7qko2qGMS4cdXw912wv6X65t0S7CmVd8jW0a1fdRPYNNB"
					+ "fU86PTjTOLBhzS9rGtSsxQ32D3t0ftDIa2jDVBPfE+eTDKF2qm+dxXkgAmP+aRmW"
					+ "qZ1aWTxkEL2KHkryh0py3Rs1/4jLAFPHT5TwDRrsbmUoVLGMLLn+XWDFAKZv"
					+ "-----END CERTIFICATE-----\"}]}";
			TrusteeCertificates trusteeCertificates;
			trusteeCertificates = Converter.unmarshal(TrusteeCertificates.class, messageString.getBytes());

			this.grantEKSAction.parseTrusteeCerts(trusteeCertificates, actionContext);
			assertEquals(1, actionContext.getTalliers().size());
		} catch (UnivoteException ex) {
			fail();
		}

	}

	/**
	 * Test parseTrusteeCerts with a invalid message
	 */
	@Test
	public void testParseTrusteeCerts2() {

		String tenant = "parseTrusteeCerts2";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		try {
			String messageString = "{\"tallierCertificates\": [1,2,3]}";
			TrusteeCertificates trusteeCertificates;
			trusteeCertificates = Converter.unmarshal(TrusteeCertificates.class, messageString.getBytes());

			this.grantEKSAction.parseTrusteeCerts(trusteeCertificates, actionContext);
			fail();
		} catch (Exception ex) {
			assertEquals(0, actionContext.getTalliers().size());
		}
	}

	/**
	 * Test parseTrusteeCerts with a invalid pem
	 */
	@Test
	public void testParseTrusteeCerts3() {

		String tenant = "parseTrusteeCerts3";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		try {
			String messageString = "{\"tallierCertificates\": [{\"pem\": \"asasdfalsfalsdf\"}]}";
			TrusteeCertificates trusteeCertificates;
			trusteeCertificates = Converter.unmarshal(TrusteeCertificates.class, messageString.getBytes());

			this.grantEKSAction.parseTrusteeCerts(trusteeCertificates, actionContext);
			fail();
		} catch (Exception ex) {
			assertEquals(0, actionContext.getTalliers().size());
		}

	}

	/**
	 * Test or retrieveTalliers with trusteeCerts available
	 */
	@Test
	public void testRetrieveTalliers1() throws InterruptedException {
		String tenant = "retrieveTalliers1";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);

		String messageString = "{\"tallierCertificates\": [{\"pem\": \"-----BEGIN CERTIFICATE-----\\n"
				+ "MIIEeTCCAmGgAwIBAgIGAUo507HRMA0GCSqGSIb3DQEBBQUAMBYxFDASBgNVBAMM"
				+ "C1VuaUNlcnQgQkZIMB4XDTE0MTIxMTE0NDk0MloXDTI0MTIxMTE0NDk0MlowXjEY"
				+ "MBYGA1UEAwwPNDgyMzExODZAYmZoLmNoMR8wHQYKCZImiZPyLGQBAQwPNDgyMzEx"
				+ "ODZAYmZoLmNoMQ8wDQYDVQQKDAZiZmguY2gxEDAOBgNVBAsMB1Vua25vd24wggFg"
				+ "MIHWBgcqhkjOOAQBMIHKAoGBAOaZHAJeQHkEW2oSnNbZs3Dky9EfTEeZ9DQzsNmZ"
				+ "pmLnNnfhACPBSwu9fs6wIzdkDAZXoddPYeYcDU6QTIdtFhdmsSKRNiqgLn2qwzFh"
				+ "Ksw0FKLL/ZrTVcyGV2jCOpdbkcLFLfCD/nUROCRKpDKBDwh67vqMjdjBAdzcpmjs"
				+ "UL4pAiEAkABbzpVQUFwuF+cdeQOv3uuQ8QuGZ78GHwQYACIKghUCIQCQAFvOlVBQ"
				+ "XC4X5x15A6/e65DxC4ZnvwYfBBgAIgqCFQOBhAACgYA4kI2sBKseQpoUnTQYQA6n"
				+ "/SXfRTnlFPCH0J5aTT3mjkJmUPTz6qPncmgUyuDMwTrJ0mEISlJ4RIpKUY2Un7gi"
				+ "7OSRaQVkt4ltCOfLFTMgUUHxqYftKAmeLnERcOb5pr2rgpvC61H3qBuTBnmnRH4n"
				+ "8NBmnzthl7IYSW7fVUKjfqNHMEUwEwYKKwYBBAHneQFlAwQFVm90ZXIwFQYKKwYB"
				+ "BAHneQFlAgQHVW5pVm90ZTAXBgorBgEEAed5AWUBBAlTd2l0Y2hBQUkwDQYJKoZI"
				+ "hvcNAQEFBQADggIBALQ61h6lceb9WYybJ5pdCUG8YKsgzKuLP1FIJM9gTeRJsRMU"
				+ "a2s6Hu6qdgtpGkiFBWK/bhBGyqBVm7x6kP/EtLfnlkuzjjlNIWi+oQL21TkyGHOf"
				+ "zqjH9PKnH74v1Ulq+FXncR9C5s1kn+RV8SPKkrLliypFGiKKFMaLobQNxkuZbvF9"
				+ "4lQnlT6W5PkcN92lgp9+Qm0k9KbiJwC4eRI8c0/ghVp4ixIQlPSlmb3nskMcDpHd"
				+ "RsMdQwtfVOSjGMddYrelq2E7jU15LkBjAs/hEbr3uWuhJc9HtOK2kWPCMZmyw9Me"
				+ "PQKn4PJKzZCiGuphrvPnjY0a+U3L6GhH9Qmi01W7JujoCsPpfAx4QiKZ9Anz6sgt"
				+ "pl3m12Um/n0rM4UDkrV80ztt7CneN06KmD1RisuVf8styoG8xgS8ENU0CeyWIClv"
				+ "aBK2mmBQrwH3Sir7HR9Ji0DXGDa2fWJp8EnAXnG5tp/+aJplswIHGUhQ3b0DfoS1"
				+ "wydKaZ/KMKXrkz1qPpB7qko2qGMS4cdXw912wv6X65t0S7CmVd8jW0a1fdRPYNNB"
				+ "fU86PTjTOLBhzS9rGtSsxQ32D3t0ftDIa2jDVBPfE+eTDKF2qm+dxXkgAmP+aRmW"
				+ "qZ1aWTxkEL2KHkryh0py3Rs1/4jLAFPHT5TwDRrsbmUoVLGMLLn+XWDFAKZv"
				+ "-----END CERTIFICATE-----\"}]}";

		ResultDTO response1 = new ResultDTO();
		response1.getPost().add(new PostDTO(messageString.getBytes(), null, null));
		this.uniboardServiceMock.addResponse(response1);

		try {
			this.grantEKSAction.retrieveTalliers(actionContext);
		} catch (UnivoteException ex) {
			fail();
		}
	}

}
