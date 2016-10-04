/*
 * Copyright (c) 2012 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniVote.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote2.admin;

import ch.bfh.univote2.admin.message.CandidateElection;
import ch.bfh.univote2.admin.message.CandidateOption;
import ch.bfh.univote2.admin.message.ElectionIssue;
import ch.bfh.univote2.admin.message.ElectionOption;
import ch.bfh.univote2.admin.message.ListElection;
import ch.bfh.univote2.admin.message.ListOption;
import ch.bfh.univote2.admin.message.Vote;
import java.util.List;

/**
 * User interface class for the UniVote Administration.
 *
 * @author Stephan Fischli &lt;stephan.fischli@bfh.ch&gt;
 */
public class PrintUtility {

	private static final String INDENT = "    ";

	public static void printElectionIssue(ElectionIssue issue, List<ElectionOption> options) {
		if (issue instanceof ListElection) {
			printListElection((ListElection) issue, options);
		} else if (issue instanceof CandidateElection) {
			printCandidateElection((CandidateElection) issue, options);
		} else if (issue instanceof Vote) {
			printVote((Vote) issue, options);
		}
	}

	public static void printListElection(ListElection election, List<ElectionOption> options) {
		System.out.println("List Election: " + election.getTitle().getDefault());
		for (Integer optionId : election.getOptionIds()) {
			for (ElectionOption option : options) {
				if (option.getId().equals(optionId) && option instanceof ListOption) {
					printListOption((ListOption) option, options);
				}
			}
		}
	}

	public static void printCandidateElection(CandidateElection election, List<ElectionOption> options) {
		System.out.println("Candidate Election: " + election.getTitle().getDefault());
		for (Integer optionId : election.getOptionIds()) {
			for (ElectionOption option : options) {
				if (option.getId().equals(optionId)) {
					printCandidateOption((CandidateOption) option);
				}
			}
		}
	}

	public static void printVote(Vote vote, List<ElectionOption> options) {
		System.out.println("Vote: " + vote.getTitle().getDefault());
		System.out.println("Question: " + vote.getQuestion().getDefault());
	}

	public static void printListOption(ListOption listOption, List<ElectionOption> options) {
		System.out.print("List " + listOption.getNumber() + ": " + listOption.getListName().getDefault());
		if (listOption.getPartyName() != null) {
			System.out.print("(" + listOption.getPartyName().getDefault() + ")");
		}
		System.out.println();
		for (Integer optionId : listOption.getCandidateIds()) {
			for (ElectionOption option : options) {
				if (option.getId().equals(optionId)) {
					printCandidateOption((CandidateOption) option);
				}
			}
		}
	}

	public static void printCandidateOption(CandidateOption candidateOption) {
		System.out.print(INDENT + "Candidate");
		if (candidateOption.getNumber() != null) {
			System.out.print(" " + candidateOption.getNumber());
		}
		System.out.print(": " + candidateOption.getLastName() + " " + candidateOption.getFirstName());
		String description = candidateOption.getStatus()
				+ ", " + candidateOption.getSex()
				+ ", " + candidateOption.getYearOfBirth()
				+ ", " + (candidateOption.getStudyBranch() != null ? candidateOption.getStudyBranch().getDefault() : null)
				+ ", " + (candidateOption.getStudyDegree() != null ? candidateOption.getStudyDegree().getDefault() : null)
				+ ", " + candidateOption.getStudySemester();
		description = description.replaceAll("null", "-");
		System.out.println(" (" + description + ")");
	}
}
