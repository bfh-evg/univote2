// Foundation JavaScript
// Documentation can be found at: http://foundation.zurb.com/docs
$(function () {
	$(document).foundation();
});



/**
 * Shows the brief instruction as overlay.
 */
function showBriefInstruction() {

	$.blockUI({
		message: '<div id="brief-instruction">' +
				'<h2>' + msg.instructionTitle + '</h2>' +
				msg.instructionText + '</div>' +
				'<p><button class="button radius" onclick="$.unblockUI();">' + msg.close + '</button></p>',
		css: {top: '20%', left: '20%', width: '60%'}

	});
	return false;
}

/**
 * Shows the support/help box as overlay
 */
function showHelp() {

	$.blockUI({
		message: '<div id="help-box">' +
				'<h2>' + msg.helpBoxTitle + '</h2>' +
				'<p>' + msg.helpBoxText + '</p>' +
				'<form action="" name="help"><div><span>' + msg.helpBoxEmail + '</span><input type="email" name="email" id="email"/>' +
				'<span>' + msg.helpBoxMessage + '</span><textarea name="message" id="message"></textarea>' +
				'<span class="tiny">' + msg.helpBoxMessageAdds + '</span>' +
				'<button class="button radius" onclick="$.unblockUI(); return false;">' + msg.cancel + '</button>' +
				'<button class="button radius" onclick="return submitHelpForm();">' + msg.helpBoxSubmit + '</button>' + '</div></form>' +
				'</div>',
		css: {top: '20%', left: '20%', width: '60%'}
	});

	return false;
}

/**
 * Submits the help form.
 */
function submitHelpForm() {
	var $email = $('#email');
	var $message = $('#message');
	if ($email.val() == '') {
		$email.addClass('required');
		$message.removeClass('required');
		return false;
	}
	if ($message.val() == '') {
		$message.addClass('required');
		$email.removeClass('required');
		return false;
	}

	var dataString = 'email=' + $email.val() + '&message=' + $message.val() + '&useragent=' + navigator.userAgent;
	$.unblockUI();
	$.blockUI({
		message: '<p>' + msg.helpBoxWait + '</p>'
	});
	$.ajax({
		type: "POST",
		url: "supportRequest.jsp",
		data: dataString,
		dataType: "text",
		crossDomain: true,
		success: function () {
			$.unblockUI();
			$.blockUI({
				message: '<p>' + msg.helpBoxSuccess + '</p>',
				timeout: 3000
			});
		},
		error: function () {
			$.unblockUI();
			$.blockUI({
				message: '<p>' + msg.helpBoxError + '</p>',
				timeout: 3000
			});
		}
	});
	return false;
}