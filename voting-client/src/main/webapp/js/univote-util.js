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
	var DEFAULT_LOCALE = 'de';

	window.getLocale = function () {
		if (locale == '') {
			var $body = $('body');
			if ($body.size() > 0) {
				locale = $body.data('locale');
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
		var text = '';
		var firstIndex = '';
		for (var index in localizedTexts) {
			firstIndex = firstIndex == '' ? index : firstIndex;
			if (index.toLowerCase() == getLocale().toLowerCase()) {
				text = localizedTexts[index];
				break;
			}
		}
		if (text == '') {
			if (localizedTexts[DEFAULT_LOCALE] != undefined) {
				text = localizedTexts[DEFAULT_LOCALE];
			} else if (firstIndex != '') {
				text = localizedTexts[firstIndex];
			}
		}

		return text;
	};

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
// R U L E S
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
		// Abstract: To be implemented by specialization
		verify: function () {
			return false;
		}
	};

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
		verify: function (vote) {
			var counter = 0;

			// Loop through the concerned options
			for (var i = 0; i < this.options.length; i++) {
				var value = vote.get(this.options[i]);
				if (value != undefined) {
					counter += value;
				}
				// If greater than upper bound => error
				if (counter > this.upperBound) {
					return false;
				}
			}

			// If smaller than lower bound => error
			if (counter < this.lowerBound) {
				return false;
			}

			// Finally return true!
			return true;
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
		verify: function (vote) {
			// Loop through the options
			for (var i = 0; i < this.options.length; i++) {
				var value = vote.get(this.options[i]);
				if (value != undefined) {
					// If greater than upper bound => error
					if (value > this.upperBound) {
						return false;
					}
					// If smaller than lower bound => error
					if (value < this.lowerBound) {
						return false;
					}
				} else if (this.lowerBound > 0) {
					return false;
				}
			}
			return true;
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