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
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.AESEncryptionScheme;
import ch.bfh.unicrypt.crypto.schemes.hashing.classes.FixedByteArrayHashingScheme;
import ch.bfh.unicrypt.helper.Alphabet;
import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.classes.ConvertMethod;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.BigIntegerToByteArray;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.ByteArrayToByteArray;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.StringToByteArray;
import ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import ch.bfh.unicrypt.helper.hash.HashMethod;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.Z;
import ch.bfh.unicrypt.math.algebra.general.classes.FiniteByteArrayElement;
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.persistence.TenantEntity;
import ch.bfh.univote2.component.core.persistence.TenantEntity_;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Stateless
public class TenantManagerImpl implements TenantManager {

    @PersistenceContext
    private EntityManager entityManager;

    protected static final HashMethod HASH_METHOD = HashMethod.getInstance(
            HashAlgorithm.SHA256,
            ConvertMethod.getInstance(
                    BigIntegerToByteArray.getInstance(ByteOrder.BIG_ENDIAN),
                    ByteArrayToByteArray.getInstance(false),
                    StringToByteArray.getInstance(Charset.forName("UTF-8"))),
            HashMethod.Mode.RECURSIVE);

    protected final Map<String, UnlockedTenant> unlockedTentants = new HashMap<>();

    private static final Logger logger = Logger.getLogger(TenantManagerImpl.class.getName());

    @Override
    public boolean unlock(String tenant, String password) {
        TenantEntity tenantEntity;
        try {
            tenantEntity = this.getTenant(tenant);
        } catch (UnivoteException ex) {
            return false;
        }
        if (this.checkHash(password, tenantEntity.getHashValue(), tenantEntity.getSalt())) {
            try {
                AESEncryptionScheme aes = AESEncryptionScheme.getInstance();
                FiniteByteArrayElement aesKey = aes.getPasswordBasedKey(password);
                //TODO Compute aes key
                Element encPrivKey = aes.getMessageSpace().getElement(tenantEntity.getEncPrivateKey());
                //Load tenant from persistence
                BigInteger dsaPrivKey = aes.decrypt(aesKey, encPrivKey).getBigInteger();
                //Decrypt the private key

                //Create the private key
                PrivateKey privKey = KeyHelper.createDSAPrivateKey(tenantEntity.getModulus(),
                        tenantEntity.getOrderFactor(), tenantEntity.getGenerator(), dsaPrivKey);
                //Add tenant
                this.unlockedTentants.put(tenant, new UnlockedTenant(aesKey.getByteArray(), privKey));
                return true;
            } catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
                //throw new UnivoteException("Could not retrieve privateKey: " + tenant, ex);
                logger.log(Level.WARNING, ex.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean lock(String tenant, String password) {
        if (this.unlockedTentants.containsKey(tenant)) {
            TenantEntity tentantEntity;
            try {
                tentantEntity = this.getTenant(tenant);
            } catch (UnivoteException ex) {
                return false;
            }
            if (this.checkHash(password, tentantEntity.getHashValue(), tentantEntity.getSalt())) {
                this.unlockedTentants.remove(tenant);
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getUnlockedTenants() {
        return this.unlockedTentants.keySet();
    }

    @Override
    public Set<String> getAllTentants() {
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<TenantEntity> query = builder.createQuery(TenantEntity.class);
            Root<TenantEntity> dbResult = query.from(TenantEntity.class);
            query.select(dbResult);
            List<TenantEntity> tentantEntities = entityManager.createQuery(query).getResultList();
            Set<String> result = new HashSet<>();
            for (TenantEntity t : tentantEntities) {
                result.add(t.getName());
            }
            return result;
        } catch (Exception ex) {
            return new HashSet<>();
        }
    }

    @Override
    public PublicKey getPublicKey(String tenant) throws UnivoteException {
        try {
            TenantEntity tenantEntity = this.getTenant(tenant);
            return KeyHelper.createDSAPublicKey(tenantEntity.getModulus(), tenantEntity.getOrderFactor(),
                    tenantEntity.getGenerator(), tenantEntity.getPublicKey());
        } catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
            throw new UnivoteException("Could not create publicKey: " + tenant, ex);
        }

    }

    @Override
    public PrivateKey getPrivateKey(String tenant) throws UnivoteException {
        if (this.unlockedTentants.containsKey(tenant)) {
            return this.unlockedTentants.get(tenant).getPrivateKey();
        }
        throw new UnivoteException("Tenant locked: " + tenant);
    }

    protected TenantEntity getTenant(String tenant) throws UnivoteException {
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<TenantEntity> query = builder.createQuery(TenantEntity.class);
            Root<TenantEntity> result = query.from(TenantEntity.class);
            query.select(result);
            query.where(builder.equal(result.get(TenantEntity_.name), tenant));
            return entityManager.createQuery(query).getSingleResult();
        } catch (Exception ex) {
            throw new UnivoteException("Could not find tenant in db: " + tenant, ex);
        }
    }

    protected boolean checkHash(String password, BigInteger hash, BigInteger salt) {
        StringMonoid stringSet = StringMonoid.getInstance(Alphabet.PRINTABLE_ASCII);
        Z z = Z.getInstance();

        Element passwordElement = stringSet.getElement(password);
        Element saltElement = z.getElement(salt);

        Pair pair = Pair.getInstance(passwordElement, saltElement);
        FixedByteArrayHashingScheme scheme = FixedByteArrayHashingScheme.getInstance(pair.getSet());
        Element hashElement = scheme.getHashSpace().getElementFrom(hash);

        return scheme.check(pair, hashElement).getValue();
    }

    @Override
    public boolean isLocked(String tenant) {
        return this.unlockedTentants.containsKey(tenant);
    }

    @Override
    public ByteArray getAESKey(String tenant) throws UnivoteException {
        if (this.unlockedTentants.containsKey(tenant)) {
            return this.unlockedTentants.get(tenant).getAESKey();
        }
        throw new UnivoteException("Tenant locked: " + tenant);
    }

    @Override
    public boolean checkLogin(String tenant, String password) {
        TenantEntity tenantEntity;
        try {
            tenantEntity = this.getTenant(tenant);
        } catch (UnivoteException ex) {
            logger.log(Level.WARNING, ex.getMessage());
            return false;
        }
        return this.checkHash(password, tenantEntity.getHashValue(), tenantEntity.getSalt());
    }

}
