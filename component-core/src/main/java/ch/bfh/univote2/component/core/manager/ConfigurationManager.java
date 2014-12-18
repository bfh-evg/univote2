/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
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

import java.util.Properties;
import javax.ejb.Local;

/**
 * Interface that allows components to retrieve their configuration without worrying from where this configuration is
 * loaded. For every application there is expected exactly one component implementing this interface.
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Local
public interface ConfigurationManager {

	public Properties getConfiguration(String key);
}
