/*
 * Copyright (c) 2012 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniVote.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote.admin;

import ch.bfh.uniboard.clientlib.PostHelper;
import ch.bfh.univote2.common.crypto.KeyUtil;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.util.Scanner;

/**
 * User interface class for the UniVote Administration.
 *
 * @author Stephan Fischli &lt;stephan.fischli@bfh.ch&gt;
 */
public class AdminClient {

	private static final String UNIBOARD_ADDRESS = "http://urd.bfh.ch:10080/UniBoardService/UniBoardServiceImpl";
	private static final String UNIBOARD_SECTION = "sub-2015";

	private static final String BOARD_CERTIFICATE = "board-certificate.pem";
	private static final String POSTER_CERTIFICATE = "ea-certificate.pem";
	private static final String POSTER_ENCRYPTED_PRIVATE_KEY = "ea-encrypted-private-key.pem";
	private static final String POSTER_PRIVATE_KEY_PASSWORD = "12345678";

	private static final String MESSAGES_PATH = "json-examples";
	private static final String MESSAGES_ENCODING = "UTF-8";

	private static PostHelper postHelper;

	public static void main(String[] args) throws Exception {
		createPostHelper();
		runMenu();
	}

	private static void createPostHelper() throws Exception {
		DSAPublicKey boardPublicKey = KeyUtil.getDSAPublicKey(BOARD_CERTIFICATE);
		DSAPublicKey posterPublicKey = KeyUtil.getDSAPublicKey(POSTER_CERTIFICATE);
		DSAPrivateKey posterPrivateKey = KeyUtil.getDSAPrivateKey(
				POSTER_ENCRYPTED_PRIVATE_KEY, POSTER_PRIVATE_KEY_PASSWORD, posterPublicKey.getParams());
		postHelper = new PostHelper(
				posterPublicKey, posterPrivateKey, boardPublicKey, UNIBOARD_ADDRESS + "?wsdl", UNIBOARD_ADDRESS);
	}

	private static void runMenu() throws Exception {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println();
			System.out.println("1 - Post Election Definition");
			System.out.println("2 - Post Trustees");
			System.out.println("3 - Post Security Level");
			System.out.println("4 - Post Election Details");
			System.out.println("5 - Post Electoral Roll");
			System.out.println("6 - Exit");
			System.out.println();
			System.out.print("Your choice: ");
			String line = scanner.nextLine().trim();
			int choice = 0;
			try {
				choice = Integer.parseInt(line);
			} catch (NumberFormatException ex) {
			}
			System.out.println();
			if (choice == 1) {
				postMessage("electionDefinition");
			} else if (choice == 2) {
				postMessage("trustees");
			} else if (choice == 3) {
				postMessage("securityLevel");
			} else if (choice == 4) {
				postMessage("electionDetails");
			} else if (choice == 5) {
				postMessage("electoralRoll");
			} else if (choice == 6) {
				System.exit(0);
			} else {
				System.err.println("Invalid choice");
			}
		}
	}

	private static void postMessage(String group) throws Exception {
		String path = MESSAGES_PATH + "/" + UNIBOARD_SECTION + "/" + group + ".json";
		String message = new String(Files.readAllBytes(Paths.get(path)), MESSAGES_ENCODING);
		System.out.println("Message:\n" + message);
		postHelper.post(message, UNIBOARD_SECTION, group);
		System.out.println("Post successful");
	}
}
