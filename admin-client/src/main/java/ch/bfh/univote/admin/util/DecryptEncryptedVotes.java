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
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.univote.admin.JsonConverter;
import ch.bfh.univote.admin.message.Vote;
import ch.bfh.univote.admin.util.MessageGet;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.common.crypto.CryptoSetup;
import ch.bfh.univote2.common.crypto.KeyUtil;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.EncryptedVote;
import java.io.FileReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class DecryptEncryptedVotes {

    private static final String CONFIG_FILE = "config.properties";

    private static final String UNIBOARD_SECTION = "test-2015";
    private static final String UNIBOARD_GROUP = "ballot";

    private static Properties props;

    private static void readConfiguration() throws Exception {
	props = new Properties();
	props.load(new FileReader(CONFIG_FILE));
    }

    public List<EncryptedVote> getEncryptedVotes() throws Exception {
	List<EncryptedVote> encryptedVotes = new ArrayList<>();
	DSAPublicKey boardPublicKey = KeyUtil.getDSAPublicKey(props.getProperty("uniboard.certificate.path"));
	QueryDTO query = MessageGet.createQuery(UNIBOARD_SECTION, UNIBOARD_GROUP);
	GetHelper getHelper = new GetHelper(boardPublicKey, props.getProperty("uniboard.wsdl.url"), props.getProperty("uniboard.endpoint.address"));

	ResultContainerDTO resultContainer = getHelper.get(query);
	System.out.println("Get successful");
	for (PostDTO post : resultContainer.getResult().getPost()) {
	    encryptedVotes.add(JsonConverter.unmarshal(EncryptedVote.class, new String(post.getMessage(), StandardCharsets.UTF_8)));
	}
	return encryptedVotes;
    }

    public CryptoSetting getCryptoSettings() throws Exception {
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

    public List<Vote> decryptVotes(List<EncryptedVote> encryptedVotes, CryptoSetting cryptoSetting, BigInteger decryptionKeyAsBigInt) throws Exception {
	List<Vote> votes = new ArrayList<>();

	CryptoSetup cSetup = CryptoProvider.getEncryptionSetup(cryptoSetting.getEncryptionSetting());

	CyclicGroup group = cSetup.cryptoGroup;
	Element generator = cSetup.cryptoGenerator;
	ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(generator);
	Element decryptionKey = group.getElementFrom(decryptionKeyAsBigInt);

	for (EncryptedVote encryptedVote : encryptedVotes) {
	    Pair encryption = Pair.getInstance(group.getElementFrom(encryptedVote.getFirstValue()), group.getElementFrom(encryptedVote.getFirstValue()));
	    Element decryptedVoteElement = elGamal.decrypt(decryptionKey, encryption);
	    Vote vote = JsonConverter.unmarshal(Vote.class, decryptedVoteElement.toString());
	    votes.add(vote);
	}
	return votes;
    }

}
