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
package ch.bfh.univote2.testdatagenerator;

import ch.bfh.unicrypt.crypto.schemes.hashing.classes.FixedByteArrayHashingScheme;
import ch.bfh.unicrypt.helper.math.Alphabet;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.Z;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.univote2.common.crypto.KeyUtil;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.interfaces.DSAPublicKey;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class TenantEntityCreator {

	private static final String TENANT_CERTIFICATE = "../trustee-certificate.pem";
	private static final String TENANT_ENCRYPTED_PRIVATE_KEY = "../trustee-encrypted-private-key.pem";
	private static final String TENANT_PRIVATE_KEY_PASSWORD = "12345678";
	private static final String TENANT_NAME = "sevi";

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws Exception {
		//Load public and private key
		DSAPublicKey posterPublicKey = KeyUtil.getDSAPublicKey(TENANT_CERTIFICATE);
		KeyUtil.getDSAPrivateKey(
				TENANT_ENCRYPTED_PRIVATE_KEY, TENANT_PRIVATE_KEY_PASSWORD, posterPublicKey.getParams());
		String encryptedKey = new String(Files.readAllBytes(Paths.get(TENANT_ENCRYPTED_PRIVATE_KEY)));
		//Choose salt and do hash
		StringMonoid stringSet = StringMonoid.getInstance(Alphabet.PRINTABLE_ASCII);
		Element password = stringSet.getElement(TENANT_PRIVATE_KEY_PASSWORD);

		ZMod saltSet = ZMod.getRandomInstance(256);
		Element salt = saltSet.getRandomElement();
		Element bigIntegerElement = Z.getInstance().getElement(salt.convertToBigInteger());

		ProductSet messageSpace = ProductSet.getInstance(stringSet, Z.getInstance());
		Tuple message = messageSpace.getElement(password, bigIntegerElement);

		FixedByteArrayHashingScheme scheme = FixedByteArrayHashingScheme.getInstance(messageSpace);

		Element hash = scheme.hash(message);
		// TODO Print sql statement
		String sql = "INSERT INTO `TENANTENTITY` (`ID`, `ENCPRIVATEKEY`, `GENERATOR`, `HASHVALUE`, `MODULUS`,"
				+ " `NAME`, `ORDERFACTOR`, `PUBLICKEY`, `SALT`) VALUES ("
				+ "1, "
				+ "'" + encryptedKey + "', "
				+ "'" + posterPublicKey.getParams().getG() + "', "
				+ "'" + hash.convertToBigInteger().toString(10) + "', "
				+ "'" + posterPublicKey.getParams().getP() + "', "
				+ "'" + TENANT_NAME + "', "
				+ "'" + posterPublicKey.getParams().getQ() + "', "
				+ "'" + posterPublicKey.getY() + "', "
				+ "'" + salt.convertToBigInteger().toString(10) + "'"
				+ ");";
		System.out.println(sql);
	}

}
