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
$(document).ready(function(){
	
	// 1. Check cookie support
	if ( !uvUtilCookie.areSupported() ) {
		$.blockUI({
			message: '<div id="browser-check">'+
				'<h2>'+msg.cookieSupport+'</h2>'+
				'<p>'+msg.cookieSupportText+'</p>'+
				'</div>',
			overlayCSS: {opacity: '0.3'}
		});
		return;
	}
	
	
	// 2. Check browser version
	// In the current version, the js file api is not needed. But for checking
	// for an actual version of FF, Safari and Chrome the file api can be used.
	// IE is checked by version as IE9 is fine but does not support the file api. 
	if( ($.browser.msie && $.browser.version < 9)){
	    $.blockUI({
			message: '<div id="browser-check">'+
				'<h2>'+msg.browsercheck1+'</h2>'+
				'<p>'+msg.browsercheck4+'</p><p>'+msg.browsercheck3+'</p>'+
				'</div>',
			overlayCSS: {opacity: '0.2'}
		});
	}
	
	if (
			(!$.browser.msie && !(window.File && window.FileReader && window.FileList && window.Blob))
		) {
		
		$.blockUI({
			message: '<div id="browser-check">'+
				'<h2>'+msg.browsercheck1+'</h2>'+
				'<p>'+msg.browsercheck2+'</p><p>'+msg.browsercheck3+'</p>'+
				'<p style="padding-top: 20px;"><button class="button" onclick="$.unblockUI();">'+msg.close+'</button></p></div>',
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
 * Shows the brief instruction as overlay.
 */
function showBriefInstruction() {
	
	$.blockUI({
		
		message: '<div id="brief-instruction">'+
			'<h2>'+msg.instructionTitle+'</h2>'+
			msg.instructionText+'</div>'+
			'<p><button class="button" onclick="$.unblockUI();">'+msg.close+'</button></p>',
		
		css: {top: '20%', left: '20%', width: '60%'}
		
	});
	
}

/**
 * Shows the support/help box as overlay
 */
function showHelp() {
	
	$.blockUI({
		message: '<div id="help-box">'+
			'<h2>'+msg.helpBoxTitle+'</h2>'+
			'<p>'+msg.helpBoxText+'</p>'+
			'<form action="" name="help"><div><span>'+msg.helpBoxEmail+'</span><input type="text" name="email" id="email"/>'+
			'<span>'+msg.helpBoxMessage+'</span><textarea name="message" id="message"></textarea>'+
			'<span class="tiny">'+msg.helpBoxMessageAdds+'</span>'+
			'<button class="button" onclick="return submitHelpForm($(\'#email\').val(),$(\'#message\').val());">'+msg.helpBoxSubmit+'</button>'+
			'<button class="button" onclick="$.unblockUI(); return false;">'+msg.close+'</button>'+'</div></form>'+
			'</div>',
		
		css: {top: '20%', left: '20%', width: '60%'}
	});
}

/**
 * Submits the help form.
 */
function submitHelpForm(email, message) {
	var dataString = 'email=' + email + '&message=' + message + '&useragent=' + navigator.userAgent;
	$.unblockUI();
	$.blockUI({
		message: '<p>'+msg.helpBoxWait+'</p>'
	});
	  
	$.ajax({
		type: "POST",
		url: "supportRequest.jsp",
		data: dataString,
		dataType: "text",
		success: function() {
			$.unblockUI();
			$.blockUI({
				message: '<p>'+msg.helpBoxSuccess+'</p>',
				timeout: 3000
			});
		},
		error: function() {
			$.unblockUI();
			$.blockUI({
				message: '<p>'+msg.helpBoxError+'</p>',
				timeout: 3000
			});
		}
	});
	return false;
}


/**
 * Retrieves election definition from Board (asynchronously).
 */
function retrieveElections() {

    var queryJson = '{"constraint": [{"@type": "equal","identifier": {"@type": "alphaIdentifier","part": [ "section" ]},"value": {"@type": "stringValue","value": ""}}, {"@type": "equal","identifier": {"@type": "alphaIdentifier","part": [ "group" ]},"value": {"@type": "stringValue","value": "electionDefinition"}}]}';

    $.ajax({
	url: uvConfig.URL_UNIBOARD,
	type: 'POST',
	contentType: "application/json",
	accept: "application/json",
	cache: false,
	dataType: 'json',
	data: queryJson,
	timeout: 10000,
	success: function(resultContainer) {
	    var lang = document.getElementById('language').value.toLowerCase();
	    
	    
	    // Save election data
	    var posts = resultContainer.result.post;
	    for(var index in posts){
		
		//assumes that only one post is retuned
		var message = JSON.parse(B64.decode(posts[index].message));
		var electionId = posts[index].alpha.attribute[0].value.value;
		var now = new Date().getTime();
		if(new Date(message.votingPeriodBegin).getTime() <= now){
		    if (new Date(message.votingPeriodEnd).getTime() >= now){
			//Current election
			$(elements.currentElectionsList).append('<li><span class="votingevent-title">'+getLocalizedText(message.title, lang)+'</span><span class="votingevent-link"><a href="vote.xhtml?electionId='+electionId+'" class="raquo">'+msg.goVote+'</a></span></li>');
		    } else {
			//Past election
			$(elements.pastElectionsList).append('<li><span class="votingevent-title">'+getLocalizedText(message.title, lang)+'</span></li>');
		    }
		} else {
		    //Future election
		    $(elements.currentElectionsList).append('<li><span class="votingevent-title">'+getLocalizedText(message.title, lang)+'</span><span class="votingevent-link"><a class="inactive raquo">'+msg.goVote+'</a></span></li>');
		} 

	    }
  
	    
	    if($(elements.currentElectionsList).find("li").length>0){
		$(elements.loadingElections).hide();
		$(elements.currentElectionsDiv).show(800);
	    }
	    
	    if($(elements.pastElectionsList).find("li").length>0){
		$(elements.loadingElections).hide();
		$(elements.pastElectionsDiv).show(800);
	    }
	    
	    if($(elements.currentElectionsList).find("li").length===0 && $(elements.pastElectionsList).find("li").length===0){
		$(elements.loadingElections).html(msg.noElections);

	    }
	    
	},
	error: function() {
	    $(elements.loadingElections).html(msg.retreiveElectionDefinitionError);
	}
    });

}/**
 * Helper for localized text elements of lists and candidates.
 * 
 * @param localizedTexts - Array of univote_bfh_ch_common_localizedText.
 * @param lang - Current language.
 */
function getLocalizedText(localizedTexts, lang) {
    if (localizedTexts == undefined)
	return '';

    var text = '';
    for (var index in localizedTexts) {
	if (localizedTexts[index].languageCode == lang.toUpperCase()) {
	    text = localizedTexts[index].text;
	    break;
	}
    }
    if (text == '' && localizedTexts.length > 0) {
	text = localizedTexts[0].text;
    }
    return text;

}


