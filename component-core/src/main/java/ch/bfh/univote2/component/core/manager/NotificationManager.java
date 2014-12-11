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

import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.data.ActionData;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.data.NotificationCondition;
import ch.bfh.univote2.component.core.data.QueryNotificationCondition;
import ch.bfh.univote2.component.core.data.UserInputNotificationCondition;
import ch.bfh.univote2.component.core.helper.RegistrationHelper;
import java.util.List;
import java.util.Map;
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

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
public class NotificationManager {
	
	static final Logger logger = Logger.getLogger(NotificationManager.class.getName());
	private static final String CONFIGURATION_NAME = "action-list";
	private Map<String, ActionData> notificationMappings;
	private List<String> actionList;
	
	@Resource(name = "sessionContext")
	private SessionContext sctx;
	
	@EJB
	RegistrationHelper registrationHelper;
	
	@EJB
	ConfigurationManager configurationManager;
	
	@EJB
	InitialisationHelper initialisationHelper;
	
	@PostConstruct
	public void init() {
		//Load action list from the configuration
		Properties tmp = this.configurationManager.getConfiguration(CONFIGURATION_NAME);
		SortedSet<String> sorted = new TreeSet<>(tmp.stringPropertyNames());
		for (String str : sorted) {
			this.actionList.add(tmp.getProperty(str));
		}
		//Get list of tennants
		for (String tenant : this.initialisationHelper.getTenants()) {
			for (String section : this.initialisationHelper.getSections(tenant)) {
				//TODO Find state of the section
				for (String actionName : this.actionList) {
					
					try {
						NotifiableAction action = this.getAction(actionName);
						if (action.checkPostCondition(tenant, section)) {
						}
					} catch (UnivoteException ex) {
						this.log(ex);
					}
					
				}
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
		for (String notificationCode : this.notificationMappings.keySet()) {
			try {
				this.registrationHelper.unregister(notificationCode);
			} catch (UnivoteException ex) {
				this.log(ex);
			}
		}
	}
	
	public void onNotification(String notificationCode, PostDTO post) {
		
		if (!this.notificationMappings.containsKey(notificationCode)) {
			logger.log(Level.INFO, "Received unknown notification code. {0}", notificationCode);
			try {
				this.registrationHelper.unregister(notificationCode);
			} catch (UnivoteException ex) {
				this.log(ex);
			}
			return;
		}
		ActionData nData = this.notificationMappings.get(notificationCode);
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
	
	public void runFinished(ActionData nData) {
		//unregister current process
		for (Map.Entry<String, ActionData> e : this.notificationMappings.entrySet()) {
			if (e.getValue().equals(nData)) {
				try {
					this.registrationHelper.unregister(e.getKey());
				} catch (UnivoteException ex) {
					this.log(ex);
				}
				this.notificationMappings.remove(e.getKey());
			}
		}

		//check if there is a next process
		int i = this.actionList.indexOf(nData.getAction()) + 1;
		if (i < this.actionList.size()) {
			try {
				
				String nextActionName = this.actionList.get(i);
				//register next process
				this.registerAction(nextActionName, nData.getTenant(), nData.getSection());
				//run next process
				this.runAction(nextActionName, nData.getTenant(), nData.getSection());
			} catch (UnivoteException ex) {
				this.log(ex);
			}
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
				String newNotificationCode = this.registrationHelper.register(qNC.getQuery());
				this.notificationMappings.put(newNotificationCode,
						new ActionData(actionName, tenant, section));
			} else if (cond instanceof UserInputNotificationCondition) {
				//TODO
			} else {
				throw new UnivoteException("Unsupported notification condition " + cond.getClass());
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
