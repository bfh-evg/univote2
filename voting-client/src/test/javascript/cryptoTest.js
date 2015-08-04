describe('encodeVoteTest', function () {
	it('encoding each choice on the same number of bits (one rule)', function () {

		var electionDetails = {
			"options": [
				{"id": 1, "type": "candidateOption"},
				{"id": 2, "type": "candidateOption"},
				{"id": 3, "type": "candidateOption"},
				{"id": 4, "type": "candidateOption"},
				{"id": 5, "type": "candidateOption"},
				{"id": 6, "type": "candidateOption"}
			],
			"rules": [
				{
					"id": 1,
					"type": "cumulation",
					"optionIds": [1, 2, 3, 4, 5, 6],
					"lowerBound": 0,
					"upperBound": 3
				}
			]
		};
		var details = new ElectionDetails(electionDetails);
		details.addChoice(1, 2);
		details.addChoice(3, 1);
		details.addChoice(5, 1);
		details.addChoice(6, 3);

		//every choice is represented on 2 bits
		//11|01|00|01|00|10 => 3346
		//=> 2^11 + 2^10 + 2^8 + 2^4 + 2^1 = 3346 => (3346,0)
		expect(uvCrypto.encodeVote(details).toString()).toEqual("3346,0");
	});

	it('encoding each choice on different number of bits (two rules)', function () {
		var electionDetails = {
			"options": [
				{"id": 1},
				{"id": 2},
				{"id": 3},
				{"id": 4},
				{"id": 5},
				{"id": 6}
			],
			"rules": [
				{
					"id": 1,
					"type": "cumulation",
					"optionIds": [1, 2, 5, 6],
					"lowerBound": 0,
					"upperBound": 34
				},
				{
					"id": 2,
					"type": "cumulation",
					"optionIds": [3, 4],
					"lowerBound": 0,
					"upperBound": 12
				}
			]
		};
		var details = new ElectionDetails(electionDetails);
		details.addChoice(1, 2);
		details.addChoice(3, 1);
		details.addChoice(5, 1);
		details.addChoice(6, 3);

		//=> 202379266
		//   c6  |  c5  | c4 | c3  |  c2  |  c1
		//00 0011|000001|0000|0 001|000000|000010
		//-- ------------------ -----------------
		// 0       6176               4098
		expect(uvCrypto.encodeVote(details).toString()).toEqual("4098,6176,0");
	});

	it('choice id not in rule => exception', function () {

		var electionDetails = {
			"options": [
				{"id": 1},
				{"id": 2},
				{"id": 3},
				{"id": 4},
				{"id": 5},
				{"id": 6}
			],
			"rules": [
				{
					"id": 1,
					"type": "cumulation",
					"optionIds": [1, 2, 4, 5, 6],
					"lowerBound": 0,
					"upperBound": 3
				}
			]
		};
		var details = new ElectionDetails(electionDetails);
		details.addChoice(1, 2);
		details.addChoice(3, 1);
		details.addChoice(5, 1);
		details.addChoice(6, 3);

		var testFn = function () {
			uvCrypto.encodeVote(details);
		};

		//=> error
		expect(testFn).toThrow(new Error("Encoding error: No upper bound for option 3."));
	});

	it('More votes for a candidate than allowd by rule (-> should never be the case, but in case of wrong rule checking...)', function () {

		var electionDetails = {
			"options": [
				{"id": 1},
				{"id": 2}
			],
			"rules": [
				{
					"id": 1,
					"type": "cumulation",
					"optionIds": [1, 2],
					"lowerBound": 0,
					"upperBound": 3
				}
			]
		};
		var details = new ElectionDetails(electionDetails);
		details.addChoice(1, 2);
		details.addChoice(2, 6);

		var testFn = function () {
			uvCrypto.encodeVote(details);
		};
		//=> error
		expect(testFn).toThrow(new Error("Encoding error: Too many votes for option!"));
	});
});


describe('votingKeyEncryption', function () {
	it('encrypt/decrypt voting key', function () {

		var error = '';

		var sk = leemon.bigInt2str(leemon.str2bigInt("1684485249618432870053204561280133322807761663018476281181354505463046570451", 10), 64);
		var pw = leemon.bigInt2str(leemon.randBigInt(Math.random() * 200), 64);

		var key = uvCrypto.encryptSecretKey(sk, pw);
		var sk2 = uvCrypto.decryptSecretKey(key, pw, function (message) {
			error = message
		});
		sk2 = leemon.bigInt2str(sk2, 64);

		expect(error).toEqual('');
		expect(sk2).toEqual(sk);

	});
});

describe('votingKeyEncryptionAES', function () {
	it('encrypt/decrypt voting key', function () {

		var error = '';

		var sk = leemon.bigInt2str(leemon.str2bigInt("1684485249618432870053204561280133322807761663018476281181354505463046570451", 10), 64);
		var pw = leemon.bigInt2str(leemon.randBigInt(Math.random() * 200), 64);
		var key = uvCrypto.encryptSecretKeyAES(sk, pw);
		var sk2 = uvCrypto.decryptSecretKeyAES(key, pw, function (message) {
			error = message
		});
		sk2 = leemon.bigInt2str(sk2, 64);

		expect(error).toEqual('');
		expect(sk2).toEqual(sk);

	});
});


describe('mapZq2GqTest', function () {

	it('mapping between Zq and Gq', function () {

		uvCrypto.setEncryptionParameters({P: "23", Q: "11", G: "4", BASE: 10});
		var bigInt4 = leemon.str2bigInt("4", 10, 1);
		var bigInt5 = leemon.str2bigInt("5", 10, 1);
		var bigInt6 = leemon.str2bigInt("6", 10, 1);

		expect(uvCrypto.mapZq2Gq(bigInt4).toString()).toEqual("18,0");
		expect(uvCrypto.mapZq2Gq(bigInt5).toString()).toEqual("6,0");
		expect(uvCrypto.mapZq2Gq(bigInt6).toString()).toEqual("16,0");
	});


	it('value not in Zq => exception', function () {

		uvCrypto.setEncryptionParameters({P: "23", Q: "11", G: "4", BASE: 10});
		var bigInt17 = leemon.str2bigInt("17", 10, 1);


		var testFn = function () {
			uvCrypto.mapZq2Gq(bigInt17);
		}

		//=> error
		expect(testFn).toThrow(new Error("Error: value not in Zq."));
	});
});

describe('pairing Tests', function () {
	it('pairing and unpairing leemon big integers', function () {

		var a = leemon.randBigInt(160);
		var b = leemon.randBigInt(160);

		var paired = uvCrypto.pair(a, b);
		var unpaired = uvCrypto.unpair(paired);

		expect(leemon.equals(unpaired[0], a)).toEqual(1);
		expect(leemon.equals(unpaired[1], b)).toEqual(1);
		expect(leemon.equals(unpaired[1], a)).toEqual(0);
		expect(leemon.equals(unpaired[0], b)).toEqual(0);
	});
});

describe('Integer square root tests', function () {
	it('computing integer square root', function () {

		var a = leemon.str2bigInt("65133683824381501983523684796057614145070427752690897588060462960319251776021", 10);

		var b = uvCrypto.isqrt(a);

		expect(leemon.bigInt2str(b, 10)).toEqual("255213016565341944910662726780139624115");

	});
});

describe('Schnorr signature tests', function () {
	it('sign and verify', function () {

		var messageHash = Hash.doString("Message");

		var SIGNATURE_SETTING = {
			P: "161931481198080639220214033595931441094586304918402813506510547237223787775475425991443924977419330663170224569788019900180050114468430413908687329871251101280878786588515668012772798298511621634145464600626619548823238185390034868354933050128115662663653841842699535282987363300852550784188180264807606304297",
			Q: "65133683824381501983523684796057614145070427752690897588060462960319251776021",
			G: "109291242937709414881219423205417309207119127359359243049468707782004862682441897432780127734395596275377218236442035534825283725782836026439537687695084410797228793004739671835061419040912157583607422965551428749149162882960112513332411954585778903685207256083057895070357159920203407651236651002676481874709"
		}

		var p = leemon.str2bigInt(SIGNATURE_SETTING.P, 10, 1);
		var q = leemon.str2bigInt(SIGNATURE_SETTING.Q, 10, 1);
		var g = leemon.str2bigInt(SIGNATURE_SETTING.G, 10, 1);

		//generate random secret key
		var sk = uvCrypto.generateDLOGSecretKey(q);
		// Compute verification key based on secret key
		var pk = uvCrypto.computeVerificationKey(p, g, sk);

		var sig = uvCrypto.createSchnorrSignature(messageHash, sk, p, q, g);
		var result = uvCrypto.verifySchnorrSignature(sig, messageHash, pk, p, q, g);

		expect(result).toEqual(1);
	});
});