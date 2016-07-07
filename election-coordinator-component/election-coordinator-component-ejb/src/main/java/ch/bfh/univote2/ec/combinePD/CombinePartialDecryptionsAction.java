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
package ch.bfh.univote2.ec.combinePD;

import ch.bfh.uniboard.clientlib.AttributeHelper;
import ch.bfh.uniboard.data.AttributeDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.unicrypt.crypto.encoder.classes.ZModPrimeToGStarModSafePrime;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.FiatShamirSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.classes.EqualityPreimageProofSystem;
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
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import ch.bfh.unicrypt.math.function.classes.GeneratorFunction;
import ch.bfh.unicrypt.math.function.classes.InvertFunction;
import ch.bfh.unicrypt.math.function.classes.MultiIdentityFunction;
import ch.bfh.unicrypt.math.function.classes.ProductFunction;
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.common.crypto.CryptoSetup;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.DecryptedVotes;
import ch.bfh.univote2.common.message.EncryptedVote;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.MixedVotes;
import ch.bfh.univote2.common.message.PartialDecryption;
import ch.bfh.univote2.common.message.TrusteeCertificates;
import ch.bfh.univote2.common.query.AlphaEnum;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.MessageFactory;
import ch.bfh.univote2.common.query.QueryFactory;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class CombinePartialDecryptionsAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = CombinePartialDecryptionsAction.class.getSimpleName();

	@EJB
	private ActionManager actionManager;
	@EJB
	private InformationService informationService;
	@EJB
	private UniboardService uniboardService;
	@EJB
	private TenantManager tenantManager;

	protected static final HashMethod HASH_METHOD = HashMethod.getInstance(HashAlgorithm.SHA256);
	protected static final ConvertMethod CONVERT_METHOD = ConvertMethod.getInstance(
			BigIntegerToByteArray.getInstance(ByteOrder.BIG_ENDIAN),
			StringToByteArray.getInstance(Charset.forName("UTF-8")));
	protected static final StringMonoid STRING_SPACE = StringMonoid.getInstance(Alphabet.UNICODE_BMP);

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		CombinePartialDecryptionsActionContext actionContext = new CombinePartialDecryptionsActionContext(ack);
		return actionContext;
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		try {
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForDecryptedVotes(actionContext.getSection()));
			return !result.getResult().isEmpty();
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not check post condition. UniBoard is not reachable." + ex.getMessage());
			return false;
		}
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		BoardPreconditionQuery bQuery = new BoardPreconditionQuery(QueryFactory.getQueryForPartialDecryptions(
				actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
		actionContext.getPreconditionQueries().add(bQuery);
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof CombinePartialDecryptionsActionContext) {
			CombinePartialDecryptionsActionContext ceksac = (CombinePartialDecryptionsActionContext) actionContext;
			try {
				//Check if amount is set
				if (ceksac.getAmount() == -1) {

					//Retrieve talliers
					this.retrieveAmountOfTalliers(ceksac);

				}
				//Check if cryptosetting is set
				if (ceksac.getCryptoSetting() == null) {
					//Retrieve cryptosetting
					this.retrieveCryptoSetting(ceksac);
				}

				if (ceksac.getGeneratorFunctions() == null) {
					//Retrieve generatorFunctions
					this.retrieveMixedVotesAndGeneratorFunctions(ceksac);
				}
			} catch (UnivoteException ex) {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						ex.getMessage());
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
			try {
				ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						QueryFactory.getQueryForEncryptionKeyShares(actionContext.getSection()));
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Amount of found partial decryptions: " + result.getResult().size());
				for (PostDTO post : result.getResult()) {
					//validate keyshare and if valid add
					if (this.validateAndAddPartialDecryption(ceksac, post)) {
						if (ceksac.getAmount() == ceksac.getPartialDecryptions().size()) {
							this.computeAndPostDecryptions(ceksac);
							return;
						}
					} else {
						this.informationService.informTenant(actionContext.getActionContextKey(),
								"Rejected invalid partial decryption.");
					}
				}
				this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
			} catch (UnivoteException ex) {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						ex.getMessage());
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			}
		} else {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Unsupported context.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	@Override
	@Asynchronous
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (actionContext instanceof CombinePartialDecryptionsActionContext) {
			CombinePartialDecryptionsActionContext ceksac = (CombinePartialDecryptionsActionContext) actionContext;
			if (notification instanceof PostDTO) {
				PostDTO post = (PostDTO) notification;
				try {
					if (ceksac.getAmount() == -1) {
						//Retrieve talliers
						this.retrieveAmountOfTalliers(ceksac);
					}
					if (ceksac.getCryptoSetting() == null) {
						this.retrieveCryptoSetting(ceksac);
					}
					if (ceksac.getGeneratorFunctions() == null) {
						//Retrieve generatorFunctions
						this.retrieveMixedVotesAndGeneratorFunctions(ceksac);
					}
					if (this.validateAndAddPartialDecryption(ceksac, post)) {
						this.informationService.informTenant(actionContext.getActionContextKey(),
								"Partial decryption added.");
						if (ceksac.getAmount() == ceksac.getPartialDecryptions().size()) {
							this.computeAndPostDecryptions(ceksac);
						} else {
							this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
						}
					} else {
						this.informationService.informTenant(actionContext.getActionContextKey(),
								"Rejected invalid partial decryption.");
					}
				} catch (UnivoteException ex) {
					this.informationService.informTenant(actionContext.getActionContextKey(),
							ex.getMessage());
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				}
			} else {
				this.informationService.informTenant(actionContext.getActionContextKey(), "Unknown notification: "
						+ notification.toString());
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			}
		} else {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Unsupported context.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	protected void retrieveAmountOfTalliers(CombinePartialDecryptionsActionContext actionContext) throws
			UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForTrusteeCerts(actionContext.getSection()));
		if (result.getResult().isEmpty()) {
			throw new UnivoteException("Trustees certificates not published yet.");
		}
		byte[] message = result.getResult().get(0).getMessage();
		TrusteeCertificates trusteeCertificates;
		try {
			trusteeCertificates = JSONConverter.unmarshal(TrusteeCertificates.class, message);
		} catch (Exception ex) {
			throw new UnivoteException("Invalid trustees certificates message. Can not be unmarshalled.", ex);
		}
		actionContext.setAmount(trusteeCertificates.getTallierCertificates().size());
		this.informationService.informTenant(actionContext.getActionContextKey(),
				"Amount: " + actionContext.getAmount());
	}

	protected void retrieveCryptoSetting(CombinePartialDecryptionsActionContext actionContext) throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForCryptoSetting(actionContext.getSection()));
		if (result.getResult().isEmpty()) {
			throw new UnivoteException("Crypto setting not yet published.");
		}
		byte[] message = result.getResult().get(0).getMessage();
		CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, message);
		actionContext.setCryptoSetting(cryptoSetting);
	}

	protected void retrieveMixedVotesAndGeneratorFunctions(CombinePartialDecryptionsActionContext actionContext)
			throws UnivoteException {
		List<PostDTO> result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForMixedVotes(actionContext.getSection())).getResult();
		if (result.isEmpty()) {
			throw new UnivoteException("Mixed votes not published yet.");

		}
		CryptoSetup cSetup = CryptoProvider.getEncryptionSetup(actionContext.getCryptoSetting().getEncryptionSetting());
		CyclicGroup cyclicGroup = cSetup.cryptoGroup;

		MixedVotes mixedVotes = JSONConverter.unmarshal(MixedVotes.class, result.get(0).getMessage());
		actionContext.setMixedVotes(mixedVotes);
		List<Function> generatorFunctions = new ArrayList<>();
		for (EncryptedVote encVote : mixedVotes.getMixedVotes()) {
			Element element = cyclicGroup.getElementFrom(encVote.getFirstValue());
			GeneratorFunction function = GeneratorFunction.getInstance(element);
			generatorFunctions.add(function);
		}
		actionContext.setGeneratorFunctions(generatorFunctions.toArray(new Function[0]));

	}

	protected boolean validateAndAddPartialDecryption(CombinePartialDecryptionsActionContext actionContext,
			PostDTO post) throws UnivoteException {

		AttributeDTO tallier
				= AttributeHelper.searchAttribute(post.getAlpha(), AlphaEnum.PUBLICKEY.getValue());
		if (tallier == null) {
			throw new UnivoteException("Publickey is missing in alpha.");
		}
		String tallierPublicKey = tallier.getValue();

		PartialDecryption partDecryptedVotes = JSONConverter.unmarshal(PartialDecryption.class, post.getMessage());
		CryptoSetup cSetup = CryptoProvider.getEncryptionSetup(actionContext.getCryptoSetting().getEncryptionSetting());
		CyclicGroup cyclicGroup = cSetup.cryptoGroup;
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(cSetup.cryptoGenerator);
		Element encryptionGenerator = cSetup.cryptoGenerator;
		HashAlgorithm hashAlgorithm = HashAlgorithm.SHA256;

		Element publicKey = cyclicGroup.getElementFrom(tallierPublicKey);

		List<Element> partialDecryptions = new ArrayList<>();
		for (String partDecVote : partDecryptedVotes.getPartiallyDecryptedVotes()) {
			Element partDecVoteEle = elGamal.getEncryptionSpace().getElementFrom(partDecVote);
			partialDecryptions.add(partDecVoteEle);

		}

		Function[] generatorFunctions = actionContext.getGeneratorFunctions();

		// Create proof functions
		Function f1 = GeneratorFunction.getInstance(encryptionGenerator);
		Function f2 = CompositeFunction.getInstance(
				InvertFunction.getInstance(cyclicGroup.getZModOrder()),
				MultiIdentityFunction.getInstance(cyclicGroup.getZModOrder(), generatorFunctions.length),
				ProductFunction.getInstance(generatorFunctions));
		// Private and public input and prover id
		Pair publicInput = Pair.getInstance(publicKey, Tuple.getInstance(partialDecryptions.toArray(new Element[0])));
		StringElement otherInput = StringMonoid.getInstance(Alphabet.UNICODE_BMP).getElement(tallierPublicKey);
		HashMethod hashMethod = HashMethod.getInstance(hashAlgorithm);
		ConvertMethod convertMethod = ConvertMethod.getInstance(
				BigIntegerToByteArray.getInstance(ByteOrder.BIG_ENDIAN),
				StringToByteArray.getInstance(Charset.forName("UTF-8")));

		Converter converter = ByteArrayToBigInteger.getInstance(hashAlgorithm.getByteLength(), 1);

		SigmaChallengeGenerator challengeGenerator = FiatShamirSigmaChallengeGenerator.getInstance(
				cyclicGroup.getZModOrder(), otherInput, convertMethod, hashMethod, converter);
		EqualityPreimageProofSystem proofSystem = EqualityPreimageProofSystem.getInstance(challengeGenerator, f1, f2);
//
		//Fill triple
		Element commitment
				= proofSystem.getCommitmentSpace().getElementFrom(partDecryptedVotes.getProof().getCommitment());
		Element challenge
				= proofSystem.getChallengeSpace().getElementFrom(partDecryptedVotes.getProof().getChallenge());
		Element response
				= proofSystem.getResponseSpace().getElementFrom(partDecryptedVotes.getProof().getResponse());

		Triple proofTriple = Triple.getInstance(commitment, challenge, response);
		if (proofSystem.verify(proofTriple, publicInput)) {
			actionContext.getPartialDecryptions().put(tallierPublicKey,
					partDecryptedVotes.getPartiallyDecryptedVotes());
			return true;
		}
		return false;
	}

	protected void computeAndPostDecryptions(CombinePartialDecryptionsActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(),
				"All partial decriptions received. Computing decrypted votes.");
		CyclicGroup cyclicGroup = CryptoProvider.getEncryptionSetup(actionContext.getCryptoSetting()
				.getEncryptionSetting()).cryptoGroup;

		//Combine
		int n = actionContext.getGeneratorFunctions().length;
		DecryptedVotes votes = new DecryptedVotes();
		for (int i = 0; i < n; i++) {
			Element wPrime = cyclicGroup.getIdentityElement();
			for (List<String> partDec : actionContext.getPartialDecryptions().values()) {
				Element a = cyclicGroup.getElementFrom(partDec.get(i));
				wPrime = wPrime.apply(a);
			}
			//Get b from mixed votes
			Element b
					= cyclicGroup.getElementFrom(actionContext.getMixedVotes().getMixedVotes().get(i).getSecondValue());
			wPrime = wPrime.apply(b);
			System.out.println(wPrime);
			//Decode votes from cyclic group

			//Case for GStarModSafePrime groups
			if (cyclicGroup instanceof GStarModSafePrime) {
				GStarModSafePrime gStarModPrimeGroup = (GStarModSafePrime) cyclicGroup;
				ZModElement w = ZModPrimeToGStarModSafePrime.getInstance(gStarModPrimeGroup).decode(wPrime);
				votes.getDecryptedVotes().add(i, w.convertToString());
			}
		}

		{
			try {
				//post ac
				PublicKey pk = this.tenantManager.getPublicKey(actionContext.getTenant());
				byte[] arMessage = MessageFactory.createAccessRight(GroupEnum.DECRYPTED_VOTES, pk, 1);
				this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
						GroupEnum.ACCESS_RIGHT.getValue(), arMessage, actionContext.getTenant());
				//post encKey
				this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
						GroupEnum.DECRYPTED_VOTES.getValue(),
						JSONConverter.marshal(votes).getBytes(Charset.forName("UTF-8")), actionContext.getTenant());
				//inform am
				this.informationService.informTenant(actionContext.getActionContextKey(), "Posted decrypted votes.");
				this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
			} catch (UnivoteException ex) {
				this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			}
		}
	}

}
