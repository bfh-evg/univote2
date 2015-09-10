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
package ch.bfh.univote2.component.core.manager;

import ch.bfh.uniboard.clientlib.KeyHelper;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.component.core.data.UniBoard;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
@DependsOn("ConfigurationManagerImpl")
public class UniBoardManagerImpl implements UniBoardManager {

	private static final String CONFIG_NAME = "uniboard-manager";
	private static final String WSDL_URL = "wsdlLocation";
	private static final String ENDPOINT_URL = "endPointUrl";
	private static final String NOTIFICATION_WSDL_URL = "notificationWsdlLocation";
	private static final String NOTIFICATION_ENDPOINT_URL = "notificationEndPointUrl";
	private static final Logger logger = Logger.getLogger(UniBoardManagerImpl.class.getName());

	private Map<String, UniBoard> uniboards;

	@EJB
	ConfigurationManager configurationManager;

	@PostConstruct
	public void init() {
		try {
			this.uniboards = new HashMap<>();
			Set<String> uniBoardNames = this.configurationManager.getConfiguration(CONFIG_NAME).stringPropertyNames();

			for (String name : uniBoardNames) {

				Properties tmpProp = this.configurationManager.getConfiguration(name);
				String wsdlUrl = tmpProp.getProperty(WSDL_URL);
				String endPointUrl = tmpProp.getProperty(ENDPOINT_URL);
				String notificationWsdlLocation = tmpProp.getProperty(NOTIFICATION_WSDL_URL);
				String notificationEndPointUrl = tmpProp.getProperty(NOTIFICATION_ENDPOINT_URL);
				PublicKey publicKey = this.getBoardKey(tmpProp);
				UniBoard tmpUniBoard = new UniBoard(wsdlUrl, endPointUrl, notificationWsdlLocation,
						notificationEndPointUrl, publicKey);
				this.uniboards.put(name, tmpUniBoard);

			}
		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Configuration error.", ex);
		}

	}

	@Override
	public UniBoard getUniBoard(String uniBoardName) throws UnivoteException {
		if (!this.uniboards.containsKey(uniBoardName)) {
			throw new UnivoteException("Unknown uniboard name: " + uniBoardName);
		}
		return this.uniboards.get(uniBoardName);
	}

	@Override
	public Collection<UniBoard> getAllUniBoards() {
		return this.uniboards.values();
	}

	protected PublicKey getBoardKey(Properties config) throws UnivoteException {
		BigInteger y = new BigInteger(config.getProperty("y"));
		BigInteger p = new BigInteger(config.getProperty("p"));
		BigInteger q = new BigInteger(config.getProperty("q"));
		BigInteger g = new BigInteger(config.getProperty("g"));
		try {
			return KeyHelper.createDSAPublicKey(p, q, g, y);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
			throw new UnivoteException("Could not create publicKey for UniBoard", ex);
		}
	}

}
