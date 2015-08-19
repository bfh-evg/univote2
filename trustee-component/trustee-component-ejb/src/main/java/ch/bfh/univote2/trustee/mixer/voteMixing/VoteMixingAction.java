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
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.ChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.classes.PermutationCommitmentProofSystem;
import ch.bfh.unicrypt.crypto.proofsystem.classes.ReEncryptionShuffleProofSystem;
import ch.bfh.unicrypt.crypto.schemes.commitment.classes.PermutationCommitmentScheme;
import ch.bfh.unicrypt.crypto.schemes.encryption.interfaces.ReEncryptionScheme;
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.function.classes.PermutationFunction;
import ch.bfh.unicrypt.random.classes.PseudoRandomOracle;
import ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomOracle;
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
import ch.bfh.univote2.component.core.message.MixedVotes;
import ch.bfh.univote2.component.core.query.AlphaEnum;
import ch.bfh.univote2.component.core.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.SecurePersistenceService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.trustee.BoardsEnum;
import ch.bfh.univote2.trustee.QueryFactory;
import ch.bfh.univote2.trustee.TrusteeActionHelper;
import ch.bfh.univote2.trustee.UniCryptCryptoSetting;
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
								 QueryFactory.getQueryForVoteMixingResult(actionContext.getSection(), publicKey));
	    if (!result.getResult().getPost().isEmpty()) {
		return true;
	    }
	} catch (UnivoteException ex) {
	    logger.log(Level.WARNING, "Could not request vote mixing result.", ex);
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
	if (!(actionContext instanceof VoteMixingActionContext)) {
	    logger.log(Level.SEVERE, "The actionContext was not the expected one.");
	    return;
	}
	VoteMixingActionContext vmac = (VoteMixingActionContext) actionContext;
	TrusteeActionHelper.checkAndSetCryptoSetting(vmac, uniboardService, tenantManager, informationService, logger);
	TrusteeActionHelper.checkAndSetAccsessRight(vmac, GroupEnum.VOTE_MIXING_RESULT, uniboardService, tenantManager, informationService, logger);

	this.checkAndSetVoteMixingRequest(vmac);
    }

    protected void checkAndSetVoteMixingRequest(VoteMixingActionContext actionContext) {
	ActionContextKey actionContextKey = actionContext.getActionContextKey();
	String section = actionContext.getSection();
	try {
	    MixedVotes voteMixingRequest = actionContext.getVoteMixingRequest();

	    //Add Notification
	    if (voteMixingRequest == null) {
		voteMixingRequest = retrieveVoteMixingRequest(actionContext);
		actionContext.setVoteMixingRequest(voteMixingRequest);
	    }

	} catch (UnivoteException ex) {
	    logger.log(Level.WARNING, "Could not get mixedVotes.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error retrieving mixed votes: " + ex.getMessage());
	} catch (JsonException ex) {
	    logger.log(Level.WARNING, "Could not parse mixed votes.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error reading mixed votes.");
	} catch (Exception ex) {
	    logger.log(Level.WARNING, "Could not parse mixed votes.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error reading mixed votes.");
	}
	if (actionContext.getVoteMixingRequest() == null) {
	    //Add Notification
	    BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
		    QueryFactory.getQueryForVoteMixingRequest(section, tenantManager.getPublicKey(tenant)), BoardsEnum.UNIVOTE.getValue());
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
	skcac.get
	try {
	    UniCryptCryptoSetting uniCryptCryptoSetting = TrusteeActionHelper.getUnicryptCryptoSetting(cryptoSetting);
	    // Ciphertexts
	    Tuple rV = ProductGroup.getInstance(Z_q, size).getRandomElement();
	    ProductGroup uVSpace = ProductGroup.getInstance(ProductGroup.getInstance(G_q, 2), size);
	    Tuple uV = uVSpace.getRandomElement();
	    Element[] uPrimes = new Element[size];
	    for (int i = 0; i < size; i++) {
		uPrimes[i] = encryptionScheme.reEncrypt(encryptionPK, uV.getAt(i), rV.getAt(i));
	    }
	    Tuple uPrimeV = PermutationFunction.getInstance(ProductGroup.getInstance(G_q, 2), size).apply(Tuple.getInstance(uPrimes), pi);

	    return Triple.getInstance(uV, uPrimeV, rV);

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

    public void proofOfShuffle(int size, CyclicGroup G_q, ReEncryptionScheme encryptionScheme, Element encryptionPK, PermutationElement pi, Tuple uV, Tuple uPrimeV, Tuple rV) {

	final RandomOracle ro = PseudoRandomOracle.getInstance();
	final ReferenceRandomByteSequence rrs = ReferenceRandomByteSequence.getInstance();

	// Permutation commitment
	PermutationCommitmentScheme pcs = PermutationCommitmentScheme.getInstance(G_q, size, rrs);
	Tuple sV = pcs.getRandomizationSpace().getRandomElement();
	Tuple cPiV = pcs.commit(pi, sV);
	System.out.println("Permutation Commitment");

	// Permutation commitment proof generator
	SigmaChallengeGenerator scg = PermutationCommitmentProofSystem.createNonInteractiveSigmaChallengeGenerator(G_q, size, kc, proverId, ro);
	ChallengeGenerator ecg = PermutationCommitmentProofSystem.createNonInteractiveEValuesGenerator(G_q, size, ke, ro);
	PermutationCommitmentProofSystem pcps = PermutationCommitmentProofSystem.getInstance(scg, ecg, G_q, size, kr, rrs);

	// Shuffle Proof Generator
	SigmaChallengeGenerator scgS = ReEncryptionShuffleProofSystem.createNonInteractiveSigmaChallengeGenerator(G_q, encryptionScheme, size, kc, proverId, ro);
	ChallengeGenerator ecgS = ReEncryptionShuffleProofSystem.createNonInteractiveEValuesGenerator(G_q, encryptionScheme, size, ke, ro);
	ReEncryptionShuffleProofSystem sps = ReEncryptionShuffleProofSystem.getInstance(scgS, ecgS, G_q, size, encryptionScheme, encryptionPK, kr, rrs);

	// Proof
	Pair proofPermutation = pcps.generate(Pair.getInstance(pi, sV), cPiV);
	Tuple privateInput = Tuple.getInstance(pi, sV, rV);
	Tuple publicInput = Tuple.getInstance(cPiV, uV, uPrimeV);
	Triple proofShuffle = sps.generate(privateInput, publicInput);
	System.out.println("Shuffle-Proof");
    }

}
