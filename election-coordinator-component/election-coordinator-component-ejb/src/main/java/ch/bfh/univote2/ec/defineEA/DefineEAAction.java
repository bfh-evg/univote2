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
package ch.bfh.univote2.ec.defineEA;

import ch.bfh.uniboard.clientlib.GetException;
import ch.bfh.uniboard.clientlib.GetHelper;
import ch.bfh.uniboard.clientlib.KeyHelper;
import ch.bfh.uniboard.clientlib.signaturehelper.SignatureException;
import ch.bfh.uniboard.data.AlphaIdentifierDTO;
import ch.bfh.uniboard.data.AttributesDTO;
import ch.bfh.uniboard.data.ConstraintDTO;
import ch.bfh.uniboard.data.EqualDTO;
import ch.bfh.uniboard.data.IdentifierDTO;
import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.data.UserInputPreconditionQuery;
import ch.bfh.univote2.component.core.data.UserInputTask;
import ch.bfh.univote2.component.core.manager.ConfigurationManager;
import ch.bfh.univote2.component.core.query.AlphaEnum;
import ch.bfh.univote2.component.core.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class DefineEAAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = "DefineEAAction";
	private static final String INPUT_NAME = "EAName";
	private static final String CONFIG_NAME = "uniboard-helper";
	private static final Logger logger = Logger.getLogger(DefineEAAction.class.getName());

	@EJB
	ActionManager actionManager;
	@EJB
	InformationService informationService;
	@EJB
	UniboardService uniboardService;
	@EJB
	ConfigurationManager configurationManager;

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		List<PreconditionQuery> preconditionsQuerys = new ArrayList<>();
		this.informationService.informTenant(ack, "Created new context.");
		return new DefineEAActionContext(ack, preconditionsQuerys);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {

		try {
			ResultContainerDTO result
					= this.uniboardService.get(this.getQueryForEACert(actionContext.getSection()));
			return !result.getResult().getPost().isEmpty();
		} catch (UnivoteException ex) {
			//TODO
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not check post condition.");
			return false;
		}
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		//Add UserInput
		UserInputPreconditionQuery uiQuery = new UserInputPreconditionQuery(new UserInputTask(INPUT_NAME,
				actionContext.getActionContextKey().getTenant(),
				actionContext.getActionContextKey().getSection()));
		actionContext.getPreconditionQueries().add(uiQuery);
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof DefineEAActionContext) {
			DefineEAActionContext deaa = (DefineEAActionContext) actionContext;
			if (!deaa.getName().isEmpty()) {
				this.runInternal(deaa);
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
		if (notification instanceof EANameUserInput) {
			EANameUserInput aeui = (EANameUserInput) notification;
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Entred value: " + aeui.getName());
			if (actionContext instanceof DefineEAActionContext) {
				DefineEAActionContext deaa = (DefineEAActionContext) actionContext;
				deaa.setName(aeui.getName());
				this.runInternal(deaa);
			} else {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Unsupported context.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			}
		} else {
			this.informationService.informTenant(actionContext.getActionContextKey(), "Unknown notification: "
					+ notification.toString());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}

	}

	private void runInternal(DefineEAActionContext actionContext) {
		try {
			//TODO Get Certificate from UniCert

			//Create message from the retrieved certificate
			byte[] message = null;
			AttributesDTO Attr = this.uniboardService.post(actionContext.getSection(),
					GroupEnum.ADMIN_CERT.getValue(), message, actionContext.getTenant());
			//TODO Store the attributes?
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not post message.");
			logger.log(Level.WARNING, "Could not post message. context: {0}. ex: {1}",
					new Object[]{actionContext.getActionContextKey(), ex.getMessage()});
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	protected QueryDTO getQueryForEACert(String section) {
		QueryDTO query = new QueryDTO();
		IdentifierDTO identifier = new AlphaIdentifierDTO();
		identifier.getPart().add(AlphaEnum.SECTION.getValue());
		ConstraintDTO constraint = new EqualDTO(identifier, new StringValueDTO(section));
		query.getConstraint().add(constraint);

		IdentifierDTO identifier2 = new AlphaIdentifierDTO();
		identifier2.getPart().add(AlphaEnum.GROUP.getValue());
		ConstraintDTO constraint2 = new EqualDTO(identifier, new StringValueDTO(GroupEnum.ADMIN_CERT.getValue()));
		query.getConstraint().add(constraint2);
		return query;
	}

	public ResultContainerDTO getFromUniCert(QueryDTO query) throws UnivoteException {
		String wsdlLocation = this.configurationManager.getConfiguration(CONFIG_NAME).getProperty("wsdlLocation");
		String endPointUrl = this.configurationManager.getConfiguration(CONFIG_NAME).getProperty("endPointUrl");
		try {
			GetHelper getHelper = new GetHelper(this.getBoardKey(), wsdlLocation, endPointUrl);
			return getHelper.get(query);
		} catch (GetException ex) {
			throw new UnivoteException("Could not create wsclient.", ex);
		} catch (SignatureException ex) {
			throw new UnivoteException("Could not verify answer.", ex);
		}

	}

	protected PublicKey getBoardKey() throws UnivoteException {
		Properties config = this.configurationManager.getConfiguration(CONFIG_NAME);
		BigInteger y = new BigInteger(config.getProperty("y"));
		BigInteger p = new BigInteger(config.getProperty("p"));
		BigInteger q = new BigInteger(config.getProperty("q"));
		BigInteger g = new BigInteger(config.getProperty("g"));
		try {
			return KeyHelper.createDSAPublicKey(p, q, g, y);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
			throw new UnivoteException("Could not create publicKey for UniBoard", ex);
		}
	}

}
