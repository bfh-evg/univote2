/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniBoard.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote2.testdatagenerator;

import ch.bfh.uniboard.clientlib.BoardErrorException;
import ch.bfh.uniboard.clientlib.PostException;
import ch.bfh.uniboard.clientlib.PostHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

/**
 * Tester for library
 *
 * @author Philémon von Bergen
 */
class DataCreator {

	//URLs of the board
	private static final String uniBoardUrl = "http://urd.bfh.ch:10080/UniBoardService/UniBoardServiceImpl";
	private static final String uniBoardWSDLurl = "http://urd.bfh.ch:10080/UniBoardService/UniBoardServiceImpl?wsdl";

	private static final String keystorePath = "../UniVote.jks";
	private static final String keystorePass = "12345678";
	private static final String privKeyPass = "12345678";

	/*
	 * This program expects an access right as follows to be able to post:
	 * {
	 "group": "ballot",
	 "crypto": {
	 "type": "RSA",
	 "publickey": "892967880362342323465014090509126578217712653234330579952343248557066560974032988448007650380498660569429733194811788498443166884053444837128331407716 [...]"
	 }
	 }
	 */
	public static void main(String[] args) throws Exception {

		/**
		 * ************************************************************************************************************
		 * POST
		 * ***********************************************************************************************************
		 */
		KeyStore ks = loadKeyStore(keystorePath, keystorePass);

		PrivateKey privKey = (DSAPrivateKey) ks.getKey("ec-demo", privKeyPass.toCharArray());
		DSAPublicKey pubKey = (DSAPublicKey) ks.getCertificate("ec-demo").getPublicKey();

		String posterPublicKey = pubKey.getY().toString(10);//MathUtil.pair(pubKey.getPublicExponent(), pubKey.getModulus()).toString(10);

		PostHelper ph = new PostHelper(pubKey, privKey, ks.getCertificate(
				"uniboardvote").getPublicKey(), uniBoardWSDLurl, uniBoardUrl);

		try {
			String message = "";
			ph.post(message, "sub-2015", "votingData");
			System.out.println("Post successful");
		} catch (PostException | BoardErrorException e) {
			System.out.println("Error during posting: " + e.getMessage());
		}

	}

	private static KeyStore loadKeyStore(String keyStorePath, String keyStorePass) throws KeyStoreException,
			FileNotFoundException,
			IOException, NoSuchAlgorithmException, CertificateException {
		//Load keystore with private key for the manager
		KeyStore ks = KeyStore.getInstance(System.getProperty("javax.net.ssl.keyStoreType", "jks"));

		File file = new File(keyStorePath);
		InputStream in = new FileInputStream(file);

		ks.load(in, keyStorePass.toCharArray());

		return ks;
	}
}
