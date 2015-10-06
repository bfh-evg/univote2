/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote.admin.util;

import ch.bfh.uniboard.clientlib.GetHelper;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.unicrypt.crypto.encoder.classes.ZModPrimeToGStarModSafePrime;
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.AESEncryptionScheme;
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import ch.bfh.unicrypt.crypto.schemes.padding.classes.PKCSPaddingScheme;
import ch.bfh.unicrypt.crypto.schemes.padding.interfaces.ReversiblePaddingScheme;
import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import ch.bfh.univote.admin.JsonConverter;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.common.crypto.CryptoSetup;
import ch.bfh.univote2.common.crypto.KeyEncryption;
import ch.bfh.univote2.common.crypto.KeyUtil;
import ch.bfh.univote2.common.message.Ballot;
import ch.bfh.univote2.common.message.CryptoSetting;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class DecryptEncryptedVotes {

    private static final String CONFIG_FILE = "config.properties";

    private static final String UNIBOARD_SECTION = "sub-2015";
    private static final String UNIBOARD_GROUP = "ballot";
    private static final String BOARD_CERTIFICATE_PATH = "board-certificate.pem";
    private static final String CERTIFICATE_PATH = "ea-certificate.pem";

    private static final String UNIBOARD_ADDRESS = "http://urd.bfh.ch:10080/UniBoardService/UniBoardServiceImpl";

    private static Properties props;

    private static void readConfiguration() throws Exception {
	props = new Properties();
	//props.load(new FileReader(CONFIG_FILE));
	props.setProperty("uniboard.certificate.path", CERTIFICATE_PATH);
	props.setProperty("uniboard.wsdl.url", UNIBOARD_ADDRESS + "?wsdl");
	props.setProperty("uniboard.endpoint.address", UNIBOARD_ADDRESS);
    }

    public static List<Ballot> getEncryptedBallots() throws Exception {
	List<Ballot> encryptedBallots = new ArrayList<>();
	DSAPublicKey boardPublicKey = KeyUtil.getDSAPublicKey(props.getProperty("uniboard.certificate.path"));
	QueryDTO query = MessageGet.createQuery(UNIBOARD_SECTION, UNIBOARD_GROUP);
	GetHelper getHelper = new GetHelper(boardPublicKey, props.getProperty("uniboard.wsdl.url"), props.getProperty("uniboard.endpoint.address"));

	ResultContainerDTO resultContainer = getHelper.get(query);
	System.out.println("Get successful");
	for (PostDTO post : resultContainer.getResult().getPost()) {
	    encryptedBallots.add(JsonConverter.unmarshal(Ballot.class, new String(post.getMessage(), StandardCharsets.UTF_8)));
	}
	return encryptedBallots;
    }

    public static CryptoSetting getCryptoSettings() throws Exception {
	CryptoSetting cryptoSetting = null;
	DSAPublicKey boardPublicKey = KeyUtil.getDSAPublicKey(props.getProperty("uniboard.certificate.path"));
	QueryDTO query = MessageGet.createQuery(UNIBOARD_SECTION, UNIBOARD_GROUP);
	GetHelper getHelper = new GetHelper(boardPublicKey, props.getProperty("uniboard.wsdl.url"), props.getProperty("uniboard.endpoint.address"));

	ResultContainerDTO resultContainer = getHelper.get(query);
	System.out.println("Get successful");
	for (PostDTO post : resultContainer.getResult().getPost()) {
	    cryptoSetting = JsonConverter.unmarshal(CryptoSetting.class, new String(post.getMessage(), StandardCharsets.UTF_8));
	}
	return cryptoSetting;
    }

    public static List<BigInteger> decryptBallots(List<Ballot> encryptedBallots, CryptoSetting cryptoSetting, BigInteger decryptionKeyAsBigInt) throws Exception {
	List<BigInteger> votes = new ArrayList<>();

	CryptoSetup cSetup = CryptoProvider.getEncryptionSetup("RC1e");//cryptoSetting.getEncryptionSetting());

	CyclicGroup group = cSetup.cryptoGroup;
	Element generator = cSetup.cryptoGenerator;
	ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(generator);
	ZModElement decryptionKey = elGamal.getKeyPairGenerator().getPrivateKeySpace().getElementFrom(decryptionKeyAsBigInt);
	for (Ballot encryptedBallot : encryptedBallots) {
	    Element left = group.getElementFrom(new BigInteger(encryptedBallot.getEncryptedVote().getFirstValue()));
	    Element right = group.getElementFrom(new BigInteger(encryptedBallot.getEncryptedVote().getSecondValue()));
	    if (left == null || right == null) {
		System.out.printf("At least one part of the encrypted vote is no element of the group (%s):\n (%s,%s)", group, left, right);
	    }
	    Pair encryption = Pair.getInstance(left, right);
	    Element decryptedVote = elGamal.decrypt(decryptionKey, encryption);
	    ZModElement encodedVote = ZModPrimeToGStarModSafePrime.getInstance((GStarModSafePrime) group).decode(decryptedVote);
	    votes.add(encodedVote.getValue());
	}
	return votes;
    }

    ////////////////////////////////////////
    private static final String ENC_PRIVATE_KEY_PREFIX = "-----BEGIN ENCRYPTED UNICERT KEY-----";
    private static final String ENC_PRIVATE_KEY_POSTFIX = "-----END ENCRYPTED UNICERT KEY-----";
    private static final String RANDOM_ALGORITHM = "SHA1PRNG";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String TRANSFORM_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 128;
    private static final int ITERATIONS = 1000;

    public static byte[] getAESKey(String privateKey, String password) throws Exception {
	String toDecrypt = privateKey.replace(ENC_PRIVATE_KEY_PREFIX, "");
	toDecrypt = toDecrypt.replace(ENC_PRIVATE_KEY_POSTFIX, "");
	toDecrypt = toDecrypt.replaceAll("\n", "");
	toDecrypt = toDecrypt.trim();

	byte[] salt = DatatypeConverter.parseBase64Binary(toDecrypt.substring(0, 24));
	PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE);
	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
	SecretKey secretKey = keyFactory.generateSecret(keySpec);

	return secretKey.getEncoded();
    }
    /////////////////////////////////////////

    public static void main(String[] args) throws Exception {
	String encryptedPrivateKey = "";

	String password = "";
	String encryptionKeyShare = "";

	byte[] privateKey = KeyEncryption.decryptPrivateKey(encryptedPrivateKey, password);

	AESEncryptionScheme aes = AESEncryptionScheme.getInstance();
	ByteArray aesKeyBa = ByteArray.getInstance(getAESKey(encryptedPrivateKey, password));
	Element aesKey = aes.getEncryptionKeySpace().getElement(aesKeyBa);
	Element encBigInt = aes.getMessageSpace().getElementFrom(new BigInteger(encryptionKeyShare));
	Element bigInt = aes.decrypt(aesKey, encBigInt);
	ReversiblePaddingScheme pkcs = PKCSPaddingScheme.getInstance(16);
	Element unpaddedBigInt = pkcs.unpad(bigInt);
	BigInteger decryptionKey = unpaddedBigInt.convertToBigInteger();

	readConfiguration();
	System.out.println(Arrays.toString(getEncryptedBallots().toArray()));
	CryptoSetting cryptoSetting = getCryptoSettings();
	List<Ballot> encryptedBallots = getEncryptedBallots();
	List<BigInteger> decrytedVotes = decryptBallots(encryptedBallots, cryptoSetting, decryptionKey);
	for (BigInteger vote : decrytedVotes) {
	    System.out.println(vote);
	}
    }
}
