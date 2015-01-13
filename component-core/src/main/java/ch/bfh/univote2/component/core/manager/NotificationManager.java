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
package ch.bfh.univote2.component.core.manager;

import ch.bfh.univote2.component.core.helper.InitialisationHelper;
import ch.bfh.uniboard.data.PostDTO;
import static ch.bfh.unicrypt.helper.Alphabet.UPPER_CASE;
import ch.bfh.unicrypt.math.algebra.general.classes.FixedStringSet;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.data.ActionContext;
import ch.bfh.univote2.component.core.data.BoardNotificationData;
import ch.bfh.univote2.component.core.data.NotificationData;
import ch.bfh.univote2.component.core.data.NotificationDataAccessor;
import ch.bfh.univote2.component.core.data.NotificationCondition;
import ch.bfh.univote2.component.core.data.QueryNotificationCondition;
import ch.bfh.univote2.component.core.data.TimerNotificationCondition;
import ch.bfh.univote2.component.core.data.TimerNotificationData;
import ch.bfh.univote2.component.core.data.UserInput;
import ch.bfh.univote2.component.core.data.UserInputNotificationCondition;
import ch.bfh.univote2.component.core.helper.RegistrationHelper;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
public class NotificationManager {

	static final Logger logger = Logger.getLogger(NotificationManager.class.getName());
	private static final String CONFIGURATION_NAME = "action-list";
	private final NotificationDataAccessor notificationDataAccessor;
	private List<String> actionList;

	/**
	 * Session context. Used to locate the notifiable actions over the JNDI.
	 */
	@Resource(name = "sessionContext")
	private SessionContext sctx;

	/**
	 * RegistrationHelper. Implements the ws-client for the registration on uniboard
	 */
	@EJB
	RegistrationHelper registrationHelper;

	/**
	 * ConfigurationHelper. Gives access to configurations stored in the JNDI.
	 */
	@EJB
	ConfigurationManager configurationManager;

	/**
	 * TenantManager. Manges all alvailable tenants on this component.
	 */
	@EJB
	TenantManager tenantManager;
	/**
	 * UserInputManager. Responsible to manage GUI parts of this component.
	 */
	@EJB
	UserInputManager userInputManager;
	/**
	 * InitialisationHelper. Provides the component specific initial action and query for new sections.
	 */
	@EJB
	InitialisationHelper initialisationHelper;

	/**
	 * TimerService used to create java-ee timers
	 */
	@Resource
	private TimerService timerService;

	public NotificationManager() {
		this.notificationDataAccessor = new NotificationDataAccessor();
	}

	@PostConstruct
	public void init() {
		//Load action list from the configuration
		Properties tmp = this.configurationManager.getConfiguration(CONFIGURATION_NAME);
		SortedSet<String> sorted = new TreeSet<>(tmp.stringPropertyNames());
		for (String str : sorted) {
			this.actionList.add(tmp.getProperty(str));
		}
		//Get list of tennants
		for (String tenant : this.tenantManager.getUnlockedTenants()) {
			for (String section : this.initialisationHelper.getSections(tenant)) {
				this.runSection(tenant, section);
			}
			try {
				//Register this tenant for new sections
				this.registerAction(this.initialisationHelper.getInitialistionAction(), tenant, "");
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
				this.registrationHelper.unregister("", notificationCode);
			} catch (UnivoteException ex) {
				this.log(ex);
			}
		}
	}

	public void onBoardNotification(String notificationCode, PostDTO post) {

		if (!this.notificationDataAccessor.containsNotificationCode(notificationCode)) {
			logger.log(Level.INFO, "Received unknown notification code for board notification. {0}", notificationCode);
			this.registrationHelper.unregisterUnknownNotification(notificationCode);
			return;
		}
		NotificationData nData = this.notificationDataAccessor.findByNotificationCode(notificationCode);
		String actionName = nData.getAction();
		NotifiableAction action;
		try {
			action = this.getAction(actionName);
		} catch (UnivoteException ex) {
			this.log(ex);
			return;
		}
		action.notifyAction(nData.getTenant(), nData.getSection(), post);
	}

	public void onUserInputNotification(String notificationCode, UserInput userInput) {
		if (!this.notificationDataAccessor.containsNotificationCode(notificationCode)) {
			logger.log(Level.INFO, "Received unknown notification code for user input. {0}", notificationCode);
			return;
		}
		NotificationData nData = this.notificationDataAccessor.findByNotificationCode(notificationCode);
		String actionName = nData.getAction();
		NotifiableAction action;
		try {
			action = this.getAction(actionName);
		} catch (UnivoteException ex) {
			this.log(ex);
			return;
		}
		action.notifyAction(nData.getTenant(), nData.getSection(), userInput);
	}

	@Timeout
	public void onTimerNotification(Timer timer) {
		String notificationCode = (String) timer.getInfo();
		NotificationData nData = this.notificationDataAccessor.findByNotificationCode(notificationCode);
		String actionName = nData.getAction();
		NotifiableAction action;
		try {
			action = this.getAction(actionName);
		} catch (UnivoteException ex) {
			this.log(ex);
			return;
		}
		action.notifyAction(nData.getTenant(), nData.getSection(), timer);
	}

	public void runFinished(ActionContext actionContext) {
		//Check if its the initialisation action
		if (actionContext.getAction().equals(this.initialisationHelper.getInitialistionAction())) {
			this.runFinishedInitialisation(actionContext);
			return;
		}

		this.unregisterAction(actionContext.getAction());

		//check if there is a next process
		int i = this.actionList.indexOf(actionContext.getAction()) + 1;
		if (i < this.actionList.size()) {
			try {

				String nextActionName = this.actionList.get(i);
				//register next process
				this.registerAction(nextActionName, actionContext.getTenant(), actionContext.getSection());
				//run next process
				this.runAction(nextActionName, actionContext.getTenant(), actionContext.getSection());
			} catch (UnivoteException ex) {
				this.log(ex);
			}
		}
	}

	protected void runFinishedInitialisation(ActionContext actionContext) {
		try {
			this.registerAction(this.actionList.get(0), actionContext.getTenant(), actionContext.getSection());
			//run next process
			this.runAction(this.actionList.get(0), actionContext.getTenant(), actionContext.getSection());
		} catch (UnivoteException ex) {
			this.log(ex);
		}
	}

	protected NotifiableAction getAction(String actionName) throws UnivoteException {
		try {
			return (NotifiableAction) sctx.lookup("java:app" + actionName
					+ "!ch.bfh.univote.component.core.action.NotifiableAction");
		} catch (Exception ex) {
			throw new UnivoteException("Could not find action with name " + actionName, ex);
		}
	}

	protected void runAction(String actionName, String tenant, String section) throws UnivoteException {
		NotifiableAction action = this.getAction(actionName);
		action.run(tenant, section);
	}

	protected void registerAction(String actionName, String tenant, String section) throws UnivoteException {
		NotifiableAction action = this.getAction(actionName);

		List<NotificationCondition> conditions
				= action.getNotificationConditions(tenant, section);
		for (NotificationCondition cond : conditions) {
			if (cond instanceof QueryNotificationCondition) {
				QueryNotificationCondition qNC = (QueryNotificationCondition) cond;
				String newNotificationCode = this.registrationHelper.register(qNC.getBoard(), qNC.getQuery());
				this.notificationDataAccessor.add(new BoardNotificationData(qNC.getBoard(),
						newNotificationCode, actionName, tenant, section));
			} else if (cond instanceof UserInputNotificationCondition) {
				UserInputNotificationCondition uiNC = (UserInputNotificationCondition) cond;
				String newNotificationCode = this.userInputManager.requestUserInput(uiNC.getUserInputRequest());
				if (newNotificationCode != null) {
					this.notificationDataAccessor.add(
							new NotificationData(newNotificationCode, actionName, tenant, section));
				}
			} else if (cond instanceof TimerNotificationCondition) {
				TimerNotificationCondition tNC = (TimerNotificationCondition) cond;
				//Get a notificationCode for a timer
				String newNotificationCode = this.createTimer(tNC);
				this.notificationDataAccessor.add(new TimerNotificationData(
						newNotificationCode, actionName, tenant, section));
			} else {
				throw new UnivoteException("Unsupported notification condition " + cond.getClass());
			}
		}
	}

	protected void unregisterAction(String actionName) {

		//unregister current process
		for (NotificationData notificationData : this.notificationDataAccessor.findByActionName(actionName)) {
			//Remove from notificationDataAccessor
			this.notificationDataAccessor.removeByNotificationCode(notificationData.getNotifictionCode());
			try {
				//Has to act based on the type of the notification
				//BoardCondition: unregister on the corresponding uniboard
				if (notificationData instanceof BoardNotificationData) {
					BoardNotificationData bnd = (BoardNotificationData) notificationData;
					this.registrationHelper.unregister(bnd.getBoard(), bnd.getNotifictionCode());
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

	protected void runSection(String tenant, String section) {
		//Find state of the section
		try {
			for (String actionName : this.actionList) {
				NotifiableAction action = this.getAction(actionName);
				if (!action.checkPostCondition(tenant, section)) {
					this.registerAction(actionName, tenant, section);
					this.runAction(actionName, tenant, section);
					break;
				}
			}

		} catch (UnivoteException ex) {
			this.log(ex);
		}
		logger.log(Level.INFO, "Tenant {0} and section {1} is finished.", new Object[]{tenant, section});
	}

	protected String createTimer(TimerNotificationCondition timerNotificationCondition) {
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

}
