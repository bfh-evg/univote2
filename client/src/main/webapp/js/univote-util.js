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


/**
 * Class representing a PoliticalList.
 * This class differs from the class PoliticalList of Java in the following points:
 * - it has a list of candidates
 * - it contains the Java PoliticalList object
 * This class is used to make the rendering of the GUI easier.
 */
//function PoliticalList(_originalList) {
//	
//	// The original list (univote_bfh_ch_common_politicalList)
//	this.originalList = _originalList;
//	// The list of candidates (list of univote_bfh_ch_common_candidate)
//	this.candidates = new Array();
//	
//	if ( typeof (PoliticalList.initialized) == "undefined" ) {
//		
//		// Sets the original list.
//		PoliticalList.prototype.setOriginalList = function(_originalList) {
//			this.originalList = _originalList;
//		}
// 
//		// Gets the original list.
//		PoliticalList.prototype.getOriginalList = function() {
//			return this.originalList;
//		}
//
//		// Adds a candidate.
//		PoliticalList.prototype.addCandidate = function(candidate) {
//			this.candidates.push(candidate);
//		}
//
//		// Gets the list of candidates.
//		PoliticalList.prototype.getCandidates = function() {
//			return this.candidates;
//		}
//
//		PoliticalList.initialized = true;
//	}
//}




//======================================================================
// R U L E - C O N T R O L
//
// A few helper function to control the rules (sum-rules and forall-rules). All
// of them return true if at least one rule is broken, otherswise (if no rule is
// broken) false. -> TODO: This is quite confusing! Might be inverted once.

(function( window) {

	function UvUtilRuleControl() {
		
		/* 
		 * Checks a vote against the upper bounds of a list of sum-rules.
		 *  
		 * @param vote - A map representing the vote.
		 * @param rules - An array holding the sum-rules.
		 * @return true if at least one rule is broken, otherwise false.
		 */
		this.checkSumRules = function(vote, rules){

			//loop through the rules
			for(var j=0; j<rules.length; j++){

				var countSR = 0;
				var ids = rules[j].choiceId;

				//loop through the concerned ids
				for(var i=0; i<ids.length; i++){
					var value = vote.get(ids[i].toString());
					//add the number of occurences of all concerned ids
					if(value != undefined){
						countSR += value;
					}
					//if greater than upper bound => error
					if(countSR>rules[j].upperBound){
						return true;
					}
				}            
			}
			return false;
		}

		/* 
		 * Checks a vote against the upper bounds of a list of forall-rules.
		 *  
		 * @param vote - A map representing the vote.
		 * @param rules - An array holding the forall-rules.
		 * @return true if at least one rule is broken, otherwise false.
		 */	
		this.checkForAllRules = function(vote, rules){

			//loop through the rules
			for(var i=0; i<rules.length; i++){
				var ids = rules[i].choiceId;

				//loop through the concerned ids
				for(var j=0; j<ids.length; j++){
					var value = vote.get(ids[j].toString());
					//check the value of the id against upper bound
					if(value != undefined){
						//if greater than upper bound => error
						if(value > rules[i].upperBound){
							return true;
						}
					}
				}
			}
			return false;
		}

		/* 
		 * Checks a vote against the lower bounds of a list of forall-rules.
		 *  
		 * @param vote - A map representing the vote.
		 * @param rules - An array holding the forall-rules.
		 * @return true if at least one rule is broken, otherwise false.
		 */
		this.checkForAllRulesMin = function(vote, rules){

			//loop through the rules
			for(var i=0; i<rules.length; i++){
				var lowerBoundSR = rules[i].lowerBound;
				//check only for rules whose limit is more than 0
				if(lowerBoundSR>0){
					var ids = rules[i].choiceId;

					//loop through the concerned ids
					for(var j=0; j<ids.length; j++){
						var value = vote.get(ids[j].toString());
						//check the value of the id against upper bound
						if(value != undefined){
							//if smaller than lower bound => error
							if(value < rules[i].lowerBound){
								return true;
							}
						}
						//if not found in map => error
						else{
							return true;
						}
					}
				}
			}
			return false;
		}

		/* 
		 * Checks a vote against the lower bounds of a list of sum-rules.
		 *  
		 * @param vote - A map representing the vote.
		 * @param rules - An array holding the sum-rules.
		 * @return true if at least one rule is broken, otherwise false.
		 */
		this.checkSumRulesMin = function(vote, rules){

			//loop through the rules
			for(var i=0; i<rules.length; i++){
				var sumRule = rules[i];
				var lowerBoundSR = sumRule.lowerBound;
				//check only for rules whose limit is more than 0
				if(lowerBoundSR>0){
					var ids = sumRule.choiceId;
					var countSR = 0;

					//loop through the concerned ids
					for(var j=0; j<ids.length; j++){
						var value = vote.get(ids[j].toString());
						//add the number of occurences of all concerned ids
						if(value != undefined){
							countSR += value;
						}

						if(countSR>=rules[i].lowerBound){
							break;
						}
					}
					//if smaller than lower bound => error
					if(countSR<rules[i].lowerBound){
						return true;
					}
				}
			}
			return false;
		}
	}
	
	window.uvUtilRuleControl = new UvUtilRuleControl();

})(window);



//======================================================================
// C O O K I E   S U P P O R T
//
// Based on http://www.quirksmode.org/js/cookies.html

(function(window){
	
	function UvUtilCookie() {
		
		// Creates a cookie.
		this.create = function(name, value, days) {
			var expires;
			if (days) {
				var date = new Date();
				date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
				expires = "; expires=" + date.toGMTString();
			}
			else expires = "";
			document.cookie = name + "=" + value + expires + "; path=/";
		}
		
		// Reads a cookie.
		this.read = function(name) {
			var nameEQ = name + "=";
			var ca = document.cookie.split(';');
			for (var i = 0; i < ca.length; i++) {
				var c = ca[i];
				while (c.charAt(0) == ' ') c = c.substring(1, c.length);
				if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
			}
			return null;
		}
		
		// Erases a cookie.
		this.erase = function(name) {
			this.create(name, "", -1);
		}
		
		// Checks whether cookies supported/accepted by the client's browser.
		// A cookie is created and tried to be read afterwards. If this success
		// then the browser supports/accepts cookies and the just created cookie
		// is earased. Otherwsie the browser does not support/accept cookies.
		this.areSupported = function() {
			this.create("univote", "test", 1);
			if (this.read("univote") != null) {
				this.erase("univote");
				return true;
			}
			return false;
		}
	}
	window.uvUtilCookie = new UvUtilCookie();
	
})(window);