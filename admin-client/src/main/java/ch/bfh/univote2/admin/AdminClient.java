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
package ch.bfh.univote2.admin;

import ch.bfh.uniboard.clientlib.PostHelper;
import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.StringToByteArray;
import ch.bfh.unicrypt.helper.converter.classes.string.ByteArrayToString;
import ch.bfh.unicrypt.helper.converter.interfaces.Converter;
import ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import ch.bfh.univote2.admin.message.CandidateElection;
import ch.bfh.univote2.admin.message.ElectionDetails;
import ch.bfh.univote2.admin.message.ElectionIssue;
import ch.bfh.univote2.admin.message.ListElection;
import ch.bfh.univote2.admin.message.Vote;
import ch.bfh.univote2.common.crypto.KeyUtil;
import ch.bfh.univote2.common.message.ElectionDefinition;
import ch.bfh.univote2.common.message.ElectoralRoll;
import ch.bfh.univote2.common.message.I18nText;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.SecurityLevel;
import ch.bfh.univote2.common.message.Trustees;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * User interface class for the UniVote Administration.
 *
 * @author Stephan Fischli &lt;stephan.fischli@bfh.ch&gt;
 */
public class AdminClient {

	private static final String CONFIG_FILE = "config.properties";
	private static final String INDENT = "    ";

	private static final Scanner CONSOLE = new Scanner(System.in);
	private static final HashAlgorithm HASH_ALGORITHM = HashAlgorithm.SHA256;
	private static final Converter<String, ByteArray> STRING_TO_BYTEARRAY = StringToByteArray.getInstance(Charset.forName("UTF-8"));
	private static final Converter<ByteArray, String> BYREARRAY_TO_STRING = ByteArrayToString.getInstance(ByteArrayToString.Radix.HEX);

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
		String privateKeyPassword = CONSOLE.nextLine();
		DSAPrivateKey posterPrivateKey = KeyUtil.getDSAPrivateKey(
				props.getProperty("admin.encrypted.private.key.path"), privateKeyPassword, posterPublicKey.getParams());
		postHelper = new PostHelper(
				posterPublicKey, posterPrivateKey, boardPublicKey,
				props.getProperty("uniboard.wsdl.url"), props.getProperty("uniboard.endpoint.address"));
	}

	private static void runMenu() throws Exception {
		while (true) {
			System.out.println();
			System.out.println("1 - Post Election Definition");
			System.out.println("2 - Post Trustees");
			System.out.println("3 - Post Security Level");
			System.out.println("4 - Post Election Details");
			System.out.println("5 - Post Electoral Roll");
			System.out.println("6 - Exit");
			System.out.println();
			System.out.print("Choice: ");
			String line = CONSOLE.nextLine().trim();
			int choice = 0;
			try {
				choice = Integer.parseInt(line);
			} catch (NumberFormatException ex) {
			}
			System.out.println();
			if (choice == 1) {
				postElectionDefinition();
			} else if (choice == 2) {
				postTrustees();
			} else if (choice == 3) {
				postSecurityLevel();
			} else if (choice == 4) {
				postElectionDetails();
			} else if (choice == 5) {
				postElectoralRoll();
			} else if (choice == 6) {
				System.exit(0);
			} else {
				System.err.println("Invalid choice");
			}
		}
	}

	private static void postElectionDefinition() throws Exception {
		Path path = Paths.get(props.getProperty("message.directory"), "electionDefinition.json");
		String message = new String(Files.readAllBytes(path), "UTF-8");
		ElectionDefinition electionDefinition = JSONConverter.unmarshal(ElectionDefinition.class, message);
		printI18nText("Election Title", electionDefinition.getTitle());
		printI18nText("Election Administration", electionDefinition.getAdministration());
		printValue("Voting Period Begin", electionDefinition.getVotingPeriodBegin());
		printValue("Voting Period End", electionDefinition.getVotingPeriodEnd());
		postMessage("electionDefinition", message);
	}

	private static void postTrustees() throws Exception {
		Path path = Paths.get(props.getProperty("message.directory"), "trustees.json");
		String message = new String(Files.readAllBytes(path), "UTF-8");
		Trustees trustees = JSONConverter.unmarshal(Trustees.class, message);
		printValues("Mixers", trustees.getMixerIds());
		printValues("Talliers", trustees.getMixerIds());
		postMessage("trustees", message);
	}

	private static void postSecurityLevel() throws Exception {
		Path path = Paths.get(props.getProperty("message.directory"), "securityLevel.json");
		String message = new String(Files.readAllBytes(path), "UTF-8");
		SecurityLevel securityLevel = JSONConverter.unmarshal(SecurityLevel.class, message);
		printValue("Security Level", securityLevel.getSecurityLevel());
		postMessage("securityLevel", message);
	}

	private static void postElectionDetails() throws Exception {
		Path path = Paths.get(props.getProperty("message.directory"), "electionDetails.json");
		String message = new String(Files.readAllBytes(path), "UTF-8");
		ElectionDetails electionDetails = JSONConverter.unmarshal(ElectionDetails.class, message);
		for (ElectionIssue electionIssue : electionDetails.getIssues()) {
			String issueType = null;
			if (electionIssue instanceof CandidateElection) {
				issueType = "Candidate Election";
			} else if (electionIssue instanceof ListElection) {
				issueType = "List Election";
			} else if (electionIssue instanceof Vote) {
				issueType = "Vote";
			}
			printI18nText(issueType, electionIssue.getTitle());
			if (electionIssue instanceof Vote) {
				printI18nText("Question", electionIssue.getQuestion());
			}
		}
		postMessage("electionDetails", message);
	}

	private static void postElectoralRoll() throws Exception {
		ElectoralRoll electoralRoll = new ElectoralRoll();
		electoralRoll.setVoterIds(new ArrayList<>());
		List<String> voterIds = Files.readAllLines(Paths.get(props.getProperty("electoral.roll.path")));
		for (String voterId : voterIds) {
			voterId = voterId.toLowerCase().trim();
			if (voterId.isEmpty()) {
				continue;
			}
			electoralRoll.getVoterIds().add(hashVoterId(voterId));
		}
		String message = JSONConverter.marshal(electoralRoll);
		printValue("Electoral Roll", electoralRoll.getVoterIds().size() + " entries");
		postMessage("electoralRoll", message);
	}

	private static String hashVoterId(String voterId) {
		return BYREARRAY_TO_STRING.convert(HASH_ALGORITHM.getHashValue(STRING_TO_BYTEARRAY.convert(voterId)));
	}

	private static void postMessage(String group, String message) throws Exception {
		System.out.print("Is this correct (yes/no)? ");
		String answer = CONSOLE.nextLine();
		if (answer.equals("yes")) {
			postHelper.post(message, props.getProperty("election.id"), group);
			System.out.println("Message successfully posted");
		}
	}

	private static void printI18nText(String title, I18nText text) {
		System.out.println(title);
		System.out.println(INDENT + "default: " + text.getDefault());
		if (text.getDe() != null) {
			System.out.println(INDENT + "de: " + text.getDe());
		}
		if (text.getFr() != null) {
			System.out.println(INDENT + "fr: " + text.getFr());
		}
		if (text.getIt() != null) {
			System.out.println(INDENT + "it: " + text.getIt());
		}
		if (text.getEn() != null) {
			System.out.println(INDENT + "en: " + text.getEn());
		}
	}

	private static void printValues(String title, List values) {
		System.out.println(title);
		for (Object value : values) {
			System.out.println(INDENT + value);
		}
	}

	private static void printValue(String title, Object value) {
		System.out.println(title);
		System.out.println(INDENT + value);
	}
}
