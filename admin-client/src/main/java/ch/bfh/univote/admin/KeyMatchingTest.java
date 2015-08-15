package ch.bfh.univote.admin;

import ch.bfh.uniboard.clientlib.KeyHelper;
import ch.bfh.uniboard.clientlib.signaturehelper.SchnorrSignatureHelper;
import ch.bfh.uniboard.data.AttributesDTO;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.util.Scanner;

public class KeyMatchingTest {

	private static final String CERTIFICATE_PATH = "ea-certificate.pem";
	private static final String ENCRYPTED_PRIVATE_KEY_PATH = "ea-encrypted-private-key.pem";
	private static final String ENCRYPTED_PRIVATE_KEY_PASSWORD = "12345678";

	public static void main(String[] args) throws Exception {
		DSAPublicKey publicKey = getDSAPublicKey();
		DSAPrivateKey privateKey = getDSAPrivateKey(publicKey.getParams());
		printDSAKeys(publicKey, privateKey);
		boolean match = checkDSAKeys(publicKey, privateKey);
		System.out.println("Keys do " + (match ? "" : "not ") + "match!");
	}

	private static DSAPublicKey getDSAPublicKey() throws Exception {
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate) factory.generateCertificate(new FileInputStream(CERTIFICATE_PATH));
		return (DSAPublicKey) certificate.getPublicKey();
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

	private static void printDSAKeys(DSAPublicKey publicKey, DSAPrivateKey privateKey) {
		DSAParams params = publicKey.getParams();
		System.out.println("Modulo:      " + params.getP());
		System.out.println("Order:       " + params.getQ());
		System.out.println("Generator:   " + params.getG());
		System.out.println("Public Key:  " + publicKey.getY());
		System.out.println("Private Key: " + privateKey.getX());
		System.out.println();
	}

	private static boolean checkDSAKeys(DSAPublicKey publicKey, DSAPrivateKey privateKey) throws Exception {
		byte[] message = "Hello World".getBytes();
		AttributesDTO alpha = new AttributesDTO();
		BigInteger signature = new SchnorrSignatureHelper(privateKey).sign(message, alpha);
		return new SchnorrSignatureHelper(publicKey).verify(message, alpha, signature);
	}
}
