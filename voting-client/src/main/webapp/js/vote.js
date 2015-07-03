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
 * This file contains the vote page specific JS.
 *
 */


// Configuration.
//-------------------------------------

var CLASS_NAME_VOTE = "Vote";
var CLASS_NAME_CANDIDATE_ELECTION = "CandidateElection";
var CLASS_NAME_PARTY_ELECTION = "PartyElection";
var CLASS_NAME_SUMMATION_RULE = "SummationRule";
var CLASS_NAME_FORALL_RULE = "ForAllRule";

/**
 * Holds the used DOM elements.
 */
var elements = {};

/**
 * Holds the used dialogs.
 */
var dialogs = {};

/**
 * Holds voter's secret key (bigInt).
 */
var secretKey;

/**
 * Holds the election data received by the voting service (univote_bfh_ch_common_electionData).
 */
var electionData;

/**
 * Holds the election id.
 */
var electionId;

/**
 * Holds the election generator (bigInt).
 */
var electionGenerator;

/**
 * Holds the encryption key (bigInt).
 */
var encryptionKey;

/**
 * Holds all choice ids (used for vote encoding).
 */
var choiceIds;

/**
 * Holds all summation rules (array of objects).
 */
var sumRules;

/**
 * Holds all for all rules (array of objects).
 */
var forAllRules;

/**
 * Holds all choices: key is choiceId.
 */
var choicesMap;

/**
 * Flag that is set to true if not only candidates but also list can be selected.
 */
var listsAreSelectable = false;

/**
 * Holds user's language/locale (used for lacalized election data).
 */
var lang;

/**
 * Flag that is set to true if no political lists are available (only candidates).
 */
var noList = false;


/**
 * Initialisation on document ready.
 */
$(document).ready(function () {

	// Get DOM elements
	elements.step1 = document.getElementById('step_1');
	elements.step2 = document.getElementById('step_2');
	elements.step3 = document.getElementById('step_3');

	elements.step1content = document.getElementById('step_1_content');
	elements.step2content = document.getElementById('step_2_content');
	elements.step3content = document.getElementById('step_3_content');

	elements.skUpload = document.getElementById('sk-upload');
	elements.password = document.getElementById('password');
	elements.uploadKeyError = document.getElementById('upload_key_error');

	elements.electionTitle = document.getElementById('election-title');
	elements.voteText = document.getElementById('vote-text');
	elements.lists = document.getElementById('lists');
	elements.listsContent = document.getElementById('lists_content');

	elements.result = document.getElementById('result-scroll');

	elements.sendVoteSuccess = document.getElementById('send-vote-success');
	elements.sendVoteError = document.getElementById('send-vote-error');
	elements.sendVoteErrorMessage = document.getElementById('send-vote-error-message');
	elements.qrcodeHolder = document.getElementById('qrcode-holder');

	// Get dialogs
	dialogs.video = document.getElementById('dialog-video');
	dialogs.confirm = document.getElementById('dialog-confirm');
	dialogs.$confirm = $(dialogs.confirm);

	// Get election id and user's language/locale
	electionId = $('#election-title').data('electionId');
	console.log(electionId);

	// Block UI while loading election data from board
	$.blockUI({
		message: '<p id="blockui-processing">' + msg.loading + '</p>',
		fadeIn: 10
	});

	// Initiate retrieving election data from voting service
	//setTimeout(retrieveElectionData, 50);
	setTimeout(retrieveElectionDefinition, 50);

});

/**
 * Uploads (to the browser not to the server) the secret key. The secret key can
 * be uploaded either by file upload or manually by copy/paste. That depends on
 * whether the browser supports the file api and whether the flag UPLOAD_SK_MANUALLY_ALWAYS
 * is set.
 */
function uploadSecretKey() {

	// Hide previous error message
	$(elements.uploadKeyError).html('').css('opacity', '0.01');

	// Get password
	var pw = elements.password.value;

	// Get secret key
	var skUpload = elements.skUpload.value;

	// Check for secret key and password
	if (skUpload == '' || pw == '') {
		showUploadKeyError('filemanuallyOrPwMissing');
	} else {
		// Decrypt secret key
		decryptSecretKey(skUpload, pw);
	}
}

/**
 * Decrypts secret key. On success, step 2 is displayed directly, otherwise on
 * error (wrong password, broken secret key), an error message is displayed to
 * the user.
 *
 * @param key - Secret key as string.
 * @param pw - Password as string.
 */
function decryptSecretKey(key, pw) {

	// Decrypt secret key using crypto library
	var sk = "";

	sk = uvCrypto.decryptSecretKey(key, pw, function (errorMsg) {
		showUploadKeyError(errorMsg);
	});

	// On success go to step 2
	if (sk !== false) {
		secretKey = sk;
		// gotoStep2();
		checkAuthorization();
	}

}

function checkAuthorization() {
	var update = setInterval(function () {
		$('#blockui-processing').append('.');
	}, 500);

	$.blockUI({
		message: '<p id="blockui-processing">' + msg.loading + '</p>',
		fadeIn: 10
	});

	var errorCB = function (errorMsg) {
		clearInterval(update);
		$.unblockUI();
		$.blockUI({message: '<p>' + errorMsg + '</p>'});
		// Redirect to step 2 after 5s
		setTimeout(function () {
			$.unblockUI();
			gotoStep2();
		}, 5000);
	}


	var successCBg = function (resultContainer) {
		// Save election data
		var posts = resultContainer.result.post;
		var post;
		if (posts == undefined || posts.length <= 0) {
			//No authorization was found for this key
			errorCB(msg.noAuthorizationFound);
			return;
		} else {
			post = posts[0];
		}

		try {
			uvCrypto.verifyResultSignature(resultContainer, uvConfig.EC_SETTING, true);
		} catch (message) {
			errorCB(msg.signatureError);
			return;
		}

		//assumes that only one post is retuned
		var message = JSON.parse(B64.decode(post.message));
		electionGenerator = leemon.str2bigInt(message.crypto.g, 10, 1);


		clearInterval(update);
		$.unblockUI();
		gotoStep2();
	}

	var successCBghat = function (resultContainer) {
		// Save election data
		var posts = resultContainer.result.post;
		var post;
		if (posts == undefined || posts.length <= 0) {
			//No authorization was found for this key, so we recompute the public key using normal g (instead of ghat)
			queryAuthorization(uvCrypto.signatureSetting.g, successCBg, errorCB);
			return;
		} else {
			post = posts[0];
		}

		try {
			uvCrypto.verifyResultSignature(resultContainer, uvConfig.EC_SETTING, true);
		} catch (message) {
			errorCB(msg.signatureError);
			return;
		}


		//assumes that only one post is retuned
		var message = JSON.parse(B64.decode(post.message));
		electionGenerator = leemon.str2bigInt(message.crypto.g, 10, 1);

		clearInterval(update);
		$.unblockUI();
		gotoStep2();
	};

	queryAuthorization(uvCrypto.signatureSetting.gHat, successCBghat, errorCB);
}

function queryAuthorization(generator, succesCB, errorCB) {

	var publicKey = uvCrypto.computeElectionVerificationKey(generator, secretKey).vkString;

	var queryJson = {
		constraint: [{
				type: "equal",
				identifier: {
					type: "alphaIdentifier",
					part: ["section"]
				},
				value: {
					type: "stringValue",
					value: electionId}
			}, {
				type: "equal",
				identifier: {
					type: "alphaIdentifier",
					part: ["group"]
				},
				value: {
					type: "stringValue",
					value: "accessRight"
				}
			}, {
				type: "equal",
				identifier: {
					type: "messageIdentifier",
					part: ["group"]
				},
				value: {
					type: "stringValue",
					value: "ballot"
				}
			}, {
				type: "equal",
				identifier: {
					type: "messageIdentifier",
					part: ["crypto", "publickey"]
				},
				value: {
					type: "stringValue",
					value: publicKey
				}
			}],
		order: [{
				identifier: {
					type: "betaIdentifier",
					parts: ["rank"]
				},
				ascDesc: false
			}],
		limit: 1};

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
		data: JSON.stringify(queryJson),
		timeout: 10000,
		crossDomain: true,
		success: succesCB,
		error: function () {
			errorCB(msg.errorAuthorizationVerification);
		}
	});
}

/**
 * Shows a message to the user if anything went wrong during uploading and
 * decrypting the secret key.
 *
 * @param message - The message to display to the user.
 */
function showUploadKeyError(message) {
	$(elements.uploadKeyError).html(msg[message]).animate({'opacity': '1'}, 500);
}


/**
 * Goes to step 2: Voting interface.
 */
function gotoStep2() {

	// Update progress bar
	$(elements.step1).removeClass("active");
	$(elements.step2).addClass("active");

	// Show vote content
	$(elements.step1content).addClass("hidden");
	$(elements.step2content).removeClass("hidden");

	// Scroll to top
	window.scrollTo(0, 0);

	// Open video dialog
	if (listsAreSelectable) {
		$(dialogs.video).dialog('open');
	}
}

/**
 * Goes to step 3: Confirmation
 */
function gotoStep3() {
	// Update proress bar
	$(elements.step2).removeClass("active");
	$(elements.step3).addClass("active");

	// Show confirmation content
	$(elements.step2content).addClass("hidden");
	$(elements.step3content).removeClass("hidden");

	// Scroll to top
	window.scrollTo(0, 0);
}

/**
 * Processes a fatal error. A fatal error is an error if occuring it is impossible
 * to continue (eg. voting service not available, corrupted election data, etc.).
 * An error message is displayed to the user for about 5s, afterwards the site is
 * redirected to home.
 *
 * @param errorMsg - The error message to display.
 */
function processFatalError(errorMsg) {
	$.unblockUI();
	// Show error message
	$.blockUI({message: '<p>' + errorMsg + '</p>'});
	// Redirect to home after 5s
	setTimeout(function () {
		location.href = uvConfig.HOME_SITE;
	}, 5000);
}



//===========================================================================
// Get and process data from election board service


function retrieveElectionDefinition() {

	var update = setInterval(function () {
		$(elements.loadingElections).append(".");
	}, 1000);

	//Query of election data over all sections
	var queryJson = {
		constraint: [{
				type: "equal",
				identifier: {
					type: "alphaIdentifier",
					part: ["section"]
				},
				value: {
					type: "stringValue",
					value: electionId}
			}, {
				type: "equal",
				identifier: {
					type: "alphaIdentifier",
					part: ["group"]
				},
				value: {
					type: "stringValue",
					value: "electionDefinition"
				}
			}]
	};

	//For IE
	$.support.cors = true;

	var successCB = function (resultContainer) {
		var posts = resultContainer.result.post;
		var message = JSON.parse(B64.decode(posts[0].message));
		//var administration = (message.administration != undefined) ? getLocalizedText(message.administration) : '';
		var title = getLocalizedText(message.title);
		$(elements.electionTitle).html(title);
		$.unblockUI();
		$(elements.step1content).addClass('active');
		clearInterval(update);
	};

	var errorCB = function (errormsg) {
		clearInterval(update);
		processFatalError(msg.retreiveElectionDataError);
	};

	uniBoard.get(queryJson, successCB, errorCB);
}




/**
 * Retrieves election data from Board (asynchronously).
 * If Board is on another domain, IE9 will not be able to retrieve the data
 * IE9 does not support cross domain ajax request.
 * JSONP would be a solution, but it only allows HTTP GET and HTTP POST is required for the
 * REST Service of the Board.
 *
 */
function retrieveElectionData() {

	var update = setInterval(function () {
		$('#blockui-processing').append('.');
	}, 500);

	//Get election data: descendant order (newest first), limit 1
	var queryJson = {
		constraint: [{
				type: "equal",
				identifier: {
					type: "alphaIdentifier",
					part: ["section"]
				},
				value: {
					type: "stringValue",
					value: electionId}
			}, {
				type: "equal",
				identifier: {
					type: "alphaIdentifier",
					part: ["group"]
				},
				value: {
					type: "stringValue",
					value: "electionData"
				}
			}],
		order: [{
				identifier: {
					type: "betaIdentifier",
					parts: ["rank"]
				},
				ascDesc: false
			}],
		limit: 1};

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
		data: JSON.stringify(queryJson),
		timeout: 10000,
		crossDomain: true,
		success: function (resultContainer) {
			// Save election data
			var posts = resultContainer.result.post;
			var post;
			if (posts == undefined || posts.length <= 0) {
				//no data received
				processFatalError(msg.retreiveElectionDataError);
				return;
			} else if (posts.length > 1) {
				//multiple posts received, the last one is the right one
				post = posts[posts.length - 1];
			} else {
				post = posts[0];
			}

			for (var i = 0; i < posts.length; i++) {
				console.log("Post " + i + ": " + B64.decode(posts[i].message));
			}

			//assumes that only one post is retuned
			var message = JSON.parse(B64.decode(post.message));
			console.log(message);


			// TODO
			//assumes that there is only one election since GUI only supports one election
			var elections = message.elections;
			if (elections.length < 0) {
				//no data received
				clearInterval(update);
				processFatalError(msg.retreiveElectionDataError);
				return;
			} else if (elections.length > 1) {
				//Multiple elections received. Only one is currently supported by the current voting client.
				clearInterval(update);
				processFatalError(msg.tooMuchDataReceived);
				return;
			}
			electionData = elections[0];

			if (electionData.type === CLASS_NAME_VOTE) {
				//Votes are not currently supported by the current voting client.
				clearInterval(update);
				processFatalError(msg.incompatibleDataReceived);
				return;
			} else if (electionData.type !== CLASS_NAME_CANDIDATE_ELECTION && electionData.type !== CLASS_NAME_PARTY_ELECTION) {
				//Unknown type of election
				clearInterval(update);
				processFatalError(msg.incompatibleDataReceived);
				return;
			}
			// Check signatures of retrieved post
			try {
				//Signature of ResultContainer (certified read is not checked, since the one post contained in the ResultContainer
				//is also signed)
				//var result = uvCrypto.verifyResultSignature(resultContainer, uvConfig.EC_SETTING, true);
				//verifySignature(result);
				//verifySignature(true);
			} catch (errormsg) {
				clearInterval(update);
				processFatalError(msg.signatureError);
				return;
			}

			$(elements.electionTitle).html(getLocalizedText(electionData.title));
			$.unblockUI();

			//verifySignature(true);
			clearInterval(update);
		},
		error: function (errormsg) {
			clearInterval(update);
			processFatalError(msg.retreiveElectionDataError);
		}
	});

}

/**
 * The callback for the verification of the election data signature. If the
 * signature is correct then the election data are processed, otherwise a
 * fatal error.
 *
 * @param success - true if the signature is correct otherwise false.
 */
function verifySignature(success) {

	if (!success) {
		processFatalError(msg.signatureError);
		return;
	}

	//
	// The signature is correct, so process election data.
	// Doing all in once is too much for IE < 9!!
	// So it is done in a few asynchronous steps.
	//
	var choices, rules, lists, choicesMap;

	// Step 1: Process cryptographic parameters
	// Set Elgamal parameters, election generator and encryption key
	uvCrypto.setEncryptionParameters(electionData.encryptionSetting.p, electionData.encryptionSetting.q, electionData.encryptionSetting.g, 10);
	uvCrypto.setSignatureParameters(electionData.signatureSetting.p, electionData.signatureSetting.q, electionData.signatureSetting.g, electionData.signatureSetting.ghat, 10);
	electionGenerator = leemon.str2bigInt(electionData.signatureSetting.ghat, 10, 1);
	encryptionKey = leemon.str2bigInt(electionData.encryptionSetting.encryptionKey, 10, 1);

	// Step 2: Process election details like title, choices and rules
	// Set title
	$(elements.electionTitle).html(getLocalizedText(electionData.title, lang));

	// Initilize arrays for choices and rules
	choiceIds = new Array();
	sumRules = new Array();
	forAllRules = new Array();
	choicesMap = new Map();

	// Get the choices and rules
	choices = electionData.choices;
	rules = electionData.rules;
	lists = [];
	if (electionData.type === CLASS_NAME_CANDIDATE_ELECTION) {
		lists = electionData.candidateLists === undefined ? new Array() : electionData.candidateLists;
	} else if (electionData.type === CLASS_NAME_PARTY_ELECTION) {
		lists = electionData.partyLists;
	}

	// Add candidates to the coresponding PoliticalList
	for (i = 0; i < choices.length; i++) {
		choicesMap.put(choices[i].choiceId, choices[i]);
	}

	// If no list exists, create one and put afterwards every candidate into it
	if (lists.length === 0) {
		var uniqueList = "{ \"choicesIds\": " + JSON.stringify(choicesMap.listKeys()) + "}";
		lists.push(JSON.parse(uniqueList));
		noList = true;
	}

	// Step 3: Process rules
	// Split different rules
	for (i = 0; i < rules.length; i++) {
		var rule = rules[i];
		if (rule.type === CLASS_NAME_SUMMATION_RULE) {
			sumRules.push(rule);
		}
		else if (rule.type === CLASS_NAME_FORALL_RULE) {
			forAllRules.push(rule);
		}
	}

	// Figure out whether the voter can vote for candidates and a list or
	// only for candidates
	listsAreSelectable = electionData.type === CLASS_NAME_PARTY_ELECTION;

	// Render the vote view: Add lists and candidates to the view
	renderVoteView(lists, choicesMap);

	// Finally unblock the GUI
	$.unblockUI();
}



/**
 * Renders the vote view; lists and candidates are added to the view.
 *
 * @param lists - Array of PoliticalList.
 * @param choicesMap - Map contaning the choices of the election (key is choiceId)
 */
function renderVoteView(lists, choicesMap) {

	// Replace vote text if only candidates and no lists are selectable
	if (!listsAreSelectable) {
		$(elements.voteText).html(msg.voteTextCandidatesOnly);
	}

	// For each list
	for (var i = 1; i <= lists.length; i++) {
		var list = lists[i - 1];

		// Get list information
		var choiceId, partyName, number, title, listNumber, listSubtitle;
		if (noList) {
			choiceId = '';
			partyName = '';
			number = '';
			title = '';
			listNumber = msg.list + " 1";
			listSubtitle = '';
		} else {
			//if PartyElection, get the choice id and partyName bound to the list
			if (electionData.type === CLASS_NAME_PARTY_ELECTION) {
				choiceId = list.partyId;
				partyName = getLocalizedText(choicesMap.get(choiceId).name, lang);
			} else {
				choiceId = -1;
				partyName = "";
			}

			//Get name of list
			title = getLocalizedText(list.name, lang);

			//If list.number is a number, create a string of the form 'List #'
			//If list.number is a string, get it as is
			var number = getLocalizedText(list.listNumber, lang);
			if (isNumber(number)) {
				listNumber = msg.list + " " + number;
			} else {
				listNumber = number;
			}
			listSubtitle = listNumber;
		}

		// Tab creation
		var dragListIcon = listsAreSelectable ? '<img class="drag_list icon" src="img/plus.png" alt="' + msg.add + '" title="' + msg.add + '"/>' : '';
		var contentToAppend = '<li><a href="#list-' + i + '">' + listNumber + '</a><input type="hidden" class="choiceid" value="' + choiceId + '"/>' + dragListIcon + '</li>';
		$(elements.lists).append(contentToAppend);

		// Creation of the list of candidates in HTML
		var contentToAppend2 = '<div id="list-' + i + '">';
		var listDescription = title === listSubtitle || listSubtitle === '' ? '' : listSubtitle + ' - ';
		contentToAppend2 += '<p>' + title + '<br/><span class="small">' + listDescription + " " + partyName + '</span></p>';
		contentToAppend2 += '<ul class="list">';

		// For each candidate in the list
		for (var j = 0; j < list.choicesIds.length; j++) {
			var candidate = choicesMap.get(list.choicesIds[j]);

			// Get candidate information
			var cLastName = candidate.lastname;
			var cFirstName = candidate.firstname;
			var cChoiceId = candidate.choiceId;

			// Create candidate's list element (currently cNumber is not displayed to the voter)
			var tooltiptext = getLocalizedText(candidate.description, lang).replace(",", "<br/>");

			contentToAppend2 += '<li class="ui-state-default">' +
					//'<span>' + cNumber + ' ' + cLastName + ' ' + cFirstName +
					'<span>&nbsp;' + cLastName + ' ' + cFirstName +
					'</span><input type="hidden" class="choiceid" value="' + cChoiceId + '"/>' +
					'<img class="drag_candidate icon" src="img/plus.png" alt="' + msg.add + '" title="' + msg.add + '"/>';
			if (tooltiptext != "") {
				contentToAppend2 += '<img class="tooltip_candidate icon" src="img/info_small.png" alt="" tooltip="' + tooltiptext + '"/>';
			}
			contentToAppend2 += '</li>';
		}
		contentToAppend2 += '</ul></div>';
		$(elements.listsContent).append(contentToAppend2);

	}

	// Generate jquery user interface
	jquery_generate({'listsAreSelectable': listsAreSelectable});
}

function isNumber(n) {
	return !isNaN(parseFloat(n)) && isFinite(n);
}

//===========================================================================
