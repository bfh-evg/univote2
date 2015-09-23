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
package ch.bfh.univote2.trustee.tallier.partialDecryption;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.ResultDTO;
import ch.bfh.uniboard.data.TransformException;
import ch.bfh.uniboard.data.Transformer;
import ch.bfh.uniboard.service.Attributes;
import ch.bfh.uniboard.service.StringValue;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.FiatShamirSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.classes.EqualityPreimageProofSystem;
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
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import ch.bfh.unicrypt.math.function.classes.GeneratorFunction;
import ch.bfh.unicrypt.math.function.classes.InvertFunction;
import ch.bfh.unicrypt.math.function.classes.MultiIdentityFunction;
import ch.bfh.unicrypt.math.function.classes.ProductFunction;
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.common.crypto.CryptoSetup;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.MixedVotes;
import ch.bfh.univote2.common.message.PartialDecryption;
import ch.bfh.univote2.common.message.SigmaProof;
import ch.bfh.univote2.common.message.Vote;
import ch.bfh.univote2.common.query.AlphaEnum;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.QueryFactory;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.SecurePersistenceService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.trustee.BoardsEnum;
import ch.bfh.univote2.trustee.TrusteeActionHelper;
import ch.bfh.univote2.trustee.tallier.sharedKeyCreation.SharedKeyCreationAction;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
@Stateless
public class PartialDecryptionAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = PartialDecryptionAction.class.getSimpleName();

	private static final Logger logger = Logger.getLogger(PartialDecryptionAction.class.getName());

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
		return new PartialDecryptionActionContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		if (!(actionContext instanceof PartialDecryptionActionContext)) {
			logger.log(Level.SEVERE, "The actionContext was not the expected one.");
			return false;
		}
		PartialDecryptionActionContext pdac = (PartialDecryptionActionContext) actionContext;
		try {
			PublicKey publicKey = tenantManager.getPublicKey(actionContext.getTenant());
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForPartialDecryptionForTallier(actionContext.getSection(), publicKey));
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
		if (!(actionContext instanceof PartialDecryptionActionContext)) {
			logger.log(Level.SEVERE, "The actionContext was not the expected one.");
			return;
		}
		PartialDecryptionActionContext pdac = (PartialDecryptionActionContext) actionContext;
		TrusteeActionHelper.checkAndSetCryptoSetting(pdac, uniboardService, tenantManager, informationService, logger);
		TrusteeActionHelper.checkAndSetAccsessRight(pdac, GroupEnum.PARTIAL_DECRYPTION, uniboardService, tenantManager,
				informationService, logger);
		this.checkAndSetMixedVotes(pdac);
	}

	protected void checkAndSetMixedVotes(PartialDecryptionActionContext actionContext) {
		ActionContextKey actionContextKey = actionContext.getActionContextKey();
		String section = actionContext.getSection();
		try {
			MixedVotes mixedVotes = actionContext.getMixedVotes();

			//Add Notification
			if (mixedVotes == null) {
				mixedVotes = retrieveMixedVotes(actionContext);
				actionContext.setMixedVotes(mixedVotes);
			}

		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not get mixedVotes.", ex);
			informationService.informTenant(actionContextKey,
					"Could not retrive mixed votes.");
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
					QueryFactory.getQueryForMixedVotes(section), BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
		}
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		if (!(actionContext instanceof PartialDecryptionActionContext)) {
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		PartialDecryptionActionContext pdac = (PartialDecryptionActionContext) actionContext;
		if (!pdac.getAccessRightGranted()) {
			TrusteeActionHelper.checkAndSetAccsessRight(pdac, GroupEnum.PARTIAL_DECRYPTION, uniboardService,
					tenantManager, informationService, logger);
			if (!pdac.getAccessRightGranted()) {
				logger.log(Level.INFO, "Access right has not been granted yet.");
				this.informationService.informTenant(pdac.getActionContextKey(), "Access right not yet granted.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
		}
		String tenant = actionContext.getTenant();
		String section = actionContext.getSection();

		if (pdac.getCryptoSetting() == null) {
			try {
				pdac.setCryptoSetting(TrusteeActionHelper.retrieveCryptoSetting(actionContext, uniboardService));
			} catch (UnivoteException ex) {
				logger.log(Level.WARNING, "Crypto setting is not published yet.", ex);
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Crypto setting not published yet.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
		}
		if (pdac.getMixedVotes() == null) {
			try {
				pdac.setMixedVotes(this.retrieveMixedVotes(actionContext));
			} catch (UnivoteException ex) {
				logger.log(Level.WARNING, "Mixed votes are not published yet.", ex);
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Mixed votes are not published yet.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
		}

		CryptoSetup cSetup = CryptoProvider.getEncryptionSetup(pdac.getCryptoSetting().getEncryptionSetting());

		try {
			BigInteger privateKey = securePersistenceService.retrieve(tenant, section,
					SharedKeyCreationAction.PERSISTENCE_NAME_FOR_SECRET_KEY_SHARE);
			CyclicGroup cyclicGroup = cSetup.cryptoGroup;
			Element encryptionGenerator = cSetup.cryptoGenerator;

			Element secretKey = cyclicGroup.getElementFrom(privateKey);
			Element decryptionKey = secretKey.invert();
			Element publicKey = encryptionGenerator.selfApply(secretKey);

			List<Element> partialDecryptions = new ArrayList<>();
			List<String> partialDecryptionsAsStrings = new ArrayList<>();
			List<Function> generatorFunctions = new ArrayList<>();
			for (Vote v : pdac.getMixedVotes().getMixedVotes()) {
				Element element = cyclicGroup.getElementFrom(v.getFirstValue());
				GeneratorFunction function = GeneratorFunction.getInstance(element);
				generatorFunctions.add(function);
				Element partialDecryption = function.apply(decryptionKey);
				partialDecryptions.add(partialDecryption);
				partialDecryptionsAsStrings.add(partialDecryption.convertToString());
			}
			SigmaProof proofDTO = createProof(tenant, pdac.getCryptoSetting(), secretKey, publicKey,
					partialDecryptions.toArray(new Element[0]), generatorFunctions.toArray(new Function[0]));
			PartialDecryption partialDecryptionDTO = new PartialDecryption(partialDecryptionsAsStrings, proofDTO);

			String partialDecryptionsString = JSONConverter.marshal(partialDecryptionDTO);
			byte[] partialDecryptionsByteArray = partialDecryptionsString.getBytes(Charset.forName("UTF-8"));

			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), section,
					GroupEnum.PARTIAL_DECRYPTION.getValue(), partialDecryptionsByteArray, tenant);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Posted key share for encrcyption. Action finished.");
			this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);

		} catch (UnivoteException ex) {
			//No key available. Unsolvable problem encountered.
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not access private key for decryption. Action failed.");
			Logger.getLogger(PartialDecryptionAction.class.getName()).log(Level.SEVERE, null, ex);
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	@Override
	@Asynchronous
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (!(actionContext instanceof PartialDecryptionActionContext)) {
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		PartialDecryptionActionContext pdac = (PartialDecryptionActionContext) actionContext;

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
				pdac.setAccessRightGranted(Boolean.TRUE);
			}

			if (pdac.getCryptoSetting() == null && (attr.containsKey(AlphaEnum.GROUP.getValue())
					&& attr.getValue(AlphaEnum.GROUP.getValue()) instanceof StringValue
					&& GroupEnum.CRYPTO_SETTING.getValue()
					.equals(((StringValue) attr.getValue(AlphaEnum.GROUP.getValue())).getValue()))) {
				CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, post.getMessage());
				pdac.setCryptoSetting(cryptoSetting);
			}
			if (pdac.getMixedVotes() == null && (attr.containsKey(AlphaEnum.GROUP.getValue())
					&& attr.getValue(AlphaEnum.GROUP.getValue()) instanceof StringValue
					&& GroupEnum.MIXED_VOTES.getValue()
					.equals(((StringValue) attr.getValue(AlphaEnum.GROUP.getValue())).getValue()))) {
				MixedVotes mixedVotes = JSONConverter.unmarshal(MixedVotes.class, post.getMessage());
				pdac.setMixedVotes(mixedVotes);
			}

			run(actionContext);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		} catch (TransformException ex) {
			Logger.getLogger(PartialDecryptionAction.class.getName()).log(Level.SEVERE, null, ex);
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	protected MixedVotes retrieveMixedVotes(ActionContext actionContext) throws UnivoteException {
		ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForMixedVotes(actionContext.getSection())).getResult();
		if (result.getPost().isEmpty()) {
			throw new UnivoteException("Mixed votes not published yet.");

		}
		MixedVotes mixedVotes = JSONConverter.unmarshal(MixedVotes.class, result.getPost().get(0).getMessage());
		return mixedVotes;

	}

	/**
	 * pi = NIZKP{(x) : y = g^x ∧ (∧_i b_i = a_i^{−x} )}.
	 *
	 * @param tenant
	 * @param cryptoSetting
	 * @param secretKey
	 * @param publicKey
	 * @param partialDecryptions
	 * @param generatorFunctions
	 * @return
	 */
	protected SigmaProof createProof(String tenant, CryptoSetting cryptoSetting, Element secretKey,
			Element publicKey, Element[] partialDecryptions, Function[] generatorFunctions) {
		CryptoSetup cSetup = CryptoProvider.getEncryptionSetup(cryptoSetting.getEncryptionSetting());
		CyclicGroup cyclicGroup = cSetup.cryptoGroup;
		Element encryptionGenerator = cSetup.cryptoGenerator;
		HashAlgorithm hashAlgorithm = HashAlgorithm.SHA256;

		// Create proof functions
		Function f1 = GeneratorFunction.getInstance(encryptionGenerator);
		Function f2 = CompositeFunction.getInstance(
				InvertFunction.getInstance(cyclicGroup.getZModOrder()),
				MultiIdentityFunction.getInstance(cyclicGroup.getZModOrder(), generatorFunctions.length),
				ProductFunction.getInstance(generatorFunctions));
		// Private and public input and prover id
		Element privateInput = secretKey;
		Pair publicInput = Pair.getInstance(publicKey, Tuple.getInstance(partialDecryptions));
		StringElement otherInput = StringMonoid.getInstance(Alphabet.UNICODE_BMP).getElement(tenant);
		HashMethod hashMethod = HashMethod.getInstance(hashAlgorithm);
		ConvertMethod convertMethod = ConvertMethod.getInstance(
				BigIntegerToByteArray.getInstance(ByteOrder.BIG_ENDIAN),
				StringToByteArray.getInstance(Charset.forName("UTF-8")));

		Converter converter = ByteArrayToBigInteger.getInstance(hashAlgorithm.getByteLength(), 1);

		SigmaChallengeGenerator challengeGenerator = FiatShamirSigmaChallengeGenerator.getInstance(
				cyclicGroup.getZModOrder(), otherInput, convertMethod, hashMethod, converter);
		EqualityPreimageProofSystem proofSystem = EqualityPreimageProofSystem.getInstance(challengeGenerator, f1, f2);
		// Generate and verify proof
		Triple proof = proofSystem.generate(privateInput, publicInput);
		boolean result = proofSystem.verify(proof, publicInput);

		SigmaProof proofDTO = new SigmaProof(proofSystem.getCommitment(proof).convertToString(),
				proofSystem.getChallenge(proof).convertToString(), proofSystem.getResponse(proof).convertToString());
		return proofDTO;
	}

}
