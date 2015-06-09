/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.services;

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
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@RunWith(Arquillian.class)
public class RegistrationServiceImplTest {

	public RegistrationServiceImplTest() {
	}

	/**
	 * Helper method for building the in-memory variant of a deployable unit. See Arquillian for more information.
	 *
	 * @return a Java archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive ja = ShrinkWrap.create(WebArchive.class)
				.addClass(TestableRegistrationServiceImpl.class)
				.addClass(ConfigurationManagerMock.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		//System.out.println(ja.toString(true));
		return ja;
	}
	@EJB
	TestableRegistrationServiceImpl regService;

	@EJB
	ConfigurationManagerMock configurationManagerMock;

	/**
	 * Test of init method with a valid configuration of 2 boards
	 */
	@Test
	public void testInit() {
		String ownEndPointURL = "endPoint";
		String boardName1 = "board1";
		String boardEP1 = "EP1";
		String boardWSDL1 = "WSDL1";
		String boardName2 = "board2";
		String boardEP2 = "EP2";
		String boardWSDL2 = "WSDL2";
		Properties p = new Properties();
		p.put("ownEndPointUrl", ownEndPointURL);
		p.put(boardName1 + ".endPointUrl", boardEP1);
		p.put(boardName1 + ".wsdlLocation", boardWSDL1);
		p.put(boardName2 + ".endPointUrl", boardEP2);
		p.put(boardName2 + ".wsdlLocation", boardWSDL2);

		this.configurationManagerMock.setProperties(p);

		this.regService.runInit();

		Map<String, RegistrationServiceImpl.StringTuple> boards = this.regService.getBoards();

		assertEquals(2, boards.keySet().size());
		assertTrue(boards.keySet().contains(boardName1));
		assertTrue(boards.keySet().contains(boardName2));
		assertEquals(boardEP1, boards.get(boardName1).getEndPointUrl());
		assertEquals(boardWSDL1, boards.get(boardName1).getWdslUrl());
		assertEquals(boardEP2, boards.get(boardName2).getEndPointUrl());
		assertEquals(boardWSDL2, boards.get(boardName2).getWdslUrl());

	}

	/**
	 * Test of init method with own Endpoint missing
	 */
	@Test
	public void testInit2() {
		String boardName1 = "board1";
		String boardEP1 = "EP1";
		String boardWSDL1 = "WSDL1";
		String boardName2 = "board2";
		String boardEP2 = "EP2";
		String boardWSDL2 = "WSDL2";
		Properties p = new Properties();
		p.put(boardName1 + ".endPointUrl", boardEP1);
		p.put(boardName1 + ".wsdlLocation", boardWSDL1);
		p.put(boardName2 + ".endPointUrl", boardEP2);
		p.put(boardName2 + ".wsdlLocation", boardWSDL2);

		this.configurationManagerMock.setProperties(p);

		this.regService.runInit();

		Map<String, RegistrationServiceImpl.StringTuple> boards = this.regService.getBoards();

		assertEquals(0, boards.keySet().size());

	}

	/**
	 * Test of init method with a valid configuration of 11 board and one only half
	 */
	@Test
	public void testInit3() {
		String ownEndPointURL = "endPoint";
		String boardName1 = "board1";
		String boardEP1 = "EP1";
		String boardWSDL1 = "WSDL1";
		String boardName2 = "board2";
		String boardEP2 = "EP2";
		Properties p = new Properties();
		p.put("ownEndPointUrl", ownEndPointURL);
		p.put(boardName1 + ".endPointUrl", boardEP1);
		p.put(boardName1 + ".wsdlLocation", boardWSDL1);
		p.put(boardName2 + ".endPointUrl", boardEP2);

		this.configurationManagerMock.setProperties(p);

		this.regService.runInit();

		Map<String, RegistrationServiceImpl.StringTuple> boards = this.regService.getBoards();

		assertEquals(1, boards.keySet().size());
		assertTrue(boards.keySet().contains(boardName1));
		assertEquals(boardEP1, boards.get(boardName1).getEndPointUrl());
		assertEquals(boardWSDL1, boards.get(boardName1).getWdslUrl());

	}

}
