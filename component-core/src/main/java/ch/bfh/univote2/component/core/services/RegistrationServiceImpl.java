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
package ch.bfh.univote2.component.core.services;

import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.notification.NotificationService;
import ch.bfh.uniboard.notification.NotificationService_Service;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.manager.ConfigurationManager;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

@Stateless
public class RegistrationServiceImpl implements RegistrationService {

	private static final String CONFIG_NAME = "registration-helper";
	private static final String WSDL_URL = "wsdlLocation";
	private static final String ENDPOINT_URL = "endPointUrl";
	private static final String OWN_ENDPOINT_URL = "ownEndPointUrl";
	private Map<String, StringTuple> boards;

	private static final Logger logger = Logger.getLogger(RegistrationServiceImpl.class.getName());

	@EJB
	ConfigurationManager configurationManager;

	@PostConstruct
	public void startUp() {
		Set<String> boardCandidates = this.configurationManager.getConfiguration(CONFIG_NAME).stringPropertyNames();
		OUTER:
		for (String boardCandidate : boardCandidates) {
			String[] split = boardCandidate.split(".");
			if (split.length == 2) {
				String name = split[0];
				String urlType = split[1];
				if (!boards.containsKey(name)) {
					String boardCandidate2;
					switch (urlType) {
						case WSDL_URL:
							boardCandidate2 = name + "." + ENDPOINT_URL;
							break;
						case ENDPOINT_URL:
							boardCandidate2 = name + "." + WSDL_URL;
							break;
						default:
							break OUTER;
					}
					if (boardCandidates.contains(boardCandidate2)) {
						this.boards.put(name, new StringTuple(
								this.configurationManager.getConfiguration(CONFIG_NAME).getProperty(boardCandidate),
								this.configurationManager.getConfiguration(CONFIG_NAME).getProperty(boardCandidate2)
						));
					}
				}
			}
		}

	}

	@Override
	public String register(String board, QueryDTO q) throws UnivoteException {
		return this.getNotificationService(board).register(
				this.configurationManager.getConfiguration(CONFIG_NAME).getProperty(OWN_ENDPOINT_URL), q);
	}

	@Override
	public void unregister(String board, String notificationCode) throws UnivoteException {
		this.getNotificationService(board).unregister(notificationCode);
	}

	@Override
	public void unregisterUnknownNotification(String notificationCode) {
		for (String board : this.boards.keySet()) {
			try {
				this.unregister(board, notificationCode);
			} catch (UnivoteException ex) {
				//TODO
			}
		}
	}

	private NotificationService getNotificationService(String board) {
		StringTuple boardUrls = this.boards.get(board);
		try {

			URL wsdlLocation = new URL(boardUrls.getWdslUrl());
			QName qname = new QName("http://uniboard.bfh.ch/", "UniBoardService");
			NotificationService_Service ubService = new NotificationService_Service(wsdlLocation, qname);
			NotificationService notificationService = ubService.getNotificationServicePort();
			BindingProvider bp = (BindingProvider) notificationService;
			bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, boardUrls.getEndPointUrl());
			return notificationService;
		} catch (Exception ex) {
			logger.log(Level.INFO, "Unable to connect to UniBoard service: {0}, exception: {1}",
					new Object[]{boardUrls.getEndPointUrl(), ex});
			return null;
		}
	}

	private class StringTuple {

		private final String wdslUrl;
		private final String endPointUrl;

		public StringTuple(String wdslUrl, String endPointUrl) {
			this.wdslUrl = wdslUrl;
			this.endPointUrl = endPointUrl;
		}

		public String getWdslUrl() {
			return wdslUrl;
		}

		public String getEndPointUrl() {
			return endPointUrl;
		}

	}
}
