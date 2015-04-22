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

import ch.bfh.univote2.component.core.data.ActionContext;
import ch.bfh.univote2.component.core.data.ActionContextKey;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.services.InitialisationService;
import ch.bfh.univote2.component.core.services.RegistrationService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertFalse;
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
        System.out.println(ja.toString(true));
        return ja;
    }

    @EJB
    TestableActionManagerImpl actionManager;

    @EJB
    RegistrationService registrationHelper;

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
     * Test of checkActionState with an exsisting actionContext
     */
    @Test
    public void testcheckActionState1() {
        String tenant = "checkActionState";
        String actionName = "MockAction";
        String section = "test1";
        ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
        ActionContext ac = new ActionContextImpl(ack, null);
        this.actionManager.addActionContext(ac);
        this.actionManager.pubCheckActionState(tenant, section, actionName);
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
        ActionContext ac = new ActionContextImpl(ack, new ArrayList<>());
        ac.setPostCondition(true);
        this.mockAction.addActionContext(ac);

        ActionContextKey ack2 = new ActionContextKey(secondActionName, tenant, section);
        ActionContext ac2 = new ActionContextImpl(ack2, new ArrayList<>());
        ac2.setPostCondition(false);
        this.secondMockAction.addActionContext(ac2);

        this.actionManager.pubCheckActionState(tenant, section, actionName);
        assertFalse(this.mockAction.containsRun(ack));
        assertFalse(this.mockAction.containsNotify(ack));
        System.out.println(this.secondMockAction.containsRun(ack2));
        //Test succsessors run
    }

    /**
     * Test of checkActionState with no actionContext and not finished but no notifications needed
     */
    @Test
    public void testcheckActionState3() {
        String tenant = "test1";
        String actionName = "test1";
        String section = "test1";
        this.actionManager.pubCheckActionState(tenant, section, actionName);
    }

    /**
     * Test of checkActionState with no actionContext and not finished and notifications needed
     */
    @Test
    public void testcheckActionState4() {
    }

    private static class ActionContextImpl extends ActionContext {

        public ActionContextImpl(ActionContextKey actionContextKey, List<PreconditionQuery> preconditionQueries) {
            super(actionContextKey, preconditionQueries);
        }
    }
}
