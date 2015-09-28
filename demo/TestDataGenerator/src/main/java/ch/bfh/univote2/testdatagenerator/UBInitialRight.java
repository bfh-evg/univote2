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

import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.util.Date;
import ch.bfh.uniboard.service.Attributes;
import ch.bfh.uniboard.service.DateValue;
import ch.bfh.uniboard.service.IntegerValue;
import ch.bfh.uniboard.service.StringValue;
import ch.bfh.univote2.common.crypto.KeyUtil;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class UBInitialRight {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws Exception {

		String keyStorePath = "../UniVote.jks";
		String ecCertPath = "../ec-certificate.pem";
		String keyStorePass = "12345678";
		String boardAlias = "uniboardvote";
		String boardPKPass = "12345678";
		String section = "test-2015";

		KeyStore caKs = KeyStore.getInstance(System.getProperty("javax.net.ssl.keyStoreType", "jks"));

		InputStream in;
		File file = new File(keyStorePath);
		in = new FileInputStream(file);

		caKs.load(in, keyStorePass.toCharArray());

		// Load uniboard key and cert
		Key boardKey = caKs.getKey(boardAlias, boardPKPass.toCharArray());
		DSAPrivateKey dsaPrivKey = (DSAPrivateKey) boardKey;

		Certificate boardCert = caKs.getCertificate(boardAlias);
		DSAPublicKey boardPubKey = (DSAPublicKey) boardCert.getPublicKey();
		BigInteger uniboardPublicKey = boardPubKey.getY();

		//Load keypair from ec
		DSAPublicKey ecPubKey = KeyUtil.getDSAPublicKey(ecCertPath);
		BigInteger electionCoordinatorPublicKey = ecPubKey.getY();

		//Create correct json message
		byte[] message1 = ("{\"group\":\"accessRight\",\"crypto\":{\"type\":\"DL\", \"p\":\""
				+ ecPubKey.getParams().getP().toString(10)
				+ "\",\"q\":\"" + ecPubKey.getParams().getQ().toString(10)
				+ "\",\"g\":\"" + ecPubKey.getParams().getG().toString(10)
				+ "\",\"publickey\":\""
				+ electionCoordinatorPublicKey.toString(10) + "\"}}").getBytes(Charset.forName("UTF-8"));

		//Create alphas and betas
		Attributes alpha = new Attributes();
		alpha.add("section", new StringValue(section));
		alpha.add("group", new StringValue("accessRight"));
		Element ubMsgSig = PostCreator.createAlphaSignatureWithDL(message1, alpha, dsaPrivKey);
		alpha.add("signature", new StringValue(ubMsgSig.convertToBigInteger().toString(10)));
		alpha.add("publickey", new StringValue(uniboardPublicKey.toString(10)));

		Attributes beta = new Attributes();
		beta.add("timestamp", new DateValue(new Date()));
		beta.add("rank", new IntegerValue(0));
		Element initMsgBetaSig = PostCreator.createBetaSignature(message1, alpha, beta, dsaPrivKey);
		beta.add("boardSignature", new StringValue(initMsgBetaSig.convertToBigInteger().toString(10)));

		//output post as json
		String post = PostCreator.createMessage(message1, alpha, beta);

		System.out.println(post);

	}

}
