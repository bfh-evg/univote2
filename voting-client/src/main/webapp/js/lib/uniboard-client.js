/* global leemon, CryptoJS, ubConfig, B64 */

////////////////////////////////////////////////////////////////////////
// Post signatures crypto
////////////////////////////////////////////////////////////////////////

/*
 * Error codes for the client
 *
 * "UBC-001", "Unexpected state of the client"
 * "UBC-002", "Invalid signature of the board in beta"
 * "UBC-003", "Invalid signature of the resultcontainer."
 * "UBC-004", "Invalid signature of the board for post: " + i
 * "UBC-005", "Invalid signature of the poster for post:" + i
 * "UBC-006", "Error: unknown identifier type! ('" + hashIdentifierType + "')."
 * "UBC-007", "Error: values can not be negative."
 * Note that there are more error codes that come from the http connection or the board.
 *
 */
function UBCLientError(code, message) {
	this.code = code;
	this.message = message;
}

UBCLientError.prototype = new Error();


(function (window) {

	var UBClient = function () {

		function ErrorResponse(code, message, type) {
			this.code = code;
			this.message = message;
			this.type = type;
		}



		var ErrorType = {
			HTTP: "http",
			BOARD: "board",
			CRYPTO: "crypto"
		};


		this.post = function (message, section, group, sk, p, q, g, successCB, errorCB) {

			//Create post
			var post = {
				message: message,
				alpha: [{"key": "section", "value": section}, {"key": "group", "value": group}],
				beta: []
			};
			//Sign post
			try {
				var sig = this.signPost(post, sk, p, q, g);

				post.alpha.push({"key": "signature", "value": sig});
				post.alpha.push({"key": "publickey", "value": sk});

				//For IE
				$.support.cors = true;
				//Ajax request
				$.ajax({
					url: ubConfig.URL_UNIBOARD_POST,
					type: 'POST',
					contentType: "application/json",
					accept: "application/json",
					cache: false,
					dataType: 'json',
					data: JSON.stringify(post),
					timeout: 10000,
					crossDomain: true,
					success: function (beta) {
						UBClient.processPost(beta, post, successCB, errorCB);
					},
					error: function (jqXHR, textStatus, errorThrown) {
						var errorObj = new ErrorResponse(errorThrown, textStatus, ErrorType.HTTP);
						errorCB(errorObj);
					}
				});
			} catch (ex) {
				var errorObj = new ErrorResponse(ex.code, ex.message, ErrorType.CRYPTO);
				errorCB(errorObj);
			}
		};

		this.get = function (query, successCB, errorCB, checkPosterSignature) {
			//For IE
			$.support.cors = true;
			//Ajax request
			$.ajax({
				url: ubConfig.URL_UNIBOARD_GET,
				type: 'POST',
				contentType: "application/json",
				accept: "application/json",
				cache: false,
				dataType: 'json',
				data: JSON.stringify(query),
				timeout: 10000,
				crossDomain: true,
				success: function (resultContainer) {
					UBClient.processGet(query, resultContainer, successCB, errorCB, checkPosterSignature);
				},
				error: function (jqXHR, textStatus, errorThrown) {
					var errorObj = new ErrorResponse(errorThrown, textStatus, ErrorType.HTTP);
					errorCB(errorObj);
				}

			});
		};

		this.procsessGet = function (query, resultContainer, successCB, errorCB, checkPosterSignature) {
			//Check if the board returned an error
			var error = $.grep(resultContainer.gamma, function (e) {
				return e.key === "error" || e.key === "rejected";
			});
			if (error > 0) {
				var code = error[0].value.substr(0, 7);
				var text = error[0].value.substr(8);
				var errorObj = new ErrorResponse(code, text, ErrorType.BOARD);
				errorCB(errorObj);
				return;
			}
			try {
				var check = this.verifyResultSignature(query, resultContainer, checkPosterSignature);
				if (check === true) {
					successCB(resultContainer);
				} else {
					var errorObj = new ErrorResponse("UBC-001", "Unexpected state of the client", ErrorType.CRYPTO);
					errorCB(errorObj);
				}
			} catch (ex) {
				var errorObj = new ErrorResponse(ex.code, ex.message, ErrorType.CRYPTO);
				errorCB(errorObj);
			}
		};


		this.processPost = function (beta, post, successCB, errorCB) {
			//Check if the board returned an error
			var error = $.grep(beta, function (e) {
				return e.key === "error" || e.key === "rejected";
			});
			if (error > 0) {
				var code = error[0].value.substr(0, 7);
				var text = error[0].value.substr(8);
				var errorObj = new ErrorResponse(code, text, ErrorType.BOARD);
				errorCB(errorObj);
				return;
			}
			//Validate beta signature
			post.beta = beta;
			try {
				var postHash = this.hashPost(post, true, false);

				if (this.verifySchnorrSignature(post.beta[2].value, postHash, leemon.str2bigInt(ubConfig.BOARD_SETTING.PK, ubConfig.BASE),
						leemon.str2bigInt(ubConfig.BOARD_SETTING.P, ubConfig.BASE),
						leemon.str2bigInt(ubConfig.BOARD_SETTING.Q, ubConfig.BASE),
						leemon.str2bigInt(ubConfig.BOARD_SETTING.G, ubConfig.BASE))) {
					//Return beta
					successCB(beta);
				} else {
					var errorObj = new ErrorResponse("UBC-002", "Invalid signature of the board in beta", ErrorType.CRYPTO);
					errorCB(errorObj);
				}
			} catch (ex) {
				var errorObj = new ErrorResponse(ex.code, ex.message, ErrorType.CRYPTO);
				errorCB(errorObj);
			}

		};

		/**
		 * Signs the post that will be posted on UniBoard using the given generator (Schnorr signature)
		 * @param post Message to be signed
		 * @param sk Private key used for signature
		 * @param p Prime p setting the group
		 * @param q Prime q setting the sub group
		 * @param generator Generator to be used in the signature
		 * @returns Signature as object containing the paired value as
		 * bigInt (sig) and its base {base} string representation (sigString)
		 */
		this.signPost = function (post, sk, p, q, generator) {

			//Hash post
			var postHash = this.hashPost(post, false, false);

			var paired = this.createSchnorrSignature(postHash, sk, p, q, generator);

			return {sig: paired, sigString: leemon.bigInt2str(paired, ubConfig.BASE)};
		};

		/**
		 * Verify the (Schnorr) signature of a result received from the board
		 * @param query The query sent to the board
		 * @param resultContainer The result received from the board
		 * @param verifyPosterSignature If true tries to also validate the signature of the autor of the post if
		 * his key is available in the config
		 * @returns True if signature is correct, error message otherwise
		 */
		this.verifyResultSignature = function (query, resultContainer, verifyPosterSignature) {
			var posts = resultContainer.result.post;

			// 1. Verify ResultContainer signature
			// Hash: [query,resultcontainer]
			//          query=...
			//          resultcontainer=[result,gamma]
			//                     result [p1,p2,...]
			//                              post=...
			//                     gamma=[timestamp]

			var queryHash = this.hashQuery(query);
			var resultHash = '';
			for (var i = 0; i < posts.length; i++) {
				resultHash += this.hashPost(posts[i], true, true);
			}
			var gamma = Hash.doDate(new Date(resultContainer.gamma[0].value));
			var resultContainerHash = Hash.doHexStr(Hash.doHexStr(resultHash) + Hash.doHexStr(gamma));
			var hash = Hash.doHexStr(queryHash + resultContainerHash);
			if (!this.verifySchnorrSignature(
					leemon.str2bigInt(resultContainer.gamma[1].value, ubConfig.BASE),
					hash,
					leemon.str2bigInt(ubConfig.BOARD_SETTING.PK, ubConfig.BASE),
					leemon.str2bigInt(ubConfig.BOARD_SETTING.P, ubConfig.BASE),
					leemon.str2bigInt(ubConfig.BOARD_SETTING.Q, ubConfig.BASE),
					leemon.str2bigInt(ubConfig.BOARD_SETTING.G, ubConfig.BASE))) {
				//Return error
				throw new UBCLientError("UBC-003", "Invalid signature of the resultcontainer.");
			}

			// 2. Verify board signature of each Post
			var searchFunction = function (e) {
				return e.PK === posterPK;
			};
			for (var i = 0; i < posts.length; i++) {
				var post = posts[i];
				var postHash = this.hashPost(post, true, false);
				if (!this.verifySchnorrSignature(
						leemon.str2bigInt(post.beta[2].value, ubConfig.BASE),
						postHash,
						leemon.str2bigInt(ubConfig.BOARD_SETTING.PK, ubConfig.BASE),
						leemon.str2bigInt(ubConfig.BOARD_SETTING.P, ubConfig.BASE),
						leemon.str2bigInt(ubConfig.BOARD_SETTING.Q, ubConfig.BASE),
						leemon.str2bigInt(ubConfig.BOARD_SETTING.G, ubConfig.BASE))) {
					//Return error
					throw new UBCLientError("UBC-004", "Invalid signature of the board for post: " + i);
				}
				if (verifyPosterSignature === true) {
					var postHash2 = this.hashPost(post, false, false);
					var posterPK = post.alpha[3];
					//3. Check if PosterPK in known authors? if yes check signature
					var result = $.grep(ubConfig.KNOWN_AUTHORS, searchFunction);

					if (result.length === 1) {
						var posterSetting = result[0];
						if (!this.verifySchnorrSignature(
								leemon.str2bigInt(post.alpha[2].value, ubConfig.BASE),
								postHash2,
								leemon.str2bigInt(posterSetting.PK, ubConfig.BASE),
								leemon.str2bigInt(posterSetting.P, ubConfig.BASE),
								leemon.str2bigInt(posterSetting.Q, ubConfig.BASE),
								leemon.str2bigInt(posterSetting.G, ubConfig.BASE))) {
							//Return error
							throw new UBCLientError("UBC-005", "Invalid signature of the poster for post:" + i);
						}
					}
				}
			}

			return true;
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

			var a = signatureValues[0];
			var b = signatureValues[1];

			var c = leemon.powMod(g, b, p);
			var d = leemon.powMod(publicKey, a, p);

			var a2Verif = leemon.multMod(c, leemon.inverseMod(d, p), p);
			var bVerif = Hash.doHexStr(messageHash + Hash.doBigInt(a2Verif));

			return leemon.equals(a, leemon.mod(leemon.str2bigInt(bVerif, 16), q));
		};

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
		 * Hashes an identifier.
		 * @param {identifier} Identifier
		 * @returns Hash of the identifier
		 */
		var hashIdentifier = function (identifier) {
			var hashIdentifierType = identifier.type;
			var hashIdentifier = Hash.doString(hashIdentifierType);
			if (hashIdentifierType === "propertyIdentifier") {
				hashIdentifier += Hash.doString(identifier.propertyType);
				hashIdentifier += Hash.doString(identifier.key);
			} else if (hashIdentifierType === "messageIdentifier") {
				hashIdentifier += Hash.doString(identifier.keyPath);
			} else
			{
				throw new UBCLientError("UBC-006", "Error: unknown identifier type! ('" + hashIdentifierType + "').");
			}
			return hashIdentifier;
		};

		/**
		 * Helper to comupte the hash of a query.
		 *
		 * @param {type} query
		 * @returns The hash of the query
		 */
		this.hashQuery = function (query) {
			// query=[contraints,order,limit]
			//          constraint=[type,identifier,value]
			//          order=[identifier,ascDesc]

			var hashConstraints = '';
			for (var i = 0; i < query.constraint.length; i++) {
				hashConstraints += Hash.doHexStr(this.hashConstraint(query.constraint[i]));
			}
			//Hash oders
			var hashOrders = '';
			for (var i = 0; i < query.order.length; i++) {
				var order = query.order[i];
				var hashOrder = Hash.doHexStr(hashIdentifier(order.identifier));
				hashOrder += Hash.doBoolean(order.ascDesc);
				hashOrders += Hash.doHexStr(hashOrder);
			}
			//Hash Limit
			var hashLimit = Hash.doInt(0);
			return Hash.doHexStr(Hash.doHexStr(hashConstraints) + Hash.doHexStr(hashOrders) + hashLimit);
		};

		/**
		 * Helper to comupte the hash of a constraint.
		 *
		 * @param {type} constraint
		 * @returns The hash of the constraint
		 */
		this.hashConstraint = function (constraint) {
			var hashConstraint = Hash.doString(constraint.type);
			//Hash identifier
			hashConstraint += Hash.doHexStr(hashIdentifier(constraint.identifier));
			//Check if a dataType is set. If yes add it to the hash
			if (typeof constraint.dataType !== 'undefined') {
				hashConstraint += Hash.doString(constraint.dataType);
			}
			//Hash value based on type
			if (constraint.type === 'between') {
				hashConstraint += Hash.doString(constraint.value);
			} else if (constraint.type === 'equal') {
				hashConstraint += Hash.doString(constraint.value);
			} else if (constraint.type === 'greater') {
				hashConstraint += Hash.doString(constraint.value);
			} else if (constraint.type === 'greaterEqual') {
				hashConstraint += Hash.doString(constraint.value);
			} else if (constraint.type === 'in') {
				var hashInValues = '';
				for (var i = 0; i < constraint.element; i++) {
					hashInValues += hashInValues.doString(constraint.element[i]);
				}
				hashConstraint += Hash.doHexStr(hashInValues);
			} else if (constraint.type === 'less') {
				hashConstraint += Hash.doString(constraint.value);
			} else if (constraint.type === 'lessEqual') {
				hashConstraint += Hash.doString(constraint.value);
			} else if (constraint.type === 'notEqual') {
				hashConstraint += Hash.doString(constraint.value);
			}
			return hashConstraint;
		};

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
			var alpha = post.alpha;
			var messageHash = this.hashPostMessage(message);
			var concatenatedAlphaHashes = "";

			for (var i = 0; i < alpha.length; i++) {
				var alphaAttribute = alpha[i];
				if ((alphaAttribute.key === "signature" || alphaAttribute.key === "publickey") && includeBeta === false) {
					//If includeBeta==false, we are checking or generating signature of poster (AccessControlled),
					//thus signature and key of post itself must not be included
					//If includeBeta==true, then we are checking signature of board (CertifiedPosting or CertifiedReading),
					//thus signature and key must be included
					continue;
				}
				concatenatedAlphaHashes += hashAttribute(alphaAttribute);
			}
			var alphaHash = Hash.doHexStr(concatenatedAlphaHashes);
			var betaHash = '';
			if (includeBeta) {
				//var beta = JSON.parse(JSON.stringify(post.beta.attribute).replace(/@/g, ""));
				var beta = post.beta;
				var concatenatedBetaHashes = "";
				for (var i = 0; i < beta.length; i++) {
					var betaAttribute = beta[i];
					if (betaAttribute.key === "boardSignature" && includeBetaSignature === false) {
						//If includeBetaSignature==false, we are checking signature of board (CertifiedPosting),
						//thus signature of post itself must not be included
						//If includeBeta==true, then we are checking signature whole board result (CertifiedReading),
						//thus signature must be included
						continue;
					}
					concatenatedBetaHashes += hashAttribute(betaAttribute);
				}
				betaHash = Hash.doHexStr(concatenatedBetaHashes);
			}
			return Hash.doHexStr(messageHash + alphaHash + betaHash);
		};

		/**
		 * Hashes a typed value.
		 * @param {type} Typed value
		 * @returns Hash of the typed value
		 */
		var hashAttribute = function (attribute) {
			var aHash = '';
			aHash += Hash.doString(attribute.key);
			aHash += Hash.doString(attribute.value);

			//Check if a dataType is set. If yes add it to the hash
			if (typeof attribute.dataType !== 'undefined') {
				aHash += Hash.doString(attribute.dataType);
			}

			return Hash.doHexStr(aHash);
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
		/**
		 * Computes the elegant pairing function for two non-negative BigInteger values.
		 * @see http://szudzik.com/ElegantPairing.pdf
		 * @param bigInt1 The first value
		 * @param bigInt2 The second value
		 * @return The result of applying the elegant pairing function
		 */
		this.pair = function (bigInt1, bigInt2) {
			if (leemon.negative(bigInt1) || leemon.negative(bigInt2)) {
				throw new UBCLientError("UBC-007", "Error: values can not be negative.");
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
	};
	window.UBClient = UBClient();
})(window);
(function (window) {

	var Hash = function () {

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
		 * Hashes a boolean
		 * returns a hex representation of the hash
		 */
		this.doBoolean = function (boolean) {
			if (boolean) {
				return this.doString("true");
			}
			return this.doString("false");
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
			if (hexStr.length % 2 !== 0) {
				hexStr = "0" + hexStr;
			}
			var hash = hashMethod(CryptoJS.enc.Hex.parse(hexStr));
			return hash.toString(CryptoJS.enc.Hex).toUpperCase();
		};
	};

	window.Hash = Hash();

})(window);