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
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.Z;
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.persistence.TenantEntity;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class TenantManagerImplTest {

    public TenantManagerImplTest() {
    }

    /**
     * Test of checkHash method with a correct hash and correct composition of password and salt
     */
    @Test
    public void testCheckHashCorrect() {
        NonEETestableTenantManagerImpl tenantManagerImpl = new NonEETestableTenantManagerImpl();

        String password = "test";
        BigInteger salt = new BigInteger("1234567890");

        StringMonoid stringSet = StringMonoid.getInstance(Alphabet.PRINTABLE_ASCII);
        Z z = Z.getInstance();

        Element passwordElement = stringSet.getElement(password);
        Element saltElement = z.getElement(salt);

        Pair pair = Pair.getInstance(passwordElement, saltElement);

        FixedByteArrayHashingScheme scheme = FixedByteArrayHashingScheme.getInstance(pair.getSet());

        BigInteger hash = scheme.hash(pair).getBigInteger();
        assertTrue(tenantManagerImpl.checkHash(password, hash, salt));
    }

    /**
     * Test of checkHash method with a correct hash but incorrect composition of password and salt
     */
    @Test
    public void testCheckHashInorrect1() {
        NonEETestableTenantManagerImpl tenantManagerImpl = new NonEETestableTenantManagerImpl();

        String password = "test";
        BigInteger salt = new BigInteger("1234567890");

        StringMonoid stringSet = StringMonoid.getInstance(Alphabet.PRINTABLE_ASCII);
        Z z = Z.getInstance();

        Element passwordElement = stringSet.getElement(password + salt.toString());

        FixedByteArrayHashingScheme scheme = FixedByteArrayHashingScheme.getInstance(passwordElement.getSet());

        BigInteger hash = scheme.hash(passwordElement).getBigInteger();
        assertFalse(tenantManagerImpl.checkHash(password, hash, salt));
    }

    /**
     * Test of checkHash method with a incorrect hash but correct composition of password and salt
     */
    @Test
    public void testCheckHashIncorrect2() {
        NonEETestableTenantManagerImpl tenantManagerImpl = new NonEETestableTenantManagerImpl();

        String password = "test";
        BigInteger salt = new BigInteger("1234567890");

        StringMonoid stringSet = StringMonoid.getInstance(Alphabet.PRINTABLE_ASCII);
        Z z = Z.getInstance();

        Element passwordElement = stringSet.getElement(password);
        Element saltElement = z.getElement(salt);

        Pair pair = Pair.getInstance(passwordElement, saltElement);

        BigInteger hash = new BigInteger("127975928354798273495");
        assertFalse(tenantManagerImpl.checkHash(password, hash, salt));
    }

    /**
     * Test of getPublicKey with a correct tentantEntity
     */
    @Test
    public void testGetPublicKeyCorrect() {

        NonEETestableTenantManagerImpl tenantManagerImpl = new NonEETestableTenantManagerImpl();

        String tenant = "test";
        TenantEntity entity = new TenantEntity();
        entity.setName(tenant);
        entity.setEncPrivateKey("435395");
        entity.setGenerator(new BigInteger("4"));
        entity.setHashValue(new BigInteger("123"));
        entity.setModulus(new BigInteger("954263"));
        entity.setOrderFactor(new BigInteger("477131"));
        entity.setPublicKey(new BigInteger("286205"));
        entity.setSalt(new BigInteger("123"));

        tenantManagerImpl.setTenantEntity(entity);

        try {
            PublicKey pubKey = tenantManagerImpl.getPublicKey(tenant);
            DSAPublicKey dsaPubKey = (DSAPublicKey) pubKey;
            assertEquals(dsaPubKey.getY(), new BigInteger("286205"));
            assertEquals(dsaPubKey.getParams().getP(), new BigInteger("954263"));
            assertEquals(dsaPubKey.getParams().getQ(), new BigInteger("477131"));
            assertEquals(dsaPubKey.getParams().getG(), new BigInteger("4"));
        } catch (UnivoteException ex) {
            fail();
        }
    }

    /**
     * Test of getPrivateKey with a correct tentantEntity
     */
    @Test
    public void testGetPrivateKeyCorrect() throws NoSuchAlgorithmException, InvalidKeySpecException {

        NonEETestableTenantManagerImpl tenantManagerImpl = new NonEETestableTenantManagerImpl();

        String tenant = "test";
        TenantEntity entity = new TenantEntity();
        entity.setName(tenant);
        entity.setEncPrivateKey("00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|06|A4|C3");
        entity.setGenerator(new BigInteger("4"));
        entity.setHashValue(new BigInteger("123"));
        entity.setModulus(new BigInteger("954263"));
        entity.setOrderFactor(new BigInteger("477131"));
        entity.setPublicKey(new BigInteger("286205"));
        entity.setSalt(new BigInteger("123"));

        tenantManagerImpl.setTenantEntity(entity);
        DSAPrivateKey privateKey = KeyHelper.createDSAPrivateKey(entity.getModulus(), entity.getModulus(), entity.getGenerator(), new BigInteger("435395"));

        UnlockedTenant unlockedTenant = new UnlockedTenant(null, privateKey);

        tenantManagerImpl.addToUnlocked(tenant, unlockedTenant);

        try {
            PrivateKey privKey = tenantManagerImpl.getPrivateKey(tenant);
            DSAPrivateKey dsaPrivKey = (DSAPrivateKey) privKey;
            assertEquals(dsaPrivKey, privateKey);
        } catch (UnivoteException ex) {
            fail();
        }
    }

    /**
     * Test of unlock with a correct password
     */
    @Test
    public void testUnlockCorrect() {

        NonEETestableTenantManagerImpl tenantManagerImpl = new NonEETestableTenantManagerImpl();

        String tenant = "tenant";
        String password = "test";
        TenantEntity entity = new TenantEntity();
        entity.setName(tenant);
        entity.setEncPrivateKey("00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|06|A4|C3");
        entity.setGenerator(new BigInteger("4"));
        entity.setHashValue(new BigInteger("376114051623954570326890061327587598724322119758894535971769158571914929055"));
        entity.setModulus(new BigInteger("954263"));
        entity.setOrderFactor(new BigInteger("477131"));
        entity.setPublicKey(new BigInteger("286205"));
        entity.setSalt(new BigInteger("1234567890"));

        tenantManagerImpl.setTenantEntity(entity);

        assertTrue(tenantManagerImpl.unlock(tenant, password));
        assertTrue(tenantManagerImpl.getUnlockedTenants().contains(tenant));

    }

    /**
     * Test of lock with a correct password //
     */
    @Test
    public void testLockCorrect() {

        NonEETestableTenantManagerImpl tenantManagerImpl = new NonEETestableTenantManagerImpl();

        String tenant = "tenant";
        String password = "test";
        TenantEntity entity = new TenantEntity();
        entity.setName(tenant);
        entity.setEncPrivateKey("00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|00|06|A4|C3");
        entity.setGenerator(new BigInteger("4"));
        entity.setHashValue(new BigInteger("376114051623954570326890061327587598724322119758894535971769158571914929055"));
        entity.setModulus(new BigInteger("954263"));
        entity.setOrderFactor(new BigInteger("477131"));
        entity.setPublicKey(new BigInteger("286205"));
        entity.setSalt(new BigInteger("1234567890"));

        tenantManagerImpl.setTenantEntity(entity);

        tenantManagerImpl.unlock(tenant, password);
        assertTrue(tenantManagerImpl.getUnlockedTenants().contains(tenant));
        assertTrue(tenantManagerImpl.lock(tenant, password));
        assertFalse(tenantManagerImpl.getUnlockedTenants().contains(tenant));

    }
}
