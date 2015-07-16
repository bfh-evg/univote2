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

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.unicrypt.crypto.keygenerator.interfaces.KeyPairGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.classes.PlainPreimageProofSystem;
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
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
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import ch.bfh.univote2.ec.QueryFactory;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class CombineEncryptionKeyShareAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = CombineEncryptionKeyShareAction.class.getSimpleName();
	private static final Logger logger = Logger.getLogger(CombineEncryptionKeyShareAction.class.getName());

	@EJB
	ActionManager actionManager;
	@EJB
	InformationService informationService;
	@EJB
	UniboardService uniboardService;

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
					"Could not check post condition. UniBoard is not reachable.");
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
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof CombineEncryptionKeyShareActionContext) {
			CombineEncryptionKeyShareActionContext ceksac = (CombineEncryptionKeyShareActionContext) actionContext;
			//Check if amount is set
			if (ceksac.getAmount() == -1) {
				try {
					//Retrieve talliers
					this.retrieveAmountOfTalliers(ceksac);
				} catch (UnivoteException | JsonException ex) {
					this.informationService.informTenant(actionContext.getActionContextKey(),
							ex.getMessage());
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					return;
				}
			}
			try {
				ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						QueryFactory.getQueryForEncryptionKeyShares(actionContext.getSection()));
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
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (actionContext instanceof CombineEncryptionKeyShareActionContext) {
			CombineEncryptionKeyShareActionContext ceksac = (CombineEncryptionKeyShareActionContext) actionContext;
			if (notification instanceof PostDTO) {
				PostDTO post = (PostDTO) notification;
				if (this.validateAndAddKeyShare(ceksac, post)) {
					if (ceksac.getAmount() == -1) {
						try {
							//Retrieve talliers
							this.retrieveAmountOfTalliers(ceksac);
						} catch (UnivoteException | JsonException ex) {
							this.informationService.informTenant(actionContext.getActionContextKey(),
									ex.getMessage());
							this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
							return;
						}
					}
					if (ceksac.getAmount() == ceksac.getKeyShares().size()) {
						this.computeAndPostKey(ceksac);
					} else {
						this.informationService.informTenant(actionContext.getActionContextKey(),
								"Keyshare added.");
						this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
					}
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
		//Prepare JsonObject for trustees
		String messageString = new String(result.getResult().getPost().get(0).getMessage(),
				Charset.forName("UTF-8"));
		JsonReader jsonReader = Json.createReader(new StringReader(messageString));
		JsonObject message = jsonReader.readObject();
		JsonArray talliers = message.getJsonArray("tallierCertificates");
		if (talliers == null) {
			throw new UnivoteException("Invalid trustees certificates message. tallierCertificates is missing.");
		}
		actionContext.setAmount(talliers.size());
	}

	protected boolean validateAndAddKeyShare(CombineEncryptionKeyShareActionContext actionContext, PostDTO post) {
		try {
			String messageString = new String(post.getMessage(), Charset.forName("UTF-8"));
			JsonReader jsonReader = Json.createReader(new StringReader(messageString));
			JsonObject message = jsonReader.readObject();
			JsonValue keyShareTmp = message.get("keyShare");
			if (keyShareTmp == null || !keyShareTmp.getValueType().equals(JsonValue.ValueType.STRING)) {
				throw new UnivoteException(
						"keyShare is missing or no string.");
			}
			JsonString keyShare = (JsonString) keyShareTmp;
			JsonValue proofTmp = message.get("keyShare");
			if (proofTmp == null || !proofTmp.getValueType().equals(JsonValue.ValueType.OBJECT)) {
				throw new UnivoteException(
						"proof is missing or no string.");
			}
			JsonObject proof = (JsonObject) proofTmp;
			JsonValue commitmentTmp = proof.get("commitment");
			if (commitmentTmp == null || !commitmentTmp.getValueType().equals(JsonValue.ValueType.STRING)) {
				throw new UnivoteException(
						"proof is missing or no string.");
			}
			JsonString commitment = (JsonString) commitmentTmp;
			JsonValue responseTmp = proof.get("response");
			if (responseTmp == null || !responseTmp.getValueType().equals(JsonValue.ValueType.STRING)) {
				throw new UnivoteException(
						"proof is missing or no string.");
			}
			JsonString response = (JsonString) responseTmp;

			//Validate Proof
			//TODO retrieve modulus and generator
			BigInteger modulus = BigInteger.ONE;
			BigInteger generator = BigInteger.ONE;
			CyclicGroup cyclicGroup = GStarModSafePrime.getInstance(modulus);
			Element generatorElement = cyclicGroup.getElementFrom(generator);
			ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(generatorElement);
			KeyPairGenerator keyPairGen = elGamal.getKeyPairGenerator();
			Element publicKey = keyPairGen.getPublicKeySpace().getElementFrom(keyShare.getString());
			Function proofFunction = keyPairGen.getPublicKeyGenerationFunction();
			PlainPreimageProofSystem pg = PlainPreimageProofSystem.getInstance(proofFunction);
			//TODO Fill triple
			Triple proofTriple = null;
			pg.verify(proofTriple, publicKey);

		} catch (JsonException | UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					ex.getMessage());
		}
		return true;
	}

	protected void computeAndPostKey(CombineEncryptionKeyShareActionContext actionContext) {
	}

}
