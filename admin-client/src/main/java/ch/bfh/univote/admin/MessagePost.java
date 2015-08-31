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
package ch.bfh.univote.admin;

import ch.bfh.uniboard.clientlib.PostHelper;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

public class MessagePost {

	private static final String MESSAGES_PATH = "json-examples";
	private static final String MESSAGES_ENCODING = "UTF-8";

	private static final String BOARD_CERTIFICATE_PATH = "ub-certificate.pem";
	private static final String POSTER_CERTIFICATE_PATH = "ea-certificate.pem";
	private static final String POSTER_ENCRYPTED_PRIVATE_KEY_PATH = "ea-encrypted-private-key.pem";
	private static final String POSTER_PRIVATE_KEY_PASSWORD = "12345678";

	private static final String UNIBOARD_ADDRESS = "http://urd.bfh.ch:10080/UniBoardService/UniBoardServiceImpl";
	private static final String UNIBOARD_SECTION = "sub-2015";
	private static final String UNIBOARD_GROUP = "electionDefinition";

	public static void main(String[] args) throws Exception {
		DSAPublicKey boardPublicKey = KeyUtil.getDSAPublicKey(BOARD_CERTIFICATE_PATH);
		DSAPublicKey posterPublicKey = KeyUtil.getDSAPublicKey(POSTER_CERTIFICATE_PATH);
		KeyUtil.printDSAPublicKey(posterPublicKey);
		DSAPrivateKey posterPrivateKey = KeyUtil.getDSAPrivateKey(
				POSTER_ENCRYPTED_PRIVATE_KEY_PATH, POSTER_PRIVATE_KEY_PASSWORD, posterPublicKey.getParams());

		PostHelper postHelper = new PostHelper(
				posterPublicKey, posterPrivateKey, boardPublicKey, UNIBOARD_ADDRESS + "?wsdl", UNIBOARD_ADDRESS);

		String path = MESSAGES_PATH + "/" + UNIBOARD_SECTION + "/" + UNIBOARD_GROUP + ".json";
		String message = new String(Files.readAllBytes(Paths.get(path)), MESSAGES_ENCODING);
		System.out.println("Message:\n" + message);
		postHelper.post(message, UNIBOARD_SECTION, UNIBOARD_GROUP);
		System.out.println("Post successful");
	}
}
