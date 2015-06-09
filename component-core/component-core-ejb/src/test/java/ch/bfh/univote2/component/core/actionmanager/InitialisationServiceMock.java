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
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
@LocalBean
public class InitialisationServiceMock implements InitialisationService {

	private List<String> sections;

	@Override
	public List<String> getSections(String tenant) {

		return sections;
	}

	public void setSections(List<String> sections) {
		this.sections = sections;
	}

}
