/*
 * UniVote2
 *
 *  UniVote2(tm): An Internet-based, verifiable e-voting system for student elections in Switzerland
 *  Copyright (c) 2015 Bern University of Applied Sciences (BFH),
 *  Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *  Quellgasse 21, CH-2501 Biel, Switzerland
 *
 *  Licensed under Dual License consisting of:
 *  1. GNU Affero General Public License (AGPL) v3
 *  and
 *  2. Commercial license
 *
 *
 *  1. This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  2. Licensees holding valid commercial licenses for UniVote2 may use this file in
 *   accordance with the commercial license agreement provided with the
 *   Software or, alternatively, in accordance with the terms contained in
 *   a written agreement between you and Bern University of Applied Sciences (BFH),
 *   Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *   Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 *   For further information contact <e-mail: univote@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
 */
package ch.bfh.univote2.component.core.services;

import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.notification.NotificationService;
import ch.bfh.uniboard.notification.NotificationService_Service;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.component.core.data.UniBoard;
import ch.bfh.univote2.component.core.manager.ConfigurationManager;
import ch.bfh.univote2.component.core.manager.UniBoardManager;
import java.net.URL;
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
	private static final String OWN_ENDPOINT_URL = "ownEndPointUrl";
	private String ownEndPointURL;

	private static final Logger logger = Logger.getLogger(RegistrationServiceImpl.class.getName());

	@EJB
	ConfigurationManager configurationManager;

	@EJB
	UniBoardManager uniBoardManager;

	@PostConstruct
	public void init() {
		try {
			if (!this.configurationManager.getConfiguration(CONFIG_NAME).containsKey(OWN_ENDPOINT_URL)) {
				logger.log(Level.SEVERE, "Own endpoint URL is not configured. Cant register.");
				return;
			}
			this.ownEndPointURL = (String) this.configurationManager.getConfiguration(CONFIG_NAME).remove(OWN_ENDPOINT_URL);
		} catch (UnivoteException ex) {
			logger.log(Level.SEVERE, "Cant load configuration.", ex);
		}
	}

	@Override
	public String register(String board, QueryDTO q) throws UnivoteException {
		return this.getNotificationService(this.uniBoardManager.getUniBoard(board)).register(ownEndPointURL, q);
	}

	@Override
	public void unregister(String board, String notificationCode) throws UnivoteException {
		this.getNotificationService(this.uniBoardManager.getUniBoard(board)).unregister(notificationCode);
	}

	@Override
	public void unregisterUnknownNotification(String notificationCode
	) {
		for (UniBoard board : this.uniBoardManager.getAllUniBoards()) {
			try {
				this.getNotificationService(board).unregister(notificationCode);
			} catch (UnivoteException ex) {
				logger.log(Level.WARNING, ex.getMessage());
				if (ex.getCause() != null) {
					logger.log(Level.WARNING, ex.getCause().getMessage());
				}
			}
		}
	}

	private NotificationService getNotificationService(UniBoard board) throws UnivoteException {
		try {

			URL wsdlLocation = new URL(board.getWsdlURL());
			QName qname = new QName("http://uniboard.bfh.ch/notification/", "NotificationService");
			NotificationService_Service ubService = new NotificationService_Service(wsdlLocation, qname);
			NotificationService notificationService = ubService.getNotificationServicePort();
			BindingProvider bp = (BindingProvider) notificationService;
			bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, board.getEndPointURL());
			return notificationService;
		} catch (Exception ex) {
			throw new UnivoteException("Unable to connect to UniBoard service: " + board.getEndPointURL(), ex);
		}
	}

}
