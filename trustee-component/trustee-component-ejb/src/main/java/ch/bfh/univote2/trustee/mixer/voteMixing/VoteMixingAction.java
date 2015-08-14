/*
 * UniVote2
 *
 *  UniVote2(tm): An Internet-based, verifiable e-voting system for student elections in Switzerland
 *  Copyright (c) 2015 Bern University of Applied Sciences (BFH),
 *  Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *  Quellgasse 21, CH-2501 Biel, Switzerland
 *
 *  Licensed under Dual License consisting of:
 *  1. GNU Affero General Public License (AGPL) v3
 *  and
 *  2. Commercial license
 *
 *
 *  1. This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  2. Licensees holding valid commercial licenses for UniVote2 may use this file in
 *   accordance with the commercial license agreement provided with the
 *   Software or, alternatively, in accordance with the terms contained in
 *   a written agreement between you and Bern University of Applied Sciences (BFH),
 *   Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *   Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 *   For further information contact <e-mail: univote@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
 */
package ch.bfh.univote2.trustee.mixer.voteMixing;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.Transformer;
import ch.bfh.uniboard.service.Attributes;
import ch.bfh.uniboard.service.StringValue;
import ch.bfh.unicrypt.crypto.keygenerator.interfaces.KeyPairGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.FiatShamirSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.classes.PlainPreimageProofSystem;
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import ch.bfh.unicrypt.helper.converter.classes.ConvertMethod;
import ch.bfh.unicrypt.helper.converter.classes.biginteger.ByteArrayToBigInteger;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.BigIntegerToByteArray;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.StringToByteArray;
import ch.bfh.unicrypt.helper.converter.interfaces.Converter;
import ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import ch.bfh.unicrypt.helper.hash.HashMethod;
import ch.bfh.unicrypt.helper.math.Alphabet;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringElement;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.message.CryptoSetting;
import ch.bfh.univote2.component.core.message.EncryptionKeyShare;
import ch.bfh.univote2.component.core.message.JSONConverter;
import ch.bfh.univote2.component.core.message.Proof;
import ch.bfh.univote2.component.core.query.AlphaEnum;
import ch.bfh.univote2.component.core.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.SecurePersistenceService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.trustee.BoardsEnum;
import ch.bfh.univote2.trustee.QueryFactory;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.JsonException;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class VoteMixingAction extends AbstractAction implements NotifiableAction {

    private static final String ACTION_NAME = VoteMixingAction.class.getSimpleName();
    private static final String PERSISTENCE_NAME_FOR_SECRET_KEY_FOR_KEY_SHARE = "SECRET_KEY_FOR_KEY_SHARE";

    private static final Logger logger = Logger.getLogger(VoteMixingAction.class.getName());

    @EJB
    ActionManager actionManager;
    @EJB
    TenantManager tenantManager;
    @EJB
    InformationService informationService;
    @EJB
    private UniboardService uniboardService;
    @EJB
    private SecurePersistenceService securePersistenceService;

    @Override
    protected ActionContext createContext(String tenant, String section) {
	ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
	return new VoteMixingActionContext(ack);
    }

    @Override
    protected boolean checkPostCondition(ActionContext actionContext) {
	if (!(actionContext instanceof VoteMixingActionContext)) {
	    logger.log(Level.SEVERE, "The actionContext was not the expected one.");
	    return false;
	}
	VoteMixingActionContext vmac = (VoteMixingActionContext) actionContext;
	try {
	    PublicKey publicKey = tenantManager.getPublicKey(actionContext.getTenant());
	    ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
								 QueryFactory.getQueryForEncryptionKeyShare(actionContext.getSection(), publicKey));
	    if (!result.getResult().getPost().isEmpty()) {
		return true;
	    }
	} catch (UnivoteException ex) {
	    logger.log(Level.WARNING, "Could not request encryption key share.", ex);
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Could not check post condition.");
	    return false;
	}
	return false;
    }

    @Override
    protected void definePreconditions(ActionContext actionContext) {
	ActionContextKey actionContextKey = actionContext.getActionContextKey();
	String section = actionContext.getSection();
	if (!(actionContext instanceof VoteMixingActionContext)) {
	    logger.log(Level.SEVERE, "The actionContext was not the expected one.");
	    return;
	}
	VoteMixingActionContext skcac = (VoteMixingActionContext) actionContext;
	try {
	    CryptoSetting cryptoSetting = skcac.getCryptoSetting();
	    if (cryptoSetting == null) {
		cryptoSetting = this.retrieveCryptoSetting(skcac);
		skcac.setCryptoSetting(cryptoSetting);
	    }

	} catch (UnivoteException ex) {
	    //Add Notification
	    BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
		    QueryFactory.getQueryForCryptoSetting(section), BoardsEnum.UNIVOTE.getValue());
	    actionContext.getPreconditionQueries().add(bQuery);
	    logger.log(Level.WARNING, "Could not get securitySetting.", ex);
	    this.informationService.informTenant(actionContextKey,
						 "Error retrieving securitySetting: " + ex.getMessage());
	} catch (JsonException ex) {
	    logger.log(Level.WARNING, "Could not parse securitySetting.", ex);
	    this.informationService.informTenant(actionContextKey,
						 "Error reading securitySetting.");
	} catch (Exception ex) {
	    logger.log(Level.WARNING, "Could not parse securitySetting.", ex);
	    this.informationService.informTenant(actionContextKey,
						 "Error reading securitySetting.");
	}
	BoardPreconditionQuery bQuery = null;
	try {
	    //Check if there is an initial AccessRight for this tenant
	    //TODO: Check if there is an actual valid access right... Right now it is only checking if there is any access right.
	    String tenant = actionContext.getTenant();
	    PublicKey publicKey = tenantManager.getPublicKey(tenant);
	    bQuery = new BoardPreconditionQuery(QueryFactory.getQueryForAccessRight(section, publicKey, GroupEnum.TRUSTEES), BoardsEnum.UNIVOTE.getValue());
	    skcac.setAccessRightGranted(uniboardService.get(bQuery.getBoard(), bQuery.getQuery()).getResult().getPost().isEmpty());
	} catch (UnivoteException ex) {
	    //Add Notification
	    actionContext.getPreconditionQueries().add(bQuery);
	}

    }

    @Override
    @Asynchronous
    public void run(ActionContext actionContext) {
	if (!(actionContext instanceof VoteMixingActionContext)) {
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	    return;
	}
	VoteMixingActionContext skcac = (VoteMixingActionContext) actionContext;
	//The following if is strange, as the run should not happen in this case?!
	if (skcac.isPreconditionReached() == null) {
	    logger.log(Level.WARNING, "Run was called but preCondition is unknown in Context.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);

	    return;
	}
	if (Objects.equals(skcac.isPreconditionReached(), Boolean.FALSE)) {
	    logger.log(Level.WARNING, "Run was called but preCondition is not yet reached.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	    return;
	}
	String tenant = actionContext.getTenant();
	String section = actionContext.getSection();

	CryptoSetting cryptoSetting = skcac.getCryptoSetting();
	if (cryptoSetting == null) {
	    logger.log(Level.SEVERE, "Precondition is reached but crypto setting is empty in Context. That is bad.");
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Error: Precondition reached, but no crypto setting available.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	    return;
	}
	try {
	    UniCryptCryptoSetting uniCryptCryptoSetting = getUnicryptCryptoSetting(cryptoSetting);
	    BigInteger privateKey;
	    EnhancedEncryptionKeyShare enhancedEncryptionKeyShare;
	    try {
		privateKey = securePersistenceService.retrieve(tenant, section, PERSISTENCE_NAME_FOR_SECRET_KEY_FOR_KEY_SHARE);
		enhancedEncryptionKeyShare = createEncryptionKeyShare(tenant, uniCryptCryptoSetting, privateKey);

	    } catch (UnivoteException ex) {
		//No key available so a new one will be built
		enhancedEncryptionKeyShare = createEncryptionKeyShare(tenant, uniCryptCryptoSetting);
		privateKey = enhancedEncryptionKeyShare.privateKey;
	    }

	    EncryptionKeyShare encryptionKeyShare = enhancedEncryptionKeyShare.encryptionKeyShare;
	    String encryptionKeyShareString = JSONConverter.marshal(encryptionKeyShare);
	    byte[] encryptionKeyShareByteArray = encryptionKeyShareString.getBytes(Charset.forName("UTF-8"));

	    securePersistenceService.persist(tenant, section, PERSISTENCE_NAME_FOR_SECRET_KEY_FOR_KEY_SHARE, privateKey);

	    this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), section, GroupEnum.TRUSTEES.getValue(), encryptionKeyShareByteArray, tenant);
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Posted key share for encrcyption. Action finished.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);

	} catch (UnivoteException ex) {
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Could not post key share for encrcyption. Action failed.");
	    Logger.getLogger(VoteMixingAction.class.getName()).log(Level.SEVERE, null, ex);
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	} catch (Exception ex) {
	    Logger.getLogger(VoteMixingAction.class.getName()).log(Level.SEVERE, null, ex);
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Could not marshal key share for encrcyption. Action failed.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	}
    }

    @Override
    @Asynchronous
    public void notifyAction(ActionContext actionContext, Object notification) {
	if (!(actionContext instanceof VoteMixingActionContext)) {
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	    return;
	}
	VoteMixingActionContext skcac = (VoteMixingActionContext) actionContext;

	this.informationService.informTenant(actionContext.getActionContextKey(), "Notified.");

	if (!(notification instanceof PostDTO)) {
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	    return;
	}
	PostDTO post = (PostDTO) notification;
	try {
	    Attributes attr = Transformer.convertAttributesDTOtoAttributes(post.getAlpha());
	    attr.containsKey(AlphaEnum.GROUP.getValue());

	    if (attr.containsKey(AlphaEnum.GROUP.getValue())
		    && attr.getValue(AlphaEnum.GROUP.getValue()) instanceof StringValue
		    && GroupEnum.ACCESS_RIGHT.getValue()
		    .equals(((StringValue) attr.getValue(AlphaEnum.GROUP.getValue())).getValue())) {
		skcac.setAccessRightGranted(Boolean.TRUE);
	    } else if (skcac.getCryptoSetting() == null) {
		CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, post.getMessage());
		skcac.setCryptoSetting(cryptoSetting);
	    }
	    run(actionContext);
	} catch (UnivoteException ex) {
	    this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	} catch (Exception ex) {
	    Logger.getLogger(VoteMixingAction.class.getName()).log(Level.SEVERE, null, ex);
	    this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	}
    }

    protected EnhancedEncryptionKeyShare createEncryptionKeyShare(String tenant, UniCryptCryptoSetting setting) throws UnivoteException {
	return this.createEncryptionKeyShare(tenant, setting, null);
    }

    protected EnhancedEncryptionKeyShare createEncryptionKeyShare(String tenant, UniCryptCryptoSetting setting, BigInteger privateKeyAsBigInt) throws UnivoteException {
	CyclicGroup cyclicGroup = setting.encryptionGroup;
	Element encryptionGenerator = setting.encryptionGenerator;
	HashAlgorithm hashAlgorithm = setting.hashAlgorithm;

	// Create ElGamal encryption scheme
	ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(encryptionGenerator);

	// Generate keys
	KeyPairGenerator kpg = elGamal.getKeyPairGenerator();
	Element privateKey = cyclicGroup.getElementFrom(privateKeyAsBigInt);
	if (privateKey == null) {
	    privateKey = kpg.generatePrivateKey();
	}
	Element publicKey = kpg.generatePublicKey(privateKey);

	// Generate proof generator
	Function function = kpg.getPublicKeyGenerationFunction();
	StringElement otherInput = StringMonoid.getInstance(Alphabet.UNICODE_BMP).getElement(tenant);
	HashMethod hashMethod = HashMethod.getInstance(hashAlgorithm);
	ConvertMethod convertMethod = ConvertMethod.getInstance(
		BigIntegerToByteArray.getInstance(ByteOrder.BIG_ENDIAN),
		StringToByteArray.getInstance(Charset.forName("UTF-8")));

	Converter converter = ByteArrayToBigInteger.getInstance(hashAlgorithm.getByteLength(), 1);

	SigmaChallengeGenerator challengeGenerator = FiatShamirSigmaChallengeGenerator.getInstance(
		cyclicGroup.getZModOrder(), otherInput, convertMethod, hashMethod, converter);

	PlainPreimageProofSystem pg = PlainPreimageProofSystem.getInstance(challengeGenerator, function);
	Triple proof = pg.generate(privateKey, publicKey);
	boolean success = pg.verify(proof, publicKey);
	if (!success) {
	    throw new UnivoteException("Math for proof system broken.");
	}
	Proof proofDTO = new Proof(pg.getCommitment(proof).getValue().toString(), pg.getChallenge(proof).getValue().toString(), pg.getResponse(proof).getValue().toString());

	EnhancedEncryptionKeyShare enhancedEncryptionKeyShare = new EnhancedEncryptionKeyShare();
	enhancedEncryptionKeyShare.privateKey = (BigInteger) privateKey.convertToBigInteger();
	EncryptionKeyShare encryptionKeyShare = new EncryptionKeyShare(publicKey.getHashValue().toString(), proofDTO);
	return enhancedEncryptionKeyShare;
    }

    protected CryptoSetting retrieveCryptoSetting(ActionContext actionContext) throws UnivoteException, Exception {
	ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
							     QueryFactory.getQueryForCryptoSetting(actionContext.getSection()));
	if (result.getResult().getPost().isEmpty()) {
	    throw new UnivoteException("Cryptosetting not published yet.");

	}
	CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, result.getResult().getPost().get(0).getMessage());
	return cryptoSetting;

    }

    protected UniCryptCryptoSetting getUnicryptCryptoSetting(CryptoSetting setting) throws UnivoteException {
	CyclicGroup cyclicGroup = null;
	Element generator = null;
	HashAlgorithm hashAlgorithm = null;
	switch (setting.getEncryptionSetting()) {
	    case "RC0e":
		cyclicGroup = GStarModSafePrime.getFirstInstance(8);
		generator = cyclicGroup.getDefaultGenerator();
		break;
	    case "RC1e":
		cyclicGroup = GStarModSafePrime.getFirstInstance(8);
		generator = cyclicGroup.getDefaultGenerator();
		break;
	    case "RC2e":
		cyclicGroup = GStarModSafePrime.getFirstInstance(1024);
		generator = cyclicGroup.getDefaultGenerator();
		break;
	    case "RC3e":
		cyclicGroup = GStarModSafePrime.getFirstInstance(2048);
		generator = cyclicGroup.getDefaultGenerator();
		break;
	    default:
		throw new UnivoteException("Unknown RC_e level");
	}
	switch (setting.getHashSetting()) {
	    case "H1":
		hashAlgorithm = HashAlgorithm.SHA1;
		break;
	    case "H2":
		hashAlgorithm = HashAlgorithm.SHA224;
		break;
	    case "H3":
		hashAlgorithm = HashAlgorithm.SHA256;
		break;
	    case "H4":
		hashAlgorithm = HashAlgorithm.SHA384;
		break;
	    case "H5":
		hashAlgorithm = HashAlgorithm.SHA512;
	    default:
		throw new UnivoteException("Unknown H_ level");
	}
	return new UniCryptCryptoSetting(cyclicGroup, generator, hashAlgorithm);

    }

    protected class EnhancedEncryptionKeyShare {

	protected EncryptionKeyShare encryptionKeyShare;
	protected BigInteger privateKey;
    }

    protected class UniCryptCryptoSetting {

	protected final CyclicGroup encryptionGroup;
	protected final Element encryptionGenerator;
	protected final HashAlgorithm hashAlgorithm;

	public UniCryptCryptoSetting(CyclicGroup encryptionGroup, Element encryptionGenerator, HashAlgorithm hashAlgorithm) {
	    this.encryptionGroup = encryptionGroup;
	    this.encryptionGenerator = encryptionGenerator;
	    this.hashAlgorithm = hashAlgorithm;
	}

    }

}
