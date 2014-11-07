describe('encodeVoteTest', function () {
    it('encoding each choice on the same number of bits (one rule)', function () {

        var map = new Map();

        map.put('1',2);
        map.put('2',0);
        map.put('3',1);
        map.put('4',0);
        map.put('5',1);
        map.put('6',3);

        var ids = Array();

        ids.push(1);
        ids.push(2);
        ids.push(3);
        ids.push(4);
        ids.push(5);
        ids.push(6);

        var rules = Array();

        var rule = {};
        rule.lowerBound = 0;
        rule.upperBound = 3;
	rule.choiceIds = [];
        rule.choiceIds[0] = 1;
        rule.choiceIds[1] = 2;
        rule.choiceIds[2] = 3;
        rule.choiceIds[3] = 4;
        rule.choiceIds[4] = 5;
        rule.choiceIds[5] = 6;


        rules.push(rule);

        //every choice is represented on 2 bits
        //11|01|00|01|00|10 => 3346
        //=> 2^11 + 2^10 + 2^8 + 2^4 + 2^1 = 3346 => (3346,0)
        expect(uvCrypto.encodeVote(map, ids, rules).toString()).toEqual("3346,0");
    });

    it('encoding each choice on different number of bits (two rules)', function () {

        var map = new Map();

        map.put('1',2);
        map.put('2',0);
        map.put('3',1);
        map.put('4',0);
        map.put('5',1);
        map.put('6',3);

        var ids = Array();

        ids.push(1);
        ids.push(2);
        ids.push(3);
        ids.push(4);
        ids.push(5);
        ids.push(6);

        var rules = Array();

        var rule1 = {};
        rule1.lowerBound =0;
        rule1.upperBound = 34;
	rule1.choiceIds = [];
        // ids 1, 2, 5, 6 are represented on 6 bits
        rule1.choiceIds[0] = 1;
        rule1.choiceIds[1] = 2;
        rule1.choiceIds[4] = 5;
        rule1.choiceIds[5] = 6;

        var rule2 = {};
        rule2.lowerBound = 0;
        rule2.upperBound = 12;
	rule2.choiceIds = [];
        // ids 3, 4 are represented on 4 bits
        rule2.choiceIds[0] = 3;
        rule2.choiceIds[1] = 4;


        rules.push(rule1);
        rules.push(rule2);

        //=> 202379266
        //   c6  |  c5  | c4 | c3  |  c2  |  c1
        //00 0011|000001|0000|0 001|000000|000010
        //-- ------------------ -----------------
        // 0       6176               4098
        expect(uvCrypto.encodeVote(map, ids, rules).toString()).toEqual("4098,6176,0");
    });

    it('choice id not in rule => exception', function () {

        var map = new Map();

        map.put('1',2);
        map.put('2',0);
        map.put('3',1);
        map.put('4',0);
        map.put('5',1);
        map.put('6',3);

        var ids = Array();

        ids.push(1);
        ids.push(2);
        ids.push(3);
        ids.push(4);
        ids.push(5);
        ids.push(6);

        var rules = Array();

        var rule = {};
        rule.lowerBound = 0;
        rule.upperBound = 3;
	rule.choiceIds = [];
        rule.choiceIds[0] = 1;
        rule.choiceIds[1] = 2;
        //choiceId 3 is not to find in any rule
        rule.choiceIds[3] = 4;
        rule.choiceIds[4] = 5;
        rule.choiceIds[5] = 6;


        rules.push(rule);

        var testFn = function () {
            uvCrypto.encodeVote(map, ids, rules)
        }

        //=> error
        expect(testFn).toThrow(new Error("Encoding error: choice not found in any rule."));
    });

    it('choiceId not in id list, but in map and in rule', function () {

        var map = new Map();

        map.put('1',2);
        map.put('2',0);
        map.put('3',1);
        map.put('4',0);
        map.put('5',1);
        map.put('6',3);

        var ids = Array();

        ids.push(1);
        ids.push(2);
        //choice id doesn't exist but is to find in map and in rule
        //ids.push(3);
        ids.push(4);
        ids.push(5);
        ids.push(6);

        var rules = Array();

        var rule = {};
        rule.lowerBound =0;
        rule.upperBound =3;
	rule.choiceIds = [];
        rule.choiceIds[0] = 1;
        rule.choiceIds[1] = 2;
        rule.choiceIds[2] = 3;
        rule.choiceIds[3] = 4;
        rule.choiceIds[4] = 5;
        rule.choiceIds[5] = 6;


        rules.push(rule);

        //=> should not give an error
        // 11|01|00|00|10 => 834 => (834,0)
        expect(uvCrypto.encodeVote(map, ids, rules).toString()).toEqual("834,0");
    });
	
	it('different forall rules for a choice (-> what never should be the case!)', function () {

        var map = new Map();
        map.put('1',2);
        map.put('2',1);
        
        var ids = Array();
        ids.push(1);
        ids.push(2);
        
        var rules = Array();

        var rule1 = {};
        rule1.lowerBound = 0;
        rule1.upperBound = 34;
        // id 1 is represented on 6 bits BUT is overruled by rule 2!
	rule1.choiceIds = [];
        rule1.choiceIds[0] = 1;

        var rule2 = {};
        rule2.lowerBound = 0;
        rule2.upperBound = 3;
        // ids 1, 2 are represented on 2 bits
	rule2.choiceIds = [];
        rule2.choiceIds[0] = 1;
        rule2.choiceIds[1] = 2;


        rules.push(rule1);
        rules.push(rule2);

        //
        //   c2 |  c1  
        //   01 |  10
        //   ---------
        //   6 
        expect(uvCrypto.encodeVote(map, ids, rules).toString()).toEqual("6,0");
    });
	
	it('More voices for a candidate than allowd by forall rule (-> should never be the case, but in case of wrong rule checking...)', function () {

        var map = new Map();
        map.put('1',6);
        map.put('2',1);
        
        var ids = Array();
        ids.push(1);
        ids.push(2);
        
        var rules = Array();

        var rule = {};
        rule.lowerBound=0;
        rule.upperBound=3;
        // ids 1, 2 are represented on 2 bits
        rule.choiceIds = [];
	rule.choiceIds[0] = 1;
        rule.choiceIds[1] = 2;
		
        rules.push(rule);
		var testFn = function () {
            uvCrypto.encodeVote(map, ids, rules)
        }
		//=> error
        expect(testFn).toThrow(new Error("Encoding error: Too many voices for candidate!"));
    });
	
});


describe('votingKeyEncryption', function () {
    it('encrypt/decrypt voting key', function () {

		var error = '';

		var sk = leemon.bigInt2str(leemon.str2bigInt("1684485249618432870053204561280133322807761663018476281181354505463046570451", 10, 64));
		var pw = leemon.bigInt2str(leemon.randBigInt(Math.random()*200), 64);
		var key = uvCrypto.encryptSecretKey(sk, pw);
		var sk2 = uvCrypto.decryptSecretKey(key, pw, function(message){error=message});
		sk2 = leemon.bigInt2str(sk2, 64);

		expect(error).toEqual('');
		expect(sk2).toEqual(sk);

    });
});


describe('mapZq2GqTest', function () {

    it('mapping between Zq and Gq', function () {

        uvCrypto.setElgamalParameters( "23", "11", "4", 10 );
        var bigInt4 = leemon.str2bigInt("4", 10, 1);
		var bigInt5 = leemon.str2bigInt("5", 10, 1);
		var bigInt6 = leemon.str2bigInt("6", 10, 1);

        expect(uvCrypto.mapZq2Gq(bigInt4).toString()).toEqual("18,0");
		expect(uvCrypto.mapZq2Gq(bigInt5).toString()).toEqual("6,0");
		expect(uvCrypto.mapZq2Gq(bigInt6).toString()).toEqual("16,0");
    });


	it('value not in Zq => exception', function () {

        uvCrypto.setElgamalParameters( "23", "11", "4", 10 );
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
	
	var paired = uvCrypto.pair(a,b);
	var unpaired = uvCrypto.unpair(paired);
	
	expect(leemon.equals(unpaired[0],a));
	expect(leemon.equals(unpaired[1],b));
	expect(!leemon.equals(unpaired[1],a));
	expect(!leemon.equals(unpaired[0],b));
    });
});
