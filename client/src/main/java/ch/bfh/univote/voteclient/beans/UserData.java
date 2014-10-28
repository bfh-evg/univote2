///*
// * Copyright (c) 2013 Berner Fachhochschule, Switzerland.
// * Bern University of Applied Sciences, Engineering and Information Technology,
// * Research Institute for Security in the Information Society, E-Voting Group,
// * Biel, Switzerland.
// *
// * Project UniVote.
// *
// * Distributable under GPL license.
// * See terms of license at gnu.org.
// */
//package ch.bfh.univote.voteclient.beans;
//
//import java.io.Serializable;
//import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.faces.bean.ManagedBean;
//import javax.faces.bean.SessionScoped;
//import javax.faces.context.ExternalContext;
//import javax.faces.context.FacesContext;
//
///**
// * Holds the user data relevant for UniVote retrieved from SWITCHaai
// * during a session. Depending on the context, some of the values
// * can be null. For example, when performing a lookup of the
// * certificates of the requestor, the field 'proof' is null. Thus,
// * clients cannot rely on the fact that all fields are always initialized.
// * <p>
// * When used in production, relevant fields are initialized upon calling
// * method readAaiValues().
// *
// * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
// */
//@ManagedBean(name="userData")
//@SessionScoped
//public final class UserData implements Serializable {
//	private static final long serialVersionUID = 1L;
//
//    // SWITCHaai has two different sets of parameter names for
//    // test environments and production envionments.
//    // TODO PRovide a means allowing to switch from either set at run-time
//
//    /** Swiss education UID. */
//    //public static final String E_UID = "Shib-SwissEP-UniqueID";
//    public static final String E_UID = "uniqueID";
//
//    /** Shibboleth UID. */
//    //public static final String P_UID = "Shib-Person-uid";
//    public static final String P_UID = "uid";
//
//    /** Persistent ID. */
//    //public static final String P_ID = "persistent-id";
//    public static final String P_ID = "persistent-id";
//
//    /** Card number. */
//    //public static final String P_CARD_NO = "Shib-SwissEP-CardUID";
//    public static final String P_CARD_NO = "cardUID";
//
//    /** Swiss education matriculation number. */
//    //public static final String P_MAT_NUMBER = "Shib-SwissEP-MatriculationNumber";
//    public static final String P_MAT_NUMBER = "matriculationNumber";
//
//    /** Internet organization name. */
//    //public static final String P_EMP_NUMBER = "Shib-InetOrgPerson-employeeNumber";
//    public static final String P_EMP_NUMBER = "employeeNumber";
//
//    /** Given name. */
//    //public static final String P_GIVENNAME = "Shib-InetOrgPerson-givenName";
//    public static final String P_GIVENNAME = "givenName";
//
//    /** Surname. */
//    //public static final String P_SURNAME = "Shib-Person-surname";
//    public static final String P_SURNAME = "surname";
//
//    /** E-mail. */
//    //public static final String P_MAIL = "Shib-InetOrgPerson-mail";
//    public static final String P_MAIL = "mail";
//
//    /** Study branch. */
//    //public static final String S_BRANCH = "Shib-SwissEP-StudyBranch1";
//    public static final String S_BRANCH = "studyBranch1";
//
//    /** Study level. */
//    //public static final String S_LEVEL = "Shib-SwissEP-StudyLevel";
//    public static final String S_LEVEL = "studyLevel";
//
//    /** Staff category. */
//    //public static final String S_CATECORY = "Shib-SwissEP-StaffCategory";
//    public static final String S_CATECORY = "staffCategory";
//
//    /** Organization name. */
//    //public static final String O_NAME = "Shib-SwissEP-HomeOrganization";
//    public static final String O_NAME = "homeOrganization";
//
//    /** Affiliation name. */
//    //public static final String O_AFFILIATION = "Shib-EP-Affiliation";
//    public static final String O_AFFILIATION = "affiliation";
//
//    /** Organization type. */
//    //public static final String O_TYPE = "Shib-SwissEP-HomeOrganizationType";
//    public static final String O_TYPE = "homeOrganizationType";
//
//    /** Organization DN. */
//    //public static final String O_DN = "Shib-EP-OrgDN";
//    public static final String O_DN = "org-dn";
//
//
//    /** The voter id. */
//    private String voterId;
//    /** The name of the organization/university. */
//    private String organization;
//    /** The study branch. */
//    private String studyBranch;
//    /** A unique identifier. */
//    private String uid;
//    /** An email address. */
//	private String email;
//
//    /** Indicator indicating whether the fields 'voterId' and 'email' are initialized or not. */
//    private boolean initialized = false;
//
//    /**
//     * The logger this servlet uses.
//     */
//    private static final Logger logger = Logger.getLogger(UserData.class.getName());
//
//    /**
//     * Constructs a default UserDate instance having dummy values. To be used
//     * for testing purposes only, i.e., if the initialization parameter 'dev-mode'
//     * of the faces servlet is set to 'true'.
//     */
//	public UserData(){
//		// Set dummy values in dev-mode
//		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
//		if ( Boolean.parseBoolean( externalContext.getInitParameter("dev-mode")) ) {
//			this.voterId = "11-222-333";
//			this.organization = "Uni Bern";
//			this.studyBranch = "Law";
//			this.uid = "123456789";
//			this.email = "test@bfh.ch";
//            this.initialized = true;
//            logger.log(Level.WARNING, "Initialization in 'dev-mode' done");
//        }
//	}
//
//    /**
//     * Constructs a UserData instance, given some initial parameters. Notice
//     * that, depending on the context, some of the parameters can be null.
//     * <p>
//     * Should be used for testing purposes only.
//     *
//     * @param voterId an id of a (potential) voter
//     * @param organization the voter's organization
//     * @param studyBranch the voter's study branch
//     * @param uid the voter's unique identifier
//     * @param email the voter's email address
//     */
//    public UserData(String voterId, String organization, String studyBranch, String uid,
//        String email) {
//        this.voterId = voterId;
//        this.organization = organization;
//        this.studyBranch = studyBranch;
//        this.uid = uid;
//		this.email = email;
//        this.initialized = true;
//        logger.log(Level.WARNING, "Initialization in 'test-mode' done");
//    }
//
//	/**
//	 * Reads the user values passed by SWITCHaai. This function is
//	 * triggered on page load of genkeys.xhtml. It might be called more than
//	 * once (page reload) so the fields are set only if the values are available
//	 * in the request.
//	 */
//	public void readAaiValues() {
//
//		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
//		Map<String, Object> requestMap = externalContext.getRequestMap();
//
//        // Log SWITCHaai values if "aai-debug-mode".
//        if (Boolean.parseBoolean(externalContext.getInitParameter("aai-debug-mode"))) {
//            logAai(requestMap);
//        }
//
//        // If not in 'dev-mode' then initialize this instance.
//        if (!Boolean.parseBoolean(externalContext.getInitParameter("dev-mode"))) {
//            // 'Smart' initialization.
//            initializeUserData(requestMap);
//            if (initialized) {
//                logger.log(Level.INFO, "SWTICHaai smart initialization done");
//            } else {
//                logger.log(Level.WARNING, "SWTICHaai smart initialization failed");
//            }
//        }
//	}
//
//    /**
//     * Returns the id of a (potential) voter.
//     * @return an id
//     */
//    public String getVoterId() {
//        return voterId;
//    }
//
//    /**
//     * Returns the name of the organization/university of a (potential) voter.
//     * @return a name
//     */
//    public String getOrganization() {
//        return organization;
//    }
//
//    /**
//     * Returns the study branch of a (potential) voter.
//     * @return a name of a study branch
//     */
//    public String getStudyBranch() {
//        return studyBranch;
//    }
//
//    /**
//     * Returns the unique identifier of a (potential) voter.
//     * @return a unique identifier
//     */
//    public String getUid() {
//        return uid;
//    }
//
//    /**
//     * Returns the email address of a (potential) voter.
//     * @return an email address
//     */
//	public String getEmail() {
//		return email;
//	}
//
//    /**
//     * Helper method for debugging purposes logging AAI provide values.
//     * @param requestMap a map of name/value pairs provided by the HTTP request object
//     */
//    private void logAai(Map<String, Object> requestMap) {
//        String[] names = {
//            E_UID,
//            P_ID,
//            P_SURNAME,
//            P_GIVENNAME,
//            P_MAIL,
//            O_NAME,
//            O_TYPE,
//            O_AFFILIATION,
//            P_UID,
//            P_MAT_NUMBER,
//            P_EMP_NUMBER,
//            P_CARD_NO,
//            S_BRANCH,
//            S_LEVEL,
//            S_CATECORY,
//            O_DN
//        };
//        for (int i = 0; i < names.length; i++) {
//            Object value = requestMap.get(names[i]);
//            logger.log(Level.INFO, "Value for {0}: {1}", new Object[]{names[i], value});
//        }
//    }
//
//    /**
//     * Tries to initialized this instance from data obtained from
//     * SWITCHaai. Sets 'initialized' to true if it can at least
//     * initialized the fields 'voterId' and 'email'.
//     * @param requestMap the HTTP request object parameter map
//     */
//    private void initializeUserData(Map<String, Object> requestMap) {
//        // Let's determine the voter id. Notic that
//        // - matriculation number (original idea) is not provided
//        // - e-mail address is cannot be used for Univ. of Zurich
//        // ==> speical case for Univ. of Zurich
//        // Home organization.
//        String value = (String) requestMap.get(O_NAME);
//        if (value != null) {
//            logger.log(Level.INFO, "Retrieved for home organization: name={0}, value={1}",
//                new Object[]{O_NAME, value});
//        } else {
//            value = "Unknown";
//        }
//        organization = value;
//
//        // Voter id. It is essential that it can be initialized.
//        if (organization != null && organization.equals("uzh.ch")) {
//            // Univ. Zurich case. We do not use the e-mail address. We
//            // use a unique identifier only.
//            value = (String) requestMap.get(E_UID);
//            if (value != null) {
//                logger.log(Level.INFO, "Retrieved for voter id: name={0}, value={1}",
//                    new Object[]{E_UID, value});
//            } else {
//                value = (String) requestMap.get(P_ID);
//                if (value != null) {
//                    logger.log(Level.INFO, "Retrieved for voter id: name={0}, value={1}",
//                        new Object[]{P_ID, value});
//                } else {
//                    // Cannot initialize voter id -- giving up.
//                    logger.log(Level.SEVERE, "Cannot initialize voter id -- giving up.");
//                    return;
//                }
//            }
//        } else {
//            // NOTE: Since the Universitiy of Bern does not provide the
//            // matriculation number we use the email address as the
//            // first trial for determining the voter id.
//            value = (String) requestMap.get(P_MAIL);
//            if (value != null) {
//                logger.log(Level.INFO, "Retrieved for voter id: name={0}, value={1}",
//                    new Object[]{P_MAIL, value});
//            } else {
//                value = (String) requestMap.get(P_MAT_NUMBER);
//                if (value != null) {
//                    logger.log(Level.INFO, "Retrieved for voter id: name={0}, value={1}",
//                        new Object[]{P_MAT_NUMBER, value});
//                } else {
//                    value = (String) requestMap.get(E_UID);
//                    if (value != null) {
//                        logger.log(Level.INFO, "Retrieved for voter id: name={0}, value={1}",
//                            new Object[]{E_UID, value});
//                    } else {
//                        value = (String) requestMap.get(P_ID);
//                        if (value != null) {
//                            logger.log(Level.INFO, "Retrieved for voter id: name={0}, value={1}",
//                                new Object[]{P_ID, value});
//                        } else {
//                            // Cannot initialize voter id -- giving up.
//                            logger.log(Level.SEVERE, "Cannot initialize voter id -- giving up.");
//                            return;
//                        }
//                    }
//                }
//            }
//        }
//        voterId = value;
//
//        // Study branch. Not really essential.
//        value = (String) requestMap.get(S_BRANCH);
//        if (value != null) {
//            logger.log(Level.INFO, "Retrieved for study branch: name={0}, value={1}",
//                new Object[]{S_BRANCH, value});
//        } else {
//            value = "Unknown";
//        }
//        studyBranch = value;
//
//        // UID. Essential in the back-end.
//        value = (String) requestMap.get(E_UID);
//        if (value != null) {
//            logger.log(Level.INFO, "Retrieved for uid: name={0}, value={1}",
//                new Object[]{E_UID, value});
//        } else {
//            value = (String) requestMap.get(P_UID);
//            if (value != null) {
//                logger.log(Level.INFO, "Retrieved for uid: name={0}, value={1}",
//                    new Object[]{P_UID, value});
//            } else {
//                value = (String) requestMap.get(P_ID);
//                if (value != null) {
//                    logger.log(Level.INFO, "Retrieved for uid: name={0}, value={1}",
//                        new Object[]{P_ID, value});
//                } else {
//                    // Cannot initialize uid -- giving up.
//                    logger.log(Level.SEVERE, "Cannot initialize uid -- giving up.");
//                    return;
//                }
//            }
//        }
//        uid = value;
//
//        // E-mail address. It is essential that it can be initialized.
//        value = (String) requestMap.get(P_MAIL);
//        if (value != null) {
//            logger.log(Level.INFO, "Retrieved for e-mail address: name={0}, value={1}",
//                new Object[]{P_MAIL, value});
//        } else {
//            // Cannot initialize e-mail address -- giving up.
//            logger.log(Level.SEVERE, "Cannot initialize e-mail address -- giving up.");
//            return;
//        }
//        email = value;
//
//        // If we reach this point then this instance is sufficiently initialized.
//        initialized = true;
//    }
//}
