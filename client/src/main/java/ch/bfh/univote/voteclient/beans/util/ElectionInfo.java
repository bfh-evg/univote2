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
//package ch.bfh.univote.voteclient.beans.util;
//
//import java.util.Calendar;
//import java.util.GregorianCalendar;
//import java.util.Map;
//
///**
// * Holds information of an election like id, title, voting period
// * and whether it is a current election or not.
// */
//public class ElectionInfo {
//
//	/** Election id. */
//	private String id;
//	/** Localized title. */
//	private Map<String,String> title;
//	/** Start of voting period. */
//	private Calendar votingPeriodStart;
//	/** End of voting period. */
//	private Calendar votingPeriodEnd;
//	/** Start of is current. */
//	private Calendar isCurrentStart;
//	/** End of is current. */
//	private Calendar isCurrentEnd;
//	/** Localized vote rules. */
//	private Map<String,String> voteRules;
//
//	/**
//	 * Constructs an ElectionInfo instance.
//	 *
//	 * @param id the election id
//	 * @param title the localized title
//	 * @param votingPeriodStart start of voting period
//	 * @param votingPeriodEnd end of voting period
//	 * @param isCurrentStart start of is current
//	 * @param isCurrentEnd end of is current
//	 * @param voteRules the localized vote rules
//	 */
//	public ElectionInfo(String id, Map<String,String> title, Calendar votingPeriodStart,
//			Calendar votingPeriodEnd, Calendar isCurrentStart, Calendar isCurrentEnd, Map<String,String> voteRules) {
//		this.id = id;
//		this.title = title;
//		this.votingPeriodStart = votingPeriodStart;
//		this.votingPeriodEnd = votingPeriodEnd;
//		this.isCurrentStart = isCurrentStart;
//		this.isCurrentEnd = isCurrentEnd;
//		this.voteRules = voteRules;
//	}
//
//	/**
//	 * Gets the id of the election.
//	 * @return election id.
//	 */
//	public String getId() {
//		return this.id;
//	}
//
//	/**
//	 * Gets the localized title of the election.
//	 * @param locale the locale
//	 * @return localized title
//	 */
//	public String getTitle(String locale) {
//		if ( this.title.containsKey(locale)) {
//			return this.title.get(locale);
//		} else {
//			return "";
//		}
//	}
//
//	/**
//	 * Gets the start of the voting period.
//	 * @return start of voting period
//	 */
//	public Calendar getVotingPeriodStart() {
//		return this.votingPeriodStart;
//	}
//
//	/**
//	 * Gets the end of the voting period.
//	 * @return end of voting period
//	 */
//	public Calendar getVotingPeriodEnd() {
//		return this.votingPeriodEnd;
//	}
//
//	/**
//	 * Gets the localized vote rules of the election.
//	 * @param locale the locale
//	 * @return localized vote rules
//	 */
//	public String getVoteRules(String locale) {
//		if ( this.voteRules.containsKey(locale)) {
//			return this.voteRules.get(locale);
//		} else {
//			return "";
//		}
//	}
//
//	/**
//	 * Gets the start of is current.
//	 * @return start of is current
//	 */
//	public Calendar getIsCurrentStart() {
//		return this.isCurrentStart;
//	}
//
//	/**
//	 * Gets the end of is current.
//	 * @return end of is current
//	 */
//	public Calendar getIsCurrentEnd() {
//		return this.isCurrentEnd;
//	}
//
//	/**
//	 * Returns <tt>true</tt> if the election is in voting period.
//	 * @return <tt>true</tt> if the election is in voting period
//	 */
//	public boolean isInVotingPeriod() {
//		Calendar now = new GregorianCalendar();
//		return now.after(this.getVotingPeriodStart()) && now.before(this.getVotingPeriodEnd());
//	}
//
//	/**
//	 * Returns <tt>true</tt> if it is a current election.
//	 * @return <tt>true</tt> if it is a current election.
//	 */
//	public boolean isCurrent() {
//		Calendar now = new GregorianCalendar();
//		return now.after(this.getIsCurrentStart()) && now.before(this.getIsCurrentEnd());
//	}
//
//	/**
//	 * Returns <tt>true</tt> if it is a passed election.
//	 * @return <tt>true</tt> if it is a passed election
//	 */
//	public boolean isPassed() {
//		Calendar now = new GregorianCalendar();
//		return now.after(this.getIsCurrentEnd());
//	}
//
//}
