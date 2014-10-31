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
 *
 * The crypto object providing the cryptographic functions for:
 *
 *    - Secret key and verification key generation
 *    - Secret key enc- and decryption
 *    - Signature verification of election data
 *    - Vote encoding and ballot creation (encryption, proofing, signing)
 *
 * Most functions are implemented in two versions: Synchronous and asynchronous.
 * The asynchronous implementation is needed for older, slower browsers; they
 * might be removed ones in the future.
 *
 */


(function(window) {

    // Check for leemon and seedrandom library. If the libraries aren't loaded,
    // an error message is displayed at the top of the page.
    if (!leemon || !Math.seedrandom) {
	window.onload = function() {
	    var body = document.getElementsByTagName('body')[0];
	    var errorDiv = document.createElement('div');
	    errorDiv.setAttribute('style', 'background-color:red; z-index:1000; position:absolute; top:0; left: 0; width: 100%; height:50px; text-align:center; font-weight:bold; padding-top: 20px;');
	    errorDiv.innerHTML = "<p>ERROR: Missing JS library! UNI-Vote won't be running as either leemon or seedrandom is missing.</p>";
	    body.appendChild(errorDiv);
	}
	return;
    }


    /**
     * Constructor.
     */
    function Crypto() {

	////////////////////////////////////////////////////////////////////////
	// Configuration

	window.uvConfig = window.uvConfig || {};

	//TODO clean what is no more needed
	// Signs used for concat.
	var CONCAT_SEPARATOR = uvConfig.CONCAT_SEPARATOR || "|";
	var CONCAT_DELIMINATOR_L = uvConfig.CONCAT_DELIMINATOR_L || "(";
	var CONCAT_DELIMINATOR_R = uvConfig.CONCAT_DELIMINATOR_R || ")";

	// Pre- and postfix used for secret key padding. Important: As the padded
	// secret key is converted into a bigInt only leemon's base64 charset can
	// be used (0-9, A-Z, a-z, _ and =)
	var PRIVATE_KEY_PREFIX = uvConfig.PRIVATE_KEY_PREFIX || "=====BEGIN_UNIVOTE_PRIVATE_KEY=====";
	var PRIVATE_KEY_POSTFIX = uvConfig.PRIVATE_KEY_POSTFIX || "=====END_UNIVOTE_PRIVATE_KEY=====";

	// Pre- and postfix used for padding the encrypted secret key.
	var ENC_PRIVATE_KEY_PREFIX = uvConfig.ENC_PRIVATE_KEY_PREFIX || "-----BEGIN UNIVOTE ENCRYPTED VOTING KEY-----";
	var ENC_PRIVATE_KEY_POSTFIX = uvConfig.ENC_PRIVATE_KEY_POSTFIX || "-----END UNIVOTE ENCRYPTED VOTING KEY-----";

	// IMPORTANT: (size of q) + (size of pre- and postfix) = 256 + 411 < 1024
	var PRIVATE_KEY_ONE_TIME_PAD_SIZE = uvConfig.PRIVATE_KEY_ONE_TIME_PAD_SIZE || 1024;

	// Base refers only to the bigInt representation of Schnorr, Elgamal and RSA parameters.
	var base = uvConfig.BASE || 10;

	// Schnorr
	uvConfig.SCHNORR = uvConfig.SCHNORR || {};
	var schnorr = {};
	schnorr.pStr = uvConfig.SCHNORR.P || "161931481198080639220214033595931441094586304918402813506510547237223787775475425991443924977419330663170224569788019900180050114468430413908687329871251101280878786588515668012772798298511621634145464600626619548823238185390034868354933050128115662663653841842699535282987363300852550784188180264807606304297";
	schnorr.qStr = uvConfig.SCHNORR.Q || "65133683824381501983523684796057614145070427752690897588060462960319251776021";
	schnorr.gStr = uvConfig.SCHNORR.G || "109291242937709414881219423205417309207119127359359243049468707782004862682441897432780127734395596275377218236442035534825283725782836026439537687695084410797228793004739671835061419040912157583607422965551428749149162882960112513332411954585778903685207256083057895070357159920203407651236651002676481874709";

	schnorr.p = leemon.str2bigInt(schnorr.pStr, base, 1);
	schnorr.q = leemon.str2bigInt(schnorr.qStr, base, 1);
	schnorr.g = leemon.str2bigInt(schnorr.gStr, base, 1);

	// Elgamal
	uvConfig.ELGAMAL = uvConfig.ELGAMAL || {};
	var elgamal = {};
	elgamal.pStr = uvConfig.ELGAMAL.P || "127557310857026250526155290716175721659501699151591799276600227376716505297573619294610035498965642711634086243287869889860211239877645998908773071410481719856828493012051757158513651215977686324747806475706581177754781891491034188437985448668758765692160128854525678725065063346126289455727622203325341952627";
	elgamal.qStr = uvConfig.ELGAMAL.Q || "63778655428513125263077645358087860829750849575795899638300113688358252648786809647305017749482821355817043121643934944930105619938822999454386535705240859928414246506025878579256825607988843162373903237853290588877390945745517094218992724334379382846080064427262839362532531673063144727863811101662670976313";
	elgamal.gStr = uvConfig.ELGAMAL.G || "4";

	elgamal.p = leemon.str2bigInt(elgamal.pStr, base, 1);
	elgamal.q = leemon.str2bigInt(elgamal.qStr, base, 1);
	elgamal.g = leemon.str2bigInt(elgamal.gStr, base, 1);

	// RSA
	uvConfig.RSA = uvConfig.RSA || {};
	var rsa = {};
	rsa.nStr = uvConfig.RSA.N || "143";
	rsa.pkStr = uvConfig.RSA.PK || "23";

	rsa.n = leemon.str2bigInt(rsa.nStr, base, 1);
	rsa.pk = leemon.str2bigInt(rsa.pkStr, base, 1);


	/**
	 * Sets the Elgamal parameters at runtime.
	 *
	 * @param pStr - P as string.
	 * @param qStr - Q as string.
	 * @param gStr - Generator as string.
	 * @param base - The base P, Q and G are represented in.
	 */
	this.setElgamalParameters = function(pStr, qStr, gStr, base) {
	    elgamal.p = leemon.str2bigInt(pStr, base, 1);
	    elgamal.q = leemon.str2bigInt(qStr, base, 1);
	    elgamal.g = leemon.str2bigInt(gStr, base, 1);
	}

	/**
	 * Sets the Elgamal parameters at runtime.
	 *
	 * @param pStr - P as string.
	 * @param qStr - Q as string.
	 * @param gStr - Generator as string.
	 * @param base - The base P, Q and G are represented in.
	 */
	this.setSignatureParameters = function(pStr, qStr, gStr, base) {
	    schnorr.p = leemon.str2bigInt(pStr, base, 1);
	    schnorr.q = leemon.str2bigInt(qStr, base, 1);
	    schnorr.g = leemon.str2bigInt(gStr, base, 1);
	}

	////////////////////////////////////////////////////////////////////////
	// Non-interactive zero-knowledge proof

	/**
	 * Computes a non-interactive zero-knowledge proof.
	 *
	 * @param system - The system (either Schnorr or Elgamal).
	 * @param secretInput - The secret input as bigInt.
	 * @param publicInput - The public input as bigInt.
	 * @param otherInput - Some other input as bigInt or string.
	 * @return Proof as object containing t (commitment), c (challange) and s (response) as bigInt.
	 */
	this.NIZKP = function(p, q, g, secretInput, publicInput, otherInput) {

	    //1. Choose omega at random from Zq
	    var omega = leemon.randBigIntInZq(q);

	    //2. Compute t = g^omega mod p
	    var t = leemon.powMod(g, omega, p);

	    //3. Compute c = H(H(H(publicInput)||H(t))||H(otherInput))
	    //3.1 Hash of public input
	    var hashPI = sha256BigInt(publicInput);
	    //3.2 Hash of commitment
	    var hashCommitment = sha256BigInt(t);
	    //3.3 Hash of the hash of public input concatenated with hash of commitment
	    //(Steps 3.1 to 3.3 are the computation of to the recursive hash of a Pair[publicInput, Commitment] in UniCrypt)
	    var hashPIAndCommitment = sha256HexStr(hashPI + hashCommitment);
	    //3.4 Hash of other input
	    var hashOtherInput = sha256String(otherInput);
	    //3.5 Hash of hashPIAndCommitment concatenated with hashOtherInput
	    //(Steps 3.1 to 3.5 are the computation of to the recursive hash of a Pair[Pair[publicInput, Commitment], otherInput] in UniCrypt)
	    var cStr = sha256HexStr(hashPIAndCommitment + hashOtherInput);
	    var c = leemon.mod(leemon.str2bigInt(cStr, 16, 1), q);

	    //4. Compute s = omega+c*secretInput mod q
	    var s = leemon.mod(leemon.add(omega, leemon.multMod(c, secretInput, q)), q);

	    // 5. Return proof
	    return {t: t, c: c, s: s};
	}

	/**
	 * Asynchronous version of NIZKP.
	 **/
	this.NIZKPAsync = function(p, q, g, secretInput, publicInput, otherInput, doneCb, updateCb) {

	    // step 2
	    var step2 = function(_t) {

		var t = _t;
		//3. Compute c = H(H(H(publicInput)||H(t))||H(otherInput))
		//3.1 Hash of public input
		var hashPI = sha256BigInt(publicInput);

		//3.2 Hash of commitment
		var hashCommitment = sha256BigInt(t);

		//3.3 Hash of the hash of public input concatenated with hash of commitment
		//(Steps 3.1 to 3.3 are the computation of to the recursive hash of a Pair[publicInput, Commitment] in UniCrypt)
		var hashPIAndCommitment = sha256HexStr(hashPI + hashCommitment);

		//3.4 Hash of other input
		var hashOtherInput = sha256String(otherInput);
		//3.5 Hash of hashPIAndCommitment concatenated with hashOtherInput
		//(Steps 3.1 to 3.5 are the computation of to the recursive hash of a Pair[Pair[publicInput, Commitment], otherInput] in UniCrypt)
		var cStr = sha256HexStr(hashPIAndCommitment + hashOtherInput);
		var c = leemon.mod(leemon.str2bigInt(cStr, 16, 1), q);
		//4. Compute s = omega+c*secretInput mod q
		var s = leemon.mod(leemon.add(omega, leemon.multMod(c, secretInput, q)), q);

		// 5. Call callback with proof
		doneCb({t: t, c: c, s: s});
	    }


	    // Start with step 1
	    //1. Choose omega at random from Zq
	    var omega = leemon.randBigIntInZq(q);


	    //2. Compute t = g^omega mod p
	    leemon.powModAsync(g, omega, p, updateCb, step2);
	}


	/**
	 * Blind the randomization (r) used during ballot-encryption by blinding it with the secret key (sk)
	 * 
	 * @param  r - The randomization used during ballot-encryption as BigInt
	 * @param  sk - The private key part of the verification key as BigInt
	 * @returns Blinded randomization as BigInt
	 */
	this.blindRandomization = function(r, sk) {

	    //1. map randomization into g_q
	    var rGq = this.mapZq2Gq(r);
	    //2. blind the mapped randomization with sk. This can be unblinded by rB^{-sk} = r^{sk/sk} = r
	    var rB = leemon.powMod(rGq, sk, elgamal.p);
	    //3. return the blinded
	    return rB;
	}

	/**
	 * Asynchronous version of blindRandomization.
	 */
	this.blindRandomizationAsync = function(r, sk, doneCb, updateCb) {

	    var step2 = function(_rGq) {
		//2. blind the mapped randomization with sk. This can be unblinded by rB^{-sk} = r^{sk/sk} = r
		leemon.powModAsync(_rGq, sk, elgamal.p, updateCb, doneCb);
	    }
	    var errorCb = function(message) {
		//Schould never happen! 
		doneCb(leemon.str2bigInt("0", 10, 1));
	    }
	    //1. map randomization into g_q
	    this.mapZq2GqAsync(r, step2, updateCb, errorCb);
	}

	////////////////////////////////////////////////////////////////////////
	// Secret key enc- and decryption
	/**
	 * Encrypts a secret key. The key is padded with PRIVATE_KEY_PREFIX/-POSTFIX
	 * before it is encrypted using a one-time-pad (of the size PRIVATE_KEY_ONE_TIME_PAD_SIZE).
	 * The one-time-pad is a random number using the password as seed.
	 * Finally, the encrypted key (represented as string in leemon's base 64) is
	 * padded with ENC_PRIVATE_KEY_PREFIX/-POSTFIX.
	 *
	 * IMPORTANT: Step 3 and 4 MUST be synchronized (mutex lock). As univote-random
	 * is collecting data all the time in the background and may seed rng. Currently,
	 * as long as JS is single threaded, the synchronization is implicitly given.
	 *
	 * @param sk - The secret key as string.
	 * @param password - The password used for encryption.
	 * @return Encrytped and padded secret key as string.
	 */
	this.encryptSecretKey = function(sk, password) {

	    // 1. Add pre- and postfix to key
	    var key = PRIVATE_KEY_PREFIX + sk + PRIVATE_KEY_POSTFIX;

	    // 2. Convert key into bigInt
	    key = leemon.str2bigInt(key, 64, 0);

	    //3. Create a salt of exactly 128 bits
	    var salt;
	    do {
		salt = leemon.randBigInt(128);
	    } while (leemon.bitSize(salt) != 128)
	    salt = leemon.bigInt2str(salt, 64)

	    // 4. Seed rng with password and salt (save current RNG temporary to not
	    // lose accumulated data for future randomness)
	    var cRNG = Math.random;
	    Math.seedrandom(password + "" + salt);

	    // 5. Get one-time-pad and reassign old rng
	    //compute the size of the pre/postfixed key
	    var keyLength = leemon.bitSize(key);
	    //compute the required size for one time pad, we want it to be a multiple of 16, in order
	    //to be able to recover more easily the same size for decryption
	    var oneTimePadSize = keyLength + (16 - keyLength % 16) + PRIVATE_KEY_ONE_TIME_PAD_PREPOSTFIX_SIZE;
	    var r = leemon.randBigInt(oneTimePadSize);
	    Math.random = cRNG;

	    // 6. Encrypt key using one-time-pad
	    var keyC = leemon.xor(key, r);
	    // 7. Convert key to string with base 64
	    keyC = leemon.bigInt2str(keyC, 64);
	    // 8. Pad encrypted key with pre- and postfix and add salt
	    keyC = ENC_PRIVATE_KEY_PREFIX + '\n' + salt + keyC + '\n' + ENC_PRIVATE_KEY_POSTFIX;
	    // 9. Return encrypted and padded key
	    return keyC;
	}

	/**
	 * Decrypts an encrypted secret key (counterpart to encryptSecretKey).
	 * If the key is not properly padded or the password is wrong then
	 * the error callback is called with a string denoting the error.
	 *
	 * IMPORTANT: The complete step 2 MUST be synchronized (mutex lock). As univote-random
	 * is collecting data all the time in the background and may seed rng. Currently,
	 * as long as JS is single threaded, the synchronization is implicitly given.
	 *
	 * @param key - Encrypted and padded secret key as string.
	 * @param password - The password used for encryption.
	 * @param errorCb - Callback to notify errors (type of error is passed as string).
	 * @return Secret key as bigInt.
	 */
	this.decryptSecretKey = function(key, password, errorCb) {

	    // Cleans a string (removes all special charaters but =, -, _)
	    function cleanStr(str) {
		return str.replace(/[^\w=_\-]/gi, '');
	    }


	    // 1. Check and erase pre- and postfix
	    // -> even \n and \r should be included in \s, only \s does not work!!!
	    var pattern = new RegExp(cleanStr(ENC_PRIVATE_KEY_PREFIX) + "([0-9A-Za-z=_]*)" + cleanStr(ENC_PRIVATE_KEY_POSTFIX));
	    var match = pattern.exec(cleanStr(key));
	    if (match == null || match.length != 2) {
		errorCb('invalidUploadedKey');
		return false;
	    }

	    var keyC = match[1];

	    //2. extract salt (128 bits => 22 base64 chars)
	    var salt = keyC.substring(0, 22);
	    //salt = leemon.str2bigInt(salt, 64, 0);
	    keyC = keyC.substring(22);

	    // 3. Decrypt key with password
	    keyC = leemon.str2bigInt(keyC, 64, 0);
	    // Save current RNG temporary to not lose accumulated data for future randomness
	    var cRNG = Math.random;
	    Math.seedrandom(password + "" + salt);
	    //Compute the size of the pre/post fixed encrypted key
	    var keyLength = leemon.bitSize(keyC);
	    //look for a multiple of 16, since the size of the one time pad used for encryption was
	    //also a multiple of 16
	    var oneTimePadSize = keyLength;
	    if (keyLength % 16 != 0) {
		oneTimePadSize = keyLength + (16 - keyLength % 16);
	    }

	    var r = leemon.randBigInt(oneTimePadSize);

	    // 4. Reassign old rng
	    Math.random = cRNG;
	    var keyP = leemon.xor(keyC, r);
	    keyP = leemon.bigInt2str(keyP, 64);

	    // 5. Check and erase pre- and postfix
	    pattern = new RegExp(PRIVATE_KEY_PREFIX + "([0-9A-Za-z=_]*)" + PRIVATE_KEY_POSTFIX);
	    match = pattern.exec(keyP);
	    if (match == null || match.length != 2) {
		errorCb('wrongPassword');
		return false;
	    }

	    // 6. Finally return sk
	    return leemon.str2bigInt(match[1], 64, 1);
	}

	/**
	 * Removes leading and trailing spaces and line breaks from a string.
	 *
	 * @param str - The string to trim.
	 * @return The trimmed string.
	 */
	function trim(str) {
	    return str.replace(/^[\s\n\r]+|[\s\n\r]+$/g, '');
	}




	////////////////////////////////////////////////////////////////////////
	// Vote and ballot cryptography

	/**
	 * Encodes a vote.
	 *
	 *     ... 001 00 0010
	 *     ... --- -- ----
	 *     ... c3  c2  c1
	 *
	 * The number of bits per choice is based on the allowd maximum defined
	 * in the for-all-rules.
	 *
	 * @param resultMap - A map representing the vote (choiceId => V(choice)).
	 * @param choicesIds - An array with all possible choice ids.
	 * @param forAllRules -  An array with all for-all-rules.
	 * @return The encoded vote as bigInt.
	 */
	this.encodeVote = function(resultMap, choicesIds, forAllRules) {

	    // Helper to figure out wheter an element is in array or not.
	    var isInArray = function(element, array) {
		for (var i = 0; i < array.length; i++) {
		    if (array[i] == element) {
			return true
		    }
		}
		return false;
	    }

	    // The encoded vote as binary string
	    var bitstring = "";

	    // Make sure, the choices are sorted based on the id
	    choicesIds.sort(function(a, b) {
		return a - b
	    });

	    // Loop through all choices and add the according bits to bitstring
	    for (var i = 0; i < choicesIds.length; i++) {

		var actualId = choicesIds[i];
		if (actualId != undefined && actualId >= 0) {

		    // Get maximal allowed voices for current choice. If (what shouldn't be
		    // the case!) more than one forall-rule exists for a choice, then the
		    // lowest max must be taken.
		    var nbrVoicesPerCandidate = 0;
		    for (var j = 0; j < forAllRules.length; j++) {
			var ruleIds = forAllRules[j].getChoiceId();
			if (isInArray(actualId, ruleIds)) {
			    var upperBound = forAllRules[j].getUpperBound();
			    if (nbrVoicesPerCandidate == 0 || upperBound < nbrVoicesPerCandidate) {
				nbrVoicesPerCandidate = upperBound;
			    }
			}
		    }

		    // Throw an error if the choice couldn't be found in any rule
		    if (nbrVoicesPerCandidate == 0) {
			throw new Error("Encoding error: choice not found in any rule.");
		    }
		    // Compute the number of bits needed to encode the actual choice
		    var nbrBitsPerCandidate = Math.floor((Math.log(nbrVoicesPerCandidate)) / (Math.log(2))) + 1;

		    // Get the occurences of actual id in vote
		    var votesForActualChoice = resultMap.get(actualId.toString());
		    if (votesForActualChoice === undefined) {
			votesForActualChoice = 0;
		    }

		    // Represent the number in binary number
		    var votesForActualChoiceBin = votesForActualChoice.toString(2);
		    // Check what never should be the case! (If it were true, than the
		    // ruleControle in uv-util would have a bug!)
		    if (votesForActualChoiceBin.length > nbrBitsPerCandidate) {
			throw new Error("Encoding error: Too many voices for candidate!");
		    }
		    // Add front padding to binary representation
		    while (votesForActualChoiceBin.length < nbrBitsPerCandidate) {
			votesForActualChoiceBin = "0" + votesForActualChoiceBin;
		    }

		    // Construct the bit string
		    // -> actual choice is added at the left of precedent choices
		    bitstring = votesForActualChoiceBin + bitstring;
		}
	    }

	    // Return the encoded vote as bigInt
	    return leemon.str2bigInt(bitstring, 2, 1);
	}


	/*
	 * Represents a bigInt for Zq in Gq
	 *                 -
	 *                / m' + 1      if (m'+1)^q = 1
	 *   m = G(m') = <
	 *                \ p - (m'+1)  otherwise
	 *                 -
	 * @param bigIntInZq - The bigInt in Zq (m')
	 * @return The bigInt in Gq (m)
	 */
	this.mapZq2Gq = function(bigIntInZq) {

	    if (!leemon.greater(elgamal.q, bigIntInZq)) {
		throw new Error("Error: value not in Zq.");
	    }

	    var one = leemon.str2bigInt("1", 2, 1);
	    var t1 = leemon.add(bigIntInZq, one);
	    var t2 = leemon.powMod(t1, elgamal.q, elgamal.p);

	    if (leemon.equals(t2, one) == 1) {
		return t1;
	    } else {
		return leemon.sub(elgamal.p, t1);
	    }
	}

	/**
	 * Asynchronous version of mapZq2Gq.
	 */
	this.mapZq2GqAsync = function(bigIntInZq, doneCb, updateCb, errorCb) {

	    // step 2
	    var step2 = function(result) {
		var ret;
		if (leemon.equals(result, one) == 1) {
		    ret = t1;
		} else {
		    ret = leemon.sub(elgamal.p, t1);
		}
		doneCb(ret);
	    };

	    // start with step 1
	    if (!leemon.greater(elgamal.q, bigIntInZq)) {
		errorCb("Error: value not in Zq.");
		return;
	    }

	    var one = leemon.str2bigInt("1", 2, 1);
	    var t1 = leemon.add(bigIntInZq, one);
	    leemon.powModAsync(t1, elgamal.q, elgamal.p, updateCb, step2);
	}

	/*
	 * Encrypts an encoded vote.
	 *
	 * @param vote - The vote as bigInt.
	 * @param encryptionKey - The encryption key as bigInt.
	 * @return An object with the encoded vote (univote_bfh_ch_common_encryptedVote) and
	 * the single values (r, a, b) of the encryption as bigInt (for further processing).
	 */
	this.encryptVote = function(vote, encryptionKey) {


	    var r = leemon.randBigIntInZq(elgamal.q);
	    var a = leemon.powMod(elgamal.g, r, elgamal.p);
	    var b = leemon.powMod(encryptionKey, r, elgamal.p);
	    b = leemon.multMod(b, vote, elgamal.p);

	    var encVote = {firstvalue: leemon.bigInt2str(a, 10), secondvalue: leemon.bigInt2str(b, 10)};

	    return {encVote: encVote, r: r, a: a, b: b};
	}

	/**
	 * Asynchronous version of encryptVote.
	 **/
	this.encryptVoteAsync = function(vote, encryptionKey, doneCb, updateCb) {

	    // step 2
	    var a;
	    var step2 = function(_a) {
		a = _a;
		leemon.powModAsync(encryptionKey, r, elgamal.p, updateCb, step3);
	    };

	    // step 3
	    var step3 = function(_b) {
		var b = leemon.multMod(_b, vote, elgamal.p);

		var encVote = {firstvalue: leemon.bigInt2str(a, 10), secondvalue: leemon.bigInt2str(b, 10)};

		doneCb({encVote: encVote, r: r, a: a, b: b});
	    };

	    // start with step 1
	    var r = leemon.randBigIntInZq(elgamal.q);
	    leemon.powModAsync(elgamal.g, r, elgamal.p, updateCb, step2);
	}

	/*
	 * Computes anonymous election verification key.
	 *
	 * @param generator - The election generator as bigInt.
	 * @param sk - Secret key as bigInt.
	 * @return Object with anonymous election verification key as string and bigInt.
	 */
	this.computeElectionVerificationKey = function(generator, sk) {
	    var vk = leemon.powMod(generator, sk, schnorr.p);
	    var vkString = leemon.bigInt2str(vk, 10);
	    return {vkString: vkString, vk: vk};
	}

	/**
	 * Asynchronous version of computeElectionVerificationKey.
	 **/
	this.computeElectionVerificationKeyAsync = function(generator, sk, doneCb, updateCb) {

	    // step 2
	    var step2 = function(_vk) {
		var vkString = leemon.bigInt2str(_vk, 10);
		doneCb({vkString: vkString, vk: _vk});
	    };

	    // start with step 1
	    leemon.powModAsync(generator, sk, schnorr.p, updateCb, step2);
	}

	/*
	 * Computes vote proof.
	 *
	 * @param r - The random value used in vote encryption as bigInt.
	 * @param a - The first value (public input) of the vote encryption as bigInt.
	 * @param vk - The verification key as bigInt.
	 * @return An object with the proof (univote_bfh_ch_common_proof) and the
	 * single proof values (t, c, s) as bigInt.
	 */
	this.computeVoteProof = function(r, a, vk) {

	    var result = this.NIZKP(elgamal.p, elgamal.q, elgamal.g, r, a, leemon.bigInt2str(vk, 10));

	    var proof = {commitment: [leemon.bigInt2str(result.t, 10)], response: [leemon.bigInt2str(result.s, 10)]};

	    return proof;
	}

	/**
	 * Asynchronous version of computeVoteProof
	 **/
	this.computeVoteProofAsync = function(r, a, vk, doneCb, updateCb) {

	    // done
	    var nizkpDoneCb = function(result) {
		var proof = {commitment: [leemon.bigInt2str(result.t, 10)], response: [leemon.bigInt2str(result.s, 10)]};

		doneCb(proof);
	    };

	    this.NIZKPAsync(elgamal.p, elgamal.q, elgamal.g, r, a, leemon.bigInt2str(vk, 10), nizkpDoneCb, updateCb);
	}

	/*
	 * Signs a ballot: S = Sign_sk(id||E||pi) using electionGenerator.
	 *
	 *     Sign_sk(m,r) = (a,r-a*sk),  where a=H(m||g^r)
	 *
	 * @param ballot - An object holding all ballot data.
	 * @param generator - The election generator as bigInt.
	 * @param sk - The secret key as bigInt.
	 * @return An object with the signature (univote_bfh_ch_common_voterSignature)
	 * and the single signature values as bigInt.
	 */
	this.signPost = function(post, generator, sk) {

	    // 1. Hash post
	    var postHash = this.hashPost(post, false, false);
	    
	    // 2. Choose r at random from Zq and calculate g^r
	    var r = leemon.randBigIntInZq(schnorr.q);
	    var a2 = leemon.powMod(generator, r, schnorr.p);

	    // 3. Hash and calculate second part of signature
	    var a2Hash = sha256BigInt(a2);
	    var aStr = sha256HexStr(postHash+a2Hash);
	    //TODO compute mod p or mod q ???
	    var a = leemon.str2bigInt(aStr, 16, 1);
	    var b = leemon.sub(schnorr.q, leemon.multMod(a, sk, schnorr.q));
	    b = leemon.mod(leemon.add(r, b), schnorr.q);

	    // 4. Create return object
	    var sign = {a: a, b: b};

	    return sign;
	}

	/**
	 * Asynchronous version of signBallot.
	 **/
	//TODO adapt to synchronous version
	this.signPostAsync = function(post, generator, sk, doneCb, updateCb) {

	    // step 2
//	    var step2 = function(_a2) {
//
//		//TODO adapt hash
//		// 3. Hash and calculate second part of signature
//		var aStr = SHA256(m + CONCAT_SEPARATOR + leemon.bigInt2str(_a2, 10));
//		var a = leemon.str2bigInt(aStr, 16, 1);
//		var b = leemon.sub(schnorr.q, leemon.multMod(a, sk, schnorr.q));
//		b = leemon.mod(leemon.add(r, b), schnorr.q);
//
//		// 4. Create return object
//		var sign = {a: a, b: b};
//
//		doneCb(sign);
//	    }
//
//	    // Start with step 1
//	    // 1. Concat m
//	    var m = preparePostForSignature(post);
//
//	    // 2. Choose r at random from Zq and calculate g^r
//	    var r = leemon.randBigIntInZq(schnorr.q);
//	    leemon.powModAsync(generator, r, schnorr.p, updateCb, step2);
	}

	//TODO verifiy if it works
	/**
	 * Helper method computing the hash value of a post
	 *
	 * @param post - The post to hash
	 * @param includeBeta - If the beta attributes must also be hashed
	 *	(true when checking signature of result container (CertifiedReading) or signature of board (CertifiedPosting))
	 * @param includeBetaSignature - If the board signature present in beta attibutes must also be hashed
	 *	(true when checking signature of result container (CertifiedReading) only)
	 * @return The hash value of the post.
	 */
	this.hashPost = function(post, includeBeta, includeBetaSignature) {
	    //Get message and alpha attributes
	    var message = post.message;
	    var alpha = post.alpha.attribute;

	    var messageHash = sha256ByteArray(message);

	    var concatenatedAlphaHashes = ""
	    for (var i = 0; i < alpha.length; i++) {
		var attribute = alpha[i];
		if(alpha.key==="signature" && includeBeta==false){
		    //If includeBeta==false, we are checking or generating signature of poster (AccessControlled),
		    //thus signature of post itself must not be included
		    //If includeBeta==true, then we are checking signature of board (CertifiedPosting or CertifiedReading),
		    //thus signature must be included
		    continue;
		}
		if (attribute.value.type === "stringValue") {
		    concatenatedAlphaHashes += sha256String(attribute.value.value);
		} else if (attribute.value.type === "integerValue") {
		    concatenatedAlphaHashes += sha256Int(attribute.value.value);
		} else if (attribute.value.type === "dateValue") {
		    concatenatedAlphaHashes += sha256Date(attribute.value.value);
		} else if (attribute.value.type === "integerValue") {
		    concatenatedAlphaHashes += sha256ByteArray(attribute.value.value);
		} else {
		    return "Error: unknown type of alpha attribute.";
		}
	    }
	    var alphaHash = sha256HexStr(concatenatedAlphaHashes);

	    var betaHash = "";
	    if (includeBeta) {
		var beta = post.beta.attribute;
		var concatenatedBetaHashes = ""
		for (var i = 0; i < beta.length; i++) {
		    var attribute = beta[i];
		    
		    if(beta.key==="signature" && includeBetaSignature==false){
			//If includeBetaSignature==false, we are checking signature of board (CertifiedPosting),
			//thus signature of post itself must not be included
			//If includeBeta==true, then we are checking signature whole board result (CertifiedReading),
			//thus signature must be included
			continue;
		    }
		    
		    if (attribute.value.type === "stringValue") {
			concatenatedBetaHashes += sha256String(attribute.value.value);
		    } else if (attribute.value.type === "integerValue") {
			concatenatedBetaHashes += sha256Int(attribute.value.value);
		    } else if (attribute.value.type === "dateValue") {
			concatenatedBetaHashes += sha256Date(attribute.value.value);
		    } else if (attribute.value.type === "integerValue") {
			concatenatedBetaHashes += sha256ByteArray(attribute.value.value);
		    } else {
			return "Error: unknown type of beta attribute.";
		    }
		}
		betaHash = sha256HexStr(concatenatedBetaHashes);
	    }

	    return sha256HexStr(messageHash+alphaHash+betaHash);
	}

	//TODO verifiy if it works
	/**
	 * Helper method computing the hash value of a result container
	 *
	 * @param resultContainer - The result container to hash
	 * @return The hash value of the result container.
	 */
	this.hashResultContainer = function(resultContainer) {
	    var result = resultContainer.result;
	    var gamma = resultContainer.gamma;
	    var posts = result.post;

	    //Compute hash of all the posts contained in the resultContainer received
	    var concatenatedPostHashes = "";
	    for (var i = 0; i < posts.length; i++) {
		//in the signature of the whole ResultContainer, all the beta attributes are taken into account
		//inclusive the board signature (CertifiedPosting) present in beta attributes
		concatenatedPostHashes += hashPost(posts[i], true, true);
	    }

	    var postsHash = sha256HexStr(concatenatedPostHashes);

//	    if(includeGammas){
	    //TODO Is timestamp in gamma included in signature?
	    //What is the name of this gamma field ?
	    var timestampHash = sha256String(gamma.timestamp);
	    //is this result hashed directly with postsHash or is it hashed once more on gamma level?
	    return sha256HexStr(postsHash + timestampHash);
//	    } else {
//		return postsHash;
//	    }
	}


	////////////////////////////////////////////////////////////////////////
	// Verify Signature of election data

	/*
	 * Verifies signature of election data asynchronously.
	 * (eid|description|(choice1|choice2)|(rule1|rule2)|p|q|g|encryptionKey|electionG)
	 *
	 * @param electionData - The election data as univote_bfh_ch_common_electionData
	 * @param callback - The callback passing true if signature is correct, false otherwise.
	 */
	this.verifyResultSignature = function(resultContainer, callback) {
	    //1. Verify ResultContainer signature
	    var resultContainerHash = hashResultContainer(resultContainer);
	    //TODO verify signature
	    
	    //2. Verify board signature of each Post
	    var posts = resultContainer.result.post;
	    for (var i = 0; i < posts.length; i++) {
	       var post = posts[i];
	       
	       var postHash = hashPost(post, true, false);
	       //TODO verify signature
	    }
	    
	    //3. Verify poster signature of each Post
	    for (var i = 0; i < posts.length; i++) {
	       var post = posts[i];
	       
	       var postHash = hashPost(post, false, false);
	       //TODO verify signature
	    }
	    
	    
	    
//	    var i, item;
//
//	    // Helper to concat localized text.
//	    var concatLocalizedText = function(loc) {
//		var s = [];
//		s.push(CONCAT_DELIMINATOR_L);
//		for (var i = 0; i < loc.length; i++) {
//		    if (i > 0) {
//			s.push(CONCAT_SEPARATOR);
//		    }
//		    s.push(CONCAT_DELIMINATOR_L);
//		    var t = loc[i];
//		    s.push(t.getLanguage(), CONCAT_SEPARATOR, t.getText());
//		    s.push(CONCAT_DELIMINATOR_R);
//		}
//		s.push(CONCAT_DELIMINATOR_R);
//		return s.join('');
//	    }
//
//	    // 1. CONCAT
//	    var m = [];
//	    m.push(CONCAT_DELIMINATOR_L);
//	    // 1.1 ElectionID
//	    m.push(electionData.getElectionId(), CONCAT_SEPARATOR);
//	    // 1.2 Title
//	    m.push(electionData.getTitle(), CONCAT_SEPARATOR);
//	    // 1.3 Choices
//	    m.push(CONCAT_DELIMINATOR_L);
//	    var choices = electionData.getChoice();
//	    for (i = 0; i < choices.length; i++) {
//		if (i > 0) {
//		    m.push(CONCAT_SEPARATOR);
//		}
//		item = choices[i];
//		m.push(CONCAT_DELIMINATOR_L);
//		m.push(item.getChoiceId(), CONCAT_SEPARATOR);
//		if (item instanceof univote_bfh_ch_common_politicalList) {
//		    m.push(item.getNumber(), CONCAT_SEPARATOR);
//		    m.push(concatLocalizedText(item.getTitle()), CONCAT_SEPARATOR);
//		    m.push(concatLocalizedText(item.getPartyName()), CONCAT_SEPARATOR);
//		    m.push(concatLocalizedText(item.getPartyShortName()));
//		} else if (item instanceof univote_bfh_ch_common_candidate) {
//		    m.push(item.getNumber(), CONCAT_SEPARATOR);
//		    m.push(item.getLastName(), CONCAT_SEPARATOR);
//		    m.push(item.getFirstName(), CONCAT_SEPARATOR);
//		    m.push(item.getSex(), CONCAT_SEPARATOR);
//		    m.push(item.getYearOfBirth(), CONCAT_SEPARATOR);
//		    m.push(concatLocalizedText(item.getStudyBranch()), CONCAT_SEPARATOR);
//		    m.push(concatLocalizedText(item.getStudyDegree()), CONCAT_SEPARATOR);
//		    m.push(item.getSemesterCount(), CONCAT_SEPARATOR);
//		    m.push(item.getStatus(), CONCAT_SEPARATOR);
//		    m.push(item.getListId(), CONCAT_SEPARATOR);
//		    m.push(item.getCumulation());
//		}
//		m.push(CONCAT_DELIMINATOR_R);
//	    }
//	    m.push(CONCAT_DELIMINATOR_R, CONCAT_SEPARATOR);
//	    // 1.4 Rules
//	    m.push(CONCAT_DELIMINATOR_L);
//	    var rules = electionData.getRule();
//	    for (i = 0; i < rules.length; i++) {
//		if (i > 0) {
//		    m.push(CONCAT_SEPARATOR);
//		}
//		item = rules[i];
//		m.push(CONCAT_DELIMINATOR_L);
//		if (item instanceof univote_bfh_ch_common_summationRule) {
//		    m.push('summationRule', CONCAT_SEPARATOR);
//		} else if (item instanceof univote_bfh_ch_common_forallRule) {
//		    m.push('forallRule', CONCAT_SEPARATOR);
//		}
//		m.push(CONCAT_DELIMINATOR_L, item.getChoiceId().join(CONCAT_SEPARATOR), CONCAT_DELIMINATOR_R);
//		m.push(CONCAT_SEPARATOR);
//		m.push(item.getLowerBound(), CONCAT_SEPARATOR);
//		m.push(item.getUpperBound());
//		m.push(CONCAT_DELIMINATOR_R);
//	    }
//	    m.push(CONCAT_DELIMINATOR_R, CONCAT_SEPARATOR);
//	    // 1.5 p, q, g
//	    m.push(electionData.getPrime(), CONCAT_SEPARATOR);
//	    m.push(electionData.getGroupOrder(), CONCAT_SEPARATOR);
//	    m.push(electionData.getGenerator(), CONCAT_SEPARATOR);
//	    // 1.6 encryptionKey, electionG
//	    m.push(electionData.getEncryptionKey(), CONCAT_SEPARATOR);
//	    m.push(electionData.getElectionGenerator());
//	    m.push(CONCAT_DELIMINATOR_R, CONCAT_SEPARATOR);
//	    m.push(electionData.getSignature().getTimestamp());
//
//	    // 2. Decrypt signature
//	    var signatureC = electionData.getSignature().getValue();
//	    leemon.powModAsync(leemon.str2bigInt(signatureC, 10, 1), rsa.pk, rsa.n, function() {
//	    }, function(signature) {
//
//		// 3. Compare
//		var x = leemon.mod(leemon.str2bigInt(SHA256(m.join('')), 16, 1), rsa.n);
	    callback(true);
//	    });
	}
    }

    window.uvCrypto = new Crypto();

})(window);
