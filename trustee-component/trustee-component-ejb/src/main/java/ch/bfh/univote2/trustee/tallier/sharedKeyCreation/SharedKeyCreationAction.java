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
package ch.bfh.univote2.trustee.tallier.sharedKeyCreation;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.TransformException;
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
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.common.crypto.CryptoSetup;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.EncryptionKeyShare;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.SigmaProof;
import ch.bfh.univote2.common.query.AlphaEnum;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.QueryFactory;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.SecurePersistenceService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.trustee.BoardsEnum;
import ch.bfh.univote2.trustee.TrusteeActionHelper;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class SharedKeyCreationAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = SharedKeyCreationAction.class.getSimpleName();
	public static final String PERSISTENCE_NAME_FOR_SECRET_KEY_SHARE = "SECRET_KEY_FOR_KEY_SHARE";

	private static final Logger logger = Logger.getLogger(SharedKeyCreationAction.class.getName());

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
		return new SharedKeyCreationActionContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		try {
			PublicKey publicKey = tenantManager.getPublicKey(actionContext.getTenant());
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForEncryptionKeyShareForTallier(actionContext.getSection(), publicKey));
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
		if (!(actionContext instanceof SharedKeyCreationActionContext)) {
			logger.log(Level.SEVERE, "The actionContext was not the expected one.");
			return;
		}
		SharedKeyCreationActionContext skcac = (SharedKeyCreationActionContext) actionContext;
		TrusteeActionHelper.checkAndSetCryptoSetting(skcac, uniboardService, tenantManager, informationService, logger);
		TrusteeActionHelper.checkAndSetAccsessRight(skcac, GroupEnum.ENCRYPTION_KEY_SHARE, uniboardService,
				tenantManager, informationService, logger);
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		if (!(actionContext instanceof SharedKeyCreationActionContext)) {
			this.informationService.informTenant(actionContext.getActionContextKey(), "Unsupported context.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		SharedKeyCreationActionContext skcac = (SharedKeyCreationActionContext) actionContext;
		if (!skcac.getAccessRightGranted()) {
			TrusteeActionHelper.checkAndSetAccsessRight(skcac, GroupEnum.ENCRYPTION_KEY_SHARE, uniboardService,
					tenantManager, informationService, logger);
			if (!skcac.getAccessRightGranted()) {
				logger.log(Level.INFO, "Access right has not been granted yet.");
				this.informationService.informTenant(skcac.getActionContextKey(), "Access right not yet granted.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
		}
		String tenant = actionContext.getTenant();
		String section = actionContext.getSection();

		CryptoSetting cryptoSetting = skcac.getCryptoSetting();
		if (cryptoSetting == null) {
			try {
				skcac.setCryptoSetting(TrusteeActionHelper.retrieveCryptoSetting(actionContext, uniboardService));
			} catch (UnivoteException ex) {
				logger.log(Level.WARNING, "Crypto setting is not published yet.", ex);
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Crypto setting not published yet.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
		}
		try {
			BigInteger privateKey;
			EnhancedEncryptionKeyShare enhancedEncryptionKeyShare;
			try {
				privateKey = securePersistenceService.retrieve(tenant, section,
						PERSISTENCE_NAME_FOR_SECRET_KEY_SHARE);
				String publicKey = ((DSAPublicKey) this.tenantManager.getPublicKey(tenant)).getY().toString(10);
				enhancedEncryptionKeyShare = createEncryptionKeyShare(publicKey, cryptoSetting, privateKey);

			} catch (UnivoteException ex) {
				//No key available so a new one will be built
				enhancedEncryptionKeyShare = createEncryptionKeyShare(tenant, cryptoSetting);
				privateKey = enhancedEncryptionKeyShare.privateKey;
				String publicKey = ((DSAPublicKey) this.tenantManager.getPublicKey(tenant)).getY().toString(10);
				securePersistenceService.persist(publicKey, section, PERSISTENCE_NAME_FOR_SECRET_KEY_SHARE, privateKey);
			}

			EncryptionKeyShare encryptionKeyShare = enhancedEncryptionKeyShare.encryptionKeyShare;
			String encryptionKeyShareString = JSONConverter.marshal(encryptionKeyShare);
			logger.log(Level.INFO, encryptionKeyShareString);
			byte[] encryptionKeyShareByteArray = encryptionKeyShareString.getBytes(Charset.forName("UTF-8"));

			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), section, GroupEnum.ENCRYPTION_KEY_SHARE.getValue(),
					encryptionKeyShareByteArray, tenant);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Posted key share for encrcyption. Action finished.");
			this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);

		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not post key share for encrcyption. Action failed.");
			Logger.getLogger(SharedKeyCreationAction.class.getName()).log(Level.SEVERE, null, ex);
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	@Override
	@Asynchronous
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (!(actionContext instanceof SharedKeyCreationActionContext)) {
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		SharedKeyCreationActionContext skcac = (SharedKeyCreationActionContext) actionContext;

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
			}
			if (skcac.getCryptoSetting() == null && (attr.containsKey(AlphaEnum.GROUP.getValue())
					&& attr.getValue(AlphaEnum.GROUP.getValue()) instanceof StringValue
					&& GroupEnum.CRYPTO_SETTING.getValue()
					.equals(((StringValue) attr.getValue(AlphaEnum.GROUP.getValue())).getValue()))) {
				CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, post.getMessage());
				skcac.setCryptoSetting(cryptoSetting);
			}
			run(actionContext);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		} catch (TransformException ex) {
			Logger.getLogger(SharedKeyCreationAction.class.getName()).log(Level.SEVERE, null, ex);
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	protected EnhancedEncryptionKeyShare createEncryptionKeyShare(String tenant, CryptoSetting setting)
			throws UnivoteException {
		return this.createEncryptionKeyShare(tenant, setting, null);
	}

	protected EnhancedEncryptionKeyShare createEncryptionKeyShare(String tenantPublicKey, CryptoSetting setting,
			BigInteger privateKeyAsBigInt) throws UnivoteException {
		CryptoSetup cSetup = CryptoProvider.getEncryptionSetup(setting.getEncryptionSetting());
		CyclicGroup cyclicGroup = cSetup.cryptoGroup;
		Element encryptionGenerator = cSetup.cryptoGenerator;

		// Create ElGamal encryption scheme
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(encryptionGenerator);

		// Generate keys
		Element privateKey;
		KeyPairGenerator kpg = elGamal.getKeyPairGenerator();
		if (privateKeyAsBigInt == null) {
			privateKey = kpg.generatePrivateKey();
		} else {
			privateKey = kpg.getPrivateKeySpace().getElementFrom(privateKeyAsBigInt);

		}
		Element publicKey = kpg.generatePublicKey(privateKey);

		// Generate proof generator
		Function function = kpg.getPublicKeyGenerationFunction();
		StringElement otherInput = StringMonoid.getInstance(Alphabet.UNICODE_BMP).getElement(tenantPublicKey);
		HashMethod hashMethod = HashMethod.getInstance(HashAlgorithm.SHA256);
		ConvertMethod convertMethod = ConvertMethod.getInstance(
				BigIntegerToByteArray.getInstance(ByteOrder.BIG_ENDIAN),
				StringToByteArray.getInstance(Charset.forName("UTF-8")));

		Converter converter = ByteArrayToBigInteger.getInstance(HashAlgorithm.SHA256.getByteLength(), 1);

		SigmaChallengeGenerator challengeGenerator = FiatShamirSigmaChallengeGenerator.getInstance(
				cyclicGroup.getZModOrder(), otherInput, convertMethod, hashMethod, converter);

		PlainPreimageProofSystem pg = PlainPreimageProofSystem.getInstance(challengeGenerator, function);
		Triple proof = pg.generate(privateKey, publicKey);
		boolean success = pg.verify(proof, publicKey);
		if (!success) {
			throw new UnivoteException("Math for proof system broken.");
		}
		SigmaProof proofDTO = new SigmaProof(pg.getCommitment(proof).convertToString(),
				pg.getChallenge(proof).convertToString(), pg.getResponse(proof).convertToString());

		EnhancedEncryptionKeyShare enhancedEncryptionKeyShare = new EnhancedEncryptionKeyShare();
		enhancedEncryptionKeyShare.privateKey = (BigInteger) privateKey.convertToBigInteger();
		EncryptionKeyShare encryptionKeyShare
				= new EncryptionKeyShare(publicKey.convertToBigInteger().toString(10), proofDTO);
		enhancedEncryptionKeyShare.encryptionKeyShare = encryptionKeyShare;
		return enhancedEncryptionKeyShare;
	}

	protected class EnhancedEncryptionKeyShare {

		protected EncryptionKeyShare encryptionKeyShare;
		protected BigInteger privateKey;

	}

}
