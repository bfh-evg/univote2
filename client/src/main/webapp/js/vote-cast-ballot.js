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
 * This file contains the function for confirming and casting the vote.
 */

/**
 * Holds the vote (as Map: choiceId => count) that is finally to be confirmed 
 * by the voter. 
 */
var confirmMap = null;

/**
 * Holds the ballot data. Used to create a qr-code at the end.
 */
var ballotData = {};
 
/**
 * Initialisation on document ready.
 */
$(document).ready(function(){
	
	// Configure confirm dialog 
	var dialogConfirmButtons = {};
	// Confirm action
	dialogConfirmButtons[msg.confirm] = function(){
		$( this ).dialog( "close" );
		$.blockUI({
			message: '<p id="blockui-processing">'+msg.wait+'.</p>',
			css: {
				width: '40%', 
				left: '30%'
			}
		});
		if ( COMPUTE_ASYNCHRONOUSLY ) {
			setTimeout(finalizeVoteAsync, 1000 );
		} else {
			setTimeout(finalizeVote, 1000 );
		}
	}
	// Cancel action
	dialogConfirmButtons[msg.cancel] = function(){
		dialogs.$confirm.html("");
		confirmMap = null;
		$( this ).dialog( "close" );
	}
	
	dialogs.$confirm.dialog({
		resizable: false,
		draggable:false,
		autoOpen: false,
		height:600,
		width:400,
		closeOnEscape:false,
		modal: true,
		buttons: dialogConfirmButtons,
		open: function(event, ui) {
			$("a.ui-dialog-titlebar-close").remove();
		}
	});

});

/**
 * Submits the vote (triggered by user's click on the submit button).
 * Before the vote is really submitted to the server, the vote is checked against
 * the rules and the user must confirm the final vote.
 */
function submitVote() {
	// Create the final vote for the confirm dialog
	dialogs.$confirm.html($(elements.result).html());
	dialogs.$confirm.find(".buttons, .placeholder, img, #createdlist-footer").remove();
	dialogs.$confirm.find("li").removeClass("movePointer");
	dialogs.$confirm.find("#created-list").css({'min-height': '50px'});
	
	// Place the no-list label if the user has not chosen a list
	if ( dialogs.$confirm.find("#selected-list input").size() == 0 && listsAreSelectable ){
		dialogs.$confirm.find("#selected-list li").html(msg.noListShort).css({'width':'150px'});
		dialogs.$confirm.find("#list-title").html(msg.noListLong);
	}

	// Create the map holding the final vote
	confirmMap = new Map();

	dialogs.$confirm.find("li").each(function(){
		var id = $(this).children("input").val();
		if ( id != undefined ) {
			var occurences = confirmMap.get(id.toString());
			if(occurences === undefined){
				occurences = 1;
			}
			else{
				occurences++;
			}
			confirmMap.put(id, occurences);
		}
	});

	// Check rules: Upper bound
	if(uvUtilRuleControl.checkForAllRules(confirmMap, forAllRules) || uvUtilRuleControl.checkSumRules(confirmMap, sumRules)){
		dialogs.$confirm.html(msg.incorrectResult);
		$('.ui-dialog-buttonpane button').eq(0).button('disable');
	}

	// Check rules: Lower bound
	if(uvUtilRuleControl.checkForAllRulesMin(confirmMap, forAllRules) || uvUtilRuleControl.checkSumRulesMin(confirmMap, sumRules)){
		dialogs.$confirm.html(msg.emptyResult);
		$('.ui-dialog-buttonpane button').eq(0).button('disable');
	}
	
	// Show confirm dialog
	dialogs.$confirm.dialog('open');
}

/**
 * Finalizes the vote. After encoding, encrypting, proofing, and signing the 
 * vote is finally casted. The confirmMap must be filled up prior to calling
 * this function!
 */
function finalizeVote(){
	
	var encodedVote;
	var voteInGq;
	try{
		// 1 - Encode vote (BigInt)
		encodedVote = uvCrypto.encodeVote(confirmMap, choiceIds, forAllRules);
		
		// 2 - represent encodedvote in Gq
		voteInGq = uvCrypto.mapZq2Gq(encodedVote);
		
	} catch(error){
		processExceptionInFinalizeVote(msg.encodeVoteError);   // error.message
		return;
	}

	var ballot = new univote_bfh_ch_common_ballot();
	ballot.setElectionId(electionId);
	ballotData.electionId = electionId;
	
	// 3 - Encrypt vote 
	ballotData.encryptedVote = uvCrypto.encryptVote(voteInGq, encryptionKey);
	ballot.setEncryptedVote(ballot.Data.encryptedVote.encVote);
	
	// 4 - Compute anonymous verification key
	ballotData.verifKey = uvCrypto.computeElectionVerificationKey(electionGenerator, secretKey);
	ballot.setVerificationKey(ballotData.verifKey.vkString);

	// 5 - Generate NIZKP
	ballotData.proof = uvCrypto.computeVoteProof(ballotData.encryptedVote.r, ballotData.encryptedVote.a, ballotData.verifKey.vk);
	ballot.setProof(ballotData.proof.proof);

	// 6 - Generate signature
	ballotData.signature = uvCrypto.signBallot({id: electionId, E: ballotData.encryptedVote.encVote, pi: ballotData.proof.proof}, electionGenerator, secretKey);
	ballot.setSignature(ballotData.signature.sign);

	// 7 - Finally cast vote by calling webservice
	var voteService = new univote_bfh_ch_election_Voting();
	voteService.url = URL_VOTING_SERVICE+'VotingService';
	voteService.synchronous = false;

	voteService.castVote(castVoteSuccessCallback, castVoteErrorCallback, ballot); 
	
}

/**
 * Asynchronous version of finalizeVote.
 */
function finalizeVoteAsync(){
	
	var ballot;
	
	// Update counter and callback
	var updateCounter = 0;
	var updateCb = function() {
		if (++updateCounter % 10 == 0 ){
			$('#blockui-processing').append('.');
		}
	}

	var step3 = function(_voteInGq){
		ballotData.voteInGq = _voteInGq;
		// 3 - Encrypt vote 
		uvCrypto.encryptVoteAsync(ballotData.voteInGq, encryptionKey, step4, updateCb);
	};

	var errorStep3Cb = function() {
		processExceptionInFinalizeVote(msg.encodeVoteError); 
	};
	
	var step4 = function(_encryptedVote) {
		ballotData.encryptedVote = _encryptedVote;
		ballot.setEncryptedVote(ballotData.encryptedVote.encVote);
		// 4 - Compute anonymous verification key
		uvCrypto.computeElectionVerificationKeyAsync(electionGenerator, secretKey, step5, updateCb);
	};
	
	var step5 = function(_verifKey) {
		ballotData.verifKey = _verifKey;
		ballot.setVerificationKey(ballotData.verifKey.vkString);
		// 5 - Generate NIZKP
		uvCrypto.computeVoteProofAsync(ballotData.encryptedVote.r, ballotData.encryptedVote.a, ballotData.verifKey.vk, step6, updateCb);
	};
	
	var step6 = function(_proof) {
		ballotData.proof = _proof;
		ballot.setProof(ballotData.proof.proof);
		// 6 - Generate signature
		uvCrypto.signBallotAsync({id: electionId, E: ballotData.encryptedVote.encVote, pi: ballotData.proof.proof}, electionGenerator, secretKey, step7, updateCb);	
	};
	
	var step7 = function(_signature) {
		ballotData.signature = _signature;
		ballot.setSignature(ballotData.signature.sign);
		
		// 7 - Finally cast vote by calling webservice
		var voteService = new univote_bfh_ch_election_Voting();
		voteService.url = URL_VOTING_SERVICE+'VotingService';
		voteService.synchronous = false;
		voteService.castVote(castVoteSuccessCallback, castVoteErrorCallback, ballot); 
	};
	
	// Starting finalizing asynchronously
	ballot = new univote_bfh_ch_common_ballot();
	ballot.setElectionId(electionId);
	ballotData.electionId = electionId;
	
	try{
		// 1 - Encode vote (BigInt)
		ballotData.encodedVote = uvCrypto.encodeVote(confirmMap, choiceIds, forAllRules);
	} catch(error){
		processExceptionInFinalizeVote(msg.encodeVoteError);   // error.message
		return;
	}	
	
	// 2 - represent encodedvote in Gq
	uvCrypto.mapZq2GqAsync(ballotData.encodedVote, step3, updateCb, errorStep3Cb);
	
}

/**
 * Processes an exception during finalizing the vote. A message is displayed to 
 * the user for about 3s.
 * 
 * @param msgToDisplay - The message to display.
 */
function processExceptionInFinalizeVote( msgToDisplay ) {
	$.unblockUI();
	$.blockUI({
		message: '<p>'+msgToDisplay+'</p>',
		timeout: 3000
	});
}

/**
 * Error callback to handle vote casting faults.
 * 
 *  @param httpStatus - The http status as number.
 *  @param httpStatusText - The http status as text.
 *  @param responseXML - The response from webservice holding the fault as XML object.
 */
function castVoteErrorCallback(httpStatus, httpStatusText, responseXML){
	
	var errorCode = 0;
	if ( responseXML ) {
		// The VotingServiceFault must be parsed manually, because of cxf defact!
		// Important: To get the responseXML passed to this callback, the error
		// callback call must be adapted in cxf-utils and VotingService
		 
		// Get faultstring (currently not used)
		//var faultstrings = responseXML.getElementsByTagName('faultstring');
		//var faultstring = faultstrings.length > 0 ? faultstrings[0].textContent : '';
		
		// Get error code
		if (responseXML.getElementsByTagNameNS ) {
			var errorCodes = responseXML.getElementsByTagNameNS('http://univote.bfh.ch/election','errorCode');
			errorCode = errorCodes.length > 0 ? parseInt(errorCodes[0].textContent, 10) : 0;
		} 
		else { 
			$(responseXML).find("*").each(function() {
				if ( this.tagName.indexOf('errorCode') > 0 ) {
					var $this = $(this);
					errorCode = parseInt($this.text(), 10);
				}
			});
		}
	}
	
	var errorMsg;
	switch( errorCode ) {
		case  3: errorMsg = msg.sendVoteErrorInvalidSignatureError; break;
		case  4: errorMsg = msg.sendVoteErrorInvalidElectionState; break;
		case 16: errorMsg = msg.sendVoteErrorNoEligibleVoter; break;
		case 17: errorMsg = msg.sendVoteErrorAllreadyVoted; break;
		case 18: errorMsg = msg.sendVoteErrorVerificationKeyRevoked; break;
		
		default: errorMsg = msg.sendVoteErrorInternalServerError;
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
 * @param response - The response (univote_bfh_ch_election_castVoteResponse) 
 * from webservice holding the signature (univote_bfh_ch_common_signature).
 */
function castVoteSuccessCallback(response){
	
	var blindedRandomness = 0;
	
	var createQRCode = function() {
		
		// Put content of qr-code together. The content represents an object json 
		// encoded holding the different values of ballot and signature.
		// => Do it manually, so we know exactly what's going on!
		try {
			var base = 64;
			var qrContent = [];
			qrContent.push('{');
			qrContent.push('"eID":',	'"'+ballotData.electionId+'"');											
			qrContent.push(',"eVa":',	'"'+leemon.bigInt2str(ballotData.encryptedVote.a, base)+'"');		// 1024 bit
			qrContent.push(',"eVb":',	'"'+leemon.bigInt2str(ballotData.encryptedVote.b, base)+'"');		// 1024 bit
			qrContent.push(',"rB":',	'"'+leemon.bigInt2str(blindedRandomness, base)+'"'); //  1024 bit
			qrContent.push(',"vk":',	'"'+leemon.bigInt2str(ballotData.verifKey.vk, base)+'"');			// 1024 bit
			qrContent.push(',"pC":',	'"'+leemon.bigInt2str(ballotData.proof.t, base)+'"');				// 1024 bit
			qrContent.push(',"pR":',	'"'+leemon.bigInt2str(ballotData.proof.s, base)+'"');				// 1024 bit
			qrContent.push(',"vSA":',	'"'+leemon.bigInt2str(ballotData.signature.a, base)+'"');			//  256 bit
			qrContent.push(',"vSB":',	'"'+leemon.bigInt2str(ballotData.signature.b, base)+'"');			//  256 bit
			//qrContent.push(',"sSId":',	'"'+response.getSignature().getSignerId()+'"');
			qrContent.push(',"sT":',	'"'+response.getSignature().getTimestamp()+'"');
			qrContent.push(',"sV":',	'"'+leemon.bigInt2str(leemon.str2bigInt(response.getSignature().getValue(), 10, 1), base)+'"');
			qrContent.push('}');
			// Create qr-code and add data
			var qr = qrcode(27, 'L');
			qr.addData(qrContent.join(''));


			// The async mode is used only for IE7/8!!
			qr.makeAsync(function(){
				// Create img tag representing the qr-code
				var $qrcode = $(qr.createImgTag());
				// Enlarge the code a little bit, so it can be better read by smart phone
				$qrcode.attr('height', $qrcode.attr('height') * 1.4);
				$qrcode.attr('width',  $qrcode.attr('width')  * 1.4);
				// Append qr-code
				$(elements.qrcodeHolder).append($qrcode);

				// Go to step 3
				gotoStep3();
				$.unblockUI();
			}, function() {
				// Go to step 3 in any case!!
				gotoStep3();
				$.unblockUI();
			});

		} catch (e) {
			// Go to step 3 in any case!!
			gotoStep3();
			$.unblockUI();
		}
	}
	
	var blindingDone = function(_blindedRandomness){
		blindedRandomness = _blindedRandomness;
		createQRCode();
	}
	
	uvCrypto.blindRandomizationAsync(ballotData.encryptedVote.r, secretKey, blindingDone, function(){});
	
}
