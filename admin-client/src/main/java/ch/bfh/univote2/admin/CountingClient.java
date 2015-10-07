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

import ch.bfh.uniboard.clientlib.GetHelper;
import ch.bfh.uniboard.clientlib.UniBoardAttributesName;
import ch.bfh.uniboard.data.AlphaIdentifierDTO;
import ch.bfh.uniboard.data.ConstraintDTO;
import ch.bfh.uniboard.data.EqualDTO;
import ch.bfh.uniboard.data.OrderDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.unicrypt.crypto.encoder.classes.ZModPrimeToGStarModSafePrime;
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.AESEncryptionScheme;
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import ch.bfh.unicrypt.crypto.schemes.padding.classes.PKCSPaddingScheme;
import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.common.crypto.CryptoSetup;
import ch.bfh.univote2.common.crypto.KeyEncryption;
import ch.bfh.univote2.common.crypto.KeyUtil;
import ch.bfh.univote2.common.message.Ballot;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.JSONConverter;
import java.io.FileReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * User interface class for the UniVote Administration.
 *
 * @author Stephan Fischli &lt;stephan.fischli@bfh.ch&gt;
 */
public class CountingClient {

	private static final String CONFIG_FILE = "config.properties";
	private static final Scanner CONSOLE = new Scanner(System.in);
	private static final int AES_BLOCK_LENGTH = 16;

	private static Properties props;
	private static GetHelper getHelper;

	public static void main(String[] args) throws Exception {
		readConfiguration();
		createGetHelper();

		List<Ballot> ballots = getBallots();
		CryptoSetting cryptoSetting = getCryptoSetting();
		BigInteger decryptionKey = getDecryptionKey();

		List<BigInteger> decryptedVotes = decryptVotes(ballots, cryptoSetting, decryptionKey);
		decryptedVotes.forEach(System.out::println);
	}

	private static void readConfiguration() throws Exception {
		props = new Properties();
		props.load(new FileReader(CONFIG_FILE));
	}

	private static void createGetHelper() throws Exception {
		DSAPublicKey boardPublicKey = KeyUtil.getDSAPublicKey(props.getProperty("uniboard.certificate.path"));
		getHelper = new GetHelper(boardPublicKey,
				props.getProperty("uniboard.wsdl.url"), props.getProperty("uniboard.endpoint.address"));
	}

	private static List<Ballot> getBallots() throws Exception {
		QueryDTO query = createQuery(props.getProperty("election.id"), "ballot");
		ResultContainerDTO resultContainer = getHelper.get(query);
		List<Ballot> ballots = new ArrayList<>();
		for (PostDTO post : resultContainer.getResult().getPost()) {
			ballots.add(JSONConverter.unmarshal(Ballot.class, post.getMessage()));
		}
		return ballots;
	}

	private static CryptoSetting getCryptoSetting() throws Exception {
		QueryDTO query = createQuery(props.getProperty("election.id"), "cryptoSetting");
		ResultContainerDTO resultContainer = getHelper.get(query);
		PostDTO post = resultContainer.getResult().getPost().get(0);
		return JSONConverter.unmarshal(CryptoSetting.class, post.getMessage());
	}

	private static BigInteger getDecryptionKey() throws Exception {

		// get derived key from the salt of the encrypted private key and the password
		String encryptedPrivateKey = new String(Files.readAllBytes(
				Paths.get(props.getProperty("trustee.encrypted.private.key.path"))));
		System.out.print("Private key password: ");
		String privateKeyPassword = CONSOLE.nextLine();
		byte[] derivedKey = KeyEncryption.getDerivedKey(encryptedPrivateKey, privateKeyPassword);

		// use derived key to decrypt the private key share
		AESEncryptionScheme aes = AESEncryptionScheme.getInstance();
		Element derivedKeyElement = aes.getEncryptionKeySpace().getElement(ByteArray.getInstance(derivedKey));
		System.out.print("Encrypted private key share: ");
		String privateKeyShare = CONSOLE.nextLine();
		Element encBigInt = aes.getMessageSpace().getElementFrom(new BigInteger(privateKeyShare));
		Element decrypted = aes.decrypt(derivedKeyElement, encBigInt);
		return PKCSPaddingScheme.getInstance(AES_BLOCK_LENGTH).unpad(decrypted).convertToBigInteger();
	}

	private static List<BigInteger> decryptVotes(
			List<Ballot> ballots, CryptoSetting cryptoSetting, BigInteger decryptionKey) throws Exception {

		// convert decryption key into group element
		CryptoSetup cryptoSetup = CryptoProvider.getEncryptionSetup(cryptoSetting.getEncryptionSetting());
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(cryptoSetup.cryptoGenerator);
		ZModElement decryptionKeyElement
				= elGamal.getKeyPairGenerator().getPrivateKeySpace().getElementFrom(decryptionKey);

		// get converter to decode the decrypted votes
		ZModPrimeToGStarModSafePrime converter
				= ZModPrimeToGStarModSafePrime.getInstance((GStarModSafePrime) cryptoSetup.cryptoGroup);

		List<BigInteger> votes = new ArrayList<>();
		for (Ballot ballot : ballots) {
			// convert encrypted vote to a pair of group element
			Pair encryptedVote = Pair.getInstance(
					cryptoSetup.cryptoGroup.getElementFrom(new BigInteger(ballot.getEncryptedVote().getFirstValue())),
					cryptoSetup.cryptoGroup.getElementFrom(new BigInteger(ballot.getEncryptedVote().getSecondValue())));

			// decrypt and convert votes
			Element decryptedVote = elGamal.decrypt(decryptionKeyElement, encryptedVote);
			votes.add(converter.decode(decryptedVote).getValue());
		}
		return votes;
	}

	private static QueryDTO createQuery(String section, String group) {
		AlphaIdentifierDTO sectionIdentifier
				= new AlphaIdentifierDTO(Collections.singletonList(UniBoardAttributesName.SECTION.getName()));
		AlphaIdentifierDTO groupIdentifier
				= new AlphaIdentifierDTO(Collections.singletonList(UniBoardAttributesName.GROUP.getName()));
		List<ConstraintDTO> contraints = new ArrayList<>();
		contraints.add(new EqualDTO(sectionIdentifier, new StringValueDTO(section)));
		contraints.add(new EqualDTO(groupIdentifier, new StringValueDTO(group)));
		List<OrderDTO> orders = new ArrayList<>();
		orders.add(new OrderDTO(groupIdentifier, true));
		return new QueryDTO(contraints, orders, 0);
	}
}
