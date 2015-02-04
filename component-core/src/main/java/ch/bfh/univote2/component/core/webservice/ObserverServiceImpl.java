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
package ch.bfh.univote2.component.core.webservice;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.notification.ObserverService;
import ch.bfh.univote2.component.core.manager.NotificationManagerImpl;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@WebService(serviceName = "ObserverService",
		portName = "ObserverServicePort",
		endpointInterface = "ch.bfh.uniboard.notification.ObserverService",
		targetNamespace = "http://uniboard.bfh.ch/notification",
		wsdlLocation = "META-INF/wsdl/ObserverService.wsdl")
@Stateless
public class ObserverServiceImpl implements ObserverService {

	@EJB
	NotificationManagerImpl notificationManager;

	@Override
	public void notify(String notificationCode, PostDTO post) {
		notificationManager.onBoardNotification(notificationCode, post);
	}

}
