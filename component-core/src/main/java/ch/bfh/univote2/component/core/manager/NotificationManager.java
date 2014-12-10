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

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.action.NotificationCondition;
import ch.bfh.univote2.component.core.action.QueryNotificationCondition;
import ch.bfh.univote2.component.core.action.UserInputNotificationCondition;
import ch.bfh.univote2.component.core.helper.RegistrationHelper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
public class NotificationManager {

	static final Logger logger = Logger.getLogger(NotificationManager.class.getName());
	private static final String CONFIGURATION_NAME = "action-list";
	private Map<String, NotificationData> notificationMappings;
	private List<String> actionList;

	@Resource(name = "sessionContext")
	private SessionContext sctx;

	@EJB
	RegistrationHelper registrationHelper;

	@EJB
	ConfigurationManager configurationManager;

	public void onNotification(String notificationCode, PostDTO post) {

		if (!this.notificationMappings.containsKey(notificationCode)) {
			logger.log(Level.INFO, "Received unknown notification code. {0}", notificationCode);
			this.registrationHelper.unregister(notificationCode);
			return;
		}
		NotificationData nData = this.notificationMappings.get(notificationCode);
		String actionName = nData.getAction();
		NotifiableAction action;
		try {
			action = this.getAction(actionName);
		} catch (ManagerException ex) {
			logger.log(Level.WARNING, ex.getMessage());
			if (ex.getCause() != null) {
				logger.log(Level.WARNING, ex.getCause().getMessage());
			}
			return;
		}
		boolean finished = action.notifyAction(nData.getTenant(), nData.getSection(), post);

		if (finished) {
			//unregister current process
			this.registrationHelper.unregister(notificationCode);
			this.notificationMappings.remove(notificationCode);

			//check if there is a next process
			int i = this.actionList.indexOf(actionName) + 1;
			if (i < this.actionList.size()) {
				try {
					//register next process
					String nextActionName = this.actionList.get(i);

					this.registerAction(nextActionName, actionName, actionName);
					this.runAction(nextActionName, nData.getTenant(), nData.getSection());
				} catch (ManagerException ex) {
					logger.log(Level.WARNING, ex.getMessage());
					if (ex.getCause() != null) {
						logger.log(Level.WARNING, ex.getCause().getMessage());
					}
				}
			}
		}
	}

	@PostConstruct
	public void init() {
		//Load action list from the configuration
		Properties tmp = this.configurationManager.getConfiguration(CONFIGURATION_NAME);
		SortedSet<String> sorted = new TreeSet<>(tmp.stringPropertyNames());
		for (String str : sorted) {
			this.actionList.add(tmp.getProperty(str));
		}
	}

	protected NotifiableAction getAction(String actionName) throws ManagerException {
		try {
			return (NotifiableAction) sctx.lookup("java:app" + actionName
					+ "!ch.bfh.univote.component.core.action.NotifiableAction");
		} catch (Exception ex) {
			throw new ManagerException("Could not find action with name " + actionName, ex);
		}
	}

	protected void runAction(String actionName, String tenant, String section) throws ManagerException {

		NotifiableAction action = this.getAction(actionName);
		boolean finished = action.run(tenant, section);

		if (finished) {
			this.notificationMappings.values().removeAll(Collections.singleton(
					new NotificationData(actionName, tenant, section)));
			//check if there is a next process
			int i = this.actionList.indexOf(actionName) + 1;
			if (i < this.actionList.size()) {
				//register next process
				String nextActionName = this.actionList.get(i);

				this.runAction(nextActionName, tenant, section);
			}
		}
	}

	protected void registerAction(String actionName, String tenant, String section) throws ManagerException {
		NotifiableAction action = this.getAction(actionName);

		List<NotificationCondition> conditions
				= action.getNotificationConditions(tenant, section);
		for (NotificationCondition cond : conditions) {
			if (cond instanceof QueryNotificationCondition) {
				QueryNotificationCondition qNC = (QueryNotificationCondition) cond;
				String newNotificationCode = this.registrationHelper.register(qNC.getQuery());
				this.notificationMappings.put(newNotificationCode,
						new NotificationData(actionName, tenant, section));
			} else if (cond instanceof UserInputNotificationCondition) {
				//TODO
			} else {
				throw new ManagerException("Unsupported notification condition " + cond.getClass());
			}
		}
	}

}
