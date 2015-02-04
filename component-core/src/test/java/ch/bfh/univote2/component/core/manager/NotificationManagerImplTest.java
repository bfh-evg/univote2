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
import ch.bfh.univote2.component.core.helper.InitialisationHelper;
import ch.bfh.univote2.component.core.helper.RegistrationHelper;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@RunWith(Arquillian.class)
public class NotificationManagerImplTest {

	public NotificationManagerImplTest() {
	}

	/**
	 * Helper method for building the in-memory variant of a deployable unit. See Arquillian for more information.
	 *
	 * @return a Java archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive ja = ShrinkWrap.create(WebArchive.class)
				.addClass(NotificationManagerImpl.class)
				.addClass(RegistrationHelperMock.class)
				.addClass(ConfigurationManagerMock.class)
				.addClass(InitialisationHelperMock.class)
				.addClass(TaskManagerMock.class)
				.addClass(TenantManagerMock.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		System.out.println(ja.toString(true));
		return ja;
	}

	@EJB
	NotificationManager notificationManager;

	@EJB
	RegistrationHelper registrationHelper;

	@EJB
	ConfigurationManager configurationManager;

	@EJB
	InitialisationHelper initialisationHelper;

	@EJB
	TenantManager tenantManager;

	@EJB
	TaskManager taskManager;

	/**
	 * Test of onBoardNotification method, of class NotificationManagerImpl.
	 */
	@Test
	public void testOnBoardNotification() throws Exception {
		System.out.println("onBoardNotification");
		String notificationCode = "";
		PostDTO post = null;
		notificationManager.onBoardNotification(notificationCode, post);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

}
