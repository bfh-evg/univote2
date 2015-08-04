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


/**
 * Holds the used DOM elements.
 */
var elements = {};

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

		encryptionKey = leemon.str2bigInt(message.encryptionKey, 10, 1);
		signatureGenerator = leemon.str2bigInt(message.signatureGenerator, 10, 1);

		electionDetails = new ElectionDetails(message.details);

		if (!(encryptionKey && signatureGenerator && electionDetails.getIssues().length > 0)) {
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
		UniBoard.GET(query, successCB, errorCB);
	} else {
		//Ajax request
		$.ajax({
			//url: "https://raw.githubusercontent.com/bfh-evg/univote2/development/admin-client/src/main/resources/json-examples/sub-2015/votingData.json",
			//url: "http://uni.vote/listElection.php",
			url: "http://uni.vote/voteElection.php",
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
	//gotoStep2();
	//return;

	// Hide previous error message
	$(elements.uploadKeyError).html('&nbsp;').css('opacity', '0');

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
	var issues = electionDetails.getIssues();
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









//===========================================================================
// C R E A T I N G   E L E C T I O N   C O N T E N T S
//===========================================================================

function createListElectionContent(issue) {

	// TODO: Support for no-list (only candidates, not belonging to a list)

	var electionDetails = issue.getElectionDetails();

	$('#list_vote_description').html(issue.description);
	if (!issue.listsAreChoosable()) {
		$('#list_vote_text').html(msg.voteTextCandidatesOnly);
	}

	var lists = issue.getLists();
	var $listLabels = $('#list_labels');
	var $lists = $('#lists');
	for (var i in lists) {
		var list = lists[i];

		var $listLabel = $('<li class="button-like"><a href="#">' + msg.list + ' ' + list.getNumber() + '</a></li>');
		$listLabel.data('id', list.getId());
		if (issue.listsAreChoosable()) {
			var $tools = $('<div>').addClass('tools').append($('<span>').addClass('add icon-plus-circled'));
			$listLabel.append($tools);
		}

		$listLabels.append($listLabel);

		var $list = $('<div>').attr('id', 'list-' + list.getId()).addClass('list prim-border').hide();
		$list.append($('<h5>').html(list.getName()));
		var $ul = $('<ul>');
		var candidates = issue.getListCandidates(list.getId());
		for (var j in candidates) {
			var cand = candidates[j];
			var $name = $('<span>').html(cand.getName());
			var $cand = $('<li>').append($name).addClass('button-like').data('id', cand.getId());
			var $tools = $('<div>').addClass('tools').append($('<span>').addClass('info icon-info-circled')).append($('<span>').addClass('add icon-plus-circled'));
			$cand.append($tools);

			if (cand.isPrevious()) {
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

	var candidateTooltip = function () {
		var cand = issue.getOption($(this).parents('.button-like').data('id'));
		var tp = '<strong>' + cand.getNumber() + '<br/>' + cand.getName() + '</strong><br/>';
		if (cand.isPrevious()) {
			tp += '<i>(' + msg.previous + ')</i><br/>';
		}
		tp += cand.getStudyBranch() + (cand.getStudyDegree() ? (cand.getStudyBranch() ? ' / ' : '') + cand.getStudyDegree() : '') + '<br/>';
		tp += msg.semester + ': ' + cand.getStudySemester() + '<br/>';
		tp += (cand.getYearOfBirth() > 0 ? cand.getYearOfBirth() : '') + (cand.getSex() ? (cand.getYearOfBirth() ? ' / ' : '') + cand.getSexSymbol() : '');

		return tp;
	};

	var addCandidate = function (id) {
		electionDetails.addChoice(id, 1, true);
		var ret = electionDetails.verifyVoteUpperBoundOnly();
		if (ret != Rule.SUCCESS) {

			electionDetails.removeChoice(id, 1);
			var $dialog = $('<div>')
					.attr('title', msg.error)
					.html(ret == Rule.ERROR_CUMULATION_UPPER ? msg.tooManyRepetitions : msg.tooManyCandidates);
			var buttons = {};
			buttons[msg.ok] = function () {
				$(this).dialog("close");
			};
			$dialog.dialog({
				autoOpen: true,
				modal: true,
				buttons: buttons
			});
		} else {
			$("#choice-candidates .placeholder-item").remove();
		}

		return ret == Rule.SUCCESS;
	};

	var removeCandidate = function (id) {
		electionDetails.removeChoice(id, 1);
	};

	var addList = function (id) {
		var list = issue.getOption(id);
		var process = function (withCandidates, dialog) {
			$(dialog).dialog("close");
			if (withCandidates) {
				electionDetails.removeAllChoices();
				var $choiceCandidates = $("#choice-candidates").empty();
				$('#list-' + id + ' li').each(function () {
					var $cand = $(this);
					var candId = $cand.data('id');
					var $clone = $cand.clone(false);
					$clone.data('id', candId);
					updateCandidateTools($clone);
					electionDetails.addChoice(candId, 1, true);
					$choiceCandidates.append($clone);
				});
			} else {
				var oldListId = $("#choice-list li").data('id');
				if (oldListId != undefined && oldListId > -1) {
					electionDetails.removeChoice(oldListId);
				}
			}
			electionDetails.addChoice(id);
			console.log("Vote: " + electionDetails.getVote());
			$("#choice-list li").removeClass("placeholder-item").data('id', id);
			$("#choice-list li>span").html(msg.list + ' ' + list.getNumber());
			$('#choice h5').html(list.getName());
		};

		var buttons = {};
		buttons[msg.yes] = function () {
			process(true, this);
		};
		buttons[msg.no] = function () {
			process(false, this);
		};

		$('<div title="' + msg.copyCandidatesTitle + '">' + msg.copyCandidates + '</div>').dialog({
			autoOpen: true,
			width: 400,
			modal: true,
			buttons: buttons
		});
	};

	var removeList = function () {
		var process = function (removeCandidates, dialog) {
			$(dialog).dialog("close");
			if (removeCandidates) {
				electionDetails.removeAllChoices();
				$("#choice-candidates").empty();
			} else {
				console.log(electionDetails.getVote());
				electionDetails.removeChoice($("#choice-list li").data('id'));
				console.log("Remove: " + $("#choice-list li").data('id'));
				console.log(electionDetails.getVote());
			}
			$("#choice-list li").addClass("placeholder-item").data('id', '');
			$("#choice-list li>span").html(msg.list);
			$('#choice h5').html('&nbsp;');
		};
		var buttons = {};
		buttons[msg.yes] = function () {
			process(true, this);
		};
		buttons[msg.no] = function () {
			process(false, this);
		};

		$('<div title="' + msg.removeCandidatesTitle + '">' + msg.removeCandidates + '</div>').dialog({
			autoOpen: true,
			width: 400,
			modal: true,
			buttons: buttons
		});
	};

	var updateCandidateTools = function ($candidate) {
		var id = $candidate.data('id');
		$candidate.find('.add')
				.removeClass('add icon-plus-circled')
				.addClass('remove icon-minus-circled')
				.click(function () {
					removeCandidate(id);
					$candidate.remove();
				});
		$candidate.tooltip({items: '.info', content: candidateTooltip});
	};


	// Tabs
	$('#list_labels a').click(function (event) {
		event.preventDefault();
		var $parent = $(this).parent();
		$('#list_labels li').removeClass('active');
		$parent.addClass('active');
		$('#lists .list').hide().eq($parent.index()).show();
	});


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

	// Do not allow to drag and drop the placholder itmes
	$('.placeholder-item').mousedown(function (event) {
		event.stopPropagation();
	});


	$("#choice-candidates").sortable({
		placeholder: 'placeholder',
		start: function (event, ui) {
			ui.item.addClass("candidateBeingDragged");
		},
		receive: function (e, ui) {
			if (addCandidate(ui.item.data('id'))) {
				ui.helper.data('id', ui.item.data('id')).css('z-index', 1);
				updateCandidateTools(ui.helper);
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
		accept: "#lists .list li"
	});


	// List
	if (issue.listsAreChoosable()) {
		$("#list_labels > li").draggable({
			helper: "clone",
			revert: "invalid",
			scroll: false,
			appendTo: $("#choice"),
			start: function (event, ui) {
				ui.helper.addClass("partyBeingDragged").css('z-index', 1000);
			}
		});

		$("#choice").droppable({
			activeClass: "drop-active",
			accept: "#list_labels > li",
			drop: function (event, ui) {
				addList(ui.draggable.data('id'));
			}
		});
	} else {
		$("#choice-list").hide();
		$("#list_labels li").css('cursor', 'default');
	}

	// Register events (tools)
	$('#list_labels .add').click(function () {
		addList($(this).parents('#list_labels li').data('id'));
	});

	$('#choice-list .remove').click(function () {
		removeList();
	});

	$('#lists .add').click(function () {
		var $cand = $(this).parents('#lists li');
		var candId = $cand.data('id');
		if (addCandidate(candId)) {
			var $clone = $cand.clone(false);
			$clone.data('id', candId);
			updateCandidateTools($clone);
			$('#choice-candidates').append($clone);
		}
	});

	$(document).tooltip({
		items: '#lists .info',
		content: candidateTooltip
	});

	// Finally show content!
	$('#list_election_content').show();
}


function createVoteContent(issues) {
	console.log("Creating vote content...");

	var $issues = $('#vote_issues');
	for (var i = 0; i < issues.length; i++) {
		var issue = issues[i];
		var $issue = $('<div>').addClass('issue clearfix');
		var $title = $('<h4>').html(issue.getTitle());
		var $question = $('<p>').html(issue.getQuestion());
		var $answers = $('<div>');

		var optionIds = issue.getOptionIds();
		var nrRows = Math.ceil(optionIds.length / 3);
		var $row = null;
		for (var j = 0; j < optionIds.length; j++) {
			if (j % 3 == 0) {
				$row = $('<div>').addClass('row');
				$answers.append($row);
			}
			var q = nrRows > 1 ? 4 : (12 / optionIds.length);
			var option = issue.getOption(optionIds[j]);
			var $answer = $('<div>').addClass('medium-' + q + ' columns');
			var $tools = $('<div>').addClass('tools').append($('<span>').addClass('add icon-plus-circled')).append($('<span>').addClass('remove icon-minus-circled'));
			var $button = $('<div>').addClass('button-like').data('id', option.getId()).append('<span>' + option.getAnswer() + '</span>', $tools);
			$answer.append($button);
			$row.append($answer);
		}

		$issue.append($title, $question, $answers);
		$issues.append($issue);
	}

	$('.issue .button-like').click(function () {
		var $this = $(this);
		var selectedClass = 'selected';
		if ($this.hasClass(selectedClass)) {
			electionDetails.removeChoice($this.data('id'));
			$this.removeClass(selectedClass);
		} else {
			var $selected = $this.parents('.issue').find('.' + selectedClass);
			if ($selected.size() > 0) {
				electionDetails.removeChoice($selected.data('id'));
				$selected.removeClass(selectedClass);
			}
			electionDetails.addChoice($this.data('id'));
			$this.addClass(selectedClass);
		}
	});

	// Finally show content!
	$('#vote_content').show();
}





//===========================================================================
// B A L L O T   C A S T I N G
//===========================================================================

function submitListBallot() {

	// TODO: Verify vote, although there shouldn't be any rule violations!

	var $dialog = $('<div>').attr('id', 'dialog-list-ballot').attr('title', msg.ballot);
	var $listNr = $('<div>').addClass('button-like').html(msg.noListShort);
	var $listName = $('<h5>').html(msg.noListLong);
	var $candidates = $('<ul>');

	var issue = electionDetails.getIssues()[0];
	if (issue.listsAreChoosable()) {
		$dialog.append($listNr).append($listName);
	}
	$dialog.append($candidates);
	var vote = electionDetails.getVote();
	for (var id in vote) {
		if (vote[id] == 0) {
			continue;
		}
		var option = electionDetails.getOption(id);
		if (option.isList()) {
			$listNr.html(msg.list + ' ' + option.getNumber());
			$listName.html(option.getName());
		} else if (option.isCandidate()) {
			var $name = $('<span>').html(vote[id] + ' x ' + option.getName());
			var $cand = $('<li>').append($name).addClass('button-like');
			$candidates.append($cand);
		} else {
			// If vote was verified, this can never happen!
		}
	}

	showSubmitBallotDialog($dialog);

	return false;
}


function submitVoteBallot() {
// TODO: Verify vote, although there shouldn't be any rule violations!

	var $dialog = $('<div>').attr('id', 'dialog-vote-ballot').attr('title', msg.ballot);

	var issues = electionDetails.getIssues();

	for (var i = 0; i < issues.length; i++) {
		var issue = issues[i];
		var optionIds = issue.getOptionIds();
		$dialog.append('<h4>' + issue.getTitle() + '</h4>');
		var noChoice = true;
		for (var j = 0; j < optionIds.length; j++) {
			var optionId = optionIds[j];
			if (electionDetails.getChoice(optionId) > 0) {
				$dialog.append('<div class="button-like"><span>' + electionDetails.getOption(optionId).getAnswer() + '</span></div>');
				noChoice = false;
			}
		}
		if (noChoice) {
			$dialog.append('<p>' + msg.noChoice + '</p>');
		}
	}

	showSubmitBallotDialog($dialog);

	return false;
}

function showSubmitBallotDialog($dialog) {
	var buttons = {};
	buttons[msg.confirm] = function () {
		$(this).dialog("close");
		$.blockUI({
			message: '<p id="blockui-processing">' + msg.wait + '.</p>',
			css: {
				width: '40%',
				left: '30%'
			}
		});
		setTimeout(finalizeVote, 500);
	};
	buttons[msg.cancel] = function () {
		$(this).dialog("close");
	};

	$dialog.dialog({
		autoOpen: true,
		height: 600,
		width: 400,
		modal: true,
		buttons: buttons,
		open: function (event, ui) {
			$("a.ui-dialog-titlebar-close").remove();
		}
	});
}


/**
 * Finalizes the vote. After encoding, encrypting, proofing, and signing the
 * vote is finally casted. The confirmMap must be filled up prior to calling
 * this function!
 * In order to be able to give feedback to the user that something is happening, this function is implemented
 * on an asynchronous manner
 */
function finalizeVote() {

	var ballotData = {};

	var updateProcessing = function () {
		$('#blockui-processing').append('.');
	};

	var step1 = function () {
		try {
			// 1 - Encode vote (BigInt) (LATER: Based on electionDetails.ballotEncoding)
			console.log("1. Encode Vote");
			var encodedVote = uvCrypto.encodeVote(electionDetails);

			// 2 - represent encodedvote in Gq
			console.log("2. Vote in Gq");
			var voteInGq = uvCrypto.mapZq2Gq(encodedVote);

			ballotData.electionId = electionId;

			updateProcessing();
			setTimeout(function () {
				step2(voteInGq);
			}, 100);
		} catch (error) {
			processExceptionInFinalizeVote(msg.encodeVoteError);
			return;
		}
	};

	var step2 = function (voteInGq) {
		// 3 - Encrypt vote (encrypted vote is stored in ballotData.encryptedVote.encVote)
		console.log("3. Encrypt Vote");
		ballotData.encryptedVote = uvCrypto.encryptVote(voteInGq, encryptionKey);

		updateProcessing();
		setTimeout(step3, 100);
	};

	var step3 = function () {
		// 4 - Compute anonymous verification key (verification key is stored in ballotData.verifKey.vkString)
		console.log("4. Compute Election Verififcation Key");
		ballotData.verifKey = uvCrypto.computeElectionVerificationKey(signatureGenerator, secretKey);

		updateProcessing();
		setTimeout(step4, 100);
	};

	var step4 = function () {
		// 5 - Generate NIZKP (Proof is stored in ballotData.proof)
		console.log("5. Compute Proof");
		ballotData.proof = uvCrypto.computeVoteProof(ballotData.encryptedVote.r, ballotData.encryptedVote.a, ballotData.verifKey.vk);

		var ballot = {encryptedVote: ballotData.encryptedVote.encVote, proof: ballotData.proof};

		updateProcessing();
		setTimeout(function () {
			step5(ballot);
		}, 100);
	};

	var step5 = function (ballot) {
		//6 - Sign post
		console.log("6. Sign Post");
		console.log("message: " + JSON.stringify(ballot, null, 4));
		var post = {
			message: B64.encode(JSON.stringify(ballot)),
			alpha: {
				attribute: [
					{key: "section", value: {type: "stringValue", value: electionId}},
					{key: "group", value: {type: "stringValue", value: "ballot"}}
				]}
		};

		ballotData.signature = uvCrypto.signPost(post, signatureGenerator, secretKey);

		updateProcessing();
		setTimeout(function () {
			step6(post);
		}, 100);
	};

	var step6 = function (post) {
		post.alpha.attribute[2] = {key: "signature", value: {type: "stringValue", value: ballotData.signature.sigString}};
		post.alpha.attribute[3] = {key: "publickey", value: {type: "stringValue", value: ballotData.verifKey.vkString}};

		// 7 - Finally cast ballot
		console.log("7. Cast Ballot");
		var update = setInterval(updateProcessing, 1000);
		var successCB = function (beta) {
			clearInterval(update);
			var msg = beta.attribute[0].key;
			if (msg == "rejected" || msg == "error") {
				castVoteErrorCallback(beta.attribute[0].value.value);
				return;
			}
			// TODO: Verify signature!
			// TODO: Order of post is currently ignored!
			castVoteSuccessCallback(ballotData, {signature: {timestamp: beta.attribute[0].value.value, value: beta.attribute[2].value.value}});
		};
		var errorCB = function () {
			clearInterval(update);
			castVoteErrorCallback();
		};
		UniBoard.POST(post, successCB, errorCB);
	};

	step1();
}

/**
 * Processes an exception during finalizing the vote. A message is displayed to
 * the user for about 3s.
 *
 * @param msgToDisplay - The message to display.
 */
function processExceptionInFinalizeVote(msgToDisplay) {
	$.unblockUI();
	$.blockUI({
		message: '<p>' + msgToDisplay + '</p>',
		timeout: 3000
	});
}

/**
 * Error callback to handle vote casting faults.
 *
 *  @param message - error message
 */
function castVoteErrorCallback(message) {
	message = message || "";
	var errorCode = message.substring(0, 7);
	var errorMsg = "";
	switch (errorCode) {
		case "BAC-002":
			errorMsg = msg.sendVoteErrorNoEligibleVoter;
			break;
		case "BAC-003":
			errorMsg = msg.sendVoteErrorInvalidSignatureError;
			break;
		case "BAC-004":
			errorMsg = msg.sendVoteErrorInvalidElectionState;
			break;
		case "BAC-005":
			errorMsg = msg.sendVoteErrorInvalidElectionState;
			break;
		case "BAC-007":
			errorMsg = msg.sendVoteErrorAllreadyVoted;
			break;
		default:
			errorMsg = msg.sendVoteErrorInternalServerError;
	}

	// Set error message and hide success div but show error div on step 3
	elements.sendVoteErrorMessage.innerHTML = errorMsg;
	$(elements.sendVoteSuccess).addClass('hidden');
	$(elements.sendVoteError).removeClass('hidden');

	// Go to step 3
	gotoStep3();
	$.unblockUI();
}

/**
 * Success callback of vote casting. A qr-code is created holding the whole ballot
 * and the signature received from the voting service. Finally the last step is
 * displayed.
 *
 * @param ballotData
 * @param response - The response
 */
function castVoteSuccessCallback(ballotData, response) {

	var blindedRandomness = uvCrypto.blindRandomization(ballotData.encryptedVote.r, secretKey);

	// Put content of qr-code together. The content represents an object json
	// encoded holding the different values of ballot and signature.
	// => Do it manually, so we know exactly what's going on!
	try {
		var base = 64;
		var qrContent = [];
		qrContent.push('{');
		qrContent.push('"eID":', '"' + ballotData.electionId + '"');
		qrContent.push(',"eVa":', '"' + leemon.bigInt2str(ballotData.encryptedVote.a, base) + '"');		// 1024 bits
		qrContent.push(',"eVb":', '"' + leemon.bigInt2str(ballotData.encryptedVote.b, base) + '"');		// 1024 bits
		qrContent.push(',"rB":', '"' + leemon.bigInt2str(blindedRandomness, base) + '"');			// 1024 bits
		qrContent.push(',"vk":', '"' + leemon.bigInt2str(ballotData.verifKey.vk, base) + '"');		// 1024 bits
		qrContent.push(',"pC":', '"' + leemon.bigInt2str(leemon.str2bigInt(ballotData.proof.commitment, 10), base) + '"');	// 1024 bits
		qrContent.push(',"pR":', '"' + leemon.bigInt2str(leemon.str2bigInt(ballotData.proof.response, 10), base) + '"');	// 1024 bits
		qrContent.push(',"vS":', '"' + leemon.bigInt2str(ballotData.signature.sig, base) + '"');			//  512 bits (2x 256)
		qrContent.push(',"sT":', '"' + response.signature.timestamp + '"');
		qrContent.push(',"sV":', '"' + leemon.bigInt2str(leemon.str2bigInt(response.signature.value, 10, 1), base) + '"'); //512 bits
		qrContent.push('}');

		// Create qr-code and add data
		//To determine the size see http://blog.qr4.nl/page/QR-Code-Data-Capacity.aspx
		//If size is to small, qr code is not generated
		var qr = qrcode(26, 'L');
		qr.addData(qrContent.join(''));
		qr.make();

		// Create img tag representing the qr-code
		var $qrcode = $(qr.createImgTag());
		// Enlarge the code a little bit, so it can be better read by smart phone
		$qrcode.attr('height', $qrcode.attr('height') * 1.4);
		$qrcode.attr('width', $qrcode.attr('width') * 1.4);
		// Append qr-code

		$(elements.qrcodeHolder).append($qrcode);
		// Go to step 3
		gotoStep3();
		$.unblockUI();

	} catch (e) {
		// Go to step 3 in any case!!
		console.log("ERROR: " + e);
		gotoStep3();
		$.unblockUI();
	}
}
