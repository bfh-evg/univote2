package ch.bfh.univote.admin;

import ch.bfh.uniboard.clientlib.signaturehelper.SchnorrSignatureHelper;
import ch.bfh.uniboard.data.AttributesDTO;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPrivateKeySpec;

public class KeyUtil {

	public static DSAPublicKey getDSAPublicKey(String certificatePath) throws Exception {
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate) factory.generateCertificate(
				new FileInputStream(certificatePath));
		return (DSAPublicKey) certificate.getPublicKey();
	}

	public static DSAPrivateKey getDSAPrivateKey(String keyPath, String password, DSAParams params) throws Exception {
		String encryptedKey = new String(Files.readAllBytes(Paths.get(keyPath)));
		byte[] decryptedKey = KeyEncryption.decryptPrivateKey(encryptedKey, password);
		DSAPrivateKeySpec keySpec = new DSAPrivateKeySpec(
				new BigInteger(decryptedKey), params.getP(), params.getQ(), params.getG());
		return (DSAPrivateKey) KeyFactory.getInstance("DSA").generatePrivate(keySpec);
	}

	public static boolean checkDSAKeys(DSAPublicKey publicKey, DSAPrivateKey privateKey) throws Exception {
		byte[] message = "Hello World!".getBytes();
		AttributesDTO alpha = new AttributesDTO();
		BigInteger signature = new SchnorrSignatureHelper(privateKey).sign(message, alpha);
		return new SchnorrSignatureHelper(publicKey).verify(message, alpha, signature);
	}

	public static void printDSAPublicKey(DSAPublicKey publicKey) {
		DSAParams params = publicKey.getParams();
		System.out.println("Modulo:      " + params.getP());
		System.out.println("Order:       " + params.getQ());
		System.out.println("Generator:   " + params.getG());
		System.out.println("Public Key:  " + publicKey.getY());
	}
}
