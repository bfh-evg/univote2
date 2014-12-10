/*
 * Copyright (c) 2013 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniBoard.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote2.component.core.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.NamingException;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
@Startup
public class ConfigurationManagerImpl implements ConfigurationManager {

	private static final Logger logger = Logger.getLogger(ConfigurationManagerImpl.class.getName());
	@Resource(name = "JNDI_URL")
	private String JNDI_URI;

	public Map<String, Properties> configurations;
	public Map<String, Properties> states;

	@PostConstruct
	private void init() {
		configurations = new HashMap<>();
		states = new HashMap<>();
		Properties props;
		try {
			javax.naming.InitialContext ic = new javax.naming.InitialContext();
			props = (Properties) ic.lookup(JNDI_URI);
		} catch (NamingException ex) {
			logger.log(Level.SEVERE, "JNDI lookup for " + JNDI_URI + " failed."
					+ "ConfigurationManager could not be initialized. Exception: {0}",
					new Object[]{ex});
			return;
		}
		for (String componentKey : props.stringPropertyNames()) {

			Properties tmpProperties;
			try {
				javax.naming.InitialContext ic = new javax.naming.InitialContext();
				tmpProperties = (Properties) ic.lookup(props.getProperty(componentKey));
				this.configurations.put(componentKey, tmpProperties);
			} catch (NamingException ex) {
				logger.log(Level.WARNING, "JNDI lookup for '{0}' failed. Exception: {1}",
						new Object[]{props.getProperty(componentKey), ex});
			}

		}
	}

	@Override
	public Properties getConfiguration(String key) {
		return this.configurations.get(key);
	}
}
