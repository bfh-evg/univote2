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
//import ch.bfh.univote.voteclient.beans.util.ElectionInfo;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.GregorianCalendar;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.faces.bean.ManagedBean;
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//
///**
// * Bean to provide details about elections.
// */
//@ManagedBean(name = "electionDetails")
//public class ElectionDetails {
//
//	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//	private static final Logger logger = Logger.getLogger(ElectionDetails.class.getName());
//	/**
//	 * The list holding the election information.
//	 */
//	private ArrayList<ElectionInfo> electionInfos;
//
//	/**
//	 * Reads the election information from property files and constructs an
//	 * ElectionDetails instance.
//	 */
//	public ElectionDetails() {
//		electionInfos = new ArrayList<>();
//		File dir = getElectionInfoDir();
//		if (dir == null) {
//			return;
//		}
//		for (File file : dir.listFiles()) {
//			if (!file.getName().endsWith(".properties")) {
//				continue;
//			}
//			try {
//				Properties props = new Properties();
//				props.load(new InputStreamReader(new FileInputStream(file), "utf-8"));
//				ElectionInfo info = new ElectionInfo(
//						props.getProperty("id"),
//						getTextMap(props, "title"),
//						getCalendar(props, "votingPhaseStart"),
//						getCalendar(props, "votingPhaseEnd"),
//						getCalendar(props, "currentPhaseStart"),
//						getCalendar(props, "currentPhaseEnd"),
//						getTextMap(props, "voteRules"));
//				electionInfos.add(info);
//			} catch (IOException ex) {
//				logger.log(Level.SEVERE, "Error reading property file '{0}'. Exception: {1}", new Object[]{file, ex});
//			}
//		}
//	}
//
//	/**
//	 * Gets a list of current elections (sorted according to the start of 
//	 * the voting period).
//	 *
//	 * @return list of current elections.
//	 */
//	public List<ElectionInfo> getCurrentElections() {
//		ArrayList<ElectionInfo> currentElections = new ArrayList<>();
//		for (ElectionInfo ei : electionInfos) {
//			if (ei.isCurrent()) {
//				currentElections.add(ei);
//			}
//		}
//		Collections.sort(currentElections, new Comparator<ElectionInfo>(){
//			@Override
//			public int compare(ElectionInfo e1, ElectionInfo e2) {
//				return e1.getVotingPeriodStart().compareTo(e2.getVotingPeriodStart());
//			}
//		});
//		return currentElections;
//	}
//
//	/**
//	 * Gets a list of passed elections (sorted according to the start of 
//	 * the voting period in descending order).
//	 *
//	 * @return list of passed elections.
//	 */
//	public List<ElectionInfo> getPassedElections() {
//		ArrayList<ElectionInfo> passedElections = new ArrayList<>();
//		for (ElectionInfo ei : electionInfos) {
//			if (ei.isPassed()) {
//				passedElections.add(ei);
//			}
//		}
//		Collections.sort(passedElections, new Comparator<ElectionInfo>(){
//			@Override
//			public int compare(ElectionInfo e1, ElectionInfo e2) {
//				return e2.getVotingPeriodStart().compareTo(e1.getVotingPeriodStart());
//			}
//		});
//		return passedElections;
//	}
//
//	/**
//	 * Gets the election information of a specific election.
//	 *
//	 * @param electionId the election id.
//	 * @return ElectionInfo or null if no election for the passed id exists.
//	 */
//	public ElectionInfo getElectionInfo(String electionId) {
//
//		for (ElectionInfo ei : electionInfos) {
//			if (ei.getId().endsWith(electionId)) {
//				return ei;
//			}
//		}
//		return null;
//	}
//
//	/**
//	 * Gets the vote rules of a specific election.
//	 *
//	 * @param electionId the election id.
//	 * @param locale the locale
//	 * @return localized vote rules or an empty string if no such vote rules
//	 * exist.
//	 */
//	public String getVoteRules(String electionId, String locale) {
//		ElectionInfo electionInfo = null;
//		for (ElectionInfo ei : electionInfos) {
//			if (ei.getId().endsWith(electionId)) {
//				electionInfo = ei;
//			}
//		}
//		return electionInfo != null ? electionInfo.getVoteRules(locale) : "";
//	}
//
//	/**
//	 * Gets the the directory containing the election property files.
//	 */
//	private File getElectionInfoDir() {
//		Properties props;
//		try {
//			InitialContext context = new javax.naming.InitialContext();
//			props = (Properties) context.lookup("votingClientProps");
//			String path = props.getProperty("electionInfoDir");
//			if (path == null) {
//				logger.log(Level.SEVERE, "Property 'electionInfoDir' not found.");
//				return null;
//			}
//			File dir = new File(path);
//			if (!dir.exists() || !dir.isDirectory()) {
//				logger.log(Level.SEVERE, "File '{0}' does not exist or is not a directory.", path);
//				return null;
//			}
//			return dir;
//		} catch (NamingException ex) {
//			logger.log(Level.SEVERE, "JNDI lookup for 'votingClientProps' failed. Exception: {0}", ex);
//			return null;
//		}
//	}
//
//	/**
//	 * Creates a map from a localized text property.
//	 */
//	private Map getTextMap(Properties props, String name) {
//		Map<String, String> text = new HashMap<>();
//		text.put("de", props.getProperty(name + ".de"));
//		text.put("fr", props.getProperty(name + ".fr"));
//		text.put("en", props.getProperty(name + ".en"));
//		return text;
//	}
//
//	/**
//	 * Creates a calendar from a date property.
//	 */
//	private Calendar getCalendar(Properties props, String name) {
//		String date = props.getProperty(name);
//		if (date == null) {
//			logger.log(Level.SEVERE, "Date property '{0}' not found.", name);
//			return null;
//		}
//		try {
//			Calendar calendar = new GregorianCalendar();
//			calendar.setTime(dateFormat.parse(date));
//			return calendar;
//		} catch (ParseException ex) {
//			logger.log(Level.SEVERE, "Date '{0}' not parsable. Exception: {1}", new Object[]{date, ex});
//			return null;
//		}
//	}
//}
