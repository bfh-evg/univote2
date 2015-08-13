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

	// Check for leemon and configuration. If they aren't loaded,
	// an error message is displayed at the top of the page.
	if (!leemon || !uvConfig) {
		window.onload = function () {
			var body = document.getElementsByTagName('body')[0];
			var errorDiv = document.createElement('div');
			errorDiv.setAttribute('style', 'background-color:red; z-index:1000; position:absolute; top:0; left: 0; width: 100%; height:50px; text-align:center; font-weight:bold; padding-top: 20px;');
			errorDiv.innerHTML = "<p>ERROR: Missing JS library! UniVote won't be running as either leemon, seedrandom or configuration is missing.</p>";
			body.appendChild(errorDiv);
		};
		return;
	}


	/**
	 * Constructor.
	 */
	function Crypto() {

////////////////////////////////////////////////////////////////////////
// Configuration
////////////////////////////////////////////////////////////////////////


		// Signature setting (set at runtime)
		this.signatureSetting = {};

		// Encryption setting (set at runtime)
		this.encryptionSetting = {};

		/**
		 * Sets the encryption parameters.
		 *
		 * @param {type} setting
		 */
		this.setEncryptionParameters = function (setting) {
			encryptionSetting = {
				setting: setting,
				p: leemon.str2bigInt(setting.P, setting.BASE, 1),
				q: leemon.str2bigInt(setting.Q, setting.BASE, 1),
				g: leemon.str2bigInt(setting.G, setting.BASE, 1)
			};
		};

		/**
		 * Sets the signature parameters.
		 *
		 * @param {type} setting
		 */
		this.setSignatureParameters = function (setting) {
			this.signatureSetting = {setting: setting,
				p: leemon.str2bigInt(setting.P, setting.BASE, 1),
				q: leemon.str2bigInt(setting.Q, setting.BASE, 1),
				g: leemon.str2bigInt(setting.G, setting.BASE, 1)
			};
		};


		/**
		 * Sets the hash parameters.
		 *
		 * @param {type} setting
		 */
		this.setHashParameters = function (setting) {
			Hash.setHashMethod(setting.STANDARD);
		};


////////////////////////////////////////////////////////////////////////
// Non-interactive zero-knowledge proof
////////////////////////////////////////////////////////////////////////

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

			//3. Compute c = h((publicInput, t), otherInput) = H(H(H(publicInput)||H(t))||H(otherInput))
			//3.1 Hash of public input
			var hashPI = Hash.doBigInt(publicInput);
			//3.2 Hash of commitment
			var hashCommitment = Hash.doBigInt(t);
			//3.3 Hash of the hash of public input concatenated with hash of commitment
			//(Steps 3.1 to 3.3 are the computation of to the recursive hash of a Pair[publicInput, Commitment] in UniCrypt)
			var hashPIAndCommitment = Hash.doHexStr(hashPI + hashCommitment);
			//3.4 Hash of other input
			var hashOtherInput = Hash.doString(otherInput);
			//3.5 Hash of hashPIAndCommitment concatenated with hashOtherInput
			//(Steps 3.1 to 3.5 are the computation of to the recursive hash of a Pair[Pair[publicInput, Commitment], otherInput] in UniCrypt)
			var cStr = Hash.doHexStr(hashPIAndCommitment + hashOtherInput);
			var c = leemon.mod(leemon.str2bigInt(cStr, 16, 1), q);

			//4. Compute s = omega+c*secretInput mod q
			var s = leemon.mod(leemon.add(omega, leemon.multMod(c, secretInput, q)), q);

			// 5. Return proof
			return {t: t, c: c, s: s};
		};

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
			var a2Hash = Hash.doBigInt(a2);
			var aStr = Hash.doHexStr(messageHash + a2Hash);
			var a = leemon.mod(leemon.str2bigInt(aStr, 16), q);

			var b = leemon.add(r, leemon.mult(a, privateKey));
			b = leemon.mod(b, q);

			return this.pair(a, b);
		};


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
			var bVerif = Hash.doHexStr(messageHash + Hash.doBigInt(a2Verif));

			return leemon.equals(b, leemon.mod(leemon.str2bigInt(bVerif, 16), q));
		};

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
		};

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
		};


		// This is a helper method to compute the integer square root of a BigInteger value using Newton's algorithm
		this.isqrt = function (bigInt) {
			// special case
			if (leemon.isZero(bigInt)) {
				return leemon.str2bigInt("0", 10);
			}

			// first guess
			var one = leemon.str2bigInt("1", 10, bigInt.length);
			var current = leemon.leftShift(one, leemon.bitSize(bigInt) / 2 + 1);
			var last;
			do {
				last = current;
				current = leemon.rightShift(leemon.add(last, leemon.divide(bigInt, last)), 1);
			}
			while (leemon.greater(last, current) === 1);
			return last;
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
		};

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
		};

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
		};

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
		};

		/**
		 * Compute proof of knowledge of private key
		 * @param {type} p Value of prime p as bigInt
		 * @param {type} q Value of prime q as bigInt
		 * @param {type} g Value of cyclic group generator as bigInt
		 * @param {type} sk Value of private key as bigInt
		 * @param {type} vk Value of verification key as bigInt
		 * @param {type} otherInput Additional data that must be hashed in proof as string
		 * @returns Proof as object containing t (commitment), c (challange) and s (response)
		 * as base {uvConfig.BASE} strings.
		 */
		this.computeVerificationKeyProof = function (p, q, g, sk, vk, otherInput) {
			var result = this.NIZKP(p, q, g, sk, vk, otherInput);
			var proof = {
				"t": leemon.bigInt2str(result.t, uvConfig.BASE),
				"c": leemon.bigInt2str(result.c, uvConfig.BASE),
				"s": leemon.bigInt2str(result.s, uvConfig.BASE)
			};
			return proof;
		};

		/**
		 * Compute a RSA signature proving knowledge of private key
		 * @param {type} sk Value of private key as bigInt
		 * @param {type} vk Value of public key as bigInt
		 * @param {type} modulo RSA modulus as bigInt
		 * @param {type} message Message to sign as string
		 * @returns a bigInt representing the signature
		 */
		this.computeRSASignature = function (sk, vk, modulo, message) {
			//hash the message
			var hashedMessage = Hash.doString(message); //base 16 encoded string
			//Create a BigInteger with the hashedMessage
			var messageBigInt = leemon.str2bigInt(hashedMessage, 16);
			//Computes the new BigInt modulo n since RSA message space in between 0 and n-1
			var messageBigIntMod = leemon.mod(messageBigInt, modulo);
			return leemon.powMod(messageBigIntMod, sk, modulo);
		};

////////////////////////////////////////////////////////////////////////
// Secret key enc- and decryption
////////////////////////////////////////////////////////////////////////

		/**
		 * Encrypts a secret key. The key is padded with PRIVATE_KEY_PREFIX/-POSTFIX
		 * before it is encrypted using AES.
		 * The key used is derived from the password.
		 * Finally, the encrypted key (encoded in base 64) is
		 * padded with ENC_PRIVATE_KEY_PREFIX/-POSTFIX.
		 *
		 * @param sk - The secret key as string (leemon base64).
		 * @param password - The password used for encryption.
		 * @return Encrytped and padded secret key as string.
		 */
		this.encryptSecretKeyAES = function (sk, password) {
			// 0. Transform key to base 16
			sk = leemon.bigInt2str(leemon.str2bigInt(sk, 64), 16)

			// 1. Add pre- and postfix to key
			var key = uvConfig.PRIVATE_KEY_PREFIX + sk + uvConfig.PRIVATE_KEY_POSTFIX;

			// 2. Create a salt of exactly 128 bits
			var salt = CryptoJS.lib.WordArray.random(128 / 8);
			var saltB64 = CryptoJS.enc.Base64.stringify(salt);

			// 3. Derivate key from password
			var symKey = CryptoJS.PBKDF2(password, salt, {keySize: uvConfig.SYM_KEY_SIZE / 32, iterations: uvConfig.PWD_KEY_DERIVATION_ITERATION});

			// 4. Create an IV of exactly 128 bits
			var iv = CryptoJS.lib.WordArray.random(128 / 8);
			var ivB64 = CryptoJS.enc.Base64.stringify(iv);

			// 5. Encrypt key
			var encrypted = CryptoJS.AES.encrypt(key, symKey, {iv: iv});

			// 7. Pad encrypted key with pre- and postfix and add salt and iv
			encrypted = uvConfig.ENC_PRIVATE_KEY_PREFIX + '\n' + saltB64 + ivB64 + encrypted + '\n' + uvConfig.ENC_PRIVATE_KEY_POSTFIX;

			return encrypted;
		};

		/**
		 * Decrypts an encrypted secret key (counterpart to encryptSecretKey).
		 * If the key is not properly padded or the password is wrong then
		 * the error callback is called with a string denoting the error.
		 *
		 * @param key - Encrypted and padded secret key as string.
		 * @param password - The password used for encryption.
		 * @return Secret key as bigInt or false on error.
		 */
		this.decryptSecretKeyAES = function (key, password) {

			// Cleans a string (removes all special charaters but =, -, _, +, /)
			function cleanStr(str) {
				return str.replace(/[^\w=\-\+\/]/gi, '');
			}

			//Transform a hex string to alphabetical string
			function hex2a(hexx) {
				var hex = hexx.toString(); //force conversion
				var str = '';
				for (var i = 0; i < hex.length; i += 2)
					str += String.fromCharCode(parseInt(hex.substr(i, 2), 16));
				return str;
			}

			// 1. Check and erase pre- and postfix
			var keyC = key.replace(/[-]*/g, "");
			keyC = keyC.replace(uvConfig.ENC_PRIVATE_KEY_PREFIX.replace(/[-]*/g, ""), "");
			keyC = keyC.replace(uvConfig.ENC_PRIVATE_KEY_POSTFIX.replace(/[-]*/g, ""), "");
			keyC = cleanStr(keyC);

			// 2. extract salt and iv (128 bits => 24 base64 chars)
			var salt = keyC.substring(0, 24);
			salt = CryptoJS.enc.Base64.parse(salt);
			var iv = keyC.substring(24, 48);
			iv = CryptoJS.enc.Base64.parse(iv);

			// 3. Extract encrypted key
			var encrypted = keyC.substring(48);

			// 4. Derivate key from password
			var symKey = CryptoJS.PBKDF2(password, salt, {keySize: uvConfig.SYM_KEY_SIZE / 32, iterations: uvConfig.PWD_KEY_DERIVATION_ITERATION});

			// 5. Decrypt private key with symetric key
			var decrypted = hex2a(CryptoJS.AES.decrypt(encrypted, symKey, {iv: iv}));

			// 6. Check and erase pre- and postfix
			pattern = new RegExp(uvConfig.PRIVATE_KEY_PREFIX + "([0-9A-Za-z=_+/]*)" + uvConfig.PRIVATE_KEY_POSTFIX);
			match = pattern.exec(decrypted);
			if (match == null || match.length != 2) {
				return false;
			}

			// 7. Finally return sk in leemon base 16 format
			return leemon.str2bigInt(match[1], 16);
		};



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

			return this.encryptSecretKeyAES(sk, password);
		};

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

			return this.decryptSecretKeyAES(key, password, errorCb);
		};


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
		 * @param electionDetails
		 * @return The encoded vote as bigInt.
		 */
		this.encodeVote = function (electionDetails) {

			// The encoded vote as binary string
			var bitstring = "";

			// Loop through all options and add the according bits to bitstring
			for (var id in electionDetails.getOptions()) {

				var upperBound = electionDetails.getOptionUpperBound(id);
				if (upperBound == -1) {
					throw new Error("Encoding error: No upper bound for option " + id + ".");
				}
				// Compute the number of bits needed to encode the option
				var nbrBits = Math.floor((Math.log(upperBound)) / (Math.log(2))) + 1;
				// Get the occurences of actual id in vote
				var votes = electionDetails.getChoice(id);

				// Represent the number in binary number
				var votesBin = votes.toString(2);
				// Check what never should be the case!
				if (votesBin.length > nbrBits) {
					throw new Error("Encoding error: Too many votes for option!");
				}
				// Add front padding to binary representation
				while (votesBin.length < nbrBits) {
					votesBin = "0" + votesBin;
				}

				// Construct the bit string
				// -> actual choice is added at the left of precedent choices
				bitstring = votesBin + bitstring;
			}

			// Return the encoded vote as bigInt
			return leemon.str2bigInt(bitstring, 2, 1);
		};


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
		};

		/*
		 * Encrypts an encoded vote.
		 *
		 * @param vote - The vote as bigInt.
		 * @param encryptionKey - The encryption key as bigInt.
		 * @return An object with the encoded vote (values as base {uvConfig.BASE} string) and
		 * the single values (r, a, b) of the encryption as bigInt (for further processing).
		 */
		this.encryptVote = function (vote, encryptionKey) {

			var r = leemon.randBigIntInZq(encryptionSetting.q);
			var a = leemon.powMod(encryptionSetting.g, r, encryptionSetting.p);
			var b = leemon.powMod(encryptionKey, r, encryptionSetting.p);
			b = leemon.multMod(b, vote, encryptionSetting.p);
			var encVote = {
				firstvalue: leemon.bigInt2str(a, uvConfig.BASE),
				secondvalue: leemon.bigInt2str(b, uvConfig.BASE)};
			return {encVote: encVote, r: r, a: a, b: b};
		};

		/*
		 * Computes anonymous election verification key.
		 *
		 * @param generator - The election generator as bigInt.
		 * @param sk - Secret key as bigInt.
		 * @return Object with anonymous election verification key as
		 * base {uvConfig.BASE} string and bigInt.
		 */
		this.computeElectionVerificationKey = function (generator, sk) {
			var vk = leemon.powMod(generator, sk, this.signatureSetting.p);
			var vkString = leemon.bigInt2str(vk, uvConfig.BASE);
			return {vkString: vkString, vk: vk};
		};

		/*
		 * Computes vote proof.
		 *
		 * @param r - The random value used in vote encryption as bigInt.
		 * @param a - The first value (public input) of the vote encryption as bigInt.
		 * @param vk - The verification key as bigInt.
		 * @return An object representing the proof-triple (t, c, s) as base
		 * {uvConfig.BASE} string values.
		 */
		this.computeVoteProof = function (r, a, vk) {

			var result = this.NIZKP(encryptionSetting.p, encryptionSetting.q, encryptionSetting.g, r, a, leemon.bigInt2str(vk, uvConfig.BASE));
			var proof = {
				commitment: leemon.bigInt2str(result.t, uvConfig.BASE),
				challenge: leemon.bigInt2str(result.c, uvConfig.BASE),
				response: leemon.bigInt2str(result.s, uvConfig.BASE)
			};
			return proof;
		};

////////////////////////////////////////////////////////////////////////
// Post signatures crypto
////////////////////////////////////////////////////////////////////////

		/**
		 * Signs the post that will be posted on UniBoard using hte given generator (Schnorr signature)
		 * @param post Message to be signed
		 * @param generator Generator to be used in the signature
		 * @param sk Private key used for signature
		 * @returns Signature as object containing the paired value as
		 * bigInt (sig) and its base {uvConfig.BASE} string representation (sigString)
		 */
		this.signPost = function (post, generator, sk) {

			//Hash post
			var postHash = this.hashPost(post, false, false);
			var paired = this.createSchnorrSignature(postHash, sk, this.signatureSetting.p, this.signatureSetting.q, generator)

			return {sig: paired, sigString: leemon.bigInt2str(paired, uvConfig.BASE)};
		};

		/**
		 * Verify the (Schnorr) signature of a result received from the board
		 * @param resultContainer The result received from the board
		 * @param posterSetting Crypto setting of the poster
		 * @param verifyPosterSignature True if signature of poster of the posts contained in the result, fals if not
		 * @returns True if signature is correct, exception otherwise
		 */
		this.verifyResultSignature = function (resultContainer, posterSetting, verifyPosterSignature) {
			//1. Verify ResultContainer signature
			//Currently this signature is not checked since most of the time, the result container contains only
			//one post, so checking the post signature is sufficient

			//2. Verify board signature of each Post
			var posts = resultContainer.result.post;
			for (var i = 0; i < posts.length; i++) {
				var post = posts[i];
				var postHash = this.hashPost(post, true, false);
				if (!this.verifySchnorrSignature(
						leemon.str2bigInt(post.beta.attribute[2].value.value, uvConfig.BASE),
						postHash,
						leemon.str2bigInt(uvConfig.BOARD_SETTING.PK, uvConfig.BASE),
						leemon.str2bigInt(uvConfig.BOARD_SETTING.P, uvConfig.BASE),
						leemon.str2bigInt(uvConfig.BOARD_SETTING.Q, uvConfig.BASE),
						leemon.str2bigInt(uvConfig.BOARD_SETTING.G, uvConfig.BASE))) {
					throw "Wrong board signature in post " + i;
				}

			}

			//3. Verify poster signature of each Post contained in the result
			if (verifyPosterSignature == true) {
				for (var i = 0; i < posts.length; i++) {
					var post = posts[i];
					var postHash = this.hashPost(post, false, false);
					if (!this.verifySchnorrSignature(
							leemon.str2bigInt(post.alpha.attribute[2].value.value, uvConfig.BASE),
							postHash,
							leemon.str2bigInt(posterSetting.PK, uvConfig.BASE),
							leemon.str2bigInt(posterSetting.P, uvConfig.BASE),
							leemon.str2bigInt(posterSetting.Q, uvConfig.BASE),
							leemon.str2bigInt(posterSetting.G, uvConfig.BASE))) {
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
			var messageHash = this.hashPostMessage(message);
			var concatenatedAlphaHashes = "";

			var hashAttribute = function (attribute) {
				var aHash = '';
				var type = attribute.value.type;
				var value = attribute.value.value;
				if (type === "stringValue") {
					aHash = Hash.doString(value);
				} else if (type === "integerValue") {
					aHash = Hash.doInt(value);
				} else if (type === "dateValue") {
					aHash = Hash.doDate(new Date(value));
				} else if (type === "integerValue") {
					aHash = Hash.doByteArray(value);
				} else {
					throw "Error: unknown type of attribute ('" + type + "').";
				}
				return aHash;
			};

			for (var i = 0; i < alpha.length; i++) {
				var attribute = alpha[i];
				if ((attribute.key === "signature" || attribute.key === "publickey") && includeBeta == false) {
					//If includeBeta==false, we are checking or generating signature of poster (AccessControlled),
					//thus signature and key of post itself must not be included
					//If includeBeta==true, then we are checking signature of board (CertifiedPosting or CertifiedReading),
					//thus signature and key must be included
					continue;
				}
				concatenatedAlphaHashes += hashAttribute(attribute);
			}
			var alphaHash = Hash.doHexStr(concatenatedAlphaHashes);
			var betaHash = '';
			if (includeBeta) {
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
					concatenatedBetaHashes += hashAttribute(attribute);
				}
				betaHash = Hash.doHexStr(concatenatedBetaHashes);
			}
			return Hash.doHexStr(messageHash + alphaHash + betaHash);
		};

		/**
		 * Helper method computing the hash value of the post message
		 *
		 * @param message - The Base64 encoded message of the post
		 * @return The hash value of the message.
		 */
		this.hashPostMessage = function (message) {
			return Hash.doString(B64.decode(message));
		};
	}



	window.uvCrypto = new Crypto();

})(window);


(function (window) {

	var Hash = new function () {

		// Default hash method
		var hashMethod = CryptoJS.SHA256;

		this.setHashMethod = function (method) {
			switch (method) {
				case 'SHA-1':
					hashMethod = CryptoJS.SHA1;
					break;
				case 'SHA-224':
					hashMethod = CryptoJS.SHA224;
					break;
				case 'SHA-256':
					hashMethod = CryptoJS.SHA256;
					break;
				case 'SHA-384':
					hashMethod = CryptoJS.SHA384;
					break;
				case 'SHA-512':
					hashMethod = CryptoJS.SHA512;
					break;
				default:
					// Unsupported!
					console.log("Unsupported hash algorithm: '" + method + "'");
			}
		};

		/*
		 * Hashes a UTF-8 string
		 * returns a hex representation of the hash
		 */
		this.doString = function (msg) {
			var hash = hashMethod(msg);
			return hash.toString(CryptoJS.enc.Hex).toUpperCase();
		};

		/*
		 * Hashes a leemon BigInteger
		 * returns a hex representation of the hash
		 */
		this.doBigInt = function (bigInteger) {

			//In UniCrypt (Java) the BigInteger as considered as positive before being hashed (0s are added in front of the byte
			//array in case of a negative big int. So, we do the same here
			var hexStr = leemon.bigInt2str(bigInteger, 16);

			if (parseInt(hexStr.substr(0, 2), 16) > 127) {
				hexStr = "0" + hexStr;
			}

			return this.doHexStr(hexStr);
		};

		/*
		 * Hashes an integer
		 * returns a hex representation of the hash
		 */
		this.doInt = function (long) {

			var bigint = leemon.int2bigInt(long, 1);
			return this.doBigInt(bigint);
		};

		/*
		 * Hashes a date
		 * Computes the hash of the ISO format without milliseconds
		 * returns a hex representation of the hash
		 */
		this.doDate = function (date) {

			var dateFormatted = date.toISOString();

			//Workaround because current Java implementation does not includes milliseconds
			dateFormatted = dateFormatted.substring(0, dateFormatted.length - 5) + "Z";

			return this.doString(dateFormatted);
		};

		/*
		 * Hashes a base 64 representation of a byte array
		 * returns a hex representation of the hash
		 */
		this.doByteArray = function (base64ByteArray) {

			var byteArray = B64.decode(base64ByteArray);
			return this.doBigInt(byteArray);
		};

		/*
		 * Hashes a hexadecimal representation
		 * returns a hex representation of the hash
		 */
		this.doHexStr = function (hexStr) {

			// If the length of the string is not a multiple of 2, "0" is added at the
			// beginning of the string.
			// Reason: CryptoJS.enc.Hex.parse('ABC').toString() results in 'AB0C'!
			if (hexStr.length % 2 != 0) {
				hexStr = "0" + hexStr;
			}
			var hash = hashMethod(CryptoJS.enc.Hex.parse(hexStr));
			return hash.toString(CryptoJS.enc.Hex).toUpperCase();
		};
	};

	window.Hash = Hash;

})(window);