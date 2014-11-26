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
 *    - Signature verification of posts
 *    - Signature creation for posting
 *    - Vote encoding and ballot creation (encryption, proofing, signing)
 *
 */


(function (window) {

    // Check for leemon and seedrandom library and configuration. If they aren't loaded,
    // an error message is displayed at the top of the page.
    if (!leemon || !Math.seedrandom || !uvConfig) {
	window.onload = function () {
	    var body = document.getElementsByTagName('body')[0];
	    var errorDiv = document.createElement('div');
	    errorDiv.setAttribute('style', 'background-color:red; z-index:1000; position:absolute; top:0; left: 0; width: 100%; height:50px; text-align:center; font-weight:bold; padding-top: 20px;');
	    errorDiv.innerHTML = "<p>ERROR: Missing JS library! UNI-Vote won't be running as either leemon, seedrandom or configuration is missing.</p>";
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
	////////////////////////////////////////////////////////////////////////
	
	// Base refers only to the bigInt representation of Schnorr, Elgamal and RSA parameters.
	var base = uvConfig.BASE;

	// Default initialization of signature setting
	var signatureSetting = {};
	signatureSetting.pStr = uvConfig.SIGNATURE_SETTING.P;
	signatureSetting.qStr = uvConfig.SIGNATURE_SETTING.Q;
	signatureSetting.gStr = uvConfig.SIGNATURE_SETTING.G;

	signatureSetting.p = leemon.str2bigInt(signatureSetting.pStr, base, 1);
	signatureSetting.q = leemon.str2bigInt(signatureSetting.qStr, base, 1);
	signatureSetting.g = leemon.str2bigInt(signatureSetting.gStr, base, 1);

	// Default initialization of encryption setting
	var encryptionSetting = {};
	encryptionSetting.pStr = uvConfig.ENCRYPTION_SETTING.P;
	encryptionSetting.qStr = uvConfig.ENCRYPTION_SETTING.Q;
	encryptionSetting.gStr = uvConfig.ENCRYPTION_SETTING.G;

	encryptionSetting.p = leemon.str2bigInt(encryptionSetting.pStr, base, 1);
	encryptionSetting.q = leemon.str2bigInt(encryptionSetting.qStr, base, 1);
	encryptionSetting.g = leemon.str2bigInt(encryptionSetting.gStr, base, 1);

	
	/**
	 * Sets the encryption parameters at runtime.
	 *
	 * @param pStr - P as string.
	 * @param qStr - Q as string.
	 * @param gStr - Generator as string.
	 * @param base - The base P, Q and G are represented in.
	 */
	this.setElgamalParameters = function (pStr, qStr, gStr, base) {
	    encryptionSetting.pStr = pStr;
	    encryptionSetting.qStr = qStr;
	    encryptionSetting.gStr = gStr;
	    encryptionSetting.p = leemon.str2bigInt(pStr, base, 1);
	    encryptionSetting.q = leemon.str2bigInt(qStr, base, 1);
	    encryptionSetting.g = leemon.str2bigInt(gStr, base, 1);
	}

	/**
	 * Sets the signature parameters at runtime.
	 *
	 * @param pStr - P as string.
	 * @param qStr - Q as string.
	 * @param gStr - Generator as string.
	 * @param base - The base P, Q and G are represented in.
	 */
	this.setSignatureParameters = function (pStr, qStr, gStr, base) {
	    signatureSetting.pStr = pStr;
	    signatureSetting.qStr = qStr;
	    signatureSetting.gStr = gStr;
	    signatureSetting.p = leemon.str2bigInt(pStr, base, 1);
	    signatureSetting.q = leemon.str2bigInt(qStr, base, 1);
	    signatureSetting.g = leemon.str2bigInt(gStr, base, 1);
	}

	////////////////////////////////////////////////////////////////////////
	// Non-interactive zero-knowledge proof
	////////////////////////////////////////////////////////////////////////

	/**
	 * Computes a non-interactive zero-knowledge proof of knowledge of a private value.
	 *
	 * @param system - The system (either Schnorr or Elgamal).
	 * @param secretInput - The secret input as bigInt.
	 * @param publicInput - The public input as bigInt.
	 * @param otherInput - Some other input as bigInt or string.
	 * @return Proof as object containing t (commitment), c (challange) and s (response) as bigInt.
	 */
	/**
	 * Computes a non-interactive zero-knowledge proof of knowledge of a private value.
	 * @param p Prime p as big integer
	 * @param q Prime q as big integer
	 * @param g Generator as big integer
	 * @param secretInput Secret value to proove knowledge as big integer
	 * @param publicInput Public value corresponding to the private value as big integer
	 * @param otherInput Other input that must be included in the hash contained in the proof as string
	 * @returns Proof as object containing t (commitment), c (challange) and s (response) as bigInt.
	 */
	this.NIZKP = function (p, q, g, secretInput, publicInput, otherInput) {

	    //1. Choose omega at random from Zq
	    var omega = leemon.randBigIntInZq(q);

	    //2. Compute t = g^omega mod p
	    var t = leemon.powMod(g, omega, p);

	    //3. Compute c = H(H(H(publicInput)||H(t))||H(otherInput))
	    //3.1 Hash of public input
	    var hashPI = sha256.hashBigInt(publicInput);
	    //3.2 Hash of commitment
	    var hashCommitment = sha256.hashBigInt(t);
	    //3.3 Hash of the hash of public input concatenated with hash of commitment
	    //(Steps 3.1 to 3.3 are the computation of to the recursive hash of a Pair[publicInput, Commitment] in UniCrypt)
	    var hashPIAndCommitment = sha256.hashHexStr(hashPI + hashCommitment);
	    //3.4 Hash of other input
	    var hashOtherInput = sha256.hashString(otherInput);
	    //3.5 Hash of hashPIAndCommitment concatenated with hashOtherInput
	    //(Steps 3.1 to 3.5 are the computation of to the recursive hash of a Pair[Pair[publicInput, Commitment], otherInput] in UniCrypt)
	    var cStr = sha256.hashHexStr(hashPIAndCommitment + hashOtherInput);
	    var c = leemon.mod(leemon.str2bigInt(cStr, 16, 1), q);

	    //4. Compute s = omega+c*secretInput mod q
	    var s = leemon.mod(leemon.add(omega, leemon.multMod(c, secretInput, q)), q);

	    // 5. Return proof
	    return {t: t, c: c, s: s};
	}

//	/**
//	 * Asynchronous version of NIZKP.
//	 **/
//	this.NIZKPAsync = function(p, q, g, secretInput, publicInput, otherInput, doneCb, updateCb) {
//
//	    // step 2
//	    var step2 = function(_t) {
//
//		var t = _t;
//		//3. Compute c = H(H(H(publicInput)||H(t))||H(otherInput))
//		//3.1 Hash of public input
//		var hashPI = sha256.hashBigInt(publicInput);
//
//		//3.2 Hash of commitment
//		var hashCommitment = sha256.hashBigInt(t);
//
//		//3.3 Hash of the hash of public input concatenated with hash of commitment
//		//(Steps 3.1 to 3.3 are the computation of to the recursive hash of a Pair[publicInput, Commitment] in UniCrypt)
//		var hashPIAndCommitment = sha256.hashHexStr(hashPI + hashCommitment);
//
//		//3.4 Hash of other input
//		var hashOtherInput = sha256.hashString(otherInput);
//		//3.5 Hash of hashPIAndCommitment concatenated with hashOtherInput
//		//(Steps 3.1 to 3.5 are the computation of to the recursive hash of a Pair[Pair[publicInput, Commitment], otherInput] in UniCrypt)
//		var cStr = sha256.hashHexStr(hashPIAndCommitment + hashOtherInput);
//		var c = leemon.mod(leemon.str2bigInt(cStr, 16, 1), q);
//		//4. Compute s = omega+c*secretInput mod q
//		var s = leemon.mod(leemon.add(omega, leemon.multMod(c, secretInput, q)), q);
//
//		// 5. Call callback with proof
//		doneCb({t: t, c: c, s: s});
//	    }
//
//
//	    // Start with step 1
//	    //1. Choose omega at random from Zq
//	    var omega = leemon.randBigIntInZq(q);
//
//
//	    //2. Compute t = g^omega mod p
//	    leemon.powModAsync(g, omega, p, updateCb, step2);
//	}

	////////////////////////////////////////////////////////////////////////
	// Schnorr Signature
	////////////////////////////////////////////////////////////////////////

	/**
	 * Create a Schnorr signature
	 * @param messageHash Hash of message to sign encoded in base 16
	 * @param privateKey Private key to use to sign in leemon big int format
	 * @param p Prime p in leemon big int format
	 * @param q Prime q in leemon big int format
	 * @param g Generator g in leemon big int format
	 * @returns the paired value of the two signature elements (s,e)
	 */
	this.createSchnorrSignature = function (messageHash, privateKey, p, q, g) {
	    // 1. Choose r at random from Zq and calculate g^r
	    var r = leemon.randBigIntInZq(q);
	    var a2 = leemon.powMod(g, r, p);

	    // 2. Hash and calculate second part of signature
	    var a2Hash = sha256.hashBigInt(a2);
	    var aStr = sha256.hashHexStr(messageHash + a2Hash);
	    var a = leemon.mod(leemon.str2bigInt(aStr, 16),q);

	    var b = leemon.add(r, leemon.mult(a, privateKey));
	    b = leemon.mod(b, q);

	    return this.pair(a, b);
	}


	/**
	 * Verify a Schnorr signature
	 * @param signature The paired value of the signature (s,e)
	 * @param messageHash Hash of message to sign encoded in base 16
	 * @param publicKey Public key to use for the verification in leemon big int format
	 * @param p Prime p in leemon big int format
	 * @param q Prime q in leemon big int format
	 * @param g Generator g in leemon big int format
	 * @returns true if signature is valid, false otherwise
	 */
	this.verifySchnorrSignature = function (signature, messageHash, publicKey, p, q, g) {

	    var signatureValues = this.unpair(signature);

	    var a = signatureValues[1];
	    var b = signatureValues[0];

	    var c = leemon.powMod(g, a, p);
	    var d = leemon.powMod(publicKey, b, p);

	    var a2Verif = leemon.multMod(c, leemon.inverseMod(d, p), p);

	    var bVerif = sha256.hashHexStr(messageHash + sha256.hashBigInt(a2Verif));

	    return leemon.equals(b, leemon.mod(leemon.str2bigInt(bVerif, 16), q));
	}

	/**
	 * Computes the elegant pairing function for two non-negative BigInteger values.
	 * @see http://szudzik.com/ElegantPairing.pdf
	 * @param bigInt1 The first value
	 * @param bigInt2 The second value
	 * @return The result of applying the elegant pairing function
	 */
	this.pair = function (bigInt1, bigInt2) {
	    if (leemon.negative(bigInt1) || leemon.negative(bigInt2)) {
		throw Error("Cannot be negative");
	    }
	    if (leemon.greater(bigInt2, bigInt1) || leemon.equals(bigInt2, bigInt1)) {
		return leemon.add(leemon.mult(bigInt2, bigInt2), bigInt1);
	    } else {
		return leemon.add(leemon.add(leemon.mult(bigInt1, bigInt1), bigInt1), bigInt2);
	    }
	}

	/**
	 * Computes the inverse of the binary elegant pairing function for a given non-negative BigInteger value.
	 * @see http://szudzik.com/ElegantPairing.pdf
	 * @param bigInt The input value
	 * @return An array containing the two resulting values
	 */
	this.unpair = function (bigInt) {
	    var x1 = this.isqrt(bigInt);
	    var x2 = leemon.sub(bigInt, leemon.mult(x1, x1));

	    if (leemon.greater(x1, x2)) {
		return [x2, x1];
	    } else {
		return [x1, leemon.sub(x2, x1)];
	    }
	}

	// This is a helper method to compute the integer square root of a BigInteger value.
	this.isqrt = function (bigInt) {
	    var one = leemon.str2bigInt("1", 10);
	    var a = one;

	    var b = leemon.add(leemon.rightShift(bigInt, 5), leemon.str2bigInt("8", 10));

	    while (leemon.greater(b, a) || leemon.equals(b, a)) {

		var mid = leemon.rightShift(leemon.add(a, b), 1);
		var square = leemon.mult(mid, mid);


		if (leemon.greater(square, bigInt)) {
		    b = leemon.sub(mid, one);
		} else {
		    a = leemon.add(mid, one);
		}
	    }
	    return leemon.sub(a, one);
	};

	////////////////////////////////////////////////////////////////////////
	// Randomization bliding functions
	////////////////////////////////////////////////////////////////////////

	/**
	 * Blind the randomization (r) used during ballot-encryption by blinding it with the secret key (sk)
	 *
	 * @param  r - The randomization used during ballot-encryption as BigInt
	 * @param  sk - The private key part of the verification key as BigInt
	 * @returns Blinded randomization as BigInt
	 */
	this.blindRandomization = function (r, sk) {

	    //1. map randomization into g_q
	    var rGq = this.mapZq2Gq(r);
	    //2. blind the mapped randomization with sk. This can be unblinded by rB^{-sk} = r^{sk/sk} = r
	    var rB = leemon.powMod(rGq, sk, encryptionSetting.p);
	    //3. return the blinded
	    return rB;
	}

	/**
	 * Asynchronous version of blindRandomization.
	 */
//	this.blindRandomizationAsync = function(r, sk, doneCb, updateCb) {
//
//	    var step2 = function(_rGq) {
//		//2. blind the mapped randomization with sk. This can be unblinded by rB^{-sk} = r^{sk/sk} = r
//		leemon.powModAsync(_rGq, sk, encryptionSetting.p, updateCb, doneCb);
//	    }
//	    var errorCb = function(message) {
//		//Schould never happen!
//		doneCb(leemon.str2bigInt("0", 10, 1));
//	    }
//	    //1. map randomization into g_q
//	    this.mapZq2GqAsync(r, step2, updateCb, errorCb);
//	}


	////////////////////////////////////////////////////////////////////////
	// Secret key and verification key generation
	////////////////////////////////////////////////////////////////////////

	/**
	 * Generates a discrete log secret key. A secret key is a random number in Zq in
	 * the Schnorr system.
	 *
	 * @param q Size of Zq
	 * @return Secret key as bigInt.
	 */
	this.generateDLOGSecretKey = function (q) {

	    var sk = leemon.randBigIntInZq(q);
	    return sk;
	}

	/**
	 * Generates a RSA key pair.
	 *
	 * @param size size of the modulo n to obtain
	 * @return an array containing secret key d, public key e and modulo n
	 */
	this.generateRSASecretKey = function (size, doneCb, progressCb) {
	    var p, q, n;
	    var e = leemon.str2bigInt("65537", 10, 0);
	    var keys = [];


	    var qCb = function (prime) {
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

	    var pCb = function (prime) {
		p = prime;
		leemon.randProbPrimeAsync(size / 2 + 2, qCb, progressCb);
	    };

	    leemon.randProbPrimeAsync(size / 2 + 2, pCb, progressCb);
	    return;
	}

	/**
	 * Computes the verification key for a secret key.
	 * => vk = g^sk mod p
	 *
	 * @param p Value of prime p
	 * @param g Value of generator g
	 * @param sk - Secret key as bigInt.
	 * @return Verification key as bigInt.
	 */
	this.computeVerificationKey = function (p, g, sk) {
	    return leemon.powMod(g, sk, p);
	}

//	/**
//	 * Asynchronous version of comupteVerificationKey.
//	 */
//	this.computeVerificationKeyAsync = function(p, g, sk, doneCb, progressCb) {
//
//	    leemon.powModAsync(g, sk, p, progressCb, doneCb);
//	}

	/*
	 * Computes verification key proof.
	 *
	 * @param sk - The secret key as bigInt.
	 * @param vk - The verification key as bigInt.
	 * @param voterId - Voter's id as string.
	 * @return Proof as object containing t (commitment), c (challange) and
	 * s (response) as string representing a bigInt to the base 10.
	 */
	this.computeVerificationKeyProof = function (p, q, g, sk, vk, voterId) {
	    var proof = this.NIZKP(p, q, g, sk, vk, voterId);
	    proof.t = leemon.bigInt2str(proof.t, 10);
	    proof.c = leemon.bigInt2str(proof.c, 10);
	    proof.s = leemon.bigInt2str(proof.s, 10);
	    return proof;
	}

//	/**
//	 * Asynchronous version of comupteVerificationKeyProof.
//	 */
//	this.computeVerificationKeyProofAsync = function(p, q, g, sk, vk, voterId, doneCb, updateCb) {
//
//	    // done
//	    var nizkpDoneCb = function(proof) {
//		proof.t = leemon.bigInt2str(proof.t, 10);
//		proof.c = leemon.bigInt2str(proof.c, 10);
//		proof.s = leemon.bigInt2str(proof.s, 10);
//		doneCb(proof);
//	    };
//
//	    this.NIZKPAsync(p, q, g, sk, vk, voterId, nizkpDoneCb, updateCb);
//	}


	this.computeRSASignature = function (sk, vk, modulo, message) {
	    //hash the message
	    var hashedMessage = sha256.hashString(message); //base 16 encoded string
	    //Create a BigInteger with the hashedMessage
	    var messageBigInt = leemon.str2bigInt(hashedMessage, 16);
	    //Computes the new BigInt modulo n since RSA message space in between 0 and n-1
	    var messageBigIntMod = leemon.mod(messageBigInt, modulo);
	    return leemon.powMod(messageBigIntMod, sk, modulo);
	}

	////////////////////////////////////////////////////////////////////////
	// Secret key enc- and decryption
	////////////////////////////////////////////////////////////////////////

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
	this.encryptSecretKey = function (sk, password) {

	    // 1. Add pre- and postfix to key
	    var key = uvConfig.PRIVATE_KEY_PREFIX + sk + uvConfig.PRIVATE_KEY_POSTFIX;

	    // 2. Convert key into bigInt} catch (errormsg) {
//		clearInterval(update);
//		processFatalError(msg.signatureError);
//		return;
//	    }
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
	    var oneTimePadSize = keyLength + (16 - keyLength % 16) + uvConfig.PRIVATE_KEY_ONE_TIME_PAD_PREPOSTFIX_SIZE;
	    var r = leemon.randBigInt(oneTimePadSize);
	    Math.random = cRNG;

	    // 6. Encrypt key using one-time-pad
	    var keyC = leemon.xor(key, r);
	    // 7. Convert key to string with base 64
	    keyC = leemon.bigInt2str(keyC, 64);
	    // 8. Pad encrypted key with pre- and postfix and add salt
	    keyC = uvConfig.ENC_PRIVATE_KEY_PREFIX + '\n' + salt + keyC + '\n' + uvConfig.ENC_PRIVATE_KEY_POSTFIX;
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
	this.decryptSecretKey = function (key, password, errorCb) {

	    // Cleans a string (removes all special charaters but =, -, _)
	    function cleanStr(str) {
		return str.replace(/[^\w=_\-]/gi, '');
	    }


	    // 1. Check and erase pre- and postfix
	    // -> even \n and \r should be included in \s, only \s does not work!!!
	    var pattern = new RegExp(cleanStr(uvConfig.ENC_PRIVATE_KEY_PREFIX) + "([0-9A-Za-z=_]*)" + cleanStr(uvConfig.ENC_PRIVATE_KEY_POSTFIX));
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
	    pattern = new RegExp(uvConfig.PRIVATE_KEY_PREFIX + "([0-9A-Za-z=_]*)" + uvConfig.PRIVATE_KEY_POSTFIX);
	    match = pattern.exec(keyP);
	    if (match == null || match.length != 2) {
		errorCb('wrongPassword');
		return false;
	    }

	    // 6. Finally return sk
	    return leemon.str2bigInt(match[1], 64, 1);
	}

	/**
	 * LEGACY SUPPORT: UniVote 1 keys
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
	this.decryptSecretKeyUniVote1 = function (key, password, errorCb) {

	    // Cleans a string (removes all special charaters but =, -, _)
	    function cleanStr(str) {
		return str.replace(/[^\w=_\-]/gi, '');
	    }


	    // 1. Check and erase pre- and postfix
	    // -> even \n and \r should be included in \s, only \s does not work!!!
	    var pattern = new RegExp(cleanStr(uvConfig.ENC_PRIVATE_KEY_PREFIX_UNIVOTE_1) + "([0-9A-Za-z=_]*)" + cleanStr(uvConfig.ENC_PRIVATE_KEY_POSTFIX_UNIVOTE_1));
	    var match = pattern.exec(cleanStr(key));
	    if (match == null || match.length != 2) {
		errorCb('invalidUploadedKey');
		return false;
	    }

	    var keyC = match[1];

	    // 2. Decrypt key with password
	    keyC = leemon.str2bigInt(keyC, 64, 0);
	    // Save current RNG temporary to not lose accumulated data for future randomness
	    var cRNG = Math.random;
	    Math.seedrandom(password);
	    var r = leemon.randBigInt(uvConfig.PRIVATE_KEY_ONE_TIME_PAD_SIZE_UNIVOTE_1);
	    // Reassign old rng
	    Math.random = cRNG;
	    var keyP = leemon.xor(keyC, r);
	    keyP = leemon.bigInt2str(keyP, 64);


	    // 3. Check and erase pre- and postfix
	    pattern = new RegExp(uvConfig.PRIVATE_KEY_PREFIX_UNIVOTE_1 + "([0-9A-Za-z=_]*)" + uvConfig.PRIVATE_KEY_POSTFIX_UNIVOTE_1);
	    match = pattern.exec(keyP);
	    if (match == null || match.length != 2) {
		errorCb('wrongPassword');
		return false;
	    }

	    // 4. Finally return sk
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
	////////////////////////////////////////////////////////////////////////

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
	this.encodeVote = function (resultMap, choicesIds, forAllRules) {

	    // Helper to figure out wheter an element is in array or not.
	    var isInArray = function (element, array) {
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
	    choicesIds.sort(function (a, b) {
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
			var ruleIds = forAllRules[j].choiceIds;
			if (isInArray(actualId, ruleIds)) {
			    var upperBound = forAllRules[j].upperBound;
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
	this.mapZq2Gq = function (bigIntInZq) {

	    if (!leemon.greater(encryptionSetting.q, bigIntInZq)) {
		throw new Error("Error: value not in Zq.");
	    }

	    var one = leemon.str2bigInt("1", 2, 1);
	    var t1 = leemon.add(bigIntInZq, one);
	    var t2 = leemon.powMod(t1, encryptionSetting.q, encryptionSetting.p);

	    if (leemon.equals(t2, one) == 1) {
		return t1;
	    } else {
		return leemon.sub(encryptionSetting.p, t1);
	    }
	}

//	/**
//	 * Asynchronous version of mapZq2Gq.
//	 */
//	this.mapZq2GqAsync = function(bigIntInZq, doneCb, updateCb, errorCb) {
//
//	    // step 2
//	    var step2 = function(result) {
//		var ret;
//		if (leemon.equals(result, one) == 1) {
//		    ret = t1;
//		} else {
//		    ret = leemon.sub(encryptionSetting.p, t1);
//		}
//		doneCb(ret);
//	    };
//
//	    // start with step 1
//	    if (!leemon.greater(encryptionSetting.q, bigIntInZq)) {
//		errorCb("Error: value not in Zq.");
//		return;
//	    }
//
//	    var one = leemon.str2bigInt("1", 2, 1);
//	    var t1 = leemon.add(bigIntInZq, one);
//	    leemon.powModAsync(t1, encryptionSetting.q, encryptionSetting.p, updateCb, step2);
//	}

	/*
	 * Encrypts an encoded vote.
	 *
	 * @param vote - The vote as bigInt.
	 * @param encryptionKey - The encryption key as bigInt.
	 * @return An object with the encoded vote (univote_bfh_ch_common_encryptedVote) and
	 * the single values (r, a, b) of the encryption as bigInt (for further processing).
	 */
	this.encryptVote = function (vote, encryptionKey) {


	    var r = leemon.randBigIntInZq(encryptionSetting.q);
	    var a = leemon.powMod(encryptionSetting.g, r, encryptionSetting.p);
	    var b = leemon.powMod(encryptionKey, r, encryptionSetting.p);
	    b = leemon.multMod(b, vote, encryptionSetting.p);

	    var encVote = {firstvalue: leemon.bigInt2str(a, 10), secondvalue: leemon.bigInt2str(b, 10)};

	    return {encVote: encVote, r: r, a: a, b: b};
	}

//	/**
//	 * Asynchronous version of encryptVote.
//	 **/
//	this.encryptVoteAsync = function(vote, encryptionKey, doneCb, updateCb) {
//
//	    // step 2
//	    var a;
//	    var step2 = function(_a) {
//		a = _a;
//		leemon.powModAsync(encryptionKey, r, encryptionSetting.p, updateCb, step3);
//	    };
//
//	    // step 3
//	    var step3 = function(_b) {
//		var b = leemon.multMod(_b, vote, encryptionSetting.p);
//
//		var encVote = {firstvalue: leemon.bigInt2str(a, 10), secondvalue: leemon.bigInt2str(b, 10)};
//
//		doneCb({encVote: encVote, r: r, a: a, b: b});
//	    };
//
//	    // start with step 1
//	    var r = leemon.randBigIntInZq(encryptionSetting.q);
//	    leemon.powModAsync(encryptionSetting.g, r, encryptionSetting.p, updateCb, step2);
//	}

	/*
	 * Computes anonymous election verification key.
	 *
	 * @param generator - The election generator as bigInt.
	 * @param sk - Secret key as bigInt.
	 * @return Object with anonymous election verification key as string and bigInt.
	 */
	this.computeElectionVerificationKey = function (generator, sk) {
	    var vk = leemon.powMod(generator, sk, signatureSetting.p);
	    var vkString = leemon.bigInt2str(vk, 10);
	    return {vkString: vkString, vk: vk};
	}

//	/**
//	 * Asynchronous version of computeElectionVerificationKey.
//	 **/
//	this.computeElectionVerificationKeyAsync = function(generator, sk, doneCb, updateCb) {
//
//	    // step 2
//	    var step2 = function(_vk) {
//		var vkString = leemon.bigInt2str(_vk, 10);
//		doneCb({vkString: vkString, vk: _vk});
//	    };
//
//	    // start with step 1
//	    leemon.powModAsync(generator, sk, signatureSetting.p, updateCb, step2);
//	}

	/*
	 * Computes vote proof.
	 *
	 * @param r - The random value used in vote encryption as bigInt.
	 * @param a - The first value (public input) of the vote encryption as bigInt.
	 * @param vk - The verification key as bigInt.
	 * @return An object with the proof (univote_bfh_ch_common_proof) and the
	 * single proof values (t, c, s) as bigInt.
	 */
	this.computeVoteProof = function (r, a, vk) {

	    var result = this.NIZKP(encryptionSetting.p, encryptionSetting.q, encryptionSetting.g, r, a, leemon.bigInt2str(vk, 10));

	    var proof = {commitment: leemon.bigInt2str(result.t, 10), response: leemon.bigInt2str(result.s, 10)};

	    return proof;
	}

//	/**
//	 * Asynchronous version of computeVoteProof
//	 **/
//	this.computeVoteProofAsync = function(r, a, vk, doneCb, updateCb) {
//
//	    // done
//	    var nizkpDoneCb = function(result) {
//		var proof = {commitment: [leemon.bigInt2str(result.t, 10)], response: [leemon.bigInt2str(result.s, 10)]};
//
//		doneCb(proof);
//	    };
//
//	    this.NIZKPAsync(encryptionSetting.p, encryptionSetting.q, encryptionSetting.g, r, a, leemon.bigInt2str(vk, 10), nizkpDoneCb, updateCb);
//	}

	////////////////////////////////////////////////////////////////////////
	// Post signatures crypto
	////////////////////////////////////////////////////////////////////////

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
	this.signPost = function (post, generator, sk) {

	    //Hash post
	    var postHash = this.hashPost(post, false, false);

	    var paired = this.createSchnorrSignature(postHash, sk, signatureSetting.p, signatureSetting.q, generator)

	    return {sig: paired, sigString: leemon.bigInt2str(paired, 10)};
	}

	this.verifyResultSignature = function (resultContainer, posterSetting, verifyPosterSignature, callback) {
	    //1. Verify ResultContainer signature
	    //Currently this signature is not checked since most of the time, the result container contains only
	    //one post, so checking the post signature is sufficient

	    //2. Verify board signature of each Post
	    var posts = resultContainer.result.post;
	    for (var i = 0; i < posts.length; i++) {
		var post = posts[i];

		var postHash = this.hashPost(post, true, false);
		if (!this.verifySchnorrSignature(
			leemon.str2bigInt(post.beta.attribute[2].value.value, 10),
			postHash,
			leemon.str2bigInt(uvConfig.BOARD_SETTING.PK, 10),
			leemon.str2bigInt(uvConfig.BOARD_SETTING.P, 10),
			leemon.str2bigInt(uvConfig.BOARD_SETTING.Q, 10),
			leemon.str2bigInt(uvConfig.BOARD_SETTING.G, 10))) {
		    throw "Wrong board signature in post " + i;
		}

	    }

	    if (verifyPosterSignature == true) {
		//3. Verify poster signature of each Post
		for (var i = 0; i < posts.length; i++) {
		    var post = posts[i];

		    var postHash = this.hashPost(post, false, false);
		    if (!this.verifySchnorrSignature(
			    leemon.str2bigInt(post.alpha.attribute[2].value.value, 10),
			    postHash,
			    leemon.str2bigInt(posterSetting.PK, 10),
			    leemon.str2bigInt(posterSetting.P, 10),
			    leemon.str2bigInt(posterSetting.Q, 10),
			    leemon.str2bigInt(posterSetting.G, 10))) {
			throw "Wrong poster signature in post " + i;
		    }
		}
	    }
	    return true;
	}



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
	this.hashPost = function (post, includeBeta, includeBetaSignature) {
	    //Get message and alpha attributes
	    var message = post.message;
	    var alpha = post.alpha.attribute;


	    var messageHash = sha256.hashString(B64.decode(message));

	    var concatenatedAlphaHashes = ""
	    for (var i = 0; i < alpha.length; i++) {
		var attribute = alpha[i];

		if ((attribute.key === "signature" || attribute.key === "publickey") && includeBeta == false) {
		    //If includeBeta==false, we are checking or generating signature of poster (AccessControlled),
		    //thus signature and key of post itself must not be included
		    //If includeBeta==true, then we are checking signature of board (CertifiedPosting or CertifiedReading),
		    //thus signature and key must be included
		    continue;
		}

		if (attribute.value.type === "stringValue") {
		    concatenatedAlphaHashes += sha256.hashString(attribute.value.value);
		} else if (attribute.value.type === "integerValue") {
		    concatenatedAlphaHashes += sha256.hashInt(attribute.value.value);
		} else if (attribute.value.type === "dateValue") {
		    concatenatedAlphaHashes += sha256.hashDate(new Date(attribute.value.value));
		} else if (attribute.value.type === "integerValue") {
		    concatenatedAlphaHashes += sha256.hashByteArray(attribute.value.value);
		} else {
		    throw "Error: unknown type of alpha attribute.";
		}
	    }
	    var alphaHash = sha256.hashHexStr(concatenatedAlphaHashes);

	    if (includeBeta) {
		var betaHash = "";
		var beta = JSON.parse(JSON.stringify(post.beta.attribute).replace(/@/g, ""));
		var concatenatedBetaHashes = ""
		for (var i = 0; i < beta.length; i++) {
		    var attribute = beta[i];
		    if (attribute.key === "boardSignature" && includeBetaSignature == false) {
			//If includeBetaSignature==false, we are checking signature of board (CertifiedPosting),
			//thus signature of post itself must not be included
			//If includeBeta==true, then we are checking signature whole board result (CertifiedReading),
			//thus signature must be included
			continue;
		    }

		    if (attribute.value.type === "stringValue") {
			concatenatedBetaHashes += sha256.hashString(attribute.value.value);
		    } else if (attribute.value.type === "integerValue") {
			concatenatedBetaHashes += sha256.hashInt(attribute.value.value);
		    } else if (attribute.value.type === "dateValue") {
			concatenatedBetaHashes += sha256.hashDate(new Date(attribute.value.value));
		    } else if (attribute.value.type === "integerValue") {
			concatenatedBetaHashes += sha256.hashByteArray(attribute.value.value);
		    } else {
			throw "Error: unknown type of beta attribute.";
		    }
		}
		betaHash = sha256.hashHexStr(concatenatedBetaHashes);
		return sha256.hashHexStr(messageHash + alphaHash + betaHash);
	    } else {
		return sha256.hashHexStr(messageHash + alphaHash);
	    }
	}
    }

    window.uvCrypto = new Crypto();

})(window);
