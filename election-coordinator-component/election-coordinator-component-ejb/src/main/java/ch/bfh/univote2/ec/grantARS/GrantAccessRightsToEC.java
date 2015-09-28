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
package ch.bfh.univote2.ec.grantARS;

import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.MessageFactory;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class GrantAccessRightsToEC extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = "GrantAccessRightsToEC";
	private static final Logger logger = Logger.getLogger(GrantAccessRightsToEC.class.getName());

	@EJB
	private ActionManager actionManager;
	@EJB
	private InformationService informationService;
	@EJB
	private UniboardService uniboardService;

	@EJB
	private TenantManager tenantManager;

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		return new GrantAccessRightsToECContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		// TODO: Check if permissions already granted!
		return false;
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		// No preconditions known for this Action
	}

	@Override
	public void run(ActionContext actionContext) {
		if (!(actionContext instanceof GrantAccessRightsToECContext)) {
			return;
		}
		GrantAccessRightsToECContext context = (GrantAccessRightsToECContext) actionContext;
		try {
			internalRun(context);
		} catch (UnivoteException ex) {
			Logger.getLogger(GrantAccessRightsToEC.class.getName()).log(Level.SEVERE, null, ex);
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	@Override
	public void notifyAction(ActionContext actionContext, Object notification) {
		// No notifications known for this Action
	}

	private void internalRun(GrantAccessRightsToECContext context) throws UnivoteException {

		PublicKey publicKey = tenantManager.getPublicKey(context.getTenant());
		// Add infinite AccessRight for EC to election definition
		{
			byte[] message = MessageFactory.createAccessRight(GroupEnum.ELECTION_DEFINITION, publicKey);
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), message, context.getTenant());
			this.informationService.informTenant(context.getActionContextKey(),
					"new AccessRight for election definition granted.");
		}
		// Add infinite AccessRight for EC to cancelled voter certificates
		{
			byte[] message = MessageFactory.createAccessRight(GroupEnum.CANCELLED_VOTER_CERTIFICATE, publicKey);
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), message, context.getTenant());
			this.informationService.informTenant(context.getActionContextKey(),
					"new AccessRight for 'cancelled voter certificate' granted.");
		}
		// Add infinite AccessRight for EC to added voter certificates
		{
			byte[] message = MessageFactory.createAccessRight(GroupEnum.ADDED_VOTER_CERTIFICATE, publicKey);
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), message, context.getTenant());
			this.informationService.informTenant(context.getActionContextKey(),
					"new AccessRight for 'added voter certificate' granted.");
		}

		// Add infinite AccessRight for EC to new voter certificates
		{
			byte[] message = MessageFactory.createAccessRight(GroupEnum.NEW_VOTER_CERTIFICATE, publicKey);
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), message, context.getTenant());
			this.informationService.informTenant(context.getActionContextKey(),
					"new AccessRight for 'new voter certificate' granted.");
		}
	}
}
