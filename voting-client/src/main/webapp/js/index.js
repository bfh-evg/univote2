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
 * This file contains the index-page specific JS.
 *
 */

/**
 * Holds the used DOM elements.
 */
var elements = {};

/**
 * Check cookie support and browser version on document ready.
 */
$(document).ready(function () {


	// 1. Check browser version
	// Mainly to detect IE < 9!
	var myNav = navigator.userAgent.toLowerCase();
	var ie = (myNav.indexOf('msie') != -1) ? parseInt(myNav.split('msie')[1]) : false;
	if ((ie !== false && ie < 9) || Object.create == undefined) {
		$('body').empty();
		$.blockUI({
			message: '<div id="browser-check">' +
					'<h2>' + msg.browsercheck1 + '</h2>' +
					'<p>' + msg.browsercheck4 + '</p><p>' + msg.browsercheck3 + '</p>' +
					'</div>',
			css: {width: '40%', left: '30%'},
			overlayCSS: {opacity: '0.2'}
		});
	}



	// 2. Check cookie support
	if (!uvUtilCookie.areSupported()) {
		$.blockUI({
			message: '<div id="browser-check">' +
					'<h2>' + msg.cookieSupport + '</h2>' +
					'<p>' + msg.cookieSupportText + '</p>' +
					'</div>',
			overlayCSS: {opacity: '0.3'}
		});
		return;
	}



	if ((ie === false && !(window.File && window.FileReader && window.FileList && window.Blob))) {
		$.blockUI({
			message: '<div id="browser-check">' +
					'<h2>' + msg.browsercheck1 + '</h2>' +
					'<p>' + msg.browsercheck2 + '</p><p>' + msg.browsercheck3 + '</p>' +
					'<p style="padding-top: 20px;"><button class="button" onclick="$.unblockUI();">' + msg.close + '</button></p></div>',
			overlayCSS: {opacity: '0.2'}
		});
	}

	// Get DOM elements
	elements.currentElectionsDiv = document.getElementById('currentElections');
	elements.pastElectionsDiv = document.getElementById('pastElections');
	elements.currentElectionsList = document.getElementById('currentElectionsList');
	elements.pastElectionsList = document.getElementById('pastElectionsList');
	elements.loadingElections = document.getElementById('loadingElections');
	elements.noElections = document.getElementById('noElections');
	retrieveElections();
});


/**
 * Retrieves election definition from Board (asynchronously).
 * If Board is on another domain, IE9 will not be able to retrieve the data
 * IE9 does not support cross domain ajax request.
 * JSONP would be a solution, but it only allows HTTP GET, while HTTP POST is
 * required for the REST Service of the Board.
 */
function retrieveElections() {

	var update = setInterval(function () {
		$(elements.loadingElections).append(".");
	}, 1000);

	//Query of election data over all sections
	var query = {constraint: [{
				type: "equal",
				identifier: {type: "propertyIdentifier", propertyType: "alpha", key: "group"},
				value: {type: "stringValue", value: "electionDefinition"}
			}]
	};

	var successCB = function (resultContainer) {
		clearInterval(update);
		$(elements.loadingElections).hide();

		//Signature of result is not verified since the data that is displayed here is not really sensitive
		//More over, the posts are signed by EA whose key should be retrieved from the Board
		var posts = resultContainer.result.post;
		for (var index in posts) {
			var message = JSON.parse(B64.decode(posts[index].message));
			var electionId = posts[index].alpha.attribute[0].value.value;
			var administration = (message.administration != undefined) ? __(message.administration) : '';
			var title = __(message.title);
			var now = new Date().getTime();
			var $item = null;
			if (new Date(message.votingPeriodEnd).getTime() >= now) {
				$item = $('<div class="row upcoming"><div class="small-12 columns"><span>' + administration + '</span><br />' + title + '</div></div>');
				if (new Date(message.votingPeriodBegin).getTime() <= now) {
					//Current election
					$item.append('<div class="medium-4 columns end"><a href="vote.xhtml?electionId=' + electionId + '" class="button radius gradient icon-right-dir">' + msg.goVote + '</a></div>');
				} else {
					//Future election
					$item.append('<div class="medium-4 columns end"><a href="#" class="button radius icon-right-dir disabled">' + msg.goVote + '</a></div>');
				}
				$(elements.currentElectionsList).append($item);
			} else {
				//Past election
				$(elements.pastElectionsList).append('<dd><i class="red icon-right-dir"></i>' + administration + (administration != '' ? ': ' : '') + title + '</dd>');
			}

		}

		// Show containers
		if ($(elements.currentElectionsList).find("div").length > 0) {
			$(elements.currentElectionsDiv).show();
			$(elements.currentElectionsList).show();
		}
		if ($(elements.pastElectionsList).find("dd").length > 0) {
			$(elements.pastElectionsDiv).show();
		}
	};

	var errorCB = function () {
		clearInterval(update);
		$(elements.loadingElections).html(msg.retreiveElectionDefinitionError);
	};

	UniBoard.GET(query, successCB, errorCB);
}

