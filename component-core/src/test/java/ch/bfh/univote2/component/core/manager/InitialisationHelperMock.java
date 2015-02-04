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
import java.util.List;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class InitialisationHelperMock implements InitialisationHelper {

	@Override
	public List<String> getSections(String tenant) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getInitialistionAction() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
