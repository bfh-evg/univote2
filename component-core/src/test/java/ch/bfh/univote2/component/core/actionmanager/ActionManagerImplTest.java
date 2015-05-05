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
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.NotificationData;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.data.UserInput;
import ch.bfh.univote2.component.core.manager.ConfigurationManager;
import ch.bfh.univote2.component.core.manager.TaskManager;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.services.InitialisationService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import static org.junit.Assert.assertTrue;
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
                .addClass(MockAction.class)
                .addClass(SecondMockAction.class)
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
    ConfigurationManager configurationManager;

    @EJB
    InitialisationService initialisationHelper;

    @EJB
    TenantManager tenantManager;

    @EJB
    TaskManager taskManager;

    @EJB
    MockAction mockAction;

    @EJB
    SecondMockAction secondMockAction;

    /**
     * Test of checkActionState with an existing actionContext
     */
    @Test
    public void testcheckActionState1() {
        String tenant = "checkActionState";
        String actionName = "MockAction";
        String section = "test1";
        ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
        ActionContext ac = new ActionContextImpl(ack, null, false);
        this.actionManager.addActionContext(ac);
        this.actionManager.pubCheckActionState(actionName, tenant, section);
        assertFalse(this.mockAction.containsRun(ack));
        assertFalse(this.mockAction.containsNotify(ack));
    }

    /**
     * Test of checkActionState with no actionContext but postcondition=true
     */
    //@Test
    public void testcheckActionState2() {
        String tenant = "checkActionState";
        String actionName = "MockAction";
        String secondActionName = "SecondMockAction";
        String section = "test2";

        List<String> succesors = new ArrayList<>();
        succesors.add(secondActionName);
        this.actionManager.addActionGraphEntry(actionName, succesors);

        ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), true);
        this.mockAction.addActionContext(ac);

        ActionContextKey ack2 = new ActionContextKey(secondActionName, tenant, section);
        ActionContext ac2 = new ActionContextImpl(ack2, new ArrayList<>(), false);
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
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
        String section = "test3";
        this.actionManager.pubCheckActionState(tenant, section, actionName);

        ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
        List<PreconditionQuery> preconditions = new ArrayList<>();
        QueryDTO query = new QueryDTO(new ArrayList<>(), new ArrayList<>(), 10);
        preconditions.add(new BoardPreconditionQuery(query, "UNIVOTE"));
        ActionContext ac = new ActionContextImpl(ack, preconditions, false);
        this.mockAction.addActionContext(ac);

        this.actionManager.pubCheckActionState(actionName, tenant, section);
        assertTrue(this.mockAction.containsRun(ack));
        assertEquals(this.registrationService.getLastRegistredQuery(), query);
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
        this.actionManager.addActionContext(ac);
        //create notificationregistration
        NotificationData nData = new NotificationData(notificationCode, ack);
        this.actionManager.addNotificationData(nData);
        //trigger notify
        this.actionManager.onBoardNotification(notificationCode, null);
        //check that action got called
        assertTrue(this.mockAction.containsNotify(ack));
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
        this.actionManager.addActionContext(ac);
        //create notificationregistration
        //trigger notify
        this.actionManager.onBoardNotification(notificationCode, null);
        //check that action got called
        assertFalse(this.mockAction.containsNotify(ack));
        assertTrue(this.registrationService.containsUnregistredNotificationCode(notificationCode));
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
        //create notificationregistration
        NotificationData nData = new NotificationData(notificationCode, ack);
        this.actionManager.addNotificationData(nData);
        //trigger notify
        this.actionManager.onBoardNotification(notificationCode, null);
        //check that action got called
        assertFalse(this.mockAction.containsNotify(ack));
        assertFalse(this.registrationService.containsUnregistredNotificationCode(notificationCode));
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
        this.actionManager.addActionContext(ac);
        //create notificationregistration
        NotificationData nData = new NotificationData(notificationCode, ack);
        this.actionManager.addNotificationData(nData);
        //trigger notify
        this.actionManager.onUserInputNotification(notificationCode, null);
        //check that action got called
        assertTrue(this.mockAction.containsNotify(ack));
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
        this.actionManager.addActionContext(ac);
        //create notificationregistration
        //trigger notify
        this.actionManager.onUserInputNotification(notificationCode, null);
        //check that action got called
        assertFalse(this.mockAction.containsNotify(ack));
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
        //create notificationregistration
        NotificationData nData = new NotificationData(notificationCode, ack);
        this.actionManager.addNotificationData(nData);
        //trigger notify
        this.actionManager.onUserInputNotification(notificationCode, null);
        //check that action got called
        assertFalse(this.mockAction.containsNotify(ack));
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
        this.actionManager.addActionContext(ac);
        //create notificationregistration
        //trigger notify
        MockTimer timer = new MockTimer(notificationCode);
        this.actionManager.onTimerNotification(timer);
        //check that action got called
        assertFalse(this.mockAction.containsNotify(ack));
        assertTrue(timer.isCanceled());
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
        //create notificationregistration
        NotificationData nData = new NotificationData(notificationCode, ack);
        this.actionManager.addNotificationData(nData);
        //trigger notify
        MockTimer timer = new MockTimer(notificationCode);
        this.actionManager.onTimerNotification(timer);
        //check that action got called
        assertFalse(this.mockAction.containsNotify(ack));
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
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
    }

    /**
     * Test of runFinished for an existing actionContext and resultStatus = finished
     */
    @Test
    public void testRunFinished1() {
        //Create action context
        String tenant = "runFinished";
        String actionName = "MockAction";
        String secondActionName = "SecondMockAction";
        String section = "test1";

        ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), true);
        ac.getPreconditionQueries().add(new BoardPreconditionQuery(null, tenant));
        ac.getQueuedNotifications().add(section);

        ActionContextKey ack2 = new ActionContextKey(secondActionName, tenant, section);
        ActionContext ac2 = new ActionContextImpl(ack2, new ArrayList<>(), false);
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
        //Check that checkActionState gets called for the successors
        assertTrue(this.secondMockAction.containsRun(ack2));
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
        ac.setInUse(true);

        //Run finished
        this.actionManager.runFinished(ac, ResultStatus.RUN_FINISHED);
        //Check that context gets purged
        assertFalse(ac.isInUse());
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
        String section = "test2";

        ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
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
        String section = "test2";

        ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
        ac.setInUse(true);
        ac.getQueuedNotifications().add(section);

        //Run finished
        this.actionManager.runFinished(ac, ResultStatus.RUN_FINISHED);
        assertFalse(this.mockAction.containsNotify(ack));
        assertFalse(ac.isInUse());
    }

    /**
     * Test of runFinished for an existing actionContext and resultStatus = failure
     */
    @Test
    public void testRunFinished5() {
        //Create action context
        String tenant = "runFinished";
        String actionName = "MockAction";
        String section = "test2";

        ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), false);
        ac.setInUse(true);
        ac.getQueuedNotifications().add(section);

        //Run finished
        this.actionManager.runFinished(ac, ResultStatus.FAILURE);
        assertFalse(this.mockAction.containsNotify(ack));
        assertFalse(ac.isInUse());
    }

    private static class ActionContextImpl extends ActionContext {

        public ActionContextImpl(ActionContextKey actionContextKey, List<PreconditionQuery> preconditionQueries,
                boolean postCondition) {
            super(actionContextKey, preconditionQueries);
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
