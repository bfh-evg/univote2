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
package ch.bfh.univote2.component.core.manager;

import ch.bfh.uniboard.clientlib.KeyHelper;
import ch.bfh.unicrypt.crypto.schemes.hashing.classes.FixedByteArrayHashingScheme;
import ch.bfh.unicrypt.helper.Alphabet;
import ch.bfh.unicrypt.helper.converter.classes.ConvertMethod;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.BigIntegerToByteArray;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.ByteArrayToByteArray;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.StringToByteArray;
import ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import ch.bfh.unicrypt.helper.hash.HashMethod;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.Z;
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.helper.AESEncryptionHelper;
import ch.bfh.univote2.component.core.helper.EncryptionHelper;
import ch.bfh.univote2.component.core.persistence.TenantEntity;
import ch.bfh.univote2.component.core.persistence.TenantEntity_;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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

	protected final Map<String, EncryptionHelper> unlockedTentants = new HashMap<>();

	@Override
	public boolean unlock(String tenant, String password) throws UnivoteException {
		TenantEntity tentantEntity = this.getTenant(tenant);
		if (this.checkHash(password, tentantEntity.getHashValue(), tentantEntity.getSalt())) {
			this.unlockedTentants.put(tenant, new AESEncryptionHelper(password));
			return true;
		}
		return false;
	}

	@Override
	public boolean lock(String tenant, String password) throws UnivoteException {
		if (this.unlockedTentants.containsKey(tenant)) {
			TenantEntity tentantEntity = this.getTenant(tenant);
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
	public EncryptionHelper getEncrytpionHelper(String tenant) throws UnivoteException {
		if (this.unlockedTentants.containsKey(tenant)) {
			return this.unlockedTentants.get(tenant);
		}
		throw new UnivoteException("Tenant locked: " + tenant);
	}

	@Override
	public PublicKey getPublicKey(String tenant) throws UnivoteException {
		try {
			TenantEntity tenantEntity = this.getTenant(tenant);
			return KeyHelper.createDSAPublicKey(tenantEntity.getModulus(), tenantEntity.getOrderFactor(),
					tenantEntity.getGenerator(), tenantEntity.getPublicKey());
		} catch (InvalidKeySpecException ex) {
			throw new UnivoteException("Could not create publicKey: " + tenant, ex);
		}

	}

	@Override
	public PrivateKey getPrivateKey(String tenant) throws UnivoteException {
		if (this.unlockedTentants.containsKey(tenant)) {
			try {
				TenantEntity tenantEntity = this.getTenant(tenant);
				BigInteger privateKey = this.unlockedTentants.get(tenant)
						.decryptBigInteger(tenantEntity.getEncPrivateKey());
				return KeyHelper.createDSAPrivateKey(tenantEntity.getModulus(), tenantEntity.getOrderFactor(),
						tenantEntity.getGenerator(), privateKey);
			} catch (InvalidKeySpecException ex) {
				throw new UnivoteException("Could not create privateKey: " + tenant, ex);
			}
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

}
