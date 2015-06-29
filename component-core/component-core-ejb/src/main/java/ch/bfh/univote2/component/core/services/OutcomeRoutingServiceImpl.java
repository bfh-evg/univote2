/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.services;

import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.manager.ConfigurationManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;

@Singleton
@DependsOn("ConfigurationManagerImpl")
public class OutcomeRoutingServiceImpl implements OutcomeRoutingService {

	private static final String CONFIGURATION_NAME = "outcome-routing";
	private static final Logger logger = Logger.getLogger(OutcomeRoutingServiceImpl.class.getName());

	private final Map<String, String> routing = new HashMap<>();

	/**
	 * ConfigurationHelper. Gives access to configurations stored in the JNDI.
	 */
	@EJB
	private ConfigurationManager configurationManager;

	@PostConstruct
	public void init() {
		try {
			Properties config = this.configurationManager.getConfiguration(CONFIGURATION_NAME);
			for (String userInputname : config.stringPropertyNames()) {
				this.routing.put(userInputname, config.getProperty(userInputname));
			}
		} catch (UnivoteException ex) {
			logger.log(Level.SEVERE, "Cant load configuration.", ex);
		}
	}

	@Override
	public String getRoutingForUserInput(String userInputName) throws UnivoteException {
		if (this.routing.containsKey(userInputName)) {
			return this.routing.get(userInputName);
		}
		throw new UnivoteException("Unknown user input name: " + userInputName);
	}
	// Methods for testing

	protected Map<String, String> getRouting() {
		return this.routing;
	}

}
