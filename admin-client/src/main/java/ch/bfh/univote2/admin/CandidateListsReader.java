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

import ch.bfh.univote2.admin.message.CandidateOption;
import ch.bfh.univote2.admin.message.CandidateOption.Sex;
import ch.bfh.univote2.admin.message.CandidateOption.Status;
import ch.bfh.univote2.admin.message.CumulationRule;
import ch.bfh.univote2.admin.message.ElectionDetails;
import ch.bfh.univote2.admin.message.ElectionIssue;
import ch.bfh.univote2.admin.message.ElectionOption;
import ch.bfh.univote2.admin.message.ListElection;
import ch.bfh.univote2.admin.message.ListOption;
import ch.bfh.univote2.admin.message.SummationRule;
import ch.bfh.univote2.common.message.I18nText;
import ch.bfh.univote2.common.message.JSONConverter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * User interface class for the UniVote Administration.
 *
 * @author Stephan Fischli &lt;stephan.fischli@bfh.ch&gt;
 */
public class CandidateListsReader {

	private static final String CONFIG_FILE = "config.properties";

	private static Properties props;
	private static ElectionDetails electionDetails;
	private static ElectionIssue electionIssue;
	private static SummationRule listSummationRule;
	private static CumulationRule listCumulationRule;
	private static SummationRule candidateSummationRule;
	private static CumulationRule candidateCumulationRule;

	public static void main(String[] args) throws Exception {
		readConfiguration();
		createElectionDetails();
		createElectionIssue();
		createElectionRules();
		readCandidateLists();
		writeElectionDetails();
	}

	private static void readConfiguration() throws Exception {
		props = new Properties();
		props.load(new FileReader(CONFIG_FILE));
	}

	private static void createElectionDetails() {
		electionDetails = new ElectionDetails();
		electionDetails.setBallotEncoding(props.getProperty("ballot.encoding"));
	}

	private static void createElectionIssue() {
		electionIssue = new ListElection();
		electionIssue.setId(electionDetails.getIssues().size() + 1);
		I18nText title = new I18nText();
		title.setDefault(props.getProperty("election.issue.title"));
		electionIssue.setTitle(title);
		electionDetails.getIssues().add(electionIssue);
	}

	private static void createElectionRules() {
		listSummationRule = new SummationRule(electionDetails.getRules().size() + 1,
				0, Integer.parseInt(props.getProperty("list.summation.bound")));
		electionDetails.getRules().add(listSummationRule);
		electionIssue.getRuleIds().add(listSummationRule.getId());

		listCumulationRule = new CumulationRule(electionDetails.getRules().size() + 1,
				0, Integer.parseInt(props.getProperty("list.cumulation.bound")));
		electionDetails.getRules().add(listCumulationRule);
		electionIssue.getRuleIds().add(listCumulationRule.getId());

		candidateSummationRule = new SummationRule(electionDetails.getRules().size() + 1,
				0, Integer.parseInt(props.getProperty("candidate.summation.bound")));
		electionDetails.getRules().add(candidateSummationRule);
		electionIssue.getRuleIds().add(candidateSummationRule.getId());

		candidateCumulationRule = new CumulationRule(electionDetails.getRules().size() + 1,
				0, Integer.parseInt(props.getProperty("candidate.cumulation.bound")));
		electionDetails.getRules().add(candidateCumulationRule);
		electionIssue.getRuleIds().add(candidateCumulationRule.getId());
	}

	private static void readCandidateLists() throws Exception {
		Workbook workbook = WorkbookFactory.create(new FileInputStream(props.getProperty("candidate.lists.path")));
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			readCandidateList(workbook.getSheetAt(i));
		}
	}

	private static void readCandidateList(Sheet sheet) {
		// parse header
		if (!hasValue(sheet.getRow(2), 2)) {
			return;
		}
		ListOption listOption = new ListOption();
		listOption.setId(electionDetails.getOptions().size() + 1);
		listOption.setNumber(getText(sheet.getRow(2), 2));
		listOption.setListName(getI18nText(sheet.getRow(3), 2));
		if (hasValue(sheet.getRow(4), 2)) {
			listOption.setPartyName(getI18nText(sheet.getRow(4), 2));
		}
		electionDetails.getOptions().add(listOption);
		electionIssue.getOptionIds().add(listOption.getId());
		listSummationRule.getOptionIds().add(listOption.getId());
		listCumulationRule.getOptionIds().add(listOption.getId());

		// parse candidates
		for (int i = 8; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			CandidateOption candidateOption = parseCandidateOption(row);
			if (candidateOption == null) {
				continue;
			}
			Integer candidateId = findCandidateOption(candidateOption.getLastName(), candidateOption.getFirstName());
			if (candidateId == null) {
				// new candidate
				candidateId = electionDetails.getOptions().size() + 1;
				candidateOption.setId(candidateId);
				electionDetails.getOptions().add(candidateOption);
				candidateSummationRule.getOptionIds().add(candidateId);
				candidateCumulationRule.getOptionIds().add(candidateId);
			}
			if (!electionIssue.getOptionIds().contains(candidateId)) {
				electionIssue.getOptionIds().add(candidateId);
			}
			listOption.getCandidateIds().add(candidateId);
		}
		PrintUtility.printListOption(listOption, electionDetails.getOptions());
	}

	private static Integer findCandidateOption(String lastName, String firstName) {
		for (ElectionOption option : electionDetails.getOptions()) {
			if (option instanceof CandidateOption) {
				CandidateOption candidateOption = (CandidateOption) option;
				if (candidateOption.getLastName().equals(lastName) && candidateOption.getFirstName().equals(firstName)) {
					return candidateOption.getId();
				}
			}
		}
		return null;
	}

	private static CandidateOption parseCandidateOption(Row row) {
		if (!hasValue(row, 1)) {
			return null;
		}
		CandidateOption candidateOption = new CandidateOption();
		candidateOption.setLastName(getText(row, 1));
		candidateOption.setFirstName(getText(row, 2));
		String status = getText(row, 3);
		if (status.toLowerCase().equals("neu")) {
			candidateOption.setStatus(Status.NEW);
		} else if (status.toLowerCase().equals("bisher")) {
			candidateOption.setStatus(Status.OLD);
		}
		if (hasValue(row, 4)) {
			String sex = getText(row, 4);
			if (sex.toLowerCase().equals("m")) {
				candidateOption.setSex(Sex.M);
			} else if (sex.toLowerCase().equals("w")) {
				candidateOption.setSex(Sex.F);
			}
		}
		if (hasValue(row, 5)) {
			candidateOption.setYearOfBirth((int) row.getCell(5).getNumericCellValue());
		}
		if (hasValue(row, 6)) {
			candidateOption.setStudyBranch(getI18nText(row, 6));
		}
		if (hasValue(row, 7)) {
			candidateOption.setStudyDegree(getI18nText(row, 7));
		}
		if (hasValue(row, 8)) {
			candidateOption.setStudySemester((int) row.getCell(8).getNumericCellValue());
		}
		return candidateOption;
	}

	private static void writeElectionDetails() throws Exception {
		Path path = Paths.get(props.getProperty("message.directory"), "electionDetails.json");
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(path.toFile()), "UTF-8")) {
			writer.write(JSONConverter.marshal(electionDetails));
		}
	}

	private static boolean hasValue(Row row, int col) {
		Cell cell = row.getCell(col);
		if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			return false;
		}
		if (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().trim().isEmpty()) {
			return false;
		}
		return true;
	}

	private static String getText(Row row, int col) {
		Cell cell = row.getCell(col);
		cell.setCellType(Cell.CELL_TYPE_STRING);
		return cell.getStringCellValue().trim();
	}

	private static I18nText getI18nText(Row row, int col) {
		I18nText i18nText = new I18nText();
		i18nText.setDefault(row.getCell(col).getStringCellValue().trim());
		return i18nText;
	}
}
