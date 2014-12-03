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

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 * Loads configuration information from the property set defined in the user-defined global JNDI name space. The
 * properties
 * <em>must</em>
 * be defined externally, e.g., via the admin console of Glassfish.
 * <p>
 * A singleton.
 *
 * @author Philémon von Bergen &lt;philemon.vonbergen@bfh.ch&gt;
 */
public class ConfigurationHelperImpl implements ConfigurationHelper {

    private static final Logger logger = Logger.getLogger(ConfigurationHelperImpl.class.getName());

    private String keyType;
    private String keySize;
    private String primeP;
    private String primeQ;
    private String generator;
    private String applicationIdentifier;
    private String role;
    private int identityFunctionIndex;

    private boolean showKeyTypeField = true;
    private boolean showKeySizeField = true;
    private boolean showPrimePField = true;
    private boolean showPrimeQField = true;
    private boolean showGeneratorField = true;
    private boolean showApplicationIdentifierField = true;
    private boolean showRoleField = true;
    private boolean showIdentityFunctionIndexField = true;

    /**
     * Create a new ConfigurationHelperImpl Object
     *
     * @param propertySetName the name of the property set to laod
     */
    public ConfigurationHelperImpl(String propertySetName) {
	try {
	    this.init(propertySetName);
	} catch (ParametersNotFoundException ex) {
	    logger.log(Level.WARNING,
		    "Could not read JNDI parmeter set, exception: {0}",
		    new Object[]{ex});
	}

    }

    /**
     * Helper method reading the properties
     *
     * @param propertySetName the name of the property set to laod
     * @throws ParametersNotFoundException if a error occured reading the properties
     */
    private void init(String propertySetName) throws ParametersNotFoundException {
	Properties props;
	try {
	    javax.naming.InitialContext ic = new javax.naming.InitialContext();
	    props = (Properties) ic.lookup(propertySetName);

	} catch (NamingException ex) {
	    logger.log(Level.SEVERE, "JNDI lookup for '{0}' failed. Exception: {1}",
		    new Object[]{propertySetName, ex});
	    throw new ParametersNotFoundException("Fail to load configuration");
	}

	//Load UniCert application dependant properties
	this.keyType = retrieveStringProperty(props, "keyType");
	this.showKeyTypeField = this.keyType == null;
	this.keySize = retrieveStringProperty(props, "keySize");
	this.showKeySizeField = this.keySize == null;

	this.primeP = retrieveStringProperty(props, "primeP");
	this.showPrimePField = this.primeP == null;
	this.primeQ = retrieveStringProperty(props, "primeQ");
	this.showPrimeQField = this.primeQ == null;
	this.generator = retrieveStringProperty(props, "generator");
	this.showGeneratorField = this.generator == null;

	this.applicationIdentifier = retrieveStringProperty(props, "applicationIdentifier");
	this.showApplicationIdentifierField = this.applicationIdentifier == null;

	this.role = retrieveStringProperty(props, "role");
	this.showRoleField = this.role == null;
	this.identityFunctionIndex = retrieveIntegerProperty(props, "identityFunctionIndex");
	this.showIdentityFunctionIndexField = this.identityFunctionIndex == -1;
    }

    /**
     * Given the Properties object, retrieves the value for the specified property name. Logs success (INFO) or failure
     * (SEVERE).
     *
     * @param props the Properties object
     * @param propertyName the name of the property
     * @return the value of the property
     */
    private String retrieveStringProperty(Properties props, String propertyName) {
	String propertyValue = props.getProperty(propertyName);
	if (propertyValue == null) {
	    logger.log(Level.SEVERE,
		    "Could not retrieve global JNDI property: {0}.", new Object[]{propertyName});
	} else {
	    logger.log(Level.INFO,
		    "Retrieved global JNDI property: {0}.", new Object[]{propertyName});
	}
	return propertyValue;
    }

    /**
     * Given the Properties object, retrieves the value for the specified property name. Logs success (INFO) or failure
     * (SEVERE).
     *
     * @param props the Properties object
     * @param propertyName the name of the property
     * @return the value of the property
     */
    private Integer retrieveIntegerProperty(Properties props, String propertyName) {
	String propertyValue = props.getProperty(propertyName);
	if (propertyValue == null) {
	    logger.log(Level.SEVERE,
		    "Could not retrieve global JNDI property: {0}.", new Object[]{propertyName});
	    return -1;
	} else {
	    logger.log(Level.INFO,
		    "Retrieved global JNDI property: {0}.", new Object[]{propertyName});
	    return Integer.parseInt(propertyValue);
	}
    }

    /**
     * Given the Properties object, retrieves the value for the specified property name. Logs success (INFO) or failure
     * (SEVERE).
     *
     * @param props the Properties object
     * @param propertyName the name of the property
     * @return the value of the property
     */
    private Boolean retrieveBooleanProperty(Properties props, String propertyName) {
	String propertyValue = props.getProperty(propertyName);
	if (propertyValue == null) {
	    logger.log(Level.SEVERE,
		    "Could not retrieve global JNDI property: {0}.", new Object[]{propertyName});
	} else {
	    logger.log(Level.INFO,
		    "Retrieved global JNDI property: {0}.", new Object[]{propertyName});
	}
	return Boolean.valueOf(propertyValue);
    }

    @Override
    public String getKeyType() {
	return this.keyType;
    }

    @Override
    public String getKeySize() {
	return this.keySize;
    }

    @Override
    public String getPrimeP() {
	return this.primeP;
    }

    @Override
    public String getPrimeQ() {
	return this.primeQ;
    }

    @Override
    public String getGenerator() {
	return this.generator;
    }

    @Override
    public String getApplicationIdentifier() {
	return this.applicationIdentifier;
    }

    @Override
    public String getRole() {
	return this.role;
    }

    @Override
    public int getIdentityFunctionIndex() {
	return this.identityFunctionIndex;
    }

    public boolean isShowKeyTypeField() {
	return showKeyTypeField;
    }

    public boolean isShowKeySizeField() {
	return showKeySizeField;
    }

    public boolean isShowPrimePField() {
	return showPrimePField;
    }

    public boolean isShowPrimeQField() {
	return showPrimeQField;
    }

    public boolean isShowGeneratorField() {
	return showGeneratorField;
    }

    public boolean isShowApplicationIdentifierField() {
	return showApplicationIdentifierField;
    }

    public boolean isShowRoleField() {
	return showRoleField;
    }

    public boolean isShowIdentityFunctionIndexField() {
	return showIdentityFunctionIndexField;
    }

}
