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
	    errorDiv.innerHTML = "<p>ERROR: Missing JS library! UniCert won't be running as either leemon or seedrandom is missing.</p>";
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

	window.ucConfig = window.ucConfig || {};

	// Pre- and postfix used for secret key padding. Important: As the padded
	// secret key is converted into a bigInt only leemon's base64 charset can
	// be used (0-9, A-Z, a-z, _ and =)
	var PRIVATE_KEY_PREFIX = ucConfig.PRIVATE_KEY_PREFIX || "=====BEGIN_UNICERT_PRIVATE_KEY=====";
	var PRIVATE_KEY_POSTFIX = ucConfig.PRIVATE_KEY_POSTFIX || "=====END_UNICERT_PRIVATE_KEY=====";

	// Pre- and postfix used for padding the encrypted secret key.
	var ENC_PRIVATE_KEY_PREFIX = ucConfig.ENC_PRIVATE_KEY_PREFIX || "-----BEGIN ENCRYPTED UNICERT KEY-----";
	var ENC_PRIVATE_KEY_POSTFIX = ucConfig.ENC_PRIVATE_KEY_POSTFIX || "-----END ENCRYPTED UNICERT KEY-----";

	// IMPORTANT: size of pre- and postfix = 411 < 512
	var PRIVATE_KEY_ONE_TIME_PAD_PREPOSTFIX_SIZE = ucConfig.PRIVATE_KEY_ONE_TIME_PAD_PREPOSTFIX_SIZE || 512;


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
		//TODO remove commented code
//		console.log("p " + leemon.bigInt2str(p, 10));
//		console.log("q " + leemon.bigInt2str(q, 10));
//		console.log("g " + leemon.bigInt2str(g, 10));
//		console.log("public input 10 " + leemon.bigInt2str(publicInput, 10));
		var hashPI = sha256BigInt(publicInput);
//		console.log("public input hashed: " + hashPI);
		//3.2 Hash of commitment
		var hashCommitment = sha256BigInt(t);
//		console.log("commitment hashed: " + hashCommitment);
		//3.3 Hash of the hash of public input concatenated with hash of commitment
		//(Steps 3.1 to 3.3 are the computation of to the recursive hash of a Pair[publicInput, Commitment] in UniCrypt)
		var hashPIAndCommitment = sha256HexStr(hashPI + hashCommitment);

		//3.4 Hash of other input
		var hashOtherInput = sha256String(otherInput);
//		console.log("other input hashed: " + hashOtherInput);
		//3.5 Hash of hashPIAndCommitment concatenated with hashOtherInput
		//(Steps 3.1 to 3.5 are the computation of to the recursive hash of a Pair[Pair[publicInput, Commitment], otherInput] in UniCrypt)
		var cStr = sha256HexStr(hashPIAndCommitment + hashOtherInput);
//		console.log("Complete hash: " + cStr);
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



	////////////////////////////////////////////////////////////////////////
	// Secret key and verification key generation

	/**
	 * Generates a discrete log secret key. A secret key is a random number in Zq in
	 * the Schnorr system.
	 *
	 * @return Secret key as bigInt.
	 */
	this.generateDLOGSecretKey = function(q) {

	    var sk = leemon.randBigIntInZq(q);
	    return sk;
	}

	/**
	 * Generates a RSA key pair.
	 *
	 * @return Secret key as bigInt.
	 */
	this.generateRSASecretKey = function(size, doneCb, progressCb) {
	    var p, q, n;
	    var e = leemon.str2bigInt("65537", 10, 0);
	    var keys = [];


	    var qCb = function(prime) {
		q = prime;

		//the prime must not be congruent to 1 modulo e
		if (leemon.equalsInt(leemon.mod(p, e), 1) || leemon.equalsInt(leemon.mod(q, e), 1)) {
		    ucCrypto.generateRSASecretKey(size, doneCb, progressCb);
		    return;
		}

		n = leemon.mult(p, q);
		var phi = leemon.mult(leemon.addInt(p, -1), leemon.addInt(q, -1));

		var d = leemon.inverseMod(e, phi);

		keys[0] = d;
		keys[1] = e;
		keys[2] = n;

		doneCb(keys);
		return keys;
	    };

	    var pCb = function(prime) {
		p = prime;
		leemon.randProbPrimeAsync(size / 2 + 2, qCb, progressCb);
	    };

	    leemon.randProbPrimeAsync(size / 2 + 2, pCb, progressCb);
	    return;
	}

	/**
	 * Computes the verification key for a secret key.
	 * => vk = g^sk mod p  (Schnorr)
	 *
	 * @param sk - Secret key as bigInt.
	 * @return Verification key as bigInt.
	 */
	this.computeVerificationKey = function(sk) {

	    var vk = leemon.powMod(schnorr.g, sk, schnorr.p);
	    return vk;
	}

	/**
	 * Asynchronous version of comupteVerificationKey.
	 */
	this.computeVerificationKeyAsync = function(p, g, sk, doneCb, progressCb) {

	    leemon.powModAsync(g, sk, p, progressCb, doneCb);
	}

	/*
	 * Computes verification key proof.
	 *
	 * @param sk - The secret key as bigInt.
	 * @param vk - The verification key as bigInt.
	 * @param voterId - Voter's id as string.
	 * @return Proof as object containing t (commitment), c (challange) and
	 * s (response) as string representing a bigInt to the base 10.
	 */
	this.computeVerificationKeyProof = function(p, q, g, sk, vk, voterId) {
	    var proof = this.NIZKP(p, q, g, sk, vk, voterId);
	    proof.t = leemon.bigInt2str(proof.t, 10);
	    proof.c = leemon.bigInt2str(proof.c, 10);
	    proof.s = leemon.bigInt2str(proof.s, 10);
	    return proof;
	}

	/**
	 * Asynchronous version of comupteVerificationKeyProof.
	 */
	this.computeVerificationKeyProofAsync = function(p, q, g, sk, vk, voterId, doneCb, updateCb) {

	    // done
	    var nizkpDoneCb = function(proof) {
		proof.t = leemon.bigInt2str(proof.t, 10);
		proof.c = leemon.bigInt2str(proof.c, 10);
		proof.s = leemon.bigInt2str(proof.s, 10);
		console.log("Challenge: "+proof.c);
		doneCb(proof);
	    };
	    
	    this.NIZKPAsync(p, q, g, sk, vk, voterId, nizkpDoneCb, updateCb);
	}


	this.computeSignatureAsync = function(sk, vk, modulo, message, doneCb, updateCb) {
	    //TODO remove console comments
	    //hash the message
	    var hashedMessage = sha256String(message); //base 16 encoded string
//	    console.log("hash: " + hashedMessage)
	    //Create a BigInteger with the hashedMessage
	    var messageBigInt = leemon.str2bigInt(hashedMessage, 16);
	    //Computes the new BigInt modulo n since RSA message space in between 0 and n-1
	    var messageBigIntMod = leemon.mod(messageBigInt, modulo);
//	    console.log("messageBigIntMod: " + leemon.bigInt2str(messageBigIntMod, 10));
//	    console.log("n: " + leemon.bigInt2str(modulo, 10));
//	    console.log("pk: " + leemon.bigInt2str(vk, 10));
//	    console.log("sig oth in: " + message);
	    leemon.powModAsync(messageBigIntMod, sk, modulo, updateCb, doneCb);

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

    }
    window.ucCrypto = new Crypto();

})(window);

