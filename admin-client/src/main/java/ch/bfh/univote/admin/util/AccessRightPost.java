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
package ch.bfh.univote.admin.util;

import ch.bfh.uniboard.clientlib.PostHelper;
import ch.bfh.univote.admin.JsonConverter;
import ch.bfh.univote2.common.crypto.KeyUtil;
import ch.bfh.univote2.common.message.AccessRight;
import ch.bfh.univote2.common.message.Crypto;
import ch.bfh.univote2.common.message.DL;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

public class AccessRightPost {

	private static final String AUTHORIZED_CERTIFICATE = "ea-certificate.pem";
	private static final String[] AUTHORIZED_GROUPS = {
		"electionDefinition", "trustees", "securityLevel", "electionDetails", "electoralRoll"};

	private static final String UNIBOARD_ADDRESS = "http://urd.bfh.ch:10080/UniBoardService/UniBoardServiceImpl";
	private static final String UNIBOARD_SECTION = "sub-2015";
	private static final String UNIBOARD_GROUP = "accessRight";

	private static final String BOARD_CERTIFICATE = "board-certificate.pem";
	private static final String POSTER_CERTIFICATE = "ec-certificate.pem";
	private static final String POSTER_ENCRYPTED_PRIVATE_KEY = "ec-encrypted-private-key.pem";
	private static final String POSTER_PRIVATE_KEY_PASSWORD = "12345678";

	private static PostHelper postHelper;

	public static void main(String[] args) throws Exception {
		createPostHelper();
		for (String group : AUTHORIZED_GROUPS) {
			postAccessRight(group);
		}
	}

	private static void createPostHelper() throws Exception {
		DSAPublicKey boardPublicKey = KeyUtil.getDSAPublicKey(BOARD_CERTIFICATE);
		DSAPublicKey posterPublicKey = KeyUtil.getDSAPublicKey(POSTER_CERTIFICATE);
		DSAPrivateKey posterPrivateKey = KeyUtil.getDSAPrivateKey(
				POSTER_ENCRYPTED_PRIVATE_KEY, POSTER_PRIVATE_KEY_PASSWORD, posterPublicKey.getParams());
		postHelper = new PostHelper(
				posterPublicKey, posterPrivateKey, boardPublicKey, UNIBOARD_ADDRESS + "?wsdl", UNIBOARD_ADDRESS);
	}

	private static void postAccessRight(String group) throws Exception {
		DSAPublicKey authorizedPublicKey = KeyUtil.getDSAPublicKey(AUTHORIZED_CERTIFICATE);
		DSAParams params = authorizedPublicKey.getParams();
		Crypto crypto = new DL(params.getP().toString(), params.getQ().toString(), params.getG().toString(),
				authorizedPublicKey.getY().toString());
		AccessRight accessRight = new AccessRight();
		accessRight.setGroup(group);
		accessRight.setCrypto(crypto);
		String message = JsonConverter.marshal(accessRight);
		System.out.println("Message: " + message);
		postHelper.post(message, UNIBOARD_SECTION, UNIBOARD_GROUP);
		System.out.println("Post successful");
	}
}
