/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniCert.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 *
 * This file contains the certificate request page specific JS.
 *
 */

var FAST = 500;
var SLOW = 1000;
var BLOCK_UI_TIMEOUT = 5000;

var SEPARATOR = uvConfig.CONCAT_SEPARATOR;

var RSA = "RSA";
var DLOG = "DiscreteLog";

/**
 * Holds voter data like id, email
 */
var requester = {};

/**
 * Hold cryptographic properties
 */
var publickey;
var secretKey;
var modulo;
var p, q, g;
var keyType = "";

/**
 * Holds the used DOM elements.
 */
var elements = {};


/*********************************************************************************************************************/
/*                                                 INITIALISATIONS                                                   */
/*********************************************************************************************************************/

/**
 * Initialisation on document ready.
 */
$(function () {

	// Get voter data
	requester.id = document.getElementById('requester-id');
	requester.email = document.getElementById('requester-email');
	requester.idp = document.getElementById('requester-idp');

	// Check if voter.id is available, otherwise user is not authorised
	if (requester.id.value == '') {
		$.blockUI({message: '<p>' + msg.userNotAuthorised + '</p>'});
		setTimeout(function () {
			location.href = uvConfig.HOME_SITE;
		}, BLOCK_UI_TIMEOUT);
		return;
	}

	// Get DOM elements
	elements.step2 = document.getElementById('step_2');
	elements.step3 = document.getElementById('step_3');

	elements.step2content = document.getElementById('step_2_content');
	elements.substep21 = document.getElementById('substep_2_1');
	elements.substep22 = document.getElementById('substep_2_2');
	elements.substep221 = document.getElementById('substep_2_2_1');
	elements.substep23 = document.getElementById('substep_2_3');
	elements.substep24 = document.getElementById('substep_2_4');
	elements.step3content = document.getElementById('step_3_content');

	elements.setup = document.getElementById('setup');
	elements.manualSetup = document.getElementById('manual_setup');
	elements.secretKey = document.getElementById('secretkey_ta');

	elements.password = document.getElementById('password');
	elements.password2 = document.getElementById('password2');
	elements.passwordCheckIcon = document.getElementById('password-check-icon');
	elements.application = document.getElementById('application');
	elements.role = document.getElementById('role');
	elements.cryptoSetupType = document.getElementById('crypto_setup_type');
	elements.cryptoSetupSize = document.getElementById('crypto_setup_size');
	elements.identity_function = document.getElementById('identity_function');

	elements.rsaOptions = document.getElementById('rsa_options');
	elements.dlogOptions = document.getElementById('dlog_options');

	elements.p = document.getElementById('p');
	elements.q = document.getElementById('q');
	elements.g = document.getElementById('g');

	elements.retreiveSecretKeyButton = document.getElementById('retreive_secretkey_bu');
	elements.generateKeyButton = document.getElementById('generate_key_bu');

	//initialize fields
	secretKey = null;
	publicKey = null;
	modulo = null;
	elements.secretKey.value = "";

	//Fills personal fields
	elements.retreiveSecretKeyButton.innerHTML = elements.retreiveSecretKeyButton.innerHTML + ' ' + requester.email.value;

	//Construct with data received
	if (requester.idp.value == "SwitchAAI") {
		elements.identity_function.remove(4);
		elements.identity_function.remove(3);
	} else if (requester.idp.value == "Google") {
		elements.identity_function.remove(2);
		elements.identity_function.remove(1);
		elements.identity_function.remove(0);
	}


	// Register events (button's onclick are registered inline)
	$(elements.secretKey).click(function () {
		this.select();
	});
	$([elements.password, elements.password2]).keyup(function () {
		checkPasswords();
	});
	$(elements.password).keyup(function () {
		checkPwdLength();
	});

	$('#manual_setup select, #manual_setup input').change(function () {
		verifyKeysOptions();
	});



});


function updateSetup() {

	gotoStep21();

	var setup = elements.setup.value;
	if (setup == '') {
		// Nothing to do...
	} else if (setup == 'manually') {
		$(elements.manualSetup).show(SLOW);
		updateKeysOptions();
		verifyKeysOptions();
	} else {

		// Block UI while processing
		$.blockUI({message: '<p id="blockui-processing">' + msg.processing + '.</p>'});
		retrieveData(setup);
	}
}

/**
 * Retrieves parameters set in UniCert (asynchronously).
 * If UniCert is on another domain, IE9 will not be able to retrieve the data
 * IE9 does not support cross domain ajax request.
 * JSONP could be a solution only if it is possible to send session id with it
 *
 * @param params Parameters
 */
function retrieveData(params) {

	var update = setInterval(function () {
		$('#blockui-processing').append('.');
	}, SLOW);

	//For IE
	$.support.cors = true;

	//Ajax request
	$.ajax({
		url: uvConfig.URL_PARAMETERS,
		type: 'POST',
		accept: "application/json",
		cache: false,
		data: "params=" + params,
		timeout: 10000,
		//To send the cookie
		xhrFields: {
			withCredentials: true
		},
		//To send the cookie also in cross domain cases
		crossDomain: true,
		success: function (data) {

			clearInterval(update);

			if (data == null || data == undefined) {
				$.unblockUI();
				$.blockUI({message: '<p>' + msg.dataRetrievalError + '</p>'});
				setTimeout(function () {
					location.href = uvConfig.HOME_SITE;
				}, BLOCK_UI_TIMEOUT);
				return;
			}

			elements.cryptoSetupType.value = data.keyType;
			elements.application.value = data.applicationIdentifier;
			elements.role.value = data.role;
			elements.identity_function.value = "" + data.identityFunctionIndex;

			if (data.keyType == RSA) {
				elements.cryptoSetupSize.value = data.keySize;
			} else if (data.keyType == DLOG) {
				elements.p.value = data.primeP;
				elements.q.value = data.primeQ;
				elements.g.value = data.generator;
			}

			$(elements.substep21).removeClass('active').addClass('done');
			$(elements.substep22).addClass('active');
			elements.generateKeyButton.disabled = false;
			$(elements.substep221).css('opacity', 0.5);
			$.unblockUI();
		},
		error: function (jqXHR, textStatus, errorThrown) {
			clearInterval(update);
			$.unblockUI();
			$.blockUI({message: '<p>' + msg.dataRetrievalError + '</p><p>' + textStatus + ': ' + errorThrown});
			setTimeout(function () {
				location.href = uvConfig.HOME_SITE;
			}, BLOCK_UI_TIMEOUT);
		}
	});

}

/*********************************************************************************************************************/
/*                                                PROCESS FUNCTIONS                                                  */
/*********************************************************************************************************************/


/**
 * Goes to step 2.1
 */
function gotoStep21() {
	$('.substep').removeClass('active done');
	$('#substep_2_1').addClass('active');
	$(elements.manualSetup).hide();
	elements.generateKeyButton.disabled = true;
	elements.password.disabled = true;
	elements.password2.disabled = true;
	elements.retreiveSecretKeyButton.disabled = true;
}

/**
 * Goes to step 2.2
 */
function gotoStep22() {
	$('.substep').removeClass('active done');
	$(elements.substep21).addClass('done');
	$(elements.substep22).addClass('active');
	elements.generateKeyButton.disabled = false;
	$(elements.substep221).css('opacity', 0.5);
	elements.password.disabled = true;
	elements.password2.disabled = true;
	elements.retreiveSecretKeyButton.disabled = true;
}


/**
 * Goes to step 3
 */
function gotoStep3() {
	// Update progress bar
	$(elements.step2).removeClass("active");
	$(elements.step3).addClass("active");

	// Show the certificate content
	$(elements.step2content).addClass("hidden");
	$(elements.step3content).removeClass("hidden");

	// Scroll to top
	window.scrollTo(0, 0);
}


/**
 * Update GUI with the corresponding fields when key type dropdown is changed
 */
function updateKeysOptions() {
	if (elements.cryptoSetupType.value === RSA) {
		$(elements.rsaOptions).show(SLOW);
		$(elements.dlogOptions).hide(SLOW);
	} else if (elements.cryptoSetupType.value === DLOG) {
		$(elements.rsaOptions).hide(SLOW);
		$(elements.dlogOptions).show(SLOW);
	} else {
		$(elements.rsaOptions).hide(SLOW);
		$(elements.dlogOptions).hide(SLOW);
	}
}

elements.application = document.getElementById('application');
elements.role = document.getElementById('role');
elements.cryptoSetupType = document.getElementById('crypto_setup_type');
elements.cryptoSetupSize = document.getElementById('crypto_setup_size');
elements.identity_function = document.getElementById('identity_function');

function verifyKeysOptions() {
	if (elements.cryptoSetupType.value != ''
			&& ((elements.cryptoSetupType.value === RSA && elements.cryptoSetupType.value != '')
					|| (elements.cryptoSetupType.value === DLOG && elements.p.value != '' && elements.q.value != '' && elements.g != ''))
			&& elements.application.value != '' && elements.role.value != '' && elements.identity_function.value != '') {

		gotoStep22();
	} else {
		gotoStep21();
		$(elements.manualSetup).show();
	}
}

/**
 * Generates key pair.
 */
function generateKeyPair() {

	var skBaseTextField = 10;

	// Block UI while processing
	$.blockUI({message: '<p id="blockui-processing">' + msg.processing + '.</p>'});

	var sk = null;

	// Done callback of verification key computation
	var doneCbDlog = function (vk) {
		// Store keys
		secretKey = sk;
		publicKey = vk;

		// Display secret key to the voter and unblock UI
		elements.secretKey.value = leemon.bigInt2str(secretKey, skBaseTextField);


		$.unblockUI();

		// Enable/disable next substep
		$(elements.substep22).removeClass('active').addClass('done');
		$(elements.substep221).animate({opacity: 1}, FAST);
		$(elements.substep23).addClass('active');
		elements.password.disabled = false;
		elements.password2.disabled = false;

	};

	// Done callback of keys computation
	var doneCbRSA = function (keys) {

		secretKey = keys[0];
		publicKey = keys[1];
		modulo = keys[2];

		elements.secretKey.value = leemon.bigInt2str(secretKey, skBaseTextField);

		$.unblockUI();

		// Enable/disable next substep
		$(elements.substep22).removeClass('active').addClass('done');
		$(elements.substep221).animate({opacity: 1}, FAST);
		$(elements.substep23).addClass('active');
		elements.password.disabled = false;
		elements.password2.disabled = false;

	};

	// Update callback of verification key computation
	var updateCb = function () {
		$('#blockui-processing').append('.');
	};

	// Generate the keys
	if (elements.cryptoSetupType.value == RSA) {
		keyType = RSA;
		uvCrypto.generateRSASecretKey(elements.cryptoSetupSize.value, doneCbRSA, updateCb);
	} else if (elements.cryptoSetupType.value == DLOG) {
		if (elements.p.value == "" || elements.q.value == "" || elements.g.value == "") {
			$.unblockUI();
			$.blockUI({message: '<p>' + msg.missingValuePQG + '</p>',
				timeout: BLOCK_UI_TIMEOUT});
			return;
		}
		keyType = DLOG;
		p = leemon.str2bigInt(elements.p.value, 10, 1);
		q = leemon.str2bigInt(elements.q.value, 10, 1);
		g = leemon.str2bigInt(elements.g.value, 10, 1);
		sk = uvCrypto.generateDLOGSecretKey(q);
		// Compute verification key based on secret key (synchronously)
		var key = uvCrypto.computeVerificationKey(p, g, sk);
		doneCbDlog(key);
	}
}

function checkPwdLength() {
	var pw = elements.password.value;
	if (pw.length < 6) {
		$('#pwderror').html(msg.pwdTooShort);
	} else {
		$('#pwderror').html("");
	}
}

/**
 * Helper function to check equality of password and password-check.
 * If the passwords are not empty but equal, the voter gets a feedback
 * (green checkmark) and the next substep is enabled.
 */
function checkPasswords() {
	var pw = elements.password.value;
	var pw2 = elements.password2.value;
	if (pw.length < 6)
		return;

	if (pw == pw2 && pw != '') {
		$(elements.passwordCheckIcon).addClass('ok');
		$(elements.substep23).removeClass('active').addClass('done');
		$(elements.substep24).addClass('active');
		elements.retreiveSecretKeyButton.disabled = false;
	} else {
		$(elements.passwordCheckIcon).removeClass('ok');
		$(elements.substep23).removeClass('done').addClass('active');
		$(elements.substep24).removeClass('active');
		elements.retreiveSecretKeyButton.disabled = true;
	}
}


/**
 * Completes the certificate request.
 * (1) Computes verification key proof or the siganture depending on the chosen key type
 * (2) sends verification key and some other data to UniCert and in response get the certificate
 * (3) the secret key is handed out (encrypted) to the voter either by file download or by mail.
 *
 *  @param byMail - If true, the secret key is passed by mail, otherwise by file download to the voter.
 */
function completeCertRequest(byMail) {

	byMail = byMail || false;
	var pw = elements.password.value;

	// Done callback of certificate creation
	var createCertDoneCb = function (cert) {
		// Store cert
		certificate = cert;

		var skC;

		if (keyType == RSA) {
			// (3) Hand out secret key to the voter (size one time pad = size(n) + PRE/POST-Fix)
			var sk = leemon.bigInt2str(secretKey, 64);
			// encrypt secret key with users password
			skC = uvCrypto.encryptSecretKey(sk, pw);
		} else if (keyType == DLOG) {

			// (3) Hand out secret key to the voter
			var sk = leemon.bigInt2str(secretKey, 64);
			// encrypt secret key with users password
			skC = uvCrypto.encryptSecretKey(sk, pw);
		}


		if (byMail) {
			// Send secret key to the voter by mail (asynchronous)
			retreiveSecretKeyByMail(
					skC,
					function () {
						// Done -> go to step 3
						$.unblockUI();
						gotoStep3();
					},
					function () {
						// Error
						$.unblockUI();
						$.blockUI({
							message: '<p>' + msg.sendSecretKeyFailed + '</p>',
							timeout: BLOCK_UI_TIMEOUT});
					});

		} else {
			// File download of secret key
			retreiveSecretKeyBySaveas(skC);
			// finally go to step 3
			$.unblockUI();
			gotoStep3();
		}

	};

	// Error callback fo certificate creation
	var createCertErrorCb = function (request, status, error) {

		var message = "";

		try {
			var json = JSON.parse(request.responseText);
			if (json.error != "") {
				message = eval(window["msg.error" + json.error]);
			} else {
				message = msg.errorundefined;
			}
		} catch (err) {
			message = msg.errorundefined;
		}


		$.unblockUI();
		$.blockUI({
			message: '<p>' + msg.createCertificateFailed + " " + message + '</p>',
			timeout: BLOCK_UI_TIMEOUT});
	};

	// Update callback of verification key proof computation
	var computeUpdateCb = function () {
		$('#blockui-processing').append('.');
	};

	// (1) Compute verification key proof / signature
	var valuesToSign = requester.idp.value + SEPARATOR + requester.email.value + SEPARATOR + requester.id.value;
	valuesToSign = valuesToSign + SEPARATOR + elements.cryptoSetupType.value + SEPARATOR + elements.cryptoSetupSize.value;
	valuesToSign = valuesToSign + SEPARATOR + leemon.bigInt2str(publicKey, 10);

	$.blockUI({message: '<p id="blockui-processing">' + msg.processing + '...</p>'});
	if (keyType == RSA) {
		valuesToSign = valuesToSign + SEPARATOR + leemon.bigInt2str(modulo, 10);
		valuesToSign = valuesToSign + SEPARATOR + elements.identity_function.value + SEPARATOR + elements.application.value + SEPARATOR + elements.role.value;
		var signature = uvCrypto.computeRSASignature(secretKey, publicKey, modulo, valuesToSign);
		// (2) Send verification key to CA and get the certificate
		createRSACertificate(elements.cryptoSetupSize.value, modulo, elements.identity_function.value, publicKey, leemon.bigInt2str(signature, 10),
				elements.application.value, elements.role.value, createCertDoneCb, createCertErrorCb, computeUpdateCb);
	} else if (keyType == DLOG) {
		valuesToSign = valuesToSign + SEPARATOR + leemon.bigInt2str(p, 10) + SEPARATOR + leemon.bigInt2str(q, 10) + SEPARATOR + leemon.bigInt2str(g, 10);
		valuesToSign = valuesToSign + SEPARATOR + elements.identity_function.value + SEPARATOR + elements.application.value + SEPARATOR + elements.role.value;
		var proof = uvCrypto.computeVerificationKeyProof(p, q, g, secretKey, publicKey, valuesToSign);
		// (2) Send verification key to CA and get the certificate
		createDLogCertificate(elements.cryptoSetupSize.value, p, q, g, elements.identity_function.value, publicKey, proof,
				elements.application.value, elements.role.value, createCertDoneCb, createCertErrorCb, computeUpdateCb);
	}
}



/*********************************************************************************************************************/
/*                                          CERTIFICATE REQUEST FUNCTIONS                                            */
/*********************************************************************************************************************/
/**
 * If UniCert is on another domain, IE9 will not be able to send the certificate request over ajax
 * IE9 does not support cross domain ajax request.
 * JSONP is not usable here since a POST HTTP request must be send, and JSONP only supports HTTP GET
 */

/**
 * Creates an RSA certificate by sending (asynchronously) data to UniCert
 * @param csSize Size of RSA key
 * @param rsaModulo Modulo for RSA
 * @param identityFunction Identity function to apply to identity data prior to put it in the certificate
 * @param publicKey Public key to certify
 * @param signature Signature proving knowledge of the private key
 * @param applicationIdentifier Application the certificate is issued for
 * @param role Role which the certificate must be issued for
 * @param doneCb Code to execute after successful certificate issuance
 * @param errorCb Code to execute on error
 * @param updateCb Callback for updates
 * @returns nothing: calls doneCb passing the JSON representation of the certificate
 */
function createRSACertificate(csSize, rsaModulo, identityFunction, publicKey, signature, applicationIdentifier,
		role, doneCb, errorCb, updateCb) {
	// Verification key base 10 encoded
	//var vkStr = computeBase64(vk);
	var pkStr = leemon.bigInt2str(publicKey, 10);
	var rsaModuloStr = leemon.bigInt2str(rsaModulo, 10);

	var update = setInterval(updateCb, SLOW);

	// Success callback for ajax request. Parses the received data
	// expecting a list of certificates with voter's certficate at the top.
	var successCb = function (data) {
		clearInterval(update);

		var cert = parseCertificate(data);
		if (cert) {
			doneCb(cert);
		} else {
			errorCb();
		}
	};

	var errCb = function (data) {
		clearInterval(update);
		errorCb();
	};

	//For IE
	$.support.cors = true;

	// Ajax request
	$.ajax({
		type: "POST",
		url: uvConfig.URL_UNICERT_CERTIFICATE_AUTHORITY,
		//To send the cookie
		xhrFields: {
			withCredentials: true
		},
		//To send the cookie also in cross domain cases
		crossDomain: true,
		data: {'crypto_setup_type': RSA, 'crypto_setup_size': csSize, 'rsa_modulo': rsaModuloStr,
			'identity_function': identityFunction, 'public_key': pkStr, 'signature': signature,
			'application_identifier': applicationIdentifier, 'role': role},
		dataType: 'json',
		success: successCb,
		error: errCb
	});
}


/**
 * Creates a Discrete Log certificate by sending (asynchronously) data to UniCert
 * @param csSize Size of dlog key
 * @param {type} dlogPrimeP Prime number p
 * @param {type} dlogPrimeQ Prime number q
 * @param {type} dlogGenerator Generator of cyclic group
 * @param identityFunction Identity function to apply to identity data prior to put it in the certificate
 * @param publicKey Public key to certify
 * @param proof Proof of knowledge of the private key
 * @param applicationIdentifier Application the certificate is issued for
 * @param role Role which the certificate must be issued for
 * @param doneCb Code to execute after successful certificate issuance
 * @param errorCb Code to execute on error
 * @param updateCb Callback for updates
 * @returns nothing: calls doneCb passing the JSON representation of the certificate
 */
function createDLogCertificate(csSize, dlogPrimeP, dlogPrimeQ, dlogGenerator, identityFunction, publicKey, proof, applicationIdentifier,
		role, doneCb, errorCb, updateCb) {

	// Verification key base64 encoded
	var pkStr = leemon.bigInt2str(publicKey, 10);
	var pStr = leemon.bigInt2str(dlogPrimeP, 10);
	var qStr = leemon.bigInt2str(dlogPrimeQ, 10);
	var gStr = leemon.bigInt2str(dlogGenerator, 10);

	var update = setInterval(updateCb, SLOW);

	// Success callback for ajax request. Parses the received data
	// expecting a list of certificates with voter's certficate at the top.
	var successCb = function (data) {
		clearInterval(update);

		var cert = parseCertificate(data);
		if (cert) {
			doneCb(cert);
		} else {
			errorCb();
		}
	};

	var errCb = function (data) {
		clearInterval(update);
		errorCb();
	};

	//For IE
	$.support.cors = true;

	//Ajax request
	$.ajax({
		type: "POST",
		url: uvConfig.URL_UNICERT_CERTIFICATE_AUTHORITY,
		//To send the cookie
		xhrFields: {
			withCredentials: true
		},
		//To send the cookie also in cross domain cases
		crossDomain: true,
		data: {'crypto_setup_type': DLOG, 'crypto_setup_size': csSize, 'dlog_p': pStr, 'dlog_q': qStr, 'dlog_generator': gStr,
			'identity_function': identityFunction, 'public_key': pkStr, 'dlog_proof_commitment': proof.t, 'dlog_proof_challenge': proof.c, 'dlog_proof_response': proof.s,
			'application_identifier': applicationIdentifier, 'role': role},
		dataType: 'json',
		success: successCb,
		error: errCb
	});
}

/**
 * Parses a json certificate as is is received from the CA and
 * returns it.
 *
 * @param data - List of certificats as json object.
 * @return the certificate
 */
function parseCertificate(data) {
	if (!data) {
		return null;
	}
	var cert = data;

	return cert;
}


/**
 * Hands out the secret key through file download (saveas).
 *
 * @param skC - Encrypted secret key.
 */
function retreiveSecretKeyBySaveas(skC) {
	downloadFile(skC, 'UniVoteKey.txt');
}

/**
 * Hands out the secret key by mail
 *
 * @param skC - Encrypted secret key.
 * @param doneCb - Done callback.
 * @param errorCb - Error callback
 */
function retreiveSecretKeyByMail(skC, doneCb, errorCb) {

	// Success callback of sending secret key.
	// data holds a message and the to-address (data.message, data.to)
	var successCb = function (data) {
		// Right now just call the done callback
		doneCb();
	};

	// Ajax call to send the secret key by mail.
	$.ajax({
		type: "POST",
		url: 'sendSecretKey.jsp',
		data: {sk: skC, to: requester.email.value, to2: document.getElementById("alternate_mail").value, appid: elements.application.value, role: elements.role.value, idp: requester.idp.value, pem: certificate.pem},
		dataType: 'json',
		success: successCb,
		error: errorCb
	});
}

/**
 * Inspects the cert. Blocks the UI and displays the content of the cert.
 */
function inspectCert() {

	var cert = certificate;
	var html = '<div id="certificate-viewer"><h2>' + msg.yourCertificate + '</h2><table>';
	html += '<tr><th>' + msg.certCanonicalName + '</th><td>' + cert.commonName + '</td></tr>';
	html += '<tr><th>' + msg.certOrganization + '</th><td>' + cert.organisation + '</td></tr>';
	html += '<tr><th>' + msg.certOrganizationUnit + '</th><td>' + cert.organisationUnit + '</td></tr>';
	html += '<tr><th>' + msg.certIssuer + '</th><td>' + cert.issuer + '</td></tr>';
	html += '<tr><th>' + msg.certSerialNumber + '</th><td>' + cert.serialNumber + '</td></tr>';
	html += '<tr><th>' + msg.certValidFrom + '</th><td>' + cert.validFrom + '</td></tr>';
	html += '<tr><th>' + msg.certValidUntil + '</th><td>' + cert.validUntil + '</td></tr>';
	//html += '<tr><th>'+msg.certFpSha1+'</th><td>'+cert.fpSha1+'</td></tr>';
	html += '<tr><th>' + msg.certPem + '</th><td>' + cert.pem + '</td></tr>';
	html += '</table>';
	html += '<p><button class="button radius" onclick="$.unblockUI()">' + msg.close + '</button> &nbsp; ';
	html += '<button class="button radius" onclick="downloadCertificate()">' + msg.certDownload + '</button></p></div>';

	$.blockUI({
		message: html,
		css: {width: '60%', left: '20%', top: '50px'}
	});
}

/**
 * Function to download the certificate
 */
function downloadCertificate() {
	var cert = certificate;
	downloadFile(cert.pem, 'UniVoteCertificate.pem');
}

/**
 * Helper function to download a file. As currently not all browser
 * support saveas, a server roundtrip might be done.
 *
 * @param data - The data to download.
 * @param filename - The filename.
 */
function downloadFile(data, filename) {
	if (window.saveas) {
		// Do it straight forward if saveas is supported
		var keyBlob = new BlobBuilder();
		keyBlob.append(data);
		window.saveAs(keyBlob.getBlob(), filename);
	} else {
		// Do a server roundtrip if saveas is not supported
		// -> create a form and submit it
		var form = document.createElement('form');
		form.style.display = 'none';
		form.method = 'post';
		form.action = 'saveas.jsp';

		var n = document.createElement('input');
		n.type = 'hidden';
		n.name = 'name';
		n.value = filename;
		form.appendChild(n);

		var d = document.createElement('input');
		d.type = "hidden";
		d.name = 'data';
		d.value = data;
		form.appendChild(d);

		document.body.appendChild(form);
		form.submit();
	}
}


// Holds the blob builder
var BlobBuilder = window.BlobBuilder || window.WebKitBlobBuilder || window.MozBlobBuilder || window.MSBlobBuilder;

// window.saveAs
// Shims the saveAs method, using saveBlob in IE10.
// And for when Chrome and FireFox get round to implementing saveAs we have their vendor prefixes ready.
// But otherwise this creates a object URL resource and opens it on an anchor tag which contains the "download" attribute (Chrome)
// ... or opens it in a new tab (FireFox)
// @author Andrew Dodson
// @copyright MIT, BSD. Free to clone, modify and distribute for commercial and personal use.
window.saveAs || (window.saveAs = (window.navigator.msSaveBlob ? function (b, n) {
	return window.navigator.msSaveBlob(b, n);
} : false) || window.webkitSaveAs || window.mozSaveAs || window.msSaveAs || (function () {


	// URL's
	window.URL || (window.URL = window.webkitURL);

	if (!window.URL || !("download" in document.createElement('a'))) {
		return false;
	}

	return function (blob, name) {
		var url = URL.createObjectURL(blob);

		// Test for download link support
		if ("download" in document.createElement('a')) {

			var a = document.createElement('a');
			a.setAttribute('href', url);
			a.setAttribute('download', name);

			// Create Click event
			var clickEvent = document.createEvent("MouseEvent");
			clickEvent.initMouseEvent("click", true, true, window, 0,
					event.screenX, event.screenY, event.clientX, event.clientY,
					event.ctrlKey, event.altKey, event.shiftKey, event.metaKey,
					0, null);

			// dispatch click event to simulate download
			a.dispatchEvent(clickEvent);
		}
		else {
			// fallover, open resource in new tab.
			window.open(url, '_blank', '');
		}
	};
})());

