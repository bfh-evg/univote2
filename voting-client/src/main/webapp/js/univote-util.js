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



//======================================================================
// H E L P E R   F U N C T I O N S
//
// Just a view global helper functions

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



//======================================================================
// U N I B O A R D
//
(function (window) {

	function UniBoard() {

		this.get = function (query, successCB, errorCB) {

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
		this.post = function () {
		};
	}

	window.uniBoard = new UniBoard();
})(window);



//======================================================================
// I S S U E  A N D  R U L E S
//

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



	function ElectionDetails(details) {
		details = details || {};

		// Make sure, options are ordered by id (important for ballot encoding)
		this.options = [];
		for (var i in details.options) {
			var option = details.options[i];
			this.options[option.id] = Option.createOption(option);
		}

		// Create a Rule object for each rule
		this.rules = [];
		for (var i in details.rules) {
			this.rules[i] = Rule.createRule(details.rules[i]);
		}

		// Issues
		this.issues = [];
		for (var i in details.issues) {
			var issue = details.issues[i];
			this.issues[i] = Issue.createIssue(issue, this);
		}

		// Ballot Encoding
		this.ballotEncoding = details.ballotEncoding || '';

		// Users choice
		this.vote = [];
	}

	ElectionDetails.prototype = {
		verifyVote: function () {
			var ret = Rule.SUCCESS;
			for (var i in this.rules) {
				ret = this.rules[i].verify(this.vote);
				if (ret != Rule.SUCCESS) {
					return ret;
				}
			}
			return ret;
		},
		verifyVoteUpperBoundOnly: function () {
			var ret = Rule.SUCCESS;
			for (var i in this.rules) {
				ret = this.rules[i].verifyUpperBoundOnly(this.vote);
				if (ret != Rule.SUCCESS) {
					return ret;
				}
			}
			return ret;
		},
		// The choice is not validated against the rules. False is returned
		// if the option is not in the set of options, otherwise true.
		addChoice: function (option, count, increment) {
			count = count || 1;
			increment = increment || false;
			if (this.options[option] != undefined) {
				if (this.vote[option] == undefined || !increment) {
					this.vote[option] = count;
				} else {
					this.vote[option] += count;
				}
				return true;
			} else {
				return false;
			}
		},
		removeChoice: function (option, count) {
			count = count || -1;
			if (count == -1) {
				this.vote[option] = 0;
			} else if (this.vote[option] != undefined) {
				this.vote[option] = Math.max(this.vote[option] - count, 0);
			}
		},
		removeAllChoices: function () {
			this.vote = [];
		},
		getVote: function () {
			return this.vote;
		},
		getOption: function (id) {
			return this.options[id];
		},
		// Returns -1 if option not in options or no upper bound exists
		getOptionUpperBound: function (option) {
			if (this.options[option] == undefined) {
				return -1;
			}
			var upperBound = -1;
			for (var i in this.rules) {
				var rule = this.rules[i];
				if (rule.containsOption(option)) {
					if (upperBound == -1 || upperBound > rule.upperBound) {
						upperBound = rule.upperBound;
					}
				}
			}
			return upperBound;
		}
	};
	window.ElectionDetails = ElectionDetails;


	/**
	 *
	 * @param {type} issue
	 * @param {ElectionDetails} electionDetails
	 * @returns {univote-util_L112.Issue}
	 */
	function Issue(issue, electionDetails) {
		issue = issue || {};
		this.electionDetails = electionDetails;
		this.id = issue.id || 0;
		this.type = issue.type || '';
		this.title = getLocalizedText(issue.title);
		this.description = getLocalizedText(issue.description);
		this.options = [];
		for (var i in issue.optionIds) {
			var id = issue.optionIds[i];
			this.options[id] = this.electionDetails.options[id];
		}
		this.rules = [];
		for (var i in issue.rulesIds) {
			var id = issue.rulesIds[i];
			this.rules[id] = this.electionDetails.rules[id];
		}
	}

	Issue.prototype = {
		getOption: function (id) {
			return this.electionDetails.getOption(id);
		}
	};

	Issue.createIssue = function (issue, electionDetails) {
		var ret;
		switch (issue.type) {
			case 'listElection':
				ret = new ListElectionIssue(issue, electionDetails);
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

		// Sorted array of lists (index => option)
		this.lists = [];
		// Array of candidates (id => option)
		this.candidates = [];

		var c = 0;
		for (var i in this.options) {
			var option = this.options[i];
			if (option.isList()) {
				this.lists[c++] = option;
			} else if (option.isCandidate()) {
				this.candidates[option.id] = option;
			}
		}

		// Sort lists according to 'number'
		for (var i = 0; i < this.lists.length; i++) {
			var option = this.lists[i];
			for (var j = i + 1; j < this.lists.length; j++) {
				if (this.lists[j].number < option.number) {
					this.lists[i] = this.lists[j];
					this.lists[j] = option;
					option = this.lists[j];
				}
			}
		}
	}

	ListElectionIssue.prototype = {
		// Returns a sorted array of lists (according to the number)
		getLists: function () {
			return this.lists;
		},
		// Returns a sorted array of candidates (according to the lists's candidateIds)
		getListCandidates: function (listId) {
			var list = this.options[listId] || {};
			var candidates = [];
			for (var i = 0; i < list.candidateIds.length; i++) {
				candidates[i] = this.options[list.candidateIds[i]];
			}
			return candidates;
		},
		listsAreChoosable: function () {
			for (var i in this.lists) {
				if (this.electionDetails.getOptionUpperBound(this.lists[i].id) > 0) {
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
	 * @param {type} option
	 * @returns {univote-util_L107.Option}
	 */
	function Option(option) {
		option = option || {};
		this.id = option.id || 0;
	}

	Option.prototype = {
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
			case 'voteOption':
				ret = new VoteOption(option);
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
		this.number = option.number || '';
		this.listName = __(option.listName);
		this.partyName = __(option.partyName);
		this.candidateIds = option.candidateIds || [];
	}

	ListOption.prototype = {
		getName: function () {
			return this.partyName + ' - ' + this.listName;
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

		this.number = option.number || '';
		this.lastName = option.lastName || '';
		this.firstName = option.firstName || '';
		this.sex = option.sex || '';
		this.yearOfBirth = option.yearOfBirth || 0;
		this.studyBranch = __(option.studyBranch);
		this.studyDegree = __(option.studyDegree);
		this.studySemester = option.studySemester || 0;
		this.status = option.status || '';
	}

	CandidateOption.prototype = {
		getName: function () {
			return this.lastName + ' ' + this.firstName;
		},
		isPrevious: function () {
			return this.status == 'OLD';
		}
	};
	extend(Option, CandidateOption);
	window.CandidateOption = CandidateOption;

	/**
	 *
	 * @param {type} option
	 * @returns {univote-util_L107.VoteOption}
	 */
	function VoteOption(option) {
		Option.call(this, option);
	}

	VoteOption.prototype = {
		//
	};
	extend(Option, VoteOption);
	window.VoteOption = VoteOption;



	/**
	 * Rule. Base object for voting rules.
	 *
	 * @param {type} rule
	 * @returns {univote-util_L112.Rule}
	 */
	function Rule(rule) {
		this.rule = rule || {};
		this.id = rule.id || 0;
		this.options = rule.optionIds || [];
		this.upperBound = rule.upperBound || 0;
		this.lowerBound = rule.lowerBound || 0;
	}

	Rule.prototype = {
		containsOption: function (option) {
			return this.options.indexOf(option) !== -1;
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
			case 'summation':
				ret = new SummationRule(rule);
				break;
			case 'cumulation':
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
			for (var i = 0; i < this.options.length; i++) {
				var value = vote[this.options[i]];
				if (value != undefined) {
					counter += value;
				}
				// If greater than upper bound => error
				if (counter > this.upperBound) {
					return Rule.ERROR_SUMMATION_UPPER;
				}
			}

			// If smaller than lower bound => error
			if (!onlyUpperBound && counter < this.lowerBound) {
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
			for (var i = 0; i < this.options.length; i++) {
				var value = vote[this.options[i]];
				if (value != undefined) {
					// If greater than upper bound => error
					if (value > this.upperBound) {
						return Rule.ERROR_CUMULATION_UPPER;
					}
					// If smaller than lower bound => error
					if (!onlyUpperBound && value < this.lowerBound) {
						return Rule.ERROR_CUMULATION_LOWER;
					}
				} else if (!onlyUpperBound && this.lowerBound > 0) {
					return Rule.ERROR_CUMULATION_LOWER;
				}
			}
			return Rule.SUCCESS;
		}
	};
	extend(Rule, CumulationRule);
	window.CumulationRule = CumulationRule;
})(window);
//======================================================================
// C O O K I E   S U P P O R T
//
// Based on http://www.quirksmode.org/js/cookies.html

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