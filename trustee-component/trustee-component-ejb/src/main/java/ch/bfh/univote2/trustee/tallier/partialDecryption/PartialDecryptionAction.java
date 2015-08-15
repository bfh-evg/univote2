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
import ch.bfh.uniboard.data.Transformer;
import ch.bfh.uniboard.service.Attributes;
import ch.bfh.uniboard.service.StringValue;
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
import ch.bfh.univote2.component.core.message.MixedVotes;
import ch.bfh.univote2.component.core.message.Vote;
import ch.bfh.univote2.component.core.query.AlphaEnum;
import ch.bfh.univote2.component.core.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.SecurePersistenceService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.trustee.BoardsEnum;
import ch.bfh.univote2.trustee.QueryFactory;
import ch.bfh.univote2.trustee.TrusteeActionHelper;
import ch.bfh.univote2.trustee.UniCryptCryptoSetting;
import ch.bfh.univote2.trustee.tallier.sharedKeyCreation.SharedKeyCreationAction;
import java.math.BigInteger;
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
								 QueryFactory.getQueryForPartialDecryption(actionContext.getSection(), publicKey));
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
	if (!(actionContext instanceof PartialDecryptionActionContext)) {
	    logger.log(Level.SEVERE, "The actionContext was not the expected one.");
	    return;
	}
	PartialDecryptionActionContext pdac = (PartialDecryptionActionContext) actionContext;
	TrusteeActionHelper.checkAndSetCryptoSetting(pdac, uniboardService, tenantManager, informationService, logger);
	TrusteeActionHelper.checkAndSetAccsessRight(pdac, GroupEnum.PARTIAL_DECRYPTION, uniboardService, tenantManager, informationService, logger);
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
	if (actionContext.getMixedVotes() == null) {
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
	//The following if is strange, as the run should not happen in this case?!
	if (pdac.isPreconditionReached() == null) {
	    logger.log(Level.WARNING, "Run was called but preCondition is unknown in Context.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	    return;
	}
	if (Objects.equals(pdac.isPreconditionReached(), Boolean.FALSE)) {
	    logger.log(Level.WARNING, "Run was called but preCondition is not yet reached.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	    return;
	}
	String tenant = actionContext.getTenant();
	String section = actionContext.getSection();

	CryptoSetting cryptoSetting = pdac.getCryptoSetting();
	if (cryptoSetting == null) {
	    logger.log(Level.SEVERE, "Precondition is reached but crypto setting is empty in Context. That is bad.");
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Error: Precondition reached, but no crypto setting available.");
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	    return;
	}
	try {
	    UniCryptCryptoSetting uniCryptCryptoSetting
		    = TrusteeActionHelper.getUnicryptCryptoSetting(cryptoSetting);

	    try {
		BigInteger privateKey = securePersistenceService.retrieve(tenant, section, SharedKeyCreationAction.PERSISTENCE_NAME_FOR_SECRET_KEY_FOR_KEY_SHARE);
		CyclicGroup cyclicGroup = uniCryptCryptoSetting.encryptionGroup;
		Element secretKey = cyclicGroup.getElementFrom(privateKey);

		Tuple alphas = Tuple.getInstance();
		for (Vote v : pdac.getMixedVotes().getMixedVotes()) {
		    alphas.add(cyclicGroup.getElementFrom(v.getFirstValue()));
		}
		//TODO: GeneratorFunction MultiApply
		//TODO: NIZKP,  private key known AND each entry treated with private Key^-1.
		//TODO: Post Partial-Decryption

	    } catch (UnivoteException ex) {
		//No key available. Unsolvable problem encountered.
		this.informationService.informTenant(actionContext.getActionContextKey(),
						     "Could access private key for decryption. Action failed.");
		Logger.getLogger(PartialDecryptionAction.class.getName()).log(Level.SEVERE, null, ex);
		this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	    }
	} catch (UnivoteException ex) {
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Could not post key share for encrcyption. Action failed.");
	    Logger.getLogger(PartialDecryptionAction.class.getName()).log(Level.SEVERE, null, ex);
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	} catch (Exception ex) {
	    Logger.getLogger(PartialDecryptionAction.class.getName()).log(Level.SEVERE, null, ex);
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Could not marshal key share for encrcyption. Action failed.");
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
	    } else if (pdac.getMixedVotes() == null) {
		MixedVotes mixedVotes = JSONConverter.unmarshal(MixedVotes.class, post.getMessage());
		pdac.setMixedVotes(mixedVotes);
	    }
	    run(actionContext);
	} catch (UnivoteException ex) {
	    this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	} catch (Exception ex) {
	    Logger.getLogger(PartialDecryptionAction.class.getName()).log(Level.SEVERE, null, ex);
	    this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
	    this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	}
    }

    protected MixedVotes retrieveMixedVotes(ActionContext actionContext) throws UnivoteException, Exception {
	ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						    QueryFactory.getQueryForMixedVotes(actionContext.getSection())).getResult();
	if (result.getPost().isEmpty()) {
	    throw new UnivoteException("mixed votes not published yet.");

	}
	MixedVotes cryptoSetting = JSONConverter.unmarshal(MixedVotes.class, result.getPost().get(0).getMessage());
	return cryptoSetting;

    }

}
