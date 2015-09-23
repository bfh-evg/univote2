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
package ch.bfh.univote2.trustee;

import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.EncryptionKey;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.QueryFactory;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.trustee.mixer.voteMixing.VoteMixingActionContext;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class TrusteeActionHelper {

	public static void checkAndSetCryptoSetting(ATrusteeActionContext actionContext, UniboardService uniboardService,
			TenantManager tenantManager, InformationService informationService, Logger logger) {
		ActionContextKey actionContextKey = actionContext.getActionContextKey();
		String section = actionContext.getSection();
		try {
			CryptoSetting cryptoSetting = actionContext.getCryptoSetting();

			//Add Notification
			if (cryptoSetting == null) {
				cryptoSetting = retrieveCryptoSetting(actionContext, uniboardService);
				actionContext.setCryptoSetting(cryptoSetting);
			}

		} catch (UnivoteException ex) {
			logger.log(Level.INFO, "Could not get cryptoSetting.", ex);
			informationService.informTenant(actionContextKey, ex.getMessage());
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
					QueryFactory.getQueryForCryptoSetting(section), BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
		}
	}

	public static void checkAndSetEncryptionKey(VoteMixingActionContext actionContext, UniboardService uniboardService,
			InformationService informationService, Logger logger) {
		ActionContextKey actionContextKey = actionContext.getActionContextKey();
		String section = actionContext.getSection();
		try {
			EncryptionKey encryptionKey = actionContext.getEncryptionKey();

			//Add Notification
			if (encryptionKey == null) {
				encryptionKey = retrieveEncryptionKey(actionContext, uniboardService);
				actionContext.setEncryptionKey(encryptionKey);
			}

		} catch (UnivoteException ex) {
			logger.log(Level.INFO, "Could not retrive encryption key.", ex);
			informationService.informTenant(actionContextKey, ex.getMessage());
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
					QueryFactory.getQueryForEncryptionKey(section), BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
		}
	}

	public static void checkAndSetAccsessRight(ATrusteeActionContext actionContext, GroupEnum groupEnum,
			UniboardService uniboardService, TenantManager tenantManager, InformationService informationService,
			Logger logger) {
		ActionContextKey actionContextKey = actionContext.getActionContextKey();
		String section = actionContext.getSection();

		try {
			//Check if there is an initial AccessRight for this tenant
			String tenant = actionContext.getTenant();
			PublicKey publicKey = tenantManager.getPublicKey(tenant);
			QueryDTO query = QueryFactory.getQueryForAccessRight(section, publicKey, groupEnum);
			if (uniboardService.get(BoardsEnum.UNIVOTE.getValue(), query).getResult().getPost().isEmpty()) {
				BoardPreconditionQuery bQuery = new BoardPreconditionQuery(query,
						BoardsEnum.UNIVOTE.getValue());
				actionContext.getPreconditionQueries().add(bQuery);
				actionContext.setAccessRightGranted(false);
			} else {
				actionContext.setAccessRightGranted(true);
			}
		} catch (UnivoteException ex) {
			logger.log(Level.INFO, "Could not get access right.", ex);
			informationService.informTenant(actionContextKey, ex.getMessage());
			actionContext.setAccessRightGranted(false);
		}

	}

	public static EncryptionKey retrieveEncryptionKey(ActionContext actionContext, UniboardService uniboardService)
			throws UnivoteException {
		ResultContainerDTO result = uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForEncryptionKey(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Encryption key not published yet.");
		}
		EncryptionKey encryptionKey
				= JSONConverter.unmarshal(EncryptionKey.class, result.getResult().getPost().get(0).getMessage());
		return encryptionKey;

	}

	public static CryptoSetting retrieveCryptoSetting(ActionContext actionContext, UniboardService uniboardService)
			throws UnivoteException {
		ResultContainerDTO result = uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForCryptoSetting(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Crypto setting not published yet.");

		}
		CryptoSetting cryptoSetting
				= JSONConverter.unmarshal(CryptoSetting.class, result.getResult().getPost().get(0).getMessage());
		return cryptoSetting;

	}
}
