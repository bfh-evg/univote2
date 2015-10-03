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
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.util.Properties;
import java.util.Scanner;

/**
 * User interface class for the UniVote Administration.
 *
 * @author Stephan Fischli &lt;stephan.fischli@bfh.ch&gt;
 */
public class AdminClient {

	private static final String CONFIG_FILE = "config.properties";
	private static Properties props;
	private static PostHelper postHelper;

	public static void main(String[] args) throws Exception {
		readConfiguration();
		createPostHelper();
		runMenu();
	}

	private static void readConfiguration() throws Exception {
		props = new Properties();
		props.load(new FileReader(CONFIG_FILE));
	}

	private static void createPostHelper() throws Exception {
		DSAPublicKey boardPublicKey = KeyUtil.getDSAPublicKey(props.getProperty("uniboard.certificate.path"));
		DSAPublicKey posterPublicKey = KeyUtil.getDSAPublicKey(props.getProperty("admin.certificate.path"));
		System.out.print("Private key password: ");
		String privateKeyPassword = new Scanner(System.in).nextLine();
		DSAPrivateKey posterPrivateKey = KeyUtil.getDSAPrivateKey(
				props.getProperty("admin.encrypted.private.key.path"), privateKeyPassword, posterPublicKey.getParams());
		postHelper = new PostHelper(
				posterPublicKey, posterPrivateKey, boardPublicKey,
				props.getProperty("uniboard.wsdl.url"), props.getProperty("uniboard.endpoint.address"));
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
		Path path = Paths.get(props.getProperty("message.directory"), group + ".json");
		String message = new String(Files.readAllBytes(path), props.getProperty("message.encoding"));
		System.out.println("Message:\n" + message);
//		postHelper.post(message, UNIBOARD_SECTION, group);
//		System.out.println("Post successful");
	}
}
