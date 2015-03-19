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

import ch.bfh.uniboard.clientlib.BoardErrorException;
import ch.bfh.uniboard.clientlib.GetException;
import ch.bfh.uniboard.clientlib.GetHelper;
import ch.bfh.uniboard.clientlib.KeyHelper;
import ch.bfh.uniboard.clientlib.PostException;
import ch.bfh.uniboard.clientlib.PostHelper;
import ch.bfh.uniboard.clientlib.signaturehelper.SignatureException;
import ch.bfh.uniboard.data.AttributesDTO;
import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.manager.ConfigurationManager;
import ch.bfh.univote2.component.core.manager.TenantManager;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class UniBoardServiceImpl implements UniboardService {

	private static final Logger logger = Logger.getLogger(UniBoardServiceImpl.class.getName());
	private static final String CONFIG_NAME = "uniboard-helper";

	@EJB
	TenantManager tenantManager;

	@EJB
	ConfigurationManager configurationManager;

	@Override
	public ResultContainerDTO get(QueryDTO query) throws UnivoteException {
		String wsdlLocation = this.configurationManager.getConfiguration(CONFIG_NAME).getProperty("wsdlLocation");
		String endPointUrl = this.configurationManager.getConfiguration(CONFIG_NAME).getProperty("endPointUrl");
		try {
			GetHelper getHelper = new GetHelper(this.getBoardKey(), wsdlLocation, endPointUrl);
			return getHelper.get(query);
		} catch (GetException ex) {
			throw new UnivoteException("Could not create wsclient.", ex);
		} catch (SignatureException ex) {
			throw new UnivoteException("Could not verify answer.", ex);
		}

	}

	@Override
	public AttributesDTO post(String section, String group, byte[] message, String tenant)
			throws UnivoteException {
		String wsdlLocation = this.configurationManager.getConfiguration(CONFIG_NAME).getProperty("wsdlLocation");
		String endPointUrl = this.configurationManager.getConfiguration(CONFIG_NAME).getProperty("endPointUrl");
		try {
			PostHelper postHelper = new PostHelper(this.tenantManager.getPublicKey(tenant),
					this.tenantManager.getPrivateKey(tenant), this.getBoardKey(), wsdlLocation, endPointUrl);
			return postHelper.post(message, section, group);
		} catch (PostException ex) {
			throw new UnivoteException("Could not create wsclient.", ex);
		} catch (SignatureException ex) {
			throw new UnivoteException("Could not sign message/Verify response.", ex);
		} catch (BoardErrorException ex) {
			throw new UnivoteException("Uniboard rejected the message", ex);
		}
	}

	protected PublicKey getBoardKey() throws UnivoteException {
		Properties config = this.configurationManager.getConfiguration(CONFIG_NAME);
		BigInteger y = new BigInteger(config.getProperty("y"));
		BigInteger p = new BigInteger(config.getProperty("p"));
		BigInteger q = new BigInteger(config.getProperty("q"));
		BigInteger g = new BigInteger(config.getProperty("g"));
		try {
			return KeyHelper.createDSAPublicKey(p, q, g, y);
		} catch (InvalidKeySpecException ex) {
			throw new UnivoteException("Could not create publicKey for UniBoard", ex);
		}
	}

}
