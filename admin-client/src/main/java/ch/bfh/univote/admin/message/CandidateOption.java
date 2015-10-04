/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniBoard.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote.admin.message;

import ch.bfh.univote2.common.message.I18nText;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"id", "number", "lastName", "firstName", "sex", "yearOfBirth", "studyBranch", "studyDegree", "studySemester", "status"})
public class CandidateOption extends ElectionOption {

	public enum Sex {

		M, F
	};

	public enum Status {

		OLD, NEW
	};

	private Integer id;
	private String number;
	private String lastName;
	private String firstName;
	private Sex sex;
	private Integer yearOfBirth;
	private I18nText studyBranch;
	private I18nText studyDegree;
	private Integer studySemester;
	private Status status;

	public CandidateOption() {
	}

	public CandidateOption(Integer id, String number, String lastName, String firstName, Sex sex, Integer yearOfBirth, I18nText studyBranch, I18nText studyDegree, Integer studySemester, Status status) {
		this.id = id;
		this.number = number;
		this.lastName = lastName;
		this.firstName = firstName;
		this.sex = sex;
		this.yearOfBirth = yearOfBirth;
		this.studyBranch = studyBranch;
		this.studyDegree = studyDegree;
		this.studySemester = studySemester;
		this.status = status;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public Integer getYearOfBirth() {
		return yearOfBirth;
	}

	public void setYearOfBirth(Integer yearOfBirth) {
		this.yearOfBirth = yearOfBirth;
	}

	public I18nText getStudyBranch() {
		return studyBranch;
	}

	public void setStudyBranch(I18nText studyBranch) {
		this.studyBranch = studyBranch;
	}

	public I18nText getStudyDegree() {
		return studyDegree;
	}

	public void setStudyDegree(I18nText studyDegree) {
		this.studyDegree = studyDegree;
	}

	public Integer getStudySemester() {
		return studySemester;
	}

	public void setStudySemester(Integer studySemester) {
		this.studySemester = studySemester;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
