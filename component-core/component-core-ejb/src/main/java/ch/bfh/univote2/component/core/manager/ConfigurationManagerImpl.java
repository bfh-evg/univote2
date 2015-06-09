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

	@Resource(name = "JNDI_URI")
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
