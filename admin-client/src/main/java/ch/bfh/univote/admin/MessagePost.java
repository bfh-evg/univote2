package ch.bfh.univote.admin;

import ch.bfh.uniboard.clientlib.KeyHelper;
import ch.bfh.uniboard.clientlib.PostHelper;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.util.Scanner;

public class MessagePost {

	private static final String MESSAGES_PATH = "json-examples";

	private static final String BOARD_CERTIFICATE_PATH = "ub-certificate.pem";
	private static final String CERTIFICATE_PATH = "ea-certificate.pem";
	private static final String ENCRYPTED_PRIVATE_KEY_PATH = "ea-encrypted-private-key.pem";
	private static final String ENCRYPTED_PRIVATE_KEY_PASSWORD = "12345678";

	private static final String UNIBOARD_ENDPOINT_ADDRESS = "http://urd.bfh.ch:10080/UniBoardService/UniBoardServiceImpl";
	private static final String UNIBOARD_WSDL_LOCATION = "http://urd.bfh.ch:10080/UniBoardService/UniBoardServiceImpl?wsdl";
	private static final String UNIBOARD_SECTION = "sub-2015";
	private static final String UNIBOARD_GROUP = "electionDefinition";

	public static void main(String[] args) throws Exception {
		PublicKey boardPublicKey = getPublicKey(BOARD_CERTIFICATE_PATH);
		DSAPublicKey publicKey = (DSAPublicKey) getPublicKey(CERTIFICATE_PATH);
		System.out.println("Public Key:  " + publicKey.getY());
		DSAPrivateKey privateKey = getDSAPrivateKey(publicKey.getParams());
		System.out.println("Private Key: " + privateKey.getX());
		PostHelper postHelper = new PostHelper(publicKey, privateKey, boardPublicKey, UNIBOARD_WSDL_LOCATION, UNIBOARD_ENDPOINT_ADDRESS);
		String message = readMessage();
		System.out.println("Message:     " + message);
		postHelper.post(message, UNIBOARD_SECTION, UNIBOARD_GROUP);
		System.out.println("Post successful");
	}

	private static PublicKey getPublicKey(String path) throws Exception {
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate) factory.generateCertificate(new FileInputStream(path));
		return certificate.getPublicKey();
	}

	private static DSAPrivateKey getDSAPrivateKey(DSAParams params) throws Exception {
		StringBuilder data = new StringBuilder();
		try (Scanner scanner = new Scanner(new FileInputStream(ENCRYPTED_PRIVATE_KEY_PATH))) {
			while (scanner.hasNextLine()) {
				data.append(scanner.nextLine());
			}
		}
		BigInteger decryptedPrivateKey = new BigInteger(KeyHelper.decryptPrivateKey(ENCRYPTED_PRIVATE_KEY_PASSWORD, data.toString()));
		return KeyHelper.createDSAPrivateKey(params.getP(), params.getQ(), params.getG(), decryptedPrivateKey);
	}

	private static String readMessage() throws Exception {
		StringBuilder message = new StringBuilder();
		Scanner scanner = new Scanner(new FileInputStream(MESSAGES_PATH + "/" + UNIBOARD_SECTION + "/" + UNIBOARD_GROUP + ".json"), "UTF-8");
		while (scanner.hasNextLine()) {
			message.append(scanner.nextLine().trim());
		}
		return message.toString();
	}
}
