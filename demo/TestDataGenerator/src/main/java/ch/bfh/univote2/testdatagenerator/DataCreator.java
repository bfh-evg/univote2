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
import ch.bfh.uniboard.clientlib.GetHelper;
import ch.bfh.uniboard.clientlib.PostException;
import ch.bfh.uniboard.clientlib.PostHelper;
import ch.bfh.univote2.common.message.AccessRight;
import ch.bfh.univote2.common.message.DL;
import ch.bfh.univote2.common.message.JSONConverter;
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
		DSAPublicKey boardKey = (DSAPublicKey) ks.getCertificate("uniboardvote").getPublicKey();
		String posterPublicKey = pubKey.getY().toString(10);//MathUtil.pair(pubKey.getPublicExponent(), pubKey.getModulus()).toString(10);

		PostHelper ph = new PostHelper(pubKey, privKey, ks.getCertificate(
				"uniboardvote").getPublicKey(), uniBoardWSDLurl, uniBoardUrl);

		GetHelper gh = new GetHelper(boardKey, uniBoardWSDLurl, uniBoardUrl);

		try {
			AccessRight ar = new AccessRight();
			ar.setGroup("ballot");
			DL dl = new DL();
			dl.setP("89884656743115795386465259539451236680898848947115328636715040578866337902750481566354238661203768010560056939935696678829394884407208311246423715319737062188883946712432742638151109800623047059726541476042502884419075341171231440736956555270413618581675255529365358698328774708775703215219351545329613875969");
			dl.setG("43753966268956158683794141044609048074944399463497118601009260015278907944793396888872654797436679156171704835263342098747229841982963550871557447683404359446377648645751856913829280577934384831381295103182368037001170314531189658120206052644043469275562473160989451140877931368137440524162645073654512304068");
			dl.setQ("730750818665451459101842416358141509827966271787");
			dl.setPublickey("38333562822803284654251107179888850687472730410876297075954664522896398367696762403633508365472320458780540849623104497976371044656789206443124584193915996741899329321054843923972278293219456347480885351626222818868979798678665005203301984319700654533426046758044311417847935973236423339150329962770671291839");
			ar.setCrypto(dl);
			String message = JSONConverter.marshal(ar);
			ph.post(message, "sub-2015", "accessRight");
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
