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

import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.manager.ConfigurationManager;
import ch.bfh.univote2.component.core.manager.TaskManager;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.services.InitialisationService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
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
		System.out.println(ja.toString(true));
		return ja;
	}

	@EJB
	TestableActionManagerImpl actionManager;

	@EJB
	RegistrationServiceMock registrationHelper;

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
		ActionContext ac = new ActionContextImpl(ack, null, false);
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
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), true);
		ac.setPostCondition(true);
		this.mockAction.addActionContext(ac);

		ActionContextKey ack2 = new ActionContextKey(secondActionName, tenant, section);
		ActionContext ac2 = new ActionContextImpl(ack2, new ArrayList<>(), true);
		ac2.setPostCondition(false);
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
		this.actionManager.pubCheckActionState(tenant, section, actionName);

		ActionContextKey ack = new ActionContextKey(actionName, tenant, section);
		ActionContext ac = new ActionContextImpl(ack, new ArrayList<>(), true);
		ac.setPostCondition(false);
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
		ActionContext ac = new ActionContextImpl(ack, preconditions, true);
		ac.setPostCondition(false);
		this.mockAction.addActionContext(ac);

		this.actionManager.pubCheckActionState(actionName, tenant, section);
		assertTrue(this.mockAction.containsRun(ack));
		assertEquals(this.registrationHelper.getLastRegistredQuery(), query);
	}

	private static class ActionContextImpl extends ActionContext {

		public ActionContextImpl(ActionContextKey actionContextKey, List<PreconditionQuery> preconditionQueries,
				boolean postCondition) {
			super(actionContextKey, preconditionQueries);
			this.setPostCondition(postCondition);
		}
	}
}
