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
package ch.bfh.univote2.component.core.webservice;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.notification.ObserverService;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import java.util.logging.Level;
import java.util.logging.Logger;
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
		targetNamespace = "http://uniboard.bfh.ch/notification/",
		wsdlLocation = "WEB-INF/wsdl/ObserverService.wsdl")
@Stateless
public class ObserverServiceImpl implements ObserverService {

	static final Logger logger = Logger.getLogger(ObserverServiceImpl.class.getName());

	@EJB
	ActionManager notificationManager;

	@Override
	public void notify(String notificationCode, PostDTO post) {
		logger.log(Level.INFO, "Received notification: {0}", notificationCode);
		notificationManager.onBoardNotification(notificationCode, post);
	}

}
