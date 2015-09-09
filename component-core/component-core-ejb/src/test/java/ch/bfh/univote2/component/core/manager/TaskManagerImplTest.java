/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.manager;

import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.component.core.data.RunActionTask;
import ch.bfh.univote2.component.core.data.Task;
import ch.bfh.univote2.component.core.data.UserInput;
import ch.bfh.univote2.component.core.data.UserInputTask;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@RunWith(Arquillian.class)
public class TaskManagerImplTest {

	public TaskManagerImplTest() {
	}

	/**
	 * Helper method for building the in-memory variant of a deployable unit. See Arquillian for more information.
	 *
	 * @return a Java archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive ja = ShrinkWrap.create(WebArchive.class)
				.addClass(TestableTaskManagerImpl.class)
				.addClass(ActionManagerMock.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		//System.out.println(ja.toString(true));
		return ja;
	}

	@EJB
	TestableTaskManagerImpl taskManager;

	@EJB
	ActionManagerMock actionManagerMock;

	/**
	 * Test of getTasks method with no entry for the specified tenant.
	 */
	@Test
	public void testGetTasks1() {
		String tenantToTest = "tenant1";
		String otherTenant = "tenant2";
		String inputName1 = "inutName1";
		String section1 = "section1";
		String section2 = "section2";

		UserInputTask userInputTask1 = new UserInputTask(inputName1, otherTenant, section1);
		UserInputTask userInputTask2 = new UserInputTask(inputName1, otherTenant, section2);

		this.taskManager.getTasks().put(otherTenant + section1, userInputTask1);
		this.taskManager.getTasks().put(otherTenant + section2, userInputTask2);

		List<Task> tasks = this.taskManager.getTasks(tenantToTest);
		assertTrue(tasks.isEmpty());
	}

	/**
	 * Test of getTasks method with 1 entry for the specified tenant.
	 */
	@Test
	public void testGetTasks2() {
		String tenantToTest = "tenant1";
		String otherTenant = "tenant2";
		String inputName1 = "inutName1";
		String section1 = "section1";
		String section2 = "section2";

		UserInputTask userInputTask1 = new UserInputTask(inputName1, tenantToTest, section1);
		UserInputTask userInputTask2 = new UserInputTask(inputName1, otherTenant, section2);

		this.taskManager.getTasks().put(tenantToTest + section1, userInputTask1);
		this.taskManager.getTasks().put(otherTenant + section2, userInputTask2);

		List<Task> tasks = this.taskManager.getTasks(tenantToTest);
		assertFalse(tasks.isEmpty());
		assertEquals(1, tasks.size());
		assertEquals(userInputTask1, tasks.get(0));
		tasks.clear();
	}

	/**
	 * Test of addUserInputTask method
	 */
	@Test
	public void testAddUserInputTask1() {
		String tenantToTest = "tenant1";
		String inputName1 = "inutName1";
		String section1 = "section1";

		UserInputTask userInputTask1 = new UserInputTask(inputName1, tenantToTest, section1);

		String notificationCode = this.taskManager.addUserInputTask(userInputTask1);

		Map<String, Task> tasks = this.taskManager.getTasks();

		assertFalse(tasks.isEmpty());
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsKey(notificationCode));
		assertEquals(userInputTask1, tasks.get(notificationCode));
		tasks.clear();
	}

	/**
	 * Test of addRunActionTask method
	 */
	@Test
	public void testAddRunActionTask1() {
		String tenantToTest = "tenant1";
		String actionName1 = "actionName1";
		String section1 = "section1";

		RunActionTask runActionTask1 = new RunActionTask(actionName1, tenantToTest, section1);

		this.taskManager.addRunActionTask(runActionTask1);

		Map<String, Task> tasks = this.taskManager.getTasks();

		assertFalse(tasks.isEmpty());
		assertEquals(1, tasks.size());
		assertTrue(tasks.values().contains(runActionTask1));
		tasks.clear();
	}

	/**
	 * Test of userInputReceived method with correct notification code
	 */
	@Test
	public void testUserInputReceived1() {
		String tenantToTest = "tenant1";
		String inputName1 = "inutName1";
		String section1 = "section1";

		UserInputTask userInputTask1 = new UserInputTask(inputName1, tenantToTest, section1);

		String notificationCode = this.taskManager.addUserInputTask(userInputTask1);

		UserInput userInput = new UserInputMock(inputName1);

		try {
			this.taskManager.userInputReceived(notificationCode, userInput);
		} catch (UnivoteException ex) {
			fail();
		}

		assertEquals(notificationCode, this.actionManagerMock.getLastNotificationCode());
		assertEquals(userInput, this.actionManagerMock.getLastUserInput());

		Map<String, Task> tasks = this.taskManager.getTasks();

		assertFalse(tasks.containsKey(notificationCode));
		tasks.clear();

	}

	/**
	 * Test of userInputReceived method with wrong notification code
	 */
	@Test
	public void testUserInputReceived2() {
		String tenantToTest = "tenant1";
		String inputName1 = "inutName1";
		String section1 = "section1";

		UserInputTask userInputTask1 = new UserInputTask(inputName1, tenantToTest, section1);

		String notificationCode = this.taskManager.addUserInputTask(userInputTask1);

		UserInput userInput = new UserInputMock(inputName1);

		try {
			this.taskManager.userInputReceived(notificationCode + "1", userInput);
			fail();
		} catch (UnivoteException ex) {

		}

		Map<String, Task> tasks = this.taskManager.getTasks();

		assertTrue(tasks.containsKey(notificationCode));
		tasks.clear();

	}

	/**
	 * Test of runAction(String notificationCode) method with correct notification code
	 */
	@Test
	public void testRunAction1() {
		String tenantToTest = "runAction1";
		String actionName1 = "actionName1";
		String section1 = "section1";
		String notificationCode = tenantToTest + section1;

		RunActionTask runActionTask1 = new RunActionTask(actionName1, tenantToTest, section1);

		Map<String, Task> tasks = this.taskManager.getTasks();
		tasks.put(notificationCode, runActionTask1);

		try {
			this.taskManager.runAction(notificationCode);
		} catch (UnivoteException ex) {
			fail();
		}

		assertEquals(actionName1, this.actionManagerMock.getActionName());
		assertEquals(tenantToTest, this.actionManagerMock.getTenant());
		assertEquals(section1, this.actionManagerMock.getSection());

		assertFalse(tasks.containsKey(notificationCode));
		tasks.clear();
	}

	/**
	 * Test of runAction(String notificationCode) method with incorrect notification code
	 */
	@Test
	public void testRunAction2() {
		String tenantToTest = "runAction1";
		String actionName1 = "actionName1";
		String section1 = "section1";
		String notificationCode = tenantToTest + section1;

		RunActionTask runActionTask1 = new RunActionTask(actionName1, tenantToTest, section1);

		Map<String, Task> tasks = this.taskManager.getTasks();
		tasks.put(notificationCode, runActionTask1);

		try {
			this.taskManager.runAction(notificationCode + "1");
			fail();
		} catch (UnivoteException ex) {

		}

		assertTrue(tasks.containsKey(notificationCode));
		tasks.clear();
	}

	/**
	 * Test of runAction(String notificationCode) method with correct notification code but wrong task
	 */
	@Test
	public void testRunAction3() {
		String tenantToTest = "runAction1";
		String actionName1 = "actionName1";
		String section1 = "section1";
		String notificationCode = tenantToTest + section1;

		UserInputTask userInputTask1 = new UserInputTask(actionName1, tenantToTest, section1);

		Map<String, Task> tasks = this.taskManager.getTasks();
		tasks.put(notificationCode, userInputTask1);

		try {
			this.taskManager.runAction(notificationCode + "1");
			fail();
		} catch (UnivoteException ex) {

		}

		assertTrue(tasks.containsKey(notificationCode));
		tasks.clear();
	}

	/**
	 * Test of runAction(String actionName, String tenant, String section)
	 */
	@Test
	public void testRunAction4() {
		String tenantToTest = "runAction3";
		String actionName1 = "actionName1";
		String section1 = "section1";
		String notificationCode = tenantToTest + section1;

		try {
			this.taskManager.runAction(actionName1, tenantToTest, section1);
		} catch (UnivoteException ex) {
			fail();
		}

		assertEquals(actionName1, this.actionManagerMock.getActionName());
		assertEquals(tenantToTest, this.actionManagerMock.getTenant());
		assertEquals(section1, this.actionManagerMock.getSection());

		Map<String, Task> tasks = this.taskManager.getTasks();
		assertFalse(tasks.containsKey(notificationCode));
		tasks.clear();
	}

	private static class UserInputMock implements UserInput {

		private final String input;

		public UserInputMock(String input) {
			this.input = input;
		}

		public String getInput() {
			return input;
		}

	}
}
