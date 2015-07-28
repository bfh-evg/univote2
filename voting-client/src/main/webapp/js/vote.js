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



//<!-- DIALOGS -->

//<div id="dialog-confirm" title="#{msg.ballot}"></div>
//<div id="dialog-copy-list" title="#{msg.copycandidatetitle}">#{msg.copycandidate}</div>
//<div id="dialog-too-many-candidates" title="#{msg.error}">#{msg.toomanycandidate}</div>
//<div id="dialog-too-many-repetitions" title="#{msg.error}">#{msg.toomanyrepetitions}</div>
//<div id="list-copied-with-modification" title="#{msg.information}">#{msg.listcopiedwithmodif}</div>




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
 * Holds the election id.
 */
var electionId;

/**
 * Holds the election generator (bigInt).
 */
var signatureGenerator;

/**
 * Holds the encryption key (bigInt).
 */
var encryptionKey;

/**
 *
 */
var electionDetails;

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


	elements.sendVoteSuccess = document.getElementById('send-vote-success');
	elements.sendVoteError = document.getElementById('send-vote-error');
	elements.sendVoteErrorMessage = document.getElementById('send-vote-error-message');
	elements.qrcodeHolder = document.getElementById('qrcode-holder');

	// Get dialogs
	dialogs.confirm = document.getElementById('dialog-confirm');
	dialogs.$confirm = $(dialogs.confirm);

	// Get election id and user's language/locale
	electionId = $('#election-title').data('electionId');

	// Block UI while loading election data from board
	$.blockUI({
		message: '<p id="blockui-processing">' + msg.loading + '</p>',
		fadeIn: 10
	});

	// Initiate retrieving election data from voting service
	setTimeout(retrieveElectionData, 50);

});



function retrieveElectionData() {

	//Query of election data over all sections
	var query = {
		constraint: [{
				type: "equal",
				identifier: {type: "alphaIdentifier", part: ["section"]},
				value: {type: "stringValue", value: electionId}
			}, {
				type: "equal",
				identifier: {type: "alphaIdentifier", part: ["group"]},
				value: {type: "stringValue", value: "electionData"}
			}]
	};

	var successCB = function (resultContainer) {

		// TODO @DEV
		if (!mock) {
			if (!uvCrypto.verifyResultSignature(resultContainer, uvConfig.EC_SETTING, true)) {
				processFatalError(msg.signatureError);
				return;
			}

			var posts = resultContainer.result.post;
			// Expect exactly one post! ElectionId should be unique!
			if (posts.length != 1) {
				processFatalError(msg.retreiveElectionDataError);
				return;
			}

			var message = JSON.parse(B64.decode(posts[0].message));
		} else {
			var message = resultContainer;
		}

		// Process retreived election data
		var es = uvConfig.CS[message.cryptoSetting.encryptionSetting];
		var ss = uvConfig.CS[message.cryptoSetting.signatureSetting];
		var hs = uvConfig.CS[message.cryptoSetting.hashSetting];
		if (!(es && ss && hs)) {
			processFatalError(msg.incompatibleDataReceived);
			return;
		}

		uvCrypto.setEncryptionParameters(es);
		uvCrypto.setSignatureParameters(ss);
		uvCrypto.setHashParameters(hs);

		encryptionKey = message.encryptionKey || '';
		signatureGenerator = message.signatureGenerator || '';

		electionDetails = new ElectionDetails(message.details);

		if (!(encryptionKey && signatureGenerator && electionDetails.issues.length > 0)) {
			processFatalError(msg.incompatibleDataReceived);
			return;
		}

		var title = getLocalizedText(message.definition.title);

		$(elements.electionTitle).html(title);
		$.unblockUI();
	};

	var errorCB = function () {
		processFatalError(msg.retreiveElectionDataError);
	};

	// TODO @DEV
	var mock = true;
	if (!mock) {
		uniBoard.get(query, successCB, errorCB);
	} else {
		//Ajax request
		$.ajax({
			//url: "https://raw.githubusercontent.com/bfh-evg/univote2/development/admin-client/json-schemas/examples/sub-2015/electionData.json",
			url: "http://uni.vote/listElection.php",
			type: 'GET',
			accept: "application/json",
			dataType: 'json',
			crossDomain: true,
			success: successCB,
			error: errorCB
		});
	}
}



/**
 * Uploads (to the browser not to the server) the secret key. The secret key can
 * be uploaded either by file upload or manually by copy/paste. That depends on
 * whether the browser supports the file api and whether the flag UPLOAD_SK_MANUALLY_ALWAYS
 * is set.
 */
function uploadSecretKey() {

	// TODO @DEV
	gotoStep2();
	return;

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
		gotoStep2();
	}
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

	// Create vote content
	// Currently only the following issue combinations are supported:
	//   - 1 issue of type 'listElection'
	//   - n issues of tpye 'vote'
	var issues = electionDetails.issues;
	if (issues[0] instanceof ListElectionIssue) {
		createListElectionContent(issues[0]);
	} else {
		createVoteContent(issues);
	}

	// Update progress bar
	$(elements.step1).removeClass("active");
	$(elements.step2).addClass("active");

	// Show vote content
	$(elements.step1content).addClass("hidden");
	$(elements.step2content).removeClass("hidden");

	// Scroll to top
	window.scrollTo(0, 0);
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
	// Redirect to home after 5s (TODO: Add a link back to home instead of auto redirect)
	setTimeout(function () {
		location.href = uvConfig.HOME_SITE;
	}, 5000);
}



function createListElectionContent(issue) {

	$('#list_vote_description').html(issue.description);
	if (!issue.listsAreChoosable()) {
		$('#list_vote_text').html(msg.voteTextCandidatesOnly);
	}

	var lists = issue.getLists();
	var $listLabels = $('#list_labels');
	var $lists = $('#lists');
	for (var i in lists) {
		var list = lists[i];

		var $listLabel = $('<li class="button-like"><a href="#">' + msg.list + ' ' + list.number + '</a></li>');
		$listLabel.data('id', list.id);
		if (issue.listsAreChoosable()) {
			var $tools = $('<div>').addClass('tools').append($('<span>').addClass('icon-plus-circled'));
			$listLabel.append($tools);
		}

		$listLabels.append($listLabel);

		var $list = $('<div>').attr('id', 'list-' + list.id).addClass('list prim-border').hide();
		$list.append($('<h5>').html(__(list.partyName) + ' - ' + __(list.listName)));
		var $ul = $('<ul>');
		var candidates = issue.getListCandidates(list.id);
		for (var j in candidates) {
			var cand = candidates[j];
			var $name = $('<span>').html(cand.lastName + ' ' + cand.firstName);
			var $cand = $('<li>').append($name).addClass('button-like').data('id', cand.id);
			var $tools = $('<div>').addClass('tools').append($('<span>').addClass('icon-info-circled')).append($('<span>').addClass('icon-plus-circled'));
			$cand.append($tools);

			if (cand.status === 'OLD') {
				$cand.addClass('previous');
				$name.append('<br/><i>(' + msg.previous + ')</i>');
			}
			$ul.append($cand);
		}
		$list.append($ul);
		$lists.append($list);
		if (i == 0) {
			$listLabel.addClass('active');
			$list.show();
		}
	}


	// Tabs
	$('#list_labels a').click(function (event) {
		event.preventDefault();
		var $parent = $(this).parent();
		$('#list_labels li').removeClass('active');
		$parent.addClass('active');
		$('#lists .list').hide().eq($parent.index()).show();
	});


	var addCandidate = function () {
		return true;
	};

	var removeCandidate = function () {
	};

	var addList = function (id) {
		var list = issue.getOption(id);

		var process = function (withCandidates, dialog) {
			$(dialog).dialog("close");
			var ed = issue.electionDetails;
			if (withCandidates) {
				ed.removeAllChoices();
				var $choiceCandidates = $("#choice-candidates").empty();
				$('#list-' + id + ' li').each(function () {
					var $cand = $(this);
					var candId = $cand.data('id');
					var $clone = $cand.clone(false);
					$clone.data('id', candId);
					ed.addChoice(candId, 1, true);
					$choiceCandidates.append($clone);
				});
			} else {
				var oldListId = $("#choice-list li").data('id');
				if (oldListId != undefined && oldListId > -1) {
					ed.removeChoice(oldListId);
				}
			}
			ed.addChoice(id);
			console.log("Vote: " + ed.vote);
			$("#choice-list li").removeClass("placeholder-item").data('id', id);
			$("#choice-list li>span").html(msg.list + ' ' + list.number);
			$('#choice h5').html(__(list.partyName) + ' - ' + __(list.listName));
		};

		var buttons = {};
		buttons[msg.copyAllCandidates] = function () {
			process(true, this);
		};
		buttons[msg.copyListnumber] = function () {
			process(false, this);
		};

		$('<div title="' + msg.copycandidatetitle + '">' + msg.copycandidate + '</div>').dialog({
			resizable: false,
			draggable: false,
			autoOpen: true,
			width: 600,
			modal: true,
			buttons: buttons
		});
	};

	var removeList = function () {
		// ...
		$("#choice-list li").addClass("placeholder-item").data('id', '');
		$("#choice-list li>span").html(msg.list);
		$('#choice h5').html('');

	};





	// Candidates
	$("#lists .list li").draggable({
		helper: "clone",
		connectToSortable: "#choice-candidates",
		revert: "invalid",
		scroll: false,
		start: function (event, ui) {
			ui.helper.addClass("candidateBeingDragged").css('z-index', 1000);
		},
		stop: function (event, ui) {
			$lists.unblock({fadeOut: 200});
		}
	});

	/* Do not allow to drag and drop the placholder itmes */
	$('.placeholder-item').mousedown(function (event) {
		event.stopPropagation();
	});

	/* Declare the created list as sortable */
	$("#choice-candidates").sortable({
		placeholder: 'placeholder',
		start: function (event, ui) {
			ui.item.addClass("candidateBeingDragged");
		},
		//sort: function(event, ui) {},
		receive: function (e, ui) {
			$(this).find(".placeholder-item").remove();
			console.log("ADD ITEM: " + ui.item.data('id'));
			if (addCandidate(ui.item.data('id'))) {
				ui.helper.data('id', ui.item.data('id')).css('z-index', 1);
			} else {
				ui.helper.remove();
			}
		},
		over: function (e, ui) {
			ui.item.data('isOut', false);
			$lists.unblock({fadeOut: 200});
		},
		out: function (e, ui) {
			ui.item.data('isOut', true);
			$lists.block({message: '', fadeIn: 200, overlayCSS: {opacity: '0.4'}});
		},
		beforeStop: function (e, ui) {
			console.log("isOut: " + ui.item.data('isOut'));
			if (ui.item.data('isOut')) {
				console.log("REMOVE ITEM: " + ui.item.data('id'));
				ui.item.remove();
				removeCandidate(ui.item.data('id'));
			}
			ui.item.removeClass("candidateBeingDragged");
		},
		stop: function (e, ui) {
			$lists.unblock({fadeOut: 200});
		}

	}).droppable({
		//accept candidates
		accept: "#lists .list li"
	});


	// List
	if (issue.listsAreChoosable()) {
		/* Declare the initial lists (the tabs) as draggable */
		$("#list_labels > li").draggable({
			helper: "clone",
			revert: "invalid",
			scroll: false,
			appendTo: $("#choice"),
			start: function (event, ui) {
				//apply a css class to avoid deformation
				ui.helper.addClass("partyBeingDragged").css('z-index', 1000);
			}
		});

		/* Declare the party field as droppable */
		$("#choice").droppable({
			activeClass: "drop-active",
			accept: "#list_labels > li",
			drop: function (event, ui) {
				//showDialogCopyList(ui.draggable);
				addList(ui.draggable.data('id'));
			}

		});
	} else {
		$("#choice-list").hide();
	}


}


function createVoteContent(issues) {
	console.log("Creating vote content...");
}























//===========================================================================
// Get and process data from election board service



///////////////////////////////////////////////////////////////////////////
////////// O L D //////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////



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


function isNumber(n) {
	return !isNaN(parseFloat(n)) && isFinite(n);
}

//===========================================================================
