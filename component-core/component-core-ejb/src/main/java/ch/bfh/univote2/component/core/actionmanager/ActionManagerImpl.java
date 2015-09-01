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
package ch.bfh.univote2.component.core.actionmanager;

import ch.bfh.univote2.component.core.services.InitialisationService;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.unicrypt.helper.math.Alphabet;
import ch.bfh.unicrypt.math.algebra.general.classes.FixedStringSet;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.data.BoardNotificationData;
import ch.bfh.univote2.component.core.data.NotificationData;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.NotificationDataAccessor;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.data.TimerPreconditionQuery;
import ch.bfh.univote2.component.core.data.TimerNotificationData;
import ch.bfh.univote2.component.core.data.UserInput;
import ch.bfh.univote2.component.core.data.UserInputPreconditionQuery;
import ch.bfh.univote2.component.core.manager.ConfigurationManager;
import ch.bfh.univote2.component.core.manager.TaskManager;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.services.RegistrationService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
@Startup
@DependsOn("ConfigurationManagerImpl")
public class ActionManagerImpl implements ActionManager {

	static final Logger logger = Logger.getLogger(ActionManagerImpl.class.getName());
	private static final String CONFIGURATION_NAME = "action-graph";
	private static final String CONFIGURATION_INITIAL_ACTION = "initialAction";
	private static final String CONFIGURATION_SEPARATOR = ";";
	//Gives easy access to the mapping between notificationData and actionContext
	private final NotificationDataAccessor notificationDataAccessor;
	//All available contexts
	private final Map<ActionContextKey, ActionContext> actionContexts = new HashMap<>();
	//Stores the graph by saving all successors for any given node
	private Map<String, List<String>> actionGraph = new HashMap<>();
	//Name of the intial action
	private String initialAction;

	/**
	 * Session context. Used to locate the notifiable actions over the JNDI.
	 */
	@Resource(name = "sessionContext")
	private SessionContext sctx;

	/**
	 * RegistrationHelper. Implements the ws-client for the registration on uniboard
	 */
	@EJB
	private RegistrationService registrationService;

	/**
	 * ConfigurationHelper. Gives access to configurations stored in the JNDI.
	 */
	@EJB
	private ConfigurationManager configurationManager;

	/**
	 * TenantManager. Manages all available tenants on this component.
	 */
	@EJB
	private TenantManager tenantManager;
	/**
	 * UserInputManager. Responsible to manage GUI parts of this component.
	 */
	@EJB
	private TaskManager userTaskManager;
	/**
	 * InitialisationHelper. Provides the component specific initial action and query for new sections.
	 */
	@EJB
	private InitialisationService initialisationService;

	/**
	 * TimerService used to create java-ee timers
	 */
	@Resource
	private TimerService timerService;

	public ActionManagerImpl() {
		this.notificationDataAccessor = new NotificationDataAccessor();
	}

	@PostConstruct
	public void init() {
		//Load action graph from the configuration
		this.actionGraph = new HashMap<>();

		Properties config;
		try {
			config = this.configurationManager.getConfiguration(CONFIGURATION_NAME);
		} catch (UnivoteException ex) {
			this.log("No configuration available for the action manager.", Level.SEVERE);
			return;
		}

		//Set init action
		if (config.containsKey(CONFIGURATION_INITIAL_ACTION)) {
			this.initialAction = config.getProperty(CONFIGURATION_INITIAL_ACTION);
			config.remove(CONFIGURATION_INITIAL_ACTION);
		} else {
			this.log("Configuration error: no intial action defined.", Level.SEVERE);
			return;
		}
		//load graph
		for (Entry<Object, Object> e : config.entrySet()) {
			try {
				String action = (String) e.getKey();
				String successors = (String) e.getValue();
				List<String> successorList = new ArrayList(Arrays.asList(successors.split(CONFIGURATION_SEPARATOR)));
				this.actionGraph.put(action, successorList);
			} catch (ClassCastException ex) {
				this.log("Could not load graph entry for: " + e.getKey(), Level.SEVERE);
			}
		}
		//Get list of tennant
		for (String tenant : this.tenantManager.getAllTentants()) {
			for (String section : this.initialisationService.getSections(tenant)) {
				//Check all actions following the initAction
				for (String actionName : this.actionGraph.get(this.initialAction)) {
					this.checkActionState(actionName, tenant, section);
				}
			}
			//Register this tenant for new sections
			NotifiableAction initAction;
			try {
				initAction = this.getAction(this.initialAction);
				ActionContext ac = initAction.prepareContext(tenant, this.initialAction);
				this.actionContexts.put(ac.getActionContextKey(), ac);
				this.registerAction(ac);
			} catch (UnivoteException ex) {
				this.log(ex, Level.SEVERE);
			}
		}
	}

	protected void checkActionState(String actionName, String tenant, String section) {
		if (!this.actionContexts.containsKey(new ActionContextKey(actionName, tenant, section))) {
			try {
				ActionContext ac = this.getAction(actionName).prepareContext(tenant, section);
				this.actionContexts.put(ac.getActionContextKey(), ac);
				if (ac.checkPostCondition()) {
					for (String aName : this.actionGraph.get(ac.getActionContextKey().getAction())) {
						this.checkActionState(aName, tenant, section);
					}
				} else {
					if (ac.getPreconditionQueries().isEmpty()) {
						//All required information is available so the action can be run directly
						this.runAction(ac);
					} else {
						this.registerAction(ac);
						//Need be done in case the action does not ommit already retrieved information in the
						//registration
						this.runAction(ac);
					}
				}
			} catch (UnivoteException ex) {
				this.log(ex, Level.WARNING);
			}

		}
	}

	@PreDestroy
	public void cleanUp() {
		for (String notificationCode : this.notificationDataAccessor.getAllNotificationCodes()) {
			try {
				NotificationData data = this.notificationDataAccessor.findByNotificationCode(notificationCode);
				if (data instanceof BoardNotificationData) {
					BoardNotificationData boardData = (BoardNotificationData) data;
					this.registrationService.unregister(boardData.getBoard(), notificationCode);
				} else if (data instanceof TimerNotificationData) {
					this.cancelTimer(notificationCode);
				}
			} catch (UnivoteException ex) {
				this.log(ex, Level.WARNING);
			}
		}
	}

	@Override
	public void onBoardNotification(String notificationCode, PostDTO post) {

		if (!this.notificationDataAccessor.containsNotificationCode(notificationCode)) {
			this.log("Received unknown notification code for board notification. Code: " + notificationCode,
					Level.INFO);
			this.registrationService.unregisterUnknownNotification(notificationCode);
			return;
		}
		this.onNotification(notificationCode, post);
	}

	@Override
	public void onUserInputNotification(String notificationCode, UserInput userInput) {
		if (!this.notificationDataAccessor.containsNotificationCode(notificationCode)) {
			this.log("Received unknown notification code for user input notification. Code: " + notificationCode,
					Level.INFO);
			return;
		}
		this.onNotification(notificationCode, userInput);
		//remove nc
		this.notificationDataAccessor.removeByNotificationCode(notificationCode);
	}

	@Timeout
	@Override
	public void onTimerNotification(Timer timer) {
		String notificationCode = (String) timer.getInfo();
		if (!this.notificationDataAccessor.containsNotificationCode(notificationCode)) {
			this.log("Received unknown notification code for timer notification. Code: " + notificationCode,
					Level.INFO);
			timer.cancel();
			return;
		}
		this.onNotification(notificationCode, timer);
	}

	protected void onNotification(String notificationCode, Object notifciationObject) {

		NotificationData nData = this.notificationDataAccessor.findByNotificationCode(notificationCode);
		if (!this.actionContexts.containsKey(nData.getActionContextKey())) {
			this.log("Could not find actionContext, but had a valid notificationCondidtion. action: "
					+ nData.getActionContextKey().getAction() + " notification: "
					+ nData.getActionContextKey().getAction(), Level.SEVERE);
			return;
		}
		ActionContext actionContext = this.actionContexts.get(nData.getActionContextKey());
		if (!actionContext.runsInParallel() && actionContext.isInUse()) {
			if (!actionContext.getQueuedNotifications().offer(notifciationObject)) {
				this.log("Could not queue notification for ac:" + actionContext, Level.WARNING);
			}
		} else {
			NotifiableAction action;
			try {
				action = this.getAction(actionContext.getActionContextKey().getAction());
			} catch (UnivoteException ex) {
				this.log(ex, Level.WARNING);
				return;
			}
			action.notifyAction(actionContext, notifciationObject);
		}
	}

	@Override
	public void runAction(String actionName, String tenant, String section) {
		//TODO
		if (this.actionContexts.containsKey(new ActionContextKey(actionName, tenant, section))) {

		}
	}

	@Override
	public void runFinished(ActionContext actionContext, ResultStatus resultStatus) {
		//Check if its the initialisation action
		if (actionContext.getActionContextKey().getAction().equals(this.initialAction)) {
			this.runFinishedInitialisation(actionContext, resultStatus);
			return;
		}
		switch (resultStatus) {
			case FINISHED:
				if (!actionContext.runsInParallel()) {
					actionContext.setInUse(false);
				}
				//Remove existing notifications
				this.unregisterAction(actionContext);
				//Empty context
				actionContext.purge();
				//Check successors
				ActionContextKey ack = actionContext.getActionContextKey();
				for (String successor : this.actionGraph.get(ack.getAction())) {
					this.checkActionState(successor, ack.getTenant(), ack.getSection());
				}
				break;
			case RUN_FINISHED:
				//Check notificationQueue
				if (!actionContext.getQueuedNotifications().isEmpty()) {
					NotifiableAction action;
					try {
						action = this.getAction(actionContext.getActionContextKey().getAction());
					} catch (UnivoteException ex) {
						this.log(ex, Level.SEVERE);
						if (!actionContext.runsInParallel()) {
							actionContext.setInUse(false);
						}
						return;
					}
					action.notifyAction(actionContext, actionContext.getQueuedNotifications().poll());
				} else if (!actionContext.runsInParallel()) {
					actionContext.setInUse(false);
				}
				break;
			case FAILURE:
				if (!actionContext.runsInParallel()) {
					actionContext.setInUse(false);
				}
			//TODO Register a RunActionTask
		}
	}

	protected void runFinishedInitialisation(ActionContext actionContext, ResultStatus resultStatus) {
		//For the intial action FINISHED and RUN_FINISHED are threated the same
		//Only for the FAILURE case we want a differnt behaviour
		switch (resultStatus) {
			case FAILURE:
				this.log("Aborted new context due initial error" + actionContext, Level.WARNING);
				break;
			default:
				//If no error happend start all successor with the new section
				ActionContextKey ack = actionContext.getActionContextKey();
				for (String successor : this.actionGraph.get(ack.getAction())) {
					this.checkActionState(successor, ack.getTenant(), ack.getSection());
				}
				//Notifications of the initial action wont get removed
				break;
		}

	}

	@Override
	public void reRequireUserInput(ActionContext actionContext, UserInputPreconditionQuery inputPreconditionQuery) {
		String newNotificationCode = this.userTaskManager.addUserInputTask(inputPreconditionQuery.getUserInputTask());
		this.notificationDataAccessor.addNotificationData(
				new NotificationData(newNotificationCode, actionContext.getActionContextKey()));
	}

	protected NotifiableAction getAction(String actionName) throws UnivoteException {
		try {
			//First search in the app(ear)
			return (NotifiableAction) sctx.lookup("java:app/" + actionName
					+ "!ch.bfh.univote2.component.core.action.NotifiableAction");
		} catch (Exception ex) {
			try {
				//Then search in the module
				return (NotifiableAction) sctx.lookup("java:module/" + actionName
						+ "!ch.bfh.univote2.component.core.action.NotifiableAction");
			} catch (Exception ex2) {
				throw new UnivoteException("Could not find action with name " + actionName, ex2);
			}
		}
	}

	protected void runAction(ActionContext actionContext) throws UnivoteException {
		if (actionContext.runsInParallel() || !actionContext.isInUse()) {
			NotifiableAction action = this.getAction(actionContext.getActionContextKey().getAction());
			if (!actionContext.runsInParallel()) {
				actionContext.setInUse(true);
			}
			action.run(actionContext);
		}
	}

	protected void registerAction(ActionContext actionContext) throws UnivoteException {

		List<PreconditionQuery> conditions = actionContext.getPreconditionQueries();
		for (PreconditionQuery cond : conditions) {
			if (cond instanceof BoardPreconditionQuery) {
				BoardPreconditionQuery qNC = (BoardPreconditionQuery) cond;
				String newNotificationCode = this.registrationService.register(qNC.getBoard(), qNC.getQuery());
				this.notificationDataAccessor.addNotificationData(new BoardNotificationData(qNC.getBoard(),
						newNotificationCode, actionContext.getActionContextKey()));
			} else if (cond instanceof UserInputPreconditionQuery) {
				UserInputPreconditionQuery uiNC = (UserInputPreconditionQuery) cond;
				String newNotificationCode = this.userTaskManager.addUserInputTask(uiNC.getUserInputTask());

				this.notificationDataAccessor.addNotificationData(
						new NotificationData(newNotificationCode, actionContext.getActionContextKey()));
			} else if (cond instanceof TimerPreconditionQuery) {
				TimerPreconditionQuery tNC = (TimerPreconditionQuery) cond;
				//Get a notificationCode for a timer
				String newNotificationCode = this.createTimer(tNC);
				this.notificationDataAccessor.addNotificationData(new TimerNotificationData(
						newNotificationCode, actionContext.getActionContextKey()));
			} else {
				throw new UnivoteException("Unsupported notification condition " + cond.getClass());
			}
		}
	}

	protected void unregisterAction(ActionContext actionContext) {

		//unregister current process
		if (this.notificationDataAccessor.containsActionContextKey(actionContext.getActionContextKey())) {
			for (NotificationData notificationData : this.notificationDataAccessor.findByActionContextKey(
					actionContext.getActionContextKey())) {
				//Remove from notificationDataAccessor
				this.notificationDataAccessor.removeByNotificationCode(notificationData.getNotifictionCode());
				try {
					//Has to act based on the type of the notification
					//BoardCondition: unregister on the corresponding uniboard
					if (notificationData instanceof BoardNotificationData) {
						BoardNotificationData bnd = (BoardNotificationData) notificationData;
						this.registrationService.unregister(bnd.getBoard(), bnd.getNotifictionCode());
					}
					//No action required for UserInputCondition
					//TimerCondition
					if (notificationData instanceof TimerNotificationData) {
						this.cancelTimer(notificationData.getNotifictionCode());
					}
				} catch (UnivoteException ex) {
					this.log(ex, Level.WARNING);
				}
			}
		}
	}

	protected String createTimer(TimerPreconditionQuery timerNotificationCondition) {
		FixedStringSet fixedStringSet = FixedStringSet.getInstance(Alphabet.UPPER_CASE, 20);
		String notificationCode = fixedStringSet.getRandomElement().getValue();
		this.timerService.createTimer(timerNotificationCondition.getDate(), notificationCode);
		logger.log(Level.FINE, "Created new timer notificationCode = {0} and Date {1}",
				new Object[]{notificationCode, timerNotificationCondition.getDate()});
		return notificationCode;
	}

	protected void cancelTimer(String notificationCode) {
		Collection<Timer> timers = timerService.getTimers();
		for (Timer t : timers) {
			String timerNC = (String) t.getInfo();
			if (notificationCode.equals(timerNC)) {
				t.cancel();
			}
		}
	}

	protected void log(Exception ex, Level level) {
		logger.log(level, ex.getMessage());
		if (ex.getCause() != null) {
			logger.log(level, ex.getCause().getMessage());
		}
	}

	protected void log(String mesg, Level level) {
		logger.log(level, mesg);
	}

	/*
	 Methods used for testing
	 */
	protected void addActionContext(ActionContext actionContext) {
		this.actionContexts.put(actionContext.getActionContextKey(), actionContext);
	}

	protected void addActionGraphEntry(String actionName, List<String> successors) {
		this.actionGraph.put(actionName, successors);
	}

	protected Map<String, List<String>> getActionGraph() {
		return this.actionGraph;
	}

	protected void addNotificationData(NotificationData notificationData) {
		this.notificationDataAccessor.addNotificationData(notificationData);
	}

	protected NotificationDataAccessor getNotificationDataAccessor() {
		return this.notificationDataAccessor;
	}

	protected void setInitialAction(String action) {
		this.initialAction = action;
	}

}
