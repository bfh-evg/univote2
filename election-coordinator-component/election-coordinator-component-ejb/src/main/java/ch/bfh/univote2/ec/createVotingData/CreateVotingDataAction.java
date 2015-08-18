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
package ch.bfh.univote2.ec.createVotingData;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.message.ElectionDefinition;
import ch.bfh.univote2.component.core.message.ElectionIssue;
import ch.bfh.univote2.component.core.message.ElectoralRoll;
import ch.bfh.univote2.component.core.message.EncryptionKey;
import ch.bfh.univote2.component.core.message.JSONConverter;
import ch.bfh.univote2.component.core.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;

/**
 *
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
public class CreateVotingDataAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = "DefineEAAction";
	private static final Logger logger = Logger.getLogger(CreateVotingDataAction.class.getName());

	@EJB
	private ActionManager actionManager;
	@EJB
	private InformationService informationService;
	@EJB
	private UniboardService uniboardService;

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		this.informationService.informTenant(ack, "Created new context for " + ACTION_NAME);
		return new CreateVotingDataActionContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void run(ActionContext actionContext) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * If notified we expect one out of four different messages. These messages are:
	 * {@link ch.bfh.univote2.component.core.message.ElectionDefinition},
	 * {@link ch.bfh.univote2.component.core.message.EncryptionKey},
	 * {@link ch.bfh.univote2.component.core.message.ElectionIssue}, and
	 * {@link ch.bfh.univote2.component.core.message.ElectoralRoll}. Each message is stored in the context
	 * given. If the context contains all four messages then the action is completed else it is postponed.
	 *
	 * @param actionContext the action context for this action
	 * @param notification the notification containing one of the expected messages
	 */
	@Override
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (actionContext instanceof CreateVotingDataActionContext) {
			CreateVotingDataActionContext context = (CreateVotingDataActionContext) actionContext;
			if (notification instanceof PostDTO) {
				PostDTO post = (PostDTO) notification;
				//AttributeDTO group = AttributeHelper.searchAttribute(post.getAlpha(), AlphaEnum.GROUP.getValue());
				//if(((StringValueDTO) group.getValue()).getValue().equals(GroupEnum.ELECTION_DEFINITION));
				try {
					parsePostDTO(post.getMessage(), context);
					// Let's see iff we got all messages
					if (context.gotAllNotifications()) {
						runInternal(context); // handles failures, too...
					} else {
						// Wait for another notification
						this.actionManager.runFinished(context, ResultStatus.RUN_FINISHED);
					}
				} catch (UnivoteException ex) {
					logger.log(Level.WARNING, "Do not understand message.", ex);
					this.informationService.informTenant(actionContext.getActionContextKey(),
							"Do not understand message.");
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				}
			}
		} else {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Unsupported context.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	/**
	 * Given a byte[] JSON message, let's try to parse it in order to otain one
	 * of:
	 * @param message
	 * @param context
	 * @throws UnivoteException
	 */
	private void parsePostDTO(byte[] message, CreateVotingDataActionContext context)
			throws UnivoteException {
		ElectionDefinition electionDefinition;
		EncryptionKey encryptionKey;
		ElectionIssue electionIssue;
		ElectoralRoll electoralRoll;
		// The message could be one out of the four above declared types. Let's
		// try to get one and store it in the context. Abort with an exception
		// if none of the types is matched.
		// NOTE: A single, own instance is used in order to not intermingle with
		// other clients of the converter.
		JSONConverter converter = new JSONConverter();
		if (converter.isOfType(ElectionDefinition.class, message)) {
			// Get electionDefinition and add it to the context.
			electionDefinition = converter.getUnmarshalledMessage();
			context.setElectionDefinition(electionDefinition);
		} else if (converter.isOfType(EncryptionKey.class, message)) {
			// Get encryptionKey and add it to the context.
			encryptionKey = converter.getUnmarshalledMessage();
			context.setEncryptionKey(encryptionKey);
		} else if (converter.isOfType(ElectionIssue.class, message)) {
			// Get electionIssue and add it to the context.
			electionIssue = converter.getUnmarshalledMessage();
			context.setElectionIssue(electionIssue);
		} else if (converter.isOfType(ElectoralRoll.class, message)) {
			// Get electoralRoll and add it to the context.
			electoralRoll = converter.getUnmarshalledMessage();
			context.setElectoralRoll(electoralRoll);
		} else {
			// Fatal error: Do not understand message.
			throw new UnivoteException("Do not understand message.");
		}
	}

	private void runInternal(CreateVotingDataActionContext context) {
		// TODO: Adjust table in subsectino 11.2.5
		ElectionDefinition ed = context.getElectionDefinition();
		// ...?
		//VotingData vd = ...;
		try {
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.VOTING_DATA.getValue(), JSONConverter.marshal(new byte[1]).getBytes(),
					context.getTenant());
			// TOTO: Check with Sevi whether some log message should be sent to the logger and information service...
			//logger. ...
			//this.informationService. ...
			// TODO: Check with Sevi whether the action manager needs to be notified here...
			this.actionManager.runFinished(context, ResultStatus.FINISHED);
		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not post VOTING_DATA message.", ex);
			this.informationService.informTenant(context.getActionContextKey(),
					"Could not post VOTING_DATA message.");
			this.actionManager.runFinished(context, ResultStatus.FAILURE);
		}
	}
}
