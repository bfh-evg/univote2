/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.services;

import ch.bfh.univote2.common.UnivoteException;
import java.util.Map;
import java.util.Properties;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@RunWith(Arquillian.class)
public class OutcomeRoutingServiceImplTest {

	public OutcomeRoutingServiceImplTest() {
	}

	/**
	 * Helper method for building the in-memory variant of a deployable unit. See Arquillian for more information.
	 *
	 * @return a Java archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive ja = ShrinkWrap.create(WebArchive.class)
				.addClass(TestableOutcomeRoutingServiceImpl.class)
				.addClass(ConfigurationManagerMock.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		//System.out.println(ja.toString(true));
		return ja;
	}

	@EJB
	ConfigurationManagerMock configurationManagerMock;

	@EJB
	TestableOutcomeRoutingServiceImpl outcomeRoutingServiceImpl;

	/**
	 * Test of init method with two routings in the config
	 */
	@Test
	public void testInit() {
		String inputName1 = "inputName1";
		String outcome1 = "outcome1";
		String inputName2 = "inputName2";
		String outcome2 = "outcome2";

		Properties p = new Properties();
		p.setProperty(inputName1, outcome1);
		p.setProperty(inputName2, outcome2);

		this.configurationManagerMock.setProperties(p);

		this.outcomeRoutingServiceImpl.runInit();

		Map<String, String> routing = this.outcomeRoutingServiceImpl.getRouting();
		assertEquals(2, routing.keySet().size());
		assertTrue(routing.keySet().contains(inputName1));
		assertEquals(outcome1, routing.get(inputName1));
		assertTrue(routing.keySet().contains(inputName2));
		assertEquals(outcome2, routing.get(inputName2));
		routing.clear();
	}

	/**
	 * Test of getRoutingForUserInput method with existing routing
	 */
	@Test
	public void testGetRoutingForUserInput1() {
		String inputName1 = "inputName1";
		String outcome1 = "outcome1";
		String inputName2 = "inputName2";
		String outcome2 = "outcome2";

		Map<String, String> routing = this.outcomeRoutingServiceImpl.getRouting();
		routing.put(inputName1, outcome1);
		routing.put(inputName2, outcome2);

		try {
			String result1 = this.outcomeRoutingServiceImpl.getRoutingForUserInput(inputName1);
			assertEquals(outcome1, result1);
		} catch (UnivoteException ex) {
			fail();
		}

		try {
			String result2 = this.outcomeRoutingServiceImpl.getRoutingForUserInput(inputName2);
			assertEquals(outcome2, result2);
		} catch (UnivoteException ex) {
			fail();
		}
		routing.clear();
	}

	/**
	 * Test of getRoutingForUserInput method with a non-existing routing
	 */
	@Test
	public void testGetRoutingForUserInput2() {
		String inputName1 = "inputName1";
		String outcome1 = "outcome1";
		String inputName2 = "inputName2";

		Map<String, String> routing = this.outcomeRoutingServiceImpl.getRouting();
		routing.put(inputName1, outcome1);

		try {
			this.outcomeRoutingServiceImpl.getRoutingForUserInput(inputName2);
			fail();
		} catch (UnivoteException ex) {
			routing.clear();
		}
	}
}
