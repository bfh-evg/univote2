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
package ch.bfh.univote2.trustee.mixer.keyMixing;

import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.ResultDTO;
import ch.bfh.unicrypt.crypto.mixer.classes.IdentityMixer;
import ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
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
import ch.bfh.univote2.component.core.message.JSONConverter;
import ch.bfh.univote2.component.core.message.KeyMixingRequest;
import ch.bfh.univote2.component.core.message.KeyMixingResult;
import ch.bfh.univote2.component.core.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.SecurePersistenceService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.trustee.BoardsEnum;
import ch.bfh.univote2.trustee.QueryFactory;
import ch.bfh.univote2.trustee.TrusteeActionHelper;
import ch.bfh.univote2.trustee.UniCryptCryptoSetting;
import ch.bfh.univote2.trustee.parallel.ParallelUserInput;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.json.JsonException;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class KeyMixingAction extends AbstractAction implements NotifiableAction {

    public static final String PERSISTENCE_NAME_FOR_SECRET_PERMUTATION_VALUE_FOR_KEY_MIX = "secretPermutationValueKey";
    public static final String PERSISTENCE_NAME_FOR_VOTING_GENERATOR_EXPONENT = "secretVotingGeneratorPartKey";

    private static final String ACTION_NAME = KeyMixingAction.class.getSimpleName();

    private static final Logger logger = Logger.getLogger(KeyMixingAction.class.getName());

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
	return new KeyMixingActionContext(ack);
    }

    @Override
    protected boolean checkPostCondition(ActionContext actionContext) {
	if (!(actionContext instanceof KeyMixingActionContext)) {
	    logger.log(Level.SEVERE, "The actionContext was not the expected one.");
	    return false;
	}
	KeyMixingActionContext vmac = (KeyMixingActionContext) actionContext;
	try {
	    PublicKey publicKey = tenantManager.getPublicKey(actionContext.getTenant());
	    ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
								 QueryFactory.getQueryForKeyMixingResult(actionContext.getSection(), publicKey));
	    if (!result.getResult().getPost().isEmpty()) {
		return true;
	    }
	} catch (UnivoteException ex) {
	    logger.log(Level.WARNING, "Could not request key mixing result.", ex);
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Could not check post condition.");
	    return false;
	}
	return false;
    }

    @Override
    protected void definePreconditions(ActionContext actionContext) {
	BoardPreconditionQuery bQuery = null;
	ActionContextKey actionContextKey = actionContext.getActionContextKey();
	String section = actionContext.getSection();
	String tenant = actionContext.getTenant();
	if (!(actionContext instanceof KeyMixingActionContext)) {
	    logger.log(Level.SEVERE, "The actionContext was not the expected one.");
	    return;
	}
	KeyMixingActionContext kmac = (KeyMixingActionContext) actionContext;
	TrusteeActionHelper.checkAndSetCryptoSetting(kmac, uniboardService, tenantManager, informationService, logger);
	TrusteeActionHelper.checkAndSetAccsessRight(kmac, GroupEnum.KEY_MIXING_RESULT, uniboardService, tenantManager, informationService, logger);
	this.checkAndSetKeyMixingRequest(kmac);
    }

    protected void checkAndSetKeyMixingRequest(KeyMixingActionContext actionContext) {
	ActionContextKey actionContextKey = actionContext.getActionContextKey();
	String section = actionContext.getSection();
	try {
	    KeyMixingRequest keyMixingRequest = actionContext.getKeyMixingRequest();

	    //Add Notification
	    if (keyMixingRequest == null) {
		keyMixingRequest = retrieveKeyMixingRequest(actionContext);
		actionContext.setKeyMixingRequest(keyMixingRequest);
	    }

	} catch (UnivoteException ex) {
	    logger.log(Level.WARNING, "Could not get key mixing request.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error retrieving key mixing request: " + ex.getMessage());
	} catch (JsonException ex) {
	    logger.log(Level.WARNING, "Could not parse key mixing request.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error reading key mixing request.");
	} catch (Exception ex) {
	    logger.log(Level.WARNING, "Could not parse key mixing request.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error reading key mixing request.");
	}
	try {
	    if (actionContext.getKeyMixingRequest() == null) {
		PublicKey publicKey = tenantManager.getPublicKey(actionContext.getTenant());
		//Add Notification
		BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
			QueryFactory.getQueryForKeyMixingRequest(section, publicKey), BoardsEnum.UNIVOTE.getValue());
		actionContext.getPreconditionQueries().add(bQuery);
	    }
	} catch (UnivoteException exception) {
	    logger.log(Level.WARNING, "Could not get tenant for key mixing request.", exception);
	    informationService.informTenant(actionContextKey,
					    "Error retrieving tenant for key mixing request: " + exception.getMessage());
	}

    }

    @Override
    @Asynchronous
    public void run(ActionContext actionContext) {
	if (!(actionContext instanceof KeyMixingActionContext)) {
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	    return;
	}
	KeyMixingActionContext kmac = (KeyMixingActionContext) actionContext;
	//The following if is strange, as the run should not happen in this case?!
	if (kmac.isPreconditionReached() == null) {
	    logger.log(Level.WARNING, "Run was called but preCondition is unknown in Context.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);

	    return;
	}
	if (Objects.equals(kmac.isPreconditionReached(), Boolean.FALSE)) {
	    logger.log(Level.WARNING, "Run was called but preCondition is not yet reached.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	    return;
	}
	String tenant = actionContext.getTenant();
	String section = actionContext.getSection();

	KeyMixingRequest keyMixingRequest = kmac.getKeyMixingRequest();
	CryptoSetting cryptoSetting = kmac.getCryptoSetting();

	try {

	    UniCryptCryptoSetting uniCryptCryptoSetting = TrusteeActionHelper.getUnicryptCryptoSetting(cryptoSetting);
	    BigInteger votingGeneratorExponent = null;
	    BigInteger permutation = null;

	    try {
		votingGeneratorExponent = securePersistenceService.retrieve(tenant, section, PERSISTENCE_NAME_FOR_VOTING_GENERATOR_EXPONENT);
	    } catch (UnivoteException ex) {
		//No exponent available so a new one will be built
	    }
	    try {
		permutation = securePersistenceService.retrieve(tenant, section, PERSISTENCE_NAME_FOR_VOTING_GENERATOR_EXPONENT);
	    } catch (UnivoteException ex) {
		//No permutation available so a new one will be built
	    }

	    EnhancedKeyMixingResult enhancedKeyMixingResult = createKeyMixingResult(tenant, keyMixingRequest, uniCryptCryptoSetting, votingGeneratorExponent, permutation);
	    permutation = enhancedKeyMixingResult.permutation;
	    votingGeneratorExponent = enhancedKeyMixingResult.votingGeneratorExponent;
	    KeyMixingResult keyMixingResult = enhancedKeyMixingResult.keyMixingResult;
	    String keyMixingResultString = JSONConverter.marshal(keyMixingResult);
	    byte[] keyMixingResultByteArray = keyMixingResultString.getBytes(Charset.forName("UTF-8"));

	    securePersistenceService.persist(tenant, section, PERSISTENCE_NAME_FOR_SECRET_PERMUTATION_VALUE_FOR_KEY_MIX, permutation);
	    securePersistenceService.persist(tenant, section, PERSISTENCE_NAME_FOR_VOTING_GENERATOR_EXPONENT, votingGeneratorExponent);

	    this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), section, GroupEnum.KEY_MIXING_RESULT.getValue(), keyMixingResultByteArray, tenant);
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Posted key mixing result. Action finished.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);

	} catch (UnivoteException ex) {
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Could not post key mixing result. Action failed.");
	    Logger.getLogger(KeyMixingAction.class.getName()).log(Level.SEVERE, null, ex);
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	} catch (Exception ex) {
	    Logger.getLogger(KeyMixingAction.class.getName()).log(Level.SEVERE, null, ex);
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Could not marshal key mixing result. Action failed.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	}
    }

    @Override
    @Asynchronous
    public void notifyAction(ActionContext actionContext, Object notification) {
	if (notification instanceof ParallelUserInput) {
	    ParallelUserInput para = (ParallelUserInput) notification;
	    this.informationService.informTenant(ACTION_NAME, actionContext.getActionContextKey().getTenant(),
						 actionContext.getActionContextKey().getSection(), "Entred value: " + para.getParallelValue());

	    this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
	} else if (notification instanceof Timer) {
	    this.informationService.informTenant(ACTION_NAME, actionContext.getActionContextKey().getTenant(),
						 actionContext.getActionContextKey().getSection(), "Time did run out.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
	}
    }

    protected KeyMixingRequest retrieveKeyMixingRequest(ActionContext actionContext) throws UnivoteException, Exception {
	PublicKey publicKey = tenantManager.getPublicKey(actionContext.getTenant());
	ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						    QueryFactory.getQueryForKeyMixingRequest(actionContext.getSection(), publicKey)).getResult();
	if (result.getPost().isEmpty()) {
	    throw new UnivoteException("key mixing request not published yet.");

	}
	KeyMixingRequest keyMixingRequest = JSONConverter.unmarshal(KeyMixingRequest.class, result.getPost().get(0).getMessage());
	return keyMixingRequest;

    }

    private EnhancedKeyMixingResult createKeyMixingResult(String tenant, KeyMixingRequest keyMixingRequest, UniCryptCryptoSetting uniCryptCryptoSetting, BigInteger votingGeneratorExponentAsBigInt, BigInteger permutationAsBigInt) {
	CyclicGroup cyclicGroup = uniCryptCryptoSetting.signatureGroup;
	Element generator = uniCryptCryptoSetting.signatureGenerator;

	Element randomization = null;

	if (votingGeneratorExponentAsBigInt != null) {
	    randomization = cyclicGroup.getElementFrom(votingGeneratorExponentAsBigInt);
	} else {
	    randomization = cyclicGroup.getRandomElement();
	}

	Element mixingGenerator = cyclicGroup.getElementFrom(keyMixingRequest.getGenerator());
	mixingGenerator = mixingGenerator.selfApply(randomization);

	List<String> keysAsString = keyMixingRequest.getKeys();
	Tuple identities = Tuple.getInstance();
	for (String string : keysAsString) {
	    identities.add(cyclicGroup.getElementFrom(string));
	}

	// Create mixer and shuffle
	IdentityMixer mixer = IdentityMixer.getInstance(cyclicGroup, identities.getArity());

	// Create permutation
	PermutationElement permutation = null;
	if (permutationAsBigInt != null) {
	    permutation = mixer.getPermutationGroup().getElementFrom(permutationAsBigInt);
	} else {
	    permutation = mixer.getPermutationGroup().getRandomElement();
	}

	// Perfom shuffle
	Tuple shuffledIdentities = mixer.shuffle(identities, permutation, randomization);

	//Jetzt mit Philipp die Proofs machen.
	return null;
    }

    protected class EnhancedKeyMixingResult {

	protected KeyMixingResult keyMixingResult;
	protected BigInteger votingGeneratorExponent;
	protected BigInteger permutation;
    }

}
