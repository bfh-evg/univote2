/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniCert.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote.voteclient.parameters;


/**
 * Interface to the configuration helper.
 *
 * @author Philémon von Bergen &lt;philemon.vonbergen@bfh.ch&gt;
 */
public interface ConfigurationHelper {

    /**
     * Get the type of key that must be generated
     * 
     * @return 'RSA' for RSA keys or 'DiscreteLog' for DSA/Schnorr keys
     */
    public String getKeyType();
    
    /**
     * Only for RSA keys: the size of the modulo
     * @return the size of the modulo
     */
    public String getKeySize();
    
    /**
     * Only for DiscreteLog: value of prime p
     * @return value of prime p
     */
    public String getPrimeP();
    
    /**
     * Only for DiscreteLog: value of prime q
     * @return value of prime q
     */
    public String getPrimeQ();
    
    /**
     * Only for DiscreteLog: value of the generator
     * @return value of the generator
     */
    public String getGenerator();
    
    
    /**
     * Get the identifer of the application the certificate will be issued for
     * @return identifer of the application
     */
    public String getApplicationIdentifier();
    
    /**
     * The role the certificate will be issued for
     * 
     * @return the role
     */
    public String getRole();
    
    /**
     * Get the index of the identity function that must be applied to the identity token
     * @return the index of the function
     */
    public int getIdentityFunctionIndex();
    
    
    
}
