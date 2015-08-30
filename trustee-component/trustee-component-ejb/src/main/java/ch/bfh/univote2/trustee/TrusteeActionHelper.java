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

import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModPrime;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.message.CryptoSetting;
import ch.bfh.univote2.component.core.message.EncryptionKey;
import ch.bfh.univote2.component.core.message.JSONConverter;
import ch.bfh.univote2.component.core.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.trustee.mixer.voteMixing.VoteMixingActionContext;
import java.security.PublicKey;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonException;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class TrusteeActionHelper {

    public static void checkAndSetCryptoSetting(ATrusteeActionContext actionContext, UniboardService uniboardService, TenantManager tenantManager, InformationService informationService, Logger logger) {
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
	    logger.log(Level.WARNING, "Could not get securitySetting.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error retrieving securitySetting: " + ex.getMessage());
	} catch (JsonException ex) {
	    logger.log(Level.WARNING, "Could not parse securitySetting.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error reading securitySetting.");
	} catch (Exception ex) {
	    logger.log(Level.WARNING, "Could not parse securitySetting.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error reading securitySetting.");
	}
	if (actionContext.getCryptoSetting() == null) {
	    //Add Notification
	    BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
		    QueryFactory.getQueryForCryptoSetting(section), BoardsEnum.UNIVOTE.getValue());
	    actionContext.getPreconditionQueries().add(bQuery);
	}

    }

    public static void checkAndSetEncryptionKey(VoteMixingActionContext actionContext, UniboardService uniboardService, InformationService informationService, Logger logger) {
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
	    logger.log(Level.WARNING, "Could not get securitySetting.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error retrieving securitySetting: " + ex.getMessage());
	} catch (JsonException ex) {
	    logger.log(Level.WARNING, "Could not parse securitySetting.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error reading securitySetting.");
	} catch (Exception ex) {
	    logger.log(Level.WARNING, "Could not parse securitySetting.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error reading securitySetting.");
	}
	if (actionContext.getEncryptionKey() == null) {
	    //Add Notification
	    BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
		    QueryFactory.getQueryForEncryptionKey(section), BoardsEnum.UNIVOTE.getValue());
	    actionContext.getPreconditionQueries().add(bQuery);
	}

    }

    public static void checkAndSetAccsessRight(ATrusteeActionContext actionContext, GroupEnum groupEnum, UniboardService uniboardService, TenantManager tenantManager, InformationService informationService, Logger logger) {
	ActionContextKey actionContextKey = actionContext.getActionContextKey();
	String section = actionContext.getSection();
	BoardPreconditionQuery bQuery = null;
	try {
	    //Check if there is an initial AccessRight for this tenant
	    //TODO: Check if there is an actual valid access right... Right now it is only checking if there is any access right.
	    String tenant = actionContext.getTenant();
	    PublicKey publicKey = tenantManager.getPublicKey(tenant);
	    bQuery = new BoardPreconditionQuery(QueryFactory.getQueryForAccessRight(section, publicKey, groupEnum), BoardsEnum.UNIVOTE.getValue());
	    actionContext.setAccessRightGranted(uniboardService.get(bQuery.getBoard(), bQuery.getQuery()).getResult().getPost().isEmpty());
	} catch (UnivoteException ex) {
	    logger.log(Level.WARNING, "Could not get access right.", ex);
	    informationService.informTenant(actionContextKey,
					    "Error retrieving access rights: " + ex.getMessage());
	}
	//Add Notification
	if (!Objects.equals(actionContext.getAccessRightGranted(), Boolean.TRUE)) {
	    actionContext.getPreconditionQueries().add(bQuery);
	}

    }

    public static EncryptionKey retrieveEncryptionKey(ActionContext actionContext, UniboardService uniboardService) throws UnivoteException, Exception {
	ResultContainerDTO result = uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
							QueryFactory.getQueryForEncryptionKey(actionContext.getSection()));
	if (result.getResult().getPost().isEmpty()) {
	    throw new UnivoteException("Cryptosetting not published yet.");

	}
	EncryptionKey encryptionKey = JSONConverter.unmarshal(EncryptionKey.class, result.getResult().getPost().get(0).getMessage());
	return encryptionKey;

    }

    public static CryptoSetting retrieveCryptoSetting(ActionContext actionContext, UniboardService uniboardService) throws UnivoteException, Exception {
	ResultContainerDTO result = uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
							QueryFactory.getQueryForCryptoSetting(actionContext.getSection()));
	if (result.getResult().getPost().isEmpty()) {
	    throw new UnivoteException("Cryptosetting not published yet.");

	}
	CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, result.getResult().getPost().get(0).getMessage());
	return cryptoSetting;

    }

    public static UniCryptCryptoSetting getUnicryptCryptoSetting(CryptoSetting setting) throws UnivoteException {
	CyclicGroup cyclicEncryptionGroup = null;
	Element encryptionGenerator = null;
	CyclicGroup cyclicSignatureGroup = null;
	Element signatureGenerator = null;
	HashAlgorithm hashAlgorithm = null;
	switch (setting.getEncryptionSetting()) {
	    case "RC0e":
		cyclicEncryptionGroup = GStarModSafePrime.getFirstInstance(8);
		encryptionGenerator = cyclicEncryptionGroup.getDefaultGenerator();
		break;
	    case "RC1e":
		cyclicEncryptionGroup = GStarModSafePrime.getFirstInstance(8);
		encryptionGenerator = cyclicEncryptionGroup.getDefaultGenerator();
		break;
	    case "RC2e":
		cyclicEncryptionGroup = GStarModSafePrime.getFirstInstance(1024);
		encryptionGenerator = cyclicEncryptionGroup.getDefaultGenerator();
		break;
	    case "RC3e":
		cyclicEncryptionGroup = GStarModSafePrime.getFirstInstance(2048);
		encryptionGenerator = cyclicEncryptionGroup.getDefaultGenerator();
		break;
	    default:
		throw new UnivoteException("Unknown RC_e level");
	}
	switch (setting.getHashSetting()) {
	    case "H1":
		hashAlgorithm = HashAlgorithm.SHA1;
		break;
	    case "H2":
		hashAlgorithm = HashAlgorithm.SHA224;
		break;
	    case "H3":
		hashAlgorithm = HashAlgorithm.SHA256;
		break;
	    case "H4":
		hashAlgorithm = HashAlgorithm.SHA384;
		break;
	    case "H5":
		hashAlgorithm = HashAlgorithm.SHA512;
	    default:
		throw new UnivoteException("Unknown H_ level");
	}
	switch (setting.getSignatureSetting()) {
	    case "RC0s":
		cyclicSignatureGroup = GStarModPrime.getFirstInstance(8, 6);
		signatureGenerator = cyclicSignatureGroup.getDefaultGenerator();
		break;
	    case "RC1s":
		cyclicSignatureGroup = GStarModPrime.getFirstInstance(1024, 160);
		signatureGenerator = cyclicSignatureGroup.getDefaultGenerator();
		break;
	    case "RC2s":
		cyclicSignatureGroup = GStarModPrime.getFirstInstance(2048, 224);
		signatureGenerator = cyclicSignatureGroup.getDefaultGenerator();
		break;
	    case "RC3s":
		cyclicSignatureGroup = GStarModPrime.getFirstInstance(3072, 256);
		signatureGenerator = cyclicSignatureGroup.getDefaultGenerator();
		break;
	    default:
		throw new UnivoteException("Unknown H_ level");
	}
	return new UniCryptCryptoSetting(cyclicEncryptionGroup, encryptionGenerator, cyclicSignatureGroup, signatureGenerator, hashAlgorithm);

    }

}
