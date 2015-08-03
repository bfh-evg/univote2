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
 *
 */



//===========================================================================
// L O C A L I Z A T I O N
//===========================================================================

(function (window) {

	var locale = '';
	var DEFAULT_LOCALE = 'default';
	window.getLocale = function () {
		if (locale == '') {
			var $body = $('body');
			if ($body.size() > 0) {
				locale = $body.data('locale');
			}
			if (locale == undefined || locale == '') {
				locale = DEFAULT_LOCALE;
			}
		}
		return locale;
	};
	/**
	 * Helper function extracting the text in the desired language out
	 * of LocalizedText elements of lists and candidates.
	 *
	 * @param localizedTexts - Array of i18n texts.
	 */
	window.getLocalizedText = function (localizedTexts) {
		if (localizedTexts == undefined) {
			return '';
		}
		if (typeof localizedTexts === 'string') {
			return localizedTexts;
		}
		var text = '';
		for (var index in localizedTexts) {
			if (index.toLowerCase() == getLocale().toLowerCase()) {
				text = localizedTexts[index];
				break;
			}
		}
		if (text == '' && localizedTexts[DEFAULT_LOCALE] != undefined) {
			text = localizedTexts[DEFAULT_LOCALE];
		}

		return text;
	};
	window.__ = window.getLocalizedText;
})(window);




//===========================================================================
// U N I B O A R D
//===========================================================================

(function (window) {

	// If Board is on another domain, IE9 will not be able to send the GET/POST.
	// => IE9 does not support cross domain ajax request.
	var UniBoard = {};

	UniBoard.GET = function (query, successCB, errorCB) {

		//For IE
		$.support.cors = true;
		//Ajax request
		$.ajax({
			url: uvConfig.URL_UNIBOARD_GET,
			type: 'POST',
			contentType: "application/json",
			accept: "application/json",
			cache: false,
			dataType: 'json',
			data: JSON.stringify(query),
			timeout: 10000,
			crossDomain: true,
			success: successCB,
			error: errorCB
		});
	};

	UniBoard.POST = function (post, successCB, errorCB) {

		//For IE
		$.support.cors = true;
		//Ajax request
		$.ajax({
			url: uvConfig.URL_UNIBOARD_POST,
			type: 'POST',
			contentType: "application/json",
			accept: "application/json",
			cache: false,
			dataType: 'json',
			data: JSON.stringify(post),
			timeout: 10000,
			crossDomain: true,
			success: successCB,
			error: errorCB
		});
	};

	window.UniBoard = UniBoard;
})(window);


(function (window) {

	// @see http://stackoverflow.com/questions/4152931/javascript-inheritance-call-super-constructor-or-use-prototype-chain
	// @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/create
	function extend(base, sub) {
		var origProto = sub.prototype;
		sub.prototype = Object.create(base.prototype);
		for (var key in origProto) {
			sub.prototype[key] = origProto[key];
		}
		sub.prototype.constructor = sub;
		Object.defineProperty(sub.prototype, 'constructor', {
			enumerable: false,
			value: sub
		});
	}

	//===========================================================================
	// E L E C T I O N   D E T A I L S
	//===========================================================================

	/**
	 *
	 * @param {type} details
	 * @returns {univote-util_L123.ElectionDetails}
	 */
	function ElectionDetails(details) {
		details = details || {};

		// Make sure, options are ordered by id (important for ballot encoding)
		this._options = [];
		for (var i in details.options) {
			var option = details.options[i];
			this._options[option.id] = Option.createOption(option);
		}

		// Create a Rule object for each rule
		this._rules = [];
		for (var i in details.rules) {
			this._rules[i] = Rule.createRule(details.rules[i]);
		}

		// Issues
		this._issues = [];
		for (var i in details.issues) {
			var issue = details.issues[i];
			this._issues[i] = Issue.createIssue(issue, this);
		}

		// Ballot Encoding
		this._ballotEncoding = details.ballotEncoding || '';

		// Users choice
		this._vote = [];
	}

	ElectionDetails.prototype = {
		getOptions: function () {
			return this._options;
		},
		getRules: function () {
			return this._rules;
		},
		getIssues: function () {
			return this._issues;
		},
		getBallotEncoding: function () {
			return this._ballotEncoding;
		},
		getVote: function () {
			return this._vote;
		},
		verifyVote: function () {
			var ret = Rule.SUCCESS;
			for (var i in this._rules) {
				ret = this._rules[i].verify(this._vote);
				if (ret != Rule.SUCCESS) {
					return ret;
				}
			}
			return ret;
		},
		verifyVoteUpperBoundOnly: function () {
			var ret = Rule.SUCCESS;
			for (var i in this._rules) {
				ret = this._rules[i].verifyUpperBoundOnly(this._vote);
				if (ret != Rule.SUCCESS) {
					return ret;
				}
			}
			return ret;
		},
		// The choice is not validated against the rules. False is returned
		// if the option is not in the set of options, otherwise true.
		addChoice: function (optionId, count, increment) {
			count = count || 1;
			increment = increment || false;
			if (this._options[optionId] != undefined) {
				if (this._vote[optionId] == undefined || !increment) {
					this._vote[optionId] = count;
				} else {
					this._vote[optionId] += count;
				}
				return true;
			} else {
				return false;
			}
		},
		removeChoice: function (optionId, count) {
			count = count || -1;
			if (count == -1) {
				this._vote[optionId] = 0;
			} else if (this._vote[optionId] != undefined) {
				this._vote[optionId] = Math.max(this._vote[optionId] - count, 0);
			}
		},
		removeAllChoices: function () {
			this._vote = [];
		},
		getChoice: function (optionId) {
			return this._vote[optionId] == undefined ? 0 : this._vote[optionId];
		},
		getOption: function (id) {
			return this._options[id];
		},
		// Returns -1 if option not in options or no upper bound exists
		getOptionUpperBound: function (optionId) {
			if (this._options[optionId] == undefined) {
				return -1;
			}
			var upperBound = -1;
			for (var i in this._rules) {
				var rule = this._rules[i];
				if (rule.containsOption(optionId)) {
					if (upperBound == -1 || upperBound > rule.getUpperBound()) {
						upperBound = rule.getUpperBound();
					}
				}
			}
			return upperBound;
		}
	};
	window.ElectionDetails = ElectionDetails;


	//===========================================================================
	// I S S U E
	//===========================================================================

	/**
	 *
	 * @param {type} issue
	 * @param {ElectionDetails} electionDetails
	 * @returns {univote-util_L112.Issue}
	 */
	function Issue(issue, electionDetails) {
		issue = issue || {};
		this._electionDetails = electionDetails;
		this._id = issue.id || 0;
		this._type = issue.type || '';
		this._title = __(issue.title);
		this._optionIds = issue.optionIds;
		this._ruleIds = issue.rulesIds;
		/*this._options = [];
		 for (var i in this._optionIds) {
		 var id = this._optionIds[i];
		 this._options[id] = this._electionDetails.getOptions()[id];
		 }
		 this._rules = [];
		 for (var i in issue.rulesIds) {
		 var id = issue.rulesIds[i];
		 this._rules[id] = this._electionDetails.getRules()[id];
		 }*/
	}

	Issue.prototype = {
		getElectionDetails: function () {
			return this._electionDetails;
		},
		getId: function () {
			return this._id;
		},
		getTitle: function () {
			return this._title;
		},
		getOptionIds: function () {
			return this._optionIds;
		},
		getRuleIds: function () {
			return this._ruleIds;
		},
		/*getOptions: function () {
		 return this._options;
		 },
		 getRules: function () {
		 return this._rules;
		 },*/
		getOption: function (id) {
			return this._electionDetails.getOption(id);
		}
	};

	Issue.createIssue = function (issue, electionDetails) {
		var ret;
		switch (issue.type) {
			case 'listElection':
				ret = new ListElectionIssue(issue, electionDetails);
				break;
			case 'vote':
				ret = new VoteIssue(issue, electionDetails);
				break;
			default:
				ret = new Issue(issue, electionDetails);
		}
		return ret;
	};
	window.Issue = Issue;


	/**
	 *
	 * @param {type} issue
	 * @param {type} electionDetails
	 * @returns {univote-util_L100.ListElectionIssue}
	 */
	function ListElectionIssue(issue, electionDetails) {
		Issue.call(this, issue, electionDetails);

		this._description = __(issue.description);

		// Sorted array of lists (index => option)
		this._lists = [];
		// Array of candidates (id => option)
		this._candidates = [];

		var c = 0;
		for (var i = 0; i < this._optionIds.length; i++) {
			var option = this.getOption(this._optionIds[i]);
			if (option.isList()) {
				this._lists[c++] = option;
			} else if (option.isCandidate()) {
				this._candidates[option.getId()] = option;
			}
		}

		// Sort lists according to 'number'
		for (var i = 0; i < this._lists.length; i++) {
			var option = this._lists[i];
			for (var j = i + 1; j < this._lists.length; j++) {
				if (this._lists[j].getNumber() < option.getNumber()) {
					this._lists[i] = this._lists[j];
					this._lists[j] = option;
					option = this._lists[j];
				}
			}
		}
	}

	ListElectionIssue.prototype = {
		// Returns a sorted array of lists (according to the number)
		getLists: function () {
			return this._lists;
		},
		// Returns a sorted array of candidates (according to the lists's candidateIds)
		getListCandidates: function (listId) {
			var list = this.getOption(listId);
			var candidates = [];
			var candidateIds = list.getCandidateIds();
			for (var i = 0; i < candidateIds.length; i++) {
				candidates[i] = this.getOption(candidateIds[i]);
			}
			return candidates;
		},
		listsAreChoosable: function () {
			for (var i in this._lists) {
				if (this._electionDetails.getOptionUpperBound(this._lists[i].getId()) > 0) {
					return true;
				}
			}
			return false;
		}
	};
	extend(Issue, ListElectionIssue);
	window.ListElectionIssue = ListElectionIssue;



	/**
	 *
	 * @param {type} issue
	 * @param {type} electionDetails
	 * @returns {univote-util_L126.VoteIssue}
	 */
	function VoteIssue(issue, electionDetails) {
		Issue.call(this, issue, electionDetails);

		this._question = __(issue.question);
	}

	VoteIssue.prototype = {
		getQuestion: function () {
			return this._question;
		}
	};
	extend(Issue, VoteIssue);
	window.VoteIssue = VoteIssue;


	//===========================================================================
	// O P T I O N
	//===========================================================================

	/**
	 *
	 * @param {type} option
	 * @returns {univote-util_L107.Option}
	 */
	function Option(option) {
		option = option || {};
		this._id = option.id || 0;
	}

	Option.prototype = {
		getId: function () {
			return this._id;
		},
		isList: function () {
			return this instanceof ListOption;
		},
		isCandidate: function () {
			return this instanceof CandidateOption;
		},
		isVote: function () {
			return this instanceof VoteOption;
		}
	};

	Option.createOption = function (option) {
		var ret;
		switch (option.type) {
			case 'listOption':
				ret = new ListOption(option);
				break;
			case 'candidateOption':
				ret = new CandidateOption(option);
				break;
			case 'votingOption':
				ret = new VotingOption(option);
				break;
			default:
				ret = new Option(option);
		}
		return ret;
	};
	window.Option = Option;


	/**
	 *
	 * @param {type} option
	 * @returns {univote-util_L107.ListOption}
	 */
	function ListOption(option) {
		Option.call(this, option);
		this._number = option.number || '';
		this._listName = __(option.listName);
		this._partyName = __(option.partyName);
		this._candidateIds = option.candidateIds || [];
	}

	ListOption.prototype = {
		getNumber: function () {
			return this._number;
		},
		getListName: function () {
			return this._listName;
		},
		getPartyName: function () {
			return this._partyName;
		},
		getCandidateIds: function () {
			return this._candidateIds;
		},
		getName: function () {
			return this._partyName + ' - ' + this._listName;
		}
	};
	extend(Option, ListOption);
	window.ListOption = ListOption;

	/**
	 *
	 * @param {type} option
	 * @returns {univote-util_L107.CandidateOption}
	 */
	function CandidateOption(option) {
		Option.call(this, option);

		this._number = option.number || '';
		this._lastName = option.lastName || '';
		this._firstName = option.firstName || '';
		this._sex = option.sex || '';
		this._yearOfBirth = option.yearOfBirth || 0;
		this._studyBranch = __(option.studyBranch);
		this._studyDegree = __(option.studyDegree);
		this._studySemester = option.studySemester || 0;
		this._status = option.status || '';
	}

	CandidateOption.prototype = {
		getNumber: function () {
			return this._number;
		},
		getLastName: function () {
			return this._lastName;
		},
		getFirstName: function () {
			return this._firstName;
		},
		getSex: function () {
			return this._sex;
		},
		getYearOfBirth: function () {
			return this._yearOfBirth;
		},
		getStudyBranch: function () {
			return this._studyBranch;
		},
		getStudyDegree: function () {
			return this._studyDegree;
		},
		getStudySemester: function () {
			return this._studySemester;
		},
		getName: function () {
			return this._lastName + ' ' + this._firstName;
		},
		isPrevious: function () {
			return this._status == 'OLD';
		},
		getSexSymbol: function () {
			return this._sex == 'M' ? '&#9794' : (this._sex == 'F' ? '&#9792' : '');
		}
	};
	extend(Option, CandidateOption);
	window.CandidateOption = CandidateOption;

	/**
	 *
	 * @param {type} option
	 * @returns {univote-util_L107.VotingOption}
	 */
	function VotingOption(option) {
		Option.call(this, option);
		this._answer = __(option.answer);
	}

	VotingOption.prototype = {
		getAnswer: function () {
			return this._answer;
		}
	};
	extend(Option, VotingOption);
	window.VotingOption = VotingOption;


	//===========================================================================
	// R U L E
	//===========================================================================

	/**
	 * Rule. Base object for voting rules.
	 *
	 * @param {type} rule
	 * @returns {univote-util_L112.Rule}
	 */
	function Rule(rule) {
		rule = rule || {};
		this._id = rule.id || 0;
		this._optionsIds = rule.optionIds || [];
		this._upperBound = rule.upperBound || 0;
		this._lowerBound = rule.lowerBound || 0;
	}

	Rule.prototype = {
		getId: function () {
			return this._id;
		},
		getOptionIds: function () {
			return this._optionsIds;
		},
		getUpperBound: function () {
			return this._upperBound;
		},
		getLowerBound: function () {
			return this._lowerBound;
		},
		containsOption: function (optionId) {
			for (var i = 0; i < this._optionsIds.length; i++) {
				if (this._optionsIds[i] == optionId) {
					return true;
				}
			}
			return false;
		},
		// Abstract functions: To be implemented by specialization
		// Returns 0 on success or a value > 0 representing the error code
		verify: function (vote) {
			return false;
		},
		verifyUpperBoundOnly: function (vote) {
			return this.verify(vote, true);
		}
	};

	Rule.SUCCESS = 0;
	Rule.ERROR_SUMMATION_UPPER = 1;
	Rule.ERROR_SUMMATION_LOWER = 2;
	Rule.ERROR_CUMULATION_UPPER = 3;
	Rule.ERROR_CUMULATION_LOWER = 4;

	Rule.createRule = function (rule) {
		var ret;
		switch (rule.type) {
			case 'summationRule':
				ret = new SummationRule(rule);
				break;
			case 'cumulationRule':
				ret = new CumulationRule(rule);
				break;
			default:
				ret = new Rule(rule);
		}
		return ret;
	};

	window.Rule = Rule;
	/**
	 * SummationRule extends Rule
	 *
	 * @param {type} rule
	 * @returns {univote-util_L112.SummationRule}
	 */
	function SummationRule(rule) {
		Rule.call(this, rule);
	}

	SummationRule.prototype = {
		verify: function (vote, onlyUpperBound) {
			onlyUpperBound = onlyUpperBound || false;
			var counter = 0;
			// Loop through the concerned options
			for (var i = 0; i < this._optionsIds.length; i++) {
				var value = vote[this._optionsIds[i]];
				if (value != undefined) {
					counter += value;
				}
				// If greater than upper bound => error
				if (counter > this._upperBound) {
					return Rule.ERROR_SUMMATION_UPPER;
				}
			}

			// If smaller than lower bound => error
			if (!onlyUpperBound && counter < this._lowerBound) {
				return Rule.ERROR_SUMMATION_LOWER;
			}

			// Finally return true!
			return Rule.SUCCESS;
		}
	};
	extend(Rule, SummationRule);
	window.SummationRule = SummationRule;
	/**
	 * CumulationRule extends Rule
	 *
	 * @param {type} rule
	 * @returns {univote-util_L112.CumulationRule}
	 */
	function CumulationRule(rule) {
		Rule.call(this, rule);
	}

	CumulationRule.prototype = {
		verify: function (vote, onlyUpperBound) {
			onlyUpperBound = onlyUpperBound || false;
			// Loop through the options
			for (var i = 0; i < this._optionsIds.length; i++) {
				var value = vote[this._optionsIds[i]];
				if (value != undefined) {
					// If greater than upper bound => error
					if (value > this._upperBound) {
						return Rule.ERROR_CUMULATION_UPPER;
					}
					// If smaller than lower bound => error
					if (!onlyUpperBound && value < this._lowerBound) {
						return Rule.ERROR_CUMULATION_LOWER;
					}
				} else if (!onlyUpperBound && this._lowerBound > 0) {
					return Rule.ERROR_CUMULATION_LOWER;
				}
			}
			return Rule.SUCCESS;
		}
	};
	extend(Rule, CumulationRule);
	window.CumulationRule = CumulationRule;
})(window);


//===========================================================================
// C O O K I E   S U P P O R T
//
// Based on http://www.quirksmode.org/js/cookies.html
//===========================================================================

(function (window) {

	function UvUtilCookie() {

		// Creates a cookie.
		this.create = function (name, value, days) {
			var expires;
			if (days) {
				var date = new Date();
				date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
				expires = "; expires=" + date.toGMTString();
			}
			else
				expires = "";
			document.cookie = name + "=" + value + expires + "; path=/";
		};
		// Reads a cookie.
		this.read = function (name) {
			var nameEQ = name + "=";
			var ca = document.cookie.split(';');
			for (var i = 0; i < ca.length; i++) {
				var c = ca[i];
				while (c.charAt(0) == ' ')
					c = c.substring(1, c.length);
				if (c.indexOf(nameEQ) == 0)
					return c.substring(nameEQ.length, c.length);
			}
			return null;
		};
		// Erases a cookie.
		this.erase = function (name) {
			this.create(name, "", -1);
		};
		// Checks whether cookies supported/accepted by the client's browser.
		// A cookie is created and tried to be read afterwards. If this success
		// then the browser supports/accepts cookies and the just created cookie
		// is earased. Otherwsie the browser does not support/accept cookies.
		this.areSupported = function () {
			this.create("univote", "test", 1);
			if (this.read("univote") != null) {
				this.erase("univote");
				return true;
			}
			return false;
		};
	}
	window.uvUtilCookie = new UvUtilCookie();
})(window);