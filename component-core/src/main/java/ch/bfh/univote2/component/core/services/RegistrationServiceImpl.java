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

        for (String boardCandidate : boardCandidates) {
            String[] split = boardCandidate.split(".");
            if (split.length == 2) {
                String name = split[0];
                String urlType = split[1];
                if (urlType.equals(WSDL_URL)) {
                    String boardCandidate2 = name + "." + ENDPOINT_URL;
                    if (boardCandidates.contains(boardCandidate2)) {
                        this.boards.put(name, new StringTuple(
                                this.configurationManager.getConfiguration(CONFIG_NAME).getProperty(boardCandidate),
                                this.configurationManager.getConfiguration(CONFIG_NAME).getProperty(boardCandidate2)
                        ));
                    }
                } else if (urlType.equals(ENDPOINT_URL)) {
                    String boardCandidate2 = name + "." + WSDL_URL;
                    this.boards.put(name, new StringTuple(
                            this.configurationManager.getConfiguration(CONFIG_NAME).getProperty(boardCandidate),
                            this.configurationManager.getConfiguration(CONFIG_NAME).getProperty(boardCandidate2)
                    ));
                } else {
                    logger.log(Level.SEVERE, "Unknown property {0}", urlType);
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
    public void unregisterUnknownNotification(String notificationCode
    ) {
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
