/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniVote2.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote2.component.core.actionmanager;

import ch.bfh.univote2.component.core.services.InitialisationService;
import ch.bfh.uniboard.data.PostDTO;
import static ch.bfh.unicrypt.helper.Alphabet.UPPER_CASE;
import ch.bfh.unicrypt.math.algebra.general.classes.FixedStringSet;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.data.BoardNotificationData;
import ch.bfh.univote2.component.core.data.NotificationData;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.NotificationDataAccessor;
import ch.bfh.univote2.component.core.data.ResultContext;
import ch.bfh.univote2.component.core.data.TimerPreconditionQuery;
import ch.bfh.univote2.component.core.data.TimerNotificationData;
import ch.bfh.univote2.component.core.data.UserInput;
import ch.bfh.univote2.component.core.data.UserInputPreconditionQuery;
import ch.bfh.univote2.component.core.manager.ConfigurationManager;
import ch.bfh.univote2.component.core.manager.TaskManager;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.services.RegistrationService;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
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
public class ActionManagerImpl implements ActionManager {

	static final Logger logger = Logger.getLogger(ActionManagerImpl.class.getName());
	private static final String CONFIGURATION_NAME = "action-graph";
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
	RegistrationService registrationService;

	/**
	 * ConfigurationHelper. Gives access to configurations stored in the JNDI.
	 */
	@EJB
	ConfigurationManager configurationManager;

	/**
	 * TenantManager. Manages all available tenants on this component.
	 */
	@EJB
	TenantManager tenantManager;
	/**
	 * UserInputManager. Responsible to manage GUI parts of this component.
	 */
	@EJB
	TaskManager userTaskManager;
	/**
	 * InitialisationHelper. Provides the component specific initial action and query for new sections.
	 */
	@EJB
	InitialisationService initialisationService;

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
		//TODO load graph and invert from predecessor based to successor based as it is queried that way
		//TODO Set init action
		//Get list of tennant
		for (String tenant : this.tenantManager.getAllTentants()) {
			for (String section : this.initialisationService.getSections(tenant)) {
				for (String actionName : this.actionGraph.get(this.initialAction)) {
					this.checkActionState(actionName, tenant, section);
				}
			}
			//Register this tenant for new sections
			//TODO

		}
	}

	protected void checkActionState(String actionName, String tenant, String section) {
		if (!this.actionContexts.containsKey(new ActionContextKey(actionName, tenant, section))) {
			try {
				ActionContext ac = this.getAction(actionName).prepareContext(tenant, section);
				this.actionContexts.put(new ActionContextKey(section, tenant, section), ac);
				if (ac.checkPostCondition()) {
					for (String aName : this.actionGraph.get(ac.getActionContextKey().getAction())) {
						this.checkActionState(tenant, section, aName);
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
				this.log(ex);
			}

		}
	}

	@PreDestroy
	public void cleanUp() {
		for (String notificationCode : this.notificationDataAccessor.getAllNotificationCodes()) {
			try {
				//TODO
				this.registrationService.unregister("", notificationCode);
			} catch (UnivoteException ex) {
				this.log(ex);
			}
		}
	}

	@Override
	public void onBoardNotification(String notificationCode, PostDTO post) {

		if (!this.notificationDataAccessor.containsNotificationCode(notificationCode)) {
			logger.log(Level.INFO, "Received unknown notification code for board notification. {0}", notificationCode);
			this.registrationService.unregisterUnknownNotification(notificationCode);
			return;
		}
		NotificationData nData = this.notificationDataAccessor.findByNotificationCode(notificationCode);
		ActionContext actionContext = this.actionContexts.get(nData.getActionContextKey());
		if (actionContext.isInUse()) {
			if (!actionContext.queuedNotifications.offer(post)) {
				this.log("Could not queue post for ac:" + actionContext);
			}
		} else {
			NotifiableAction action;
			try {
				action = this.getAction(nData.getActionContextKey().getAction());
			} catch (UnivoteException ex) {
				this.log(ex);
				return;
			}
			action.notifyAction(actionContext, post);
		}
	}

	@Override
	public void onUserInputNotification(String notificationCode, UserInput userInput) {
		if (!this.notificationDataAccessor.containsNotificationCode(notificationCode)) {
			logger.log(Level.INFO, "Received unknown notification code for user input. {0}", notificationCode);
			return;
		}
		NotificationData nData = this.notificationDataAccessor.findByNotificationCode(notificationCode);
		ActionContext actionContext = this.actionContexts.get(nData.getActionContextKey());
		if (actionContext.isInUse()) {
			if (!actionContext.queuedNotifications.offer(userInput)) {
				this.log("Could not queue userinput for ac:" + actionContext);
			}
		} else {
			NotifiableAction action;
			try {
				action = this.getAction(nData.getActionContextKey().getAction());
			} catch (UnivoteException ex) {
				this.log(ex);
				return;
			}
			action.notifyAction(actionContext, userInput);
		}
	}

	@Timeout
	@Override
	public void onTimerNotification(Timer timer) {
		String notificationCode = (String) timer.getInfo();
		NotificationData nData = this.notificationDataAccessor.findByNotificationCode(notificationCode);
		ActionContext actionContext = this.actionContexts.get(nData.getActionContextKey());
		if (actionContext.isInUse()) {
			if (!actionContext.queuedNotifications.offer(timer)) {
				this.log("Could not queue timer for ac:" + actionContext);
			}
		} else {
			NotifiableAction action;
			try {
				action = this.getAction(nData.getActionContextKey().getAction());
			} catch (UnivoteException ex) {
				this.log(ex);
				return;
			}
			//TODO Check if in use
			action.notifyAction(actionContext, timer);
		}
	}

	@Override
	@Asynchronous
	public void runFinished(ActionContext actionContext, ResultContext resultContext) {
		//TODO Check if its the initialisation action
		//TODO Switch ResultStatus
		//TODO FINISHED
		//TODO Empty context
		//TODO Check successors
		//
		//TODO RUN_FINISHED
		//TODO Save context
		//TODO Check notificationQueue
		//TODO Set InUse = false
		//
		//TODO FAILURE
		//TODO Log error?
		//TODO Set InUse = false
	}

	protected void runFinishedInitialisation(ActionContext actionContext) {
		//TODO
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
		if (!actionContext.isInUse()) {
			NotifiableAction action = this.getAction(actionContext.getActionContextKey().getAction());
			actionContext.setInUse(true);
			action.run(actionContext);
		} else {
			//TODO Add to queue
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
				String newNotificationCode = this.userTaskManager.addTask(uiNC.getUserInputRequest());
				if (newNotificationCode != null) {
					this.notificationDataAccessor.addNotificationData(
							new NotificationData(newNotificationCode, actionContext.getActionContextKey()));
				}
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
				this.log(ex);
			}
		}
	}

	protected String createTimer(TimerPreconditionQuery timerNotificationCondition) {
		FixedStringSet fixedStringSet = FixedStringSet.getInstance(UPPER_CASE, 20);
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

	protected void log(Exception ex) {
		logger.log(Level.WARNING, ex.getMessage());
		if (ex.getCause() != null) {
			logger.log(Level.WARNING, ex.getCause().getMessage());
		}
	}

	protected void log(String mesg) {
		logger.log(Level.WARNING, mesg);
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
}
