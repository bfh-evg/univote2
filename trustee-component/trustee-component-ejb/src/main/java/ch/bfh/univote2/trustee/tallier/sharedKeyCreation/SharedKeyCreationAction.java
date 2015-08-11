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

import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.message.Converter;
import ch.bfh.univote2.component.core.message.CryptoSetting;
import ch.bfh.univote2.component.core.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.SecurePersistenceService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.trustee.BoardsEnum;
import ch.bfh.univote2.trustee.QueryFactory;
import ch.bfh.univote2.trustee.parallel.ParallelUserInput;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
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
public class SharedKeyCreationAction extends AbstractAction implements NotifiableAction {

    private static final String ACTION_NAME = SharedKeyCreationAction.class.getSimpleName();
    private static final String PERSISTENCE_NAME_FOR_SECRET_KEY_FOR_KEY_SHARE = "SECRET_KEY_FOR_KEY_SHARE";

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
	List<PreconditionQuery> preconditionsQuerys = new ArrayList<>();
	return new SharedKeyCreationActionContext(ack, preconditionsQuerys);
    }

    @Override
    protected boolean checkPostCondition(ActionContext actionContext) {
	try {
	    PublicKey publicKey = tenantManager.getPublicKey(actionContext.getTenant());
	    ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
								 QueryFactory.getQueryForEncryptionKeyShare(actionContext.getSection(), publicKey));
	    return !result.getResult().getPost().isEmpty();
	} catch (UnivoteException ex) {
	    logger.log(Level.WARNING, "Could not request encryption key share.", ex);
	    this.informationService.informTenant(actionContext.getActionContextKey(),
						 "Could not check post condition.");
	    return false;
	}
    }

    @Override
    protected void definePreconditions(ActionContext actionContext) {
	ActionContextKey actionContextKey = actionContext.getActionContextKey();
	String section = actionContext.getSection();
	if (!(actionContext instanceof SharedKeyCreationActionContext)) {
	    logger.log(Level.SEVERE, "The actionContext was not the expected one.");
	    return;
	}
	SharedKeyCreationActionContext skcac = (SharedKeyCreationActionContext) actionContext;
	try {
	    CryptoSetting cryptoSetting = this.retrieveCryptoSetting(skcac);
	    skcac.setCryptoSetting(cryptoSetting);
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
	    if (uniboardService.get(bQuery.getBoard(), bQuery.getQuery()).getResult().getPost().isEmpty()) {
		skcac.setAccessRight(false);
		actionContext.getPreconditionQueries().add(bQuery);

	    } else {
		skcac.setAccessRight(true);
	    }

	} catch (UnivoteException ex) {
	    //Add Notification
	    actionContext.getPreconditionQueries().add(bQuery);
	}

    }

    @Override
    @Asynchronous
    public void run(ActionContext actionContext) {
	if (actionContext instanceof SharedKeyCreationActionContext) {
	    SharedKeyCreationActionContext skcac = (SharedKeyCreationActionContext) actionContext;

	    {
		this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
	    }
	    this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
	}
	this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
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

    protected CryptoSetting retrieveCryptoSetting(ActionContext actionContext) throws UnivoteException, Exception {
	ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
							     QueryFactory.getQueryForCryptoSetting(actionContext.getSection()));
	if (result.getResult().getPost().isEmpty()) {
	    throw new UnivoteException("Cryptosetting not published yet.");
	}
	CryptoSetting cryptoSetting = Converter.unmarshal(CryptoSetting.class, result.getResult().getPost().get(0).getMessage());
	return cryptoSetting;

    }

}
