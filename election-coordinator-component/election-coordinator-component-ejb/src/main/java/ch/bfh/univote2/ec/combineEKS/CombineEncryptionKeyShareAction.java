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
package ch.bfh.univote2.ec.combineEKS;

import ch.bfh.uniboard.clientlib.AttributeHelper;
import ch.bfh.uniboard.data.AttributesDTO.AttributeDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.StringValueDTO;
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
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.common.crypto.CryptoSetup;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.EncryptionKey;
import ch.bfh.univote2.common.message.EncryptionKeyShare;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.TrusteeCertificates;
import ch.bfh.univote2.common.query.AlphaEnum;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import ch.bfh.univote2.common.query.MessageFactory;
import ch.bfh.univote2.common.query.QueryFactory;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.PublicKey;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class CombineEncryptionKeyShareAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = CombineEncryptionKeyShareAction.class.getSimpleName();

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
		this.informationService.informTenant(ack, "Created new context.");
		CombineEncryptionKeyShareActionContext actionContext = new CombineEncryptionKeyShareActionContext(ack);
		return actionContext;
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		try {
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForEncryptionKey(actionContext.getSection()));
			return !result.getResult().getPost().isEmpty();
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not check post condition. UniBoard is not reachable." + ex.getMessage());
			return false;
		}
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		BoardPreconditionQuery bQuery = new BoardPreconditionQuery(QueryFactory.getQueryForEncryptionKeyShares(
				actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
		actionContext.getPreconditionQueries().add(bQuery);
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof CombineEncryptionKeyShareActionContext) {
			CombineEncryptionKeyShareActionContext ceksac = (CombineEncryptionKeyShareActionContext) actionContext;
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
						"Amount of found keyShares: " + result.getResult().getPost().size());
				for (PostDTO post : result.getResult().getPost()) {
					//validate keyshare and if valid add
					if (this.validateAndAddKeyShare(ceksac, post)) {
						if (ceksac.getAmount() == ceksac.getKeyShares().size()) {
							this.computeAndPostKey(ceksac);
							return;
						}
					} else {
						this.informationService.informTenant(actionContext.getActionContextKey(),
								"Rejected invalid keyshare.");
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
		if (actionContext instanceof CombineEncryptionKeyShareActionContext) {
			CombineEncryptionKeyShareActionContext ceksac = (CombineEncryptionKeyShareActionContext) actionContext;
			if (notification instanceof PostDTO) {
				PostDTO post = (PostDTO) notification;
				try {
					if (this.validateAndAddKeyShare(ceksac, post)) {
						if (ceksac.getAmount() == -1) {
							//Retrieve talliers
							this.retrieveAmountOfTalliers(ceksac);
						}
						if (ceksac.getCryptoSetting() == null) {
							this.retrieveCryptoSetting(ceksac);
						}
						this.informationService.informTenant(actionContext.getActionContextKey(),
								"Keyshare added.");
						if (ceksac.getAmount() == ceksac.getKeyShares().size()) {
							this.computeAndPostKey(ceksac);
						} else {
							this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
						}
					} else {
						this.informationService.informTenant(actionContext.getActionContextKey(),
								"Rejected invalid keyshare.");
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

	protected void retrieveAmountOfTalliers(CombineEncryptionKeyShareActionContext actionContext) throws
			UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForTrusteeCerts(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Trustees certificates not published yet.");
		}
		byte[] message = result.getResult().getPost().get(0).getMessage();
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

	protected void retrieveCryptoSetting(CombineEncryptionKeyShareActionContext actionContext) throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForCryptoSetting(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Crypto setting not yet published.");
		}
		byte[] message = result.getResult().getPost().get(0).getMessage();
		CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, message);
		actionContext.setCryptoSetting(cryptoSetting);
	}

	protected boolean validateAndAddKeyShare(CombineEncryptionKeyShareActionContext actionContext, PostDTO post)
			throws UnivoteException {

		AttributeDTO tallier = AttributeHelper.searchAttribute(post.getAlpha(), AlphaEnum.PUBLICKEY.getValue());
		if (tallier == null) {
			throw new UnivoteException("Publickey is missing in alpha.");
		}
		String strTallier = ((StringValueDTO) tallier.getValue()).getValue();

		EncryptionKeyShare encryptionKeyShare = JSONConverter.unmarshal(EncryptionKeyShare.class, post.getMessage());

		CryptoSetup cSetup = CryptoProvider.getEncryptionSetup(actionContext.getCryptoSetting()
				.getEncryptionSetting());
		//Validate Proof
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(cSetup.cryptoGenerator);
		KeyPairGenerator keyPairGen = elGamal.getKeyPairGenerator();
		Element publicKey = keyPairGen.getPublicKeySpace().getElementFrom(encryptionKeyShare.getKeyShare());
		Function proofFunction = keyPairGen.getPublicKeyGenerationFunction();

		StringElement otherInput = STRING_SPACE.getElement(strTallier);

		Converter converter = ByteArrayToBigInteger.getInstance(HashAlgorithm.SHA256.getByteLength(), 1);

		SigmaChallengeGenerator challengeGenerator = FiatShamirSigmaChallengeGenerator.getInstance(
				cSetup.cryptoGroup.getZModOrder(), otherInput, CONVERT_METHOD, HASH_METHOD, converter);

		PlainPreimageProofSystem pg = PlainPreimageProofSystem.getInstance(challengeGenerator, proofFunction);
		//Fill triple
		Element commitment
				= pg.getCommitmentSpace().getElementFrom(new BigInteger(encryptionKeyShare.getProof().getCommitment()));
		Element challenge
				= pg.getChallengeSpace().getElementFrom(new BigInteger(encryptionKeyShare.getProof().getChallenge()));
		Element response
				= pg.getResponseSpace().getElementFrom(new BigInteger(encryptionKeyShare.getProof().getResponse()));

		Triple proofTriple = Triple.getInstance(commitment, challenge, response);

		if (pg.verify(proofTriple, publicKey)) {

			actionContext.getKeyShares().put(strTallier, publicKey);
			return true;
		}
		//Remove tallier
		actionContext.getKeyShares().put(strTallier, cSetup.cryptoGroup.getIdentityElement());
		return false;
	}

	protected void computeAndPostKey(CombineEncryptionKeyShareActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(),
				"All keyshares received. Computing encryption key.");
		CyclicGroup cyclicGroup = CryptoProvider.getEncryptionSetup(actionContext.getCryptoSetting()
				.getEncryptionSetting()).cryptoGroup;

		Element encKey = cyclicGroup.getIdentityElement();

		for (Element keyShare : actionContext.getKeyShares().values()) {
			encKey = encKey.apply(keyShare);
		}

		EncryptionKey message = new EncryptionKey();
		message.setEncryptionKey(encKey.convertToString());

		try {
			//post ac
			PublicKey pk = this.tenantManager.getPublicKey(actionContext.getTenant());
			byte[] arMessage = MessageFactory.createAccessRight(GroupEnum.ENCRYPTION_KEY, pk, 1);
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), arMessage, actionContext.getTenant());
			//post encKey
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.ENCRYPTION_KEY.getValue(),
					JSONConverter.marshal(message).getBytes(Charset.forName("UTF-8")), actionContext.getTenant());
			//inform am
			this.informationService.informTenant(actionContext.getActionContextKey(), "Posted encryption key.");
			this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

}
