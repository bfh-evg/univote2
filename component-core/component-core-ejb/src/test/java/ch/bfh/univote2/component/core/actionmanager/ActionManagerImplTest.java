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

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.component.core.data.BoardNotificationData;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.NotificationData;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.data.TimerNotificationData;
import ch.bfh.univote2.component.core.data.TimerPreconditionQuery;
import ch.bfh.univote2.component.core.data.UserInput;
import ch.bfh.univote2.component.core.data.UserInputPreconditionQuery;
import ch.bfh.univote2.component.core.data.UserInputTask;
import ch.bfh.univote2.component.core.manager.TaskManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.NoMoreTimeoutsException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@RunWith(Arquillian.class)
public class ActionManagerImplTest {

	public ActionManagerImplTest() {
	}

	/**
	 * Helper method for building the in-memory variant of a deployable unit. See Arquillian for more information.
	 *
	 * @return a Java archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive ja = ShrinkWrap.create(WebArchive.class)
				.addClass(TestableActionManagerImpl.class)
				.addClass(RegistrationServiceMock.class)
				.addClass(ConfigurationManagerMock.class)
				.addClass(InitialisationServiceMock.class)
				.addClass(InformationServiceMock.class)
				.addClass(MockAction.class)
				.addClass(SecondMockAction.class)
				.addClass(MockInitAction.class)
				.addClass(TaskManagerMock.class)
				.addClass(TenantManagerMock.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		//System.out.println(ja.toString(true));
		return ja;
	}

	@EJB
	TestableActionManagerImpl actionManager;

	@EJB
	RegistrationServiceMock registrationService;

	@EJB
	ConfigurationManagerMock configurationManager;

	@EJB
	InitialisationServiceMock initialisationHelper;

	@EJB
	TenantManagerMock tenantManager;

	@EJB
	TaskManager taskManager;

	@EJB
	MockAction mockAction;

	@EJB
	SecondMockAction secondMockAction;

	@EJB
	MockInitAction mockInitAction;

	/**
	 * Test of checkActionState with an existing actionContext
	 */
	@Test

	public void testcheckActionState1() {
		String tenant = "checkActionState";
		String actionName = "MockAction";
		String section = "test1";
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, null, false, false);
		this.actionManager.addActionContext(ac);
		this.actionManager.pubCheckActionState(actionName, tenant, section);
		assertFalse(this.mockAction.containsRun(ack));
		assertFalse(this.mockAction.containsNotify(ack));
	}

	/**
	 * Test of checkActionState with no actionContext but postcondition=true
	 */
	@Test
	public void testcheckActionState2() {
		String tenant = "checkActionState";
		String actionName = "MockAction";
		String secondActionName = "SecondMockAction";
		String section = "test2";

		List<String> succesors = new ArrayList<>();
		succesors.add(secondActionName);
		this.actionManager.addActionGraphEntry(actionName, succesors);

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, true);
		this.mockAction.addActionContext(ac);

		ActionContextKey ack2 = new ActionContextKey(secondActionName, tenant, section);
		ActionContext ac2 = new ActionContextImpl(ack2, new ArrayList<>(), false, false);
		this.secondMockAction.addActionContext(ac2);

		this.actionManager.pubCheckActionState(actionName, tenant, section);
		assertFalse(this.mockAction.containsRun(ack));
		assertFalse(this.mockAction.containsNotify(ack));
		assertTrue(this.secondMockAction.containsRun(ack2));
		//Test succsessors run
	}

	/**
	 * Test of checkActionState with no actionContext and not finished but no notifications needed
	 */
	@Test
	public void testcheckActionState3() {
		String tenant = "checkActionState";
		String actionName = "MockAction";
		String section = "test3";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		this.mockAction.addActionContext(ac);

		this.actionManager.pubCheckActionState(actionName, tenant, section);
		assertTrue(this.mockAction.containsRun(ack));
	}

	/**
	 * Test of checkActionState with no actionContext and not finished and notifications needed
	 */
	@Test
	public void testcheckActionState4() {
		String tenant = "checkActionState";
		String actionName = "MockAction";
		String section = "test4";
		this.actionManager.pubCheckActionState(tenant, section, actionName);

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		List<PreconditionQuery> preconditions = new ArrayList<>();
		QueryDTO query = new QueryDTO(new ArrayList<>(), new ArrayList<>(), 10);
		preconditions.add(new BoardPreconditionQuery(query, "UNIVOTE"));
		ActionContext ac = new ActionContextImpl(ack, preconditions, false, false);
		this.mockAction.addActionContext(ac);

		this.actionManager.pubCheckActionState(actionName, tenant, section);
		assertTrue(this.mockAction.containsRun(ack));
		assertEquals(this.registrationService.getLastRegistredQuery(), query);
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of onBoardNotification with notification registered and action context existing and ready
	 */
	@Test
	public void testOnBoardNotification1() {
		//Create action context
		String tenant = "onBoardNotification";
		String actionName = "MockAction";
		String section = "test1";
		String notificationCode = tenant + section;
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		this.actionManager.addActionContext(ac);
		//create notificationregistration
		NotificationData nData = new NotificationData(notificationCode, ack);
		this.actionManager.addNotificationData(nData);
		//trigger notify
		this.actionManager.onBoardNotification(notificationCode, null);
		//check that action got called
		assertTrue(this.mockAction.containsNotify(ack));
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of onBoardNotification with notification not registered but action context existing and ready
	 */
	@Test
	public void testOnBoardNotification2() {
		//Create action context
		String tenant = "onBoardNotification";
		String actionName = "MockAction";
		String section = "test2";
		String notificationCode = tenant + section;
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		this.actionManager.addActionContext(ac);
		//create notificationregistration
		//trigger notify
		this.actionManager.onBoardNotification(notificationCode, null);
		//check that action got called
		assertFalse(this.mockAction.containsNotify(ack));
		assertTrue(this.registrationService.containsUnregistredNotificationCode(notificationCode));
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of onBoardNotification with notification registered but action context not existing
	 */
	@Test
	public void testOnBoardNotification3() {
		//Create action context
		String tenant = "onBoardNotification";
		String actionName = "MockAction";
		String section = "test3";
		String notificationCode = tenant + section;
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		//create notificationregistration
		NotificationData nData = new NotificationData(notificationCode, ack);
		this.actionManager.addNotificationData(nData);
		//trigger notify
		this.actionManager.onBoardNotification(notificationCode, null);
		//check that action got called
		assertFalse(this.mockAction.containsNotify(ack));
		assertFalse(this.registrationService.containsUnregistredNotificationCode(notificationCode));
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of onBoardNotification with notification registered and action context existing but in use
	 */
	@Test
	public void testOnBoardNotification4() {
		//Create action context
		String tenant = "onBoardNotification";
		String actionName = "MockAction";
		String section = "test4";
		String notificationCode = tenant + section;
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		ac.setInUse(true);
		this.actionManager.addActionContext(ac);
		//create notificationregistration
		NotificationData nData = new NotificationData(notificationCode, ack);
		this.actionManager.addNotificationData(nData);
		//trigger notify
		byte[] msg = notificationCode.getBytes();
		PostDTO postDTO = new PostDTO(msg, null, null);
		this.actionManager.onBoardNotification(notificationCode, postDTO);
		//check that action got called
		assertFalse(this.mockAction.containsNotify(ack));
		assertFalse(this.registrationService.containsUnregistredNotificationCode(notificationCode));
		assertEquals(1, ac.getQueuedNotifications().size());
		assertEquals(postDTO, ac.getQueuedNotifications().poll());
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of onUserInputNotification with notification registered and action context existing and ready
	 */
	@Test
	public void testOnUserInputNotification1() {
		//Create action context
		String tenant = "onUserInputNotification";
		String actionName = "MockAction";
		String section = "test1";
		String notificationCode = tenant + section;
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		this.actionManager.addActionContext(ac);
		//create notificationregistration
		NotificationData nData = new NotificationData(notificationCode, ack);
		this.actionManager.addNotificationData(nData);
		//trigger notify
		this.actionManager.onUserInputNotification(notificationCode, null);
		//check that action got called
		assertTrue(this.mockAction.containsNotify(ack));
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of onUserInputNotification with notification not registered but action context existing and ready
	 */
	@Test
	public void testOnUserInputNotification2() {
		//Create action context
		String tenant = "onUserInputNotification";
		String actionName = "MockAction";
		String section = "test2";
		String notificationCode = tenant + section;
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		this.actionManager.addActionContext(ac);
		//create notificationregistration
		//trigger notify
		this.actionManager.onUserInputNotification(notificationCode, null);
		//check that action got called
		assertFalse(this.mockAction.containsNotify(ack));
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of onUserInputNotification with notification registered but action context not existing
	 */
	@Test
	public void testOnUserInputNotification3() {
		//Create action context
		String tenant = "onUserInputNotification";
		String actionName = "MockAction";
		String section = "test3";
		String notificationCode = tenant + section;
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		//create notificationregistration
		NotificationData nData = new NotificationData(notificationCode, ack);
		this.actionManager.addNotificationData(nData);
		//trigger notify
		this.actionManager.onUserInputNotification(notificationCode, null);
		//check that action got called
		assertFalse(this.mockAction.containsNotify(ack));
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of onUserInputNotification with notification registered and action context existing but in use
	 */
	@Test
	public void testOnUserInputNotification4() {
		//Create action context
		String tenant = "onUserInputNotification";
		String actionName = "MockAction";
		String section = "test4";
		String notificationCode = tenant + section;
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		ac.setInUse(true);
		this.actionManager.addActionContext(ac);
		//create notificationregistration
		NotificationData nData = new NotificationData(notificationCode, ack);
		this.actionManager.addNotificationData(nData);
		//trigger notify
		UserInput userInput = new UserInput() {
		};
		this.actionManager.onUserInputNotification(notificationCode, userInput);
		//check that action got called
		assertFalse(this.mockAction.containsNotify(ack));
		assertFalse(this.registrationService.containsUnregistredNotificationCode(notificationCode));
		assertEquals(1, ac.getQueuedNotifications().size());
		assertEquals(userInput, ac.getQueuedNotifications().poll());
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of onTimerNotification with notification registered and action context existing and ready
	 */
	@Test
	public void testOnTimerNotification1() {
		//Create action context
		String tenant = "onTimerNotification";
		String actionName = "MockAction";
		String section = "test1";
		String notificationCode = tenant + section;
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		this.actionManager.addActionContext(ac);
		//create notificationregistration
		NotificationData nData = new NotificationData(notificationCode, ack);
		this.actionManager.addNotificationData(nData);
		//trigger notify
		Timer timer = new MockTimer(notificationCode);
		this.actionManager.onTimerNotification(timer);
		//check that action got called
		assertTrue(this.mockAction.containsNotify(ack));
	}

	/**
	 * Test of onTimerNotification with notification not registered but action context existing and ready
	 */
	@Test
	public void testOnTimerNotification2() {
		//Create action context
		String tenant = "onTimerNotification";
		String actionName = "MockAction";
		String section = "test2";
		String notificationCode = tenant + section;
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		this.actionManager.addActionContext(ac);
		//create notificationregistration
		//trigger notify
		MockTimer timer = new MockTimer(notificationCode);
		this.actionManager.onTimerNotification(timer);
		//check that action got called
		assertFalse(this.mockAction.containsNotify(ack));
		assertTrue(timer.isCanceled());
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of onTimerNotification with notification registered but action context not existing
	 */
	@Test
	public void testOnTimerNotification3() {
		//Create action context
		String tenant = "onTimerNotification";
		String actionName = "MockAction";
		String section = "test3";
		String notificationCode = tenant + section;
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		//create notificationregistration
		NotificationData nData = new NotificationData(notificationCode, ack);
		this.actionManager.addNotificationData(nData);
		//trigger notify
		MockTimer timer = new MockTimer(notificationCode);
		this.actionManager.onTimerNotification(timer);
		//check that action got called
		assertFalse(this.mockAction.containsNotify(ack));
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of onTimerNotification with notification registered and action context existing but in use
	 */
	@Test
	public void testOnTimerNotification4() {
		//Create action context
		String tenant = "onTimerNotification";
		String actionName = "MockAction";
		String section = "test4";
		String notificationCode = tenant + section;
		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		ac.setInUse(true);
		this.actionManager.addActionContext(ac);
		//create notificationregistration
		NotificationData nData = new NotificationData(notificationCode, ack);
		this.actionManager.addNotificationData(nData);
		//trigger notify
		MockTimer timer = new MockTimer(notificationCode);
		this.actionManager.onTimerNotification(timer);
		//check that action got called
		assertFalse(this.mockAction.containsNotify(ack));
		assertFalse(this.registrationService.containsUnregistredNotificationCode(notificationCode));
		assertEquals(1, ac.getQueuedNotifications().size());
		assertEquals(timer, ac.getQueuedNotifications().poll());
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of runFinished for an existing actionContext, resultStatus = finished and parallel = false
	 */
	@Test
	public void testRunFinished1() {
		//Create action context
		String tenant = "runFinished";
		String actionName = "MockAction";
		String secondActionName = "SecondMockAction";
		String section = "test1";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, true);
		ac.getPreconditionQueries().add(new BoardPreconditionQuery(null, tenant));
		ac.getQueuedNotifications().add(section);
		ac.setInUse(true);

		ActionContextKey ack2 = new ActionContextKey(secondActionName, tenant, section);
		ActionContext ac2 = new ActionContextImpl(ack2, new ArrayList<>(), false, false);
		this.secondMockAction.addActionContext(ac2);

		List<String> succesors = new ArrayList<>();
		succesors.add(secondActionName);
		this.actionManager.addActionGraphEntry(actionName, succesors);
		//Run finished
		this.actionManager.runFinished(ac, ResultStatus.FINISHED);
		//Check that context gets purged
		assertFalse(ac.isInUse());
		assertTrue(ac.getPreconditionQueries().isEmpty());
		assertTrue(ac.getPreconditionQueries().isEmpty());
		assertFalse(this.actionManager.getNotificationDataAccessor().containsActionContextKey(ack));
		//Check that checkActionState gets called for the successors
		assertTrue(this.secondMockAction.containsRun(ack2));
	}

	/**
	 * Test of runFinished for an existing actionContext, resultStatus = finished and parallel = true
	 */
	@Test
	public void testRunFinished11() {
		//Create action context
		String tenant = "runFinished";
		String actionName = "MockAction";
		String secondActionName = "SecondMockAction";
		String section = "test11";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), true, true);
		ac.getPreconditionQueries().add(new BoardPreconditionQuery(null, tenant));
		ac.getQueuedNotifications().add(section);
		ac.setInUse(true);

		ActionContextKey ack2 = new ActionContextKey(secondActionName, tenant, section);
		ActionContext ac2 = new ActionContextImpl(ack2, new ArrayList<>(), false, false);
		this.secondMockAction.addActionContext(ac2);

		List<String> succesors = new ArrayList<>();
		succesors.add(secondActionName);
		this.actionManager.addActionGraphEntry(actionName, succesors);
		//Run finished
		this.actionManager.runFinished(ac, ResultStatus.FINISHED);
		//Check that context gets purged
		assertTrue(ac.getPreconditionQueries().isEmpty());
		assertTrue(ac.getPreconditionQueries().isEmpty());
		//Check that checkActionState gets called for the successors
		assertTrue(this.secondMockAction.containsRun(ack2));
		assertFalse(this.actionManager.getNotificationDataAccessor().containsActionContextKey(ack));
		//Check that inUse didnt get changed(should not be used at all in parallel mode)
		assertTrue(ac.isInUse());
	}

	/**
	 * Test of runFinished for an existing actionContext and resultStatus = run_finished and no queued notifications
	 */
	@Test
	public void testRunFinished2() {
		//Create action context
		String tenant = "runFinished";
		String actionName = "MockAction";
		String section = "test2";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		ac.setInUse(true);

		//Run finished
		this.actionManager.runFinished(ac, ResultStatus.RUN_FINISHED);
		//Check that context gets purged
		assertFalse(ac.isInUse());
		assertFalse(this.mockAction.containsNotify(ack));
	}

	/**
	 * Test of runFinished for an existing actionContext and resultStatus = run_finished, no queued notifications and
	 * parallel = true
	 */
	@Test
	public void testRunFinished21() {
		//Create action context
		String tenant = "runFinished";
		String actionName = "MockAction";
		String section = "test21";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), true, false);
		ac.setInUse(true);

		//Run finished
		this.actionManager.runFinished(ac, ResultStatus.RUN_FINISHED);
		//Check that inuse doesnt get changed
		assertTrue(ac.isInUse());
		assertFalse(this.mockAction.containsNotify(ack));
	}

	/**
	 * Test of runFinished for an existing actionContext and resultStatus = run_finished and queued notifications
	 */
	@Test
	public void testRunFinished3() {
		//Create action context
		String tenant = "runFinished";
		String actionName = "MockAction";
		String section = "test3";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		ac.setInUse(true);
		ac.getQueuedNotifications().add(section);

		//Run finished
		this.actionManager.runFinished(ac, ResultStatus.RUN_FINISHED);
		assertTrue(this.mockAction.containsNotify(ack));
		assertTrue(ac.isInUse());
	}

	/**
	 * Test of runFinished for an existing actionContext and resultStatus = run_finished and queued notifications but
	 * wrong action name
	 */
	@Test
	public void testRunFinished4() {
		//Create action context
		String tenant = "runFinished";
		String actionName = "MockAction1";
		String section = "test4";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		ac.setInUse(true);
		ac.getQueuedNotifications().add(section);

		//Run finished
		this.actionManager.runFinished(ac, ResultStatus.RUN_FINISHED);
		assertFalse(this.mockAction.containsNotify(ack));
		assertFalse(ac.isInUse());
	}

	/**
	 * Test of runFinished for an existing actionContext and resultStatus = run_finished and queued notifications but
	 * wrong action name and parallel = true
	 */
	@Test
	public void testRunFinished41() {
		//Create action context
		String tenant = "runFinished";
		String actionName = "MockAction1";
		String section = "test41";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), true, false);
		ac.setInUse(true);
		ac.getQueuedNotifications().add(section);

		//Run finished
		this.actionManager.runFinished(ac, ResultStatus.RUN_FINISHED);
		assertFalse(this.mockAction.containsNotify(ack));
		assertTrue(ac.isInUse());
	}

	/**
	 * Test of runFinished for an existing actionContext, resultStatus = failure and parallel = false
	 */
	@Test
	public void testRunFinished5() {
		//Create action context
		String tenant = "runFinished";
		String actionName = "MockAction";
		String section = "test5";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		ac.setInUse(true);
		ac.getQueuedNotifications().add(section);

		//Run finished
		this.actionManager.runFinished(ac, ResultStatus.FAILURE);
		assertFalse(this.mockAction.containsNotify(ack));
		assertFalse(ac.isInUse());
	}

	/**
	 * Test of runFinished for an existing actionContext, resultStatus = failure and parallel = true
	 */
	@Test
	public void testRunFinished51() {
		//Create action context
		String tenant = "runFinished";
		String actionName = "MockAction";
		String section = "test51";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), true, false);
		ac.setInUse(true);
		ac.getQueuedNotifications().add(section);

		//Run finished
		this.actionManager.runFinished(ac, ResultStatus.FAILURE);
		assertFalse(this.mockAction.containsNotify(ack));
		assertTrue(ac.isInUse());
	}

	/**
	 * Test of runFinished for the intialAction and result status FINISHED
	 */
	@Test
	public void testRunFinished6() {
		//Create action context
		String tenant = "runFinished";
		String actionName = "intialAction";
		String mockActionName = "MockAction";
		String section = "test6";

		this.actionManager.setInitialAction(actionName);

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);

		ActionContextKey ack2 = new ActionContextKey(mockActionName, tenant, section);
		ActionContext ac2 = new ActionContextImpl(ack2, new ArrayList<>(), false, false);
		this.mockAction.addActionContext(ac2);

		List<String> succesors = new ArrayList<>();
		succesors.add(mockActionName);
		this.actionManager.addActionGraphEntry(actionName, succesors);

		//Run finished
		this.actionManager.runFinished(ac, ResultStatus.FINISHED);
		assertFalse(this.mockAction.containsRun(ack));
		assertFalse(ac.isInUse());
		assertTrue(this.mockAction.containsRun(ack2));
	}

	/**
	 * Test of runFinished for the intialAction and result status RUN_FINISHED
	 */
	@Test
	public void testRunFinished7() {
		//Create action context
		String tenant = "runFinished";
		String actionName = "intialAction";
		String mockActionName = "MockAction";
		String section = "test7";

		this.actionManager.setInitialAction(actionName);

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);

		ActionContextKey ack2 = new ActionContextKey(mockActionName, tenant, section);
		ActionContext ac2 = new ActionContextImpl(ack2, new ArrayList<>(), false, false);
		this.mockAction.addActionContext(ac2);

		List<String> succesors = new ArrayList<>();
		succesors.add(mockActionName);
		this.actionManager.addActionGraphEntry(actionName, succesors);

		//Run finished
		this.actionManager.runFinished(ac, ResultStatus.RUN_FINISHED);
		assertFalse(this.mockAction.containsRun(ack));
		assertFalse(ac.isInUse());
		assertTrue(this.mockAction.containsRun(ack2));
	}

	/**
	 * Test of runFinished for the intialAction and result status FAILRUE
	 */
	@Test
	public void testRunFinished8() {
		//Create action context
		String tenant = "runFinished";
		String actionName = "intialAction";
		String mockActionName = "MockAction";
		String section = "test7";

		this.actionManager.setInitialAction(actionName);

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);

		ActionContextKey ack2 = new ActionContextKey(mockActionName, tenant, section);
		ActionContext ac2 = new ActionContextImpl(ack2, new ArrayList<>(), false, false);
		this.mockAction.addActionContext(ac2);

		List<String> succesors = new ArrayList<>();
		succesors.add(mockActionName);
		this.actionManager.addActionGraphEntry(actionName, succesors);

		//Run finished
		this.actionManager.runFinished(ac, ResultStatus.FAILURE);
		assertFalse(this.mockAction.containsRun(ack));
		assertFalse(ac.isInUse());
		assertFalse(this.mockAction.containsRun(ack2));
	}

	/**
	 * Test of getAction with an existing action
	 */
	@Test
	public void testGetAction1() {
		//Create action context
		String tenant = "getAction";
		String actionName = "MockAction";
		String section = "test1";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		try {
			this.actionManager.getActionTest(ac);
		} catch (UnivoteException ex) {
			fail();
		}
		assertTrue(this.mockAction.containsRun(ack));
	}

	/**
	 * Test of getAction with a non existing action
	 */
	@Test
	public void testGetAction2() {
		//Create action context
		String tenant = "getAction";
		String actionName = "MockAction1";
		String section = "test2";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		try {
			this.actionManager.getActionTest(ac);
			fail();
		} catch (UnivoteException ex) {
			//Ok
		}
	}

	/**
	 * Test of runAction with an existing action
	 */
	@Test
	public void testRunAction1() {
		//Create action context
		String tenant = "runAction";
		String actionName = "MockAction";
		String section = "test1";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		try {
			this.actionManager.runAction(ac);
		} catch (UnivoteException ex) {
			fail();
		}
		assertTrue(this.mockAction.containsRun(ack));
	}

	/**
	 * Test of runAction with an action in use
	 */
	@Test
	public void testRunAction2() {
		//Create action context
		String tenant = "runAction";
		String actionName = "MockAction";
		String section = "test2";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, false);
		ac.setInUse(true);
		try {
			this.actionManager.runAction(ac);
		} catch (UnivoteException ex) {
			fail();
		}
		assertFalse(this.mockAction.containsRun(ack));
	}

	/**
	 * Test of registerAction with a board notification
	 */
	@Test
	public void testRegisterAction1() {
		//Create action context
		String tenant = "registerAction";
		String actionName = "MockAction";
		String section = "test1";
		String board = tenant + section;

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, true);
		QueryDTO query = new QueryDTO(null, null, 1);
		ac.getPreconditionQueries().add(new BoardPreconditionQuery(query, board));

		try {
			this.actionManager.registerAction(ac);
		} catch (UnivoteException ex) {
			fail();
		}
		assertEquals(query, this.registrationService.getLastRegistredQuery());
		List<NotificationData> result = this.actionManager.getNotificationDataAccessor().findByActionContextKey(ack);
		assertEquals(1, result.size());
		BoardNotificationData bnd = (BoardNotificationData) result.get(0);
		assertEquals(board, bnd.getBoard());
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of registerAction with an user input notification
	 */
	@Test
	public void testRegisterAction2() {
		//Create action context
		String tenant = "registerAction";
		String actionName = "MockAction";
		String section = "test2";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, true);
		UserInputTask t = new UserInputTask(tenant, tenant, section) {
		};
		ac.getPreconditionQueries().add(new UserInputPreconditionQuery(t));

		try {
			this.actionManager.registerAction(ac);
		} catch (UnivoteException ex) {
			fail();
		}
		assertEquals(t, this.taskManager.getTasks(tenant).get(0));
		List<NotificationData> result = this.actionManager.getNotificationDataAccessor().findByActionContextKey(ack);
		assertEquals(1, result.size());
		assertEquals(result.get(0).getActionContextKey(), ack);
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of registerAction with an timer notification
	 */
	@Test
	public void testRegisterAction3() {
		//Create action context
		String tenant = "registerAction";
		String actionName = "MockAction";
		String section = "test3";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, true);
		Date date = new Date(new Date().getTime() + 3600);
		TimerPreconditionQuery query = new TimerPreconditionQuery(date);
		ac.getPreconditionQueries().add(query);
		try {
			this.actionManager.registerAction(ac);
		} catch (UnivoteException ex) {
			fail();
		}

		List<NotificationData> result = this.actionManager.getNotificationDataAccessor().findByActionContextKey(ack);
		assertEquals(1, result.size());
		assertEquals(result.get(0).getActionContextKey(), ack);
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of registerAction with an unknown notification
	 */
	@Test
	public void testRegisterAction4() {
		//Create action context
		String tenant = "registerAction";
		String actionName = "MockAction";
		String section = "test4";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, true);
		UnknownPreconditionQuery query = new UnknownPreconditionQuery();
		ac.getPreconditionQueries().add(query);
		try {
			this.actionManager.registerAction(ac);
			fail();
		} catch (UnivoteException ex) {

		}

		List<NotificationData> result = this.actionManager.getNotificationDataAccessor().findByActionContextKey(ack);
		assertNull(result);
		this.actionManager.getNotificationDataAccessor().purge();
	}

	/**
	 * Test of unregisterAction
	 */
	@Test
	public void testUnregisterAction1() {
		//Create action context
		String tenant = "unregisterAction";
		String actionName = "MockAction";
		String section = "test1";
		String notifcationCode = tenant + section;

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, true);

		this.actionManager.getNotificationDataAccessor().addNotificationData(new BoardNotificationData(tenant,
				notifcationCode, ack));
		this.actionManager.getNotificationDataAccessor().addNotificationData(
				new NotificationData(notifcationCode + "1", ack));

		this.actionManager.getNotificationDataAccessor().addNotificationData(new TimerNotificationData(
				notifcationCode + "2", ack));

		this.actionManager.unregisterAction(ac);
		assertNull(this.actionManager.getNotificationDataAccessor().findByActionContextKey(ack));
		assertTrue(this.registrationService.containsUnregistredNotificationCode(notifcationCode));
	}

	/**
	 * Test of unregisterAction with nothing to unregister
	 */
	@Test
	public void testUnregisterAction2() {
		//Create action context
		String tenant = "unregisterAction";
		String actionName = "MockAction";
		String section = "test1";

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false, true);

		this.actionManager.unregisterAction(ac);
		assertNull(this.actionManager.getNotificationDataAccessor().findByActionContextKey(ack));
	}

	/**
	 * Test of cleanUp
	 */
	@Test
	public void testCleanUp() {
		//Create action context
		String tenant = "cleanUp";
		String actionName = "MockAction";
		String secondActionName = "SecondMockAction";
		String section = "test1";
		String notifcationCode = tenant + section + actionName;
		String notifcationCode2 = tenant + section + secondActionName;

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);

		ActionContextKey ack2 = new ActionContextKey(secondActionName, tenant, section);

		this.actionManager.getNotificationDataAccessor().addNotificationData(new BoardNotificationData(tenant,
				notifcationCode, ack));
		this.actionManager.getNotificationDataAccessor().addNotificationData(new BoardNotificationData(tenant,
				notifcationCode2, ack2));
		this.actionManager.getNotificationDataAccessor().addNotificationData(
				new NotificationData(notifcationCode + "1", ack));

		this.actionManager.getNotificationDataAccessor().addNotificationData(new TimerNotificationData(
				notifcationCode2 + "1", ack2));

		this.actionManager.cleanUp();

		assertTrue(this.registrationService.containsUnregistredNotificationCode(notifcationCode));
		assertTrue(this.registrationService.containsUnregistredNotificationCode(notifcationCode2));

	}

	/**
	 * Test of init
	 */
	@Test
	public void testInit() {
		String initAction = "MockInitAction";
		String successor = "MockAction";
		String successor2 = "SecondMockAction";
		String tenant1 = "Tenant1";
		String tenant2 = "Tenant2";
		String section1 = "init1";
		String section2 = "init11";
		Properties p = new Properties();
		p.setProperty("initialAction", initAction);
		p.setProperty(initAction, successor + ";" + successor2);

		this.configurationManager.setProperties(p);
		Set<String> tenants = new HashSet<>();
		tenants.add(tenant1);
		tenants.add(tenant2);
		this.tenantManager.setTenants(tenants);

		List<String> sections = new ArrayList();
		sections.add(section1);
		sections.add(section2);
		this.initialisationHelper.setSections(sections);

		ActionContextKey ackInit = new ActionContextKey(initAction, tenant1, initAction);
		ActionContext acInit = new ActionContextImpl(ackInit, new ArrayList<>(), true, false);
		this.mockInitAction.addActionContext(acInit);

		ActionContextKey ackInit2 = new ActionContextKey(initAction, tenant2, initAction);
		ActionContext acInit2 = new ActionContextImpl(ackInit2, new ArrayList<>(), true, false);
		this.mockInitAction.addActionContext(acInit2);

		ActionContextKey ack = new ActionContextKey(successor, tenant1, section1);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), true, false);
		this.mockAction.addActionContext(ac);
		ActionContextKey ack2 = new ActionContextKey(successor, tenant1, section2);
		ActionContext ac2 = new ActionContextImpl(ack2, new ArrayList<>(), true, false);
		this.mockAction.addActionContext(ac2);
		ActionContextKey ack3 = new ActionContextKey(successor, tenant2, section1);
		ActionContext ac3 = new ActionContextImpl(ack3, new ArrayList<>(), true, false);
		this.mockAction.addActionContext(ac3);
		ActionContextKey ack4 = new ActionContextKey(successor, tenant2, section2);
		ActionContext ac4 = new ActionContextImpl(ack4, new ArrayList<>(), true, false);
		this.mockAction.addActionContext(ac4);
		ActionContextKey ack5 = new ActionContextKey(successor2, tenant1, section1);
		ActionContext ac5 = new ActionContextImpl(ack5, new ArrayList<>(), true, false);
		this.secondMockAction.addActionContext(ac5);
		ActionContextKey ack6 = new ActionContextKey(successor2, tenant1, section2);
		ActionContext ac6 = new ActionContextImpl(ack6, new ArrayList<>(), true, false);
		this.secondMockAction.addActionContext(ac6);
		ActionContextKey ack7 = new ActionContextKey(successor2, tenant2, section1);
		ActionContext ac7 = new ActionContextImpl(ack7, new ArrayList<>(), true, false);
		this.secondMockAction.addActionContext(ac7);
		ActionContextKey ack8 = new ActionContextKey(successor2, tenant2, section2);
		ActionContext ac8 = new ActionContextImpl(ack8, new ArrayList<>(), true, false);
		this.secondMockAction.addActionContext(ac8);

		this.actionManager.testInit();

		Map<String, List<String>> actionGraph = this.actionManager.getActionGraph();
		assertTrue(actionGraph.containsKey(initAction));
		List<String> successors = actionGraph.get(initAction);
		assertEquals(2, successors.size());
		assertTrue(successors.contains(successor));
		assertTrue(successors.contains(successor2));

		assertTrue(this.mockAction.containsRun(ack));
		assertTrue(this.mockAction.containsRun(ack2));
		assertTrue(this.mockAction.containsRun(ack3));
		assertTrue(this.mockAction.containsRun(ack4));

		assertTrue(this.secondMockAction.containsRun(ack5));
		assertTrue(this.secondMockAction.containsRun(ack6));
		assertTrue(this.secondMockAction.containsRun(ack7));
		assertTrue(this.secondMockAction.containsRun(ack8));

	}

	/**
	 * Test of init with no initial action defined
	 */
	@Test
	public void testInit2() {
		String initAction = "MockInitAction";
		String successor = "MockAction";
		String successor2 = "SecondMockAction";
		Properties p = new Properties();
		p.setProperty(initAction, successor + ";" + successor2);

		this.configurationManager.setProperties(p);

		this.actionManager.testInit();

		Map<String, List<String>> actionGraph = this.actionManager.getActionGraph();
		assertTrue(actionGraph.isEmpty());

	}

	private static class UnknownPreconditionQuery implements PreconditionQuery {

		public UnknownPreconditionQuery() {
		}

	}

	private static class ActionContextImpl extends ActionContext {

		public ActionContextImpl(ActionContextKey actionContextKey, List<PreconditionQuery> preconditionQueries,
				boolean runsInParallel, boolean postCondition) {
			super(actionContextKey, preconditionQueries, runsInParallel);
			this.setPostCondition(postCondition);
		}

		@Override
		protected void purgeData() {
		}
	}

	private static class MockTimer implements Timer {

		String notificationCode;
		boolean canceled = false;

		public MockTimer() {
		}

		public MockTimer(String notificationCode) {
			this.notificationCode = notificationCode;
		}

		@Override
		public void cancel() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
			this.canceled = true;
		}

		public boolean isCanceled() {
			return canceled;
		}

		@Override
		public long getTimeRemaining() throws IllegalStateException, NoSuchObjectLocalException, NoMoreTimeoutsException, EJBException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Date getNextTimeout() throws IllegalStateException, NoSuchObjectLocalException, NoMoreTimeoutsException, EJBException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public ScheduleExpression getSchedule() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isPersistent() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isCalendarTimer() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Serializable getInfo() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
			return this.notificationCode;
		}

		@Override
		public TimerHandle getHandle() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}
}
