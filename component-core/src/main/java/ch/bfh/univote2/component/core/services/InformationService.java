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
package ch.bfh.univote2.component.core.services;

import javax.ejb.Local;

/**
 * Allows actions to display information about their run to the corresponding tenant
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Local
public interface InformationService {

	/**
	 * Shows the provided information to the corresponding tenant
	 *
	 * @param actionName - name of the action providing the information(caller of the method)
	 * @param tenant - tenant the information is for
	 * @param section - section the information belongs to
	 * @param information - information to be shown to the tenant
	 */
	public void informTenant(String actionName, String tenant, String section, String information);
}
