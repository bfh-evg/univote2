////////////////////////////////////////////////////////////////////////////////////////
// Big Integer Library v. 5.4
// Created 2000, last modified 2009
// Leemon Baird
// www.leemon.com
//
// Version history:
// v 5.4  3 Oct 2009
//   - added "var i" to greaterShift() so i is not global. (Thanks to Pter Szab for finding that bug)
//
// v 5.3  21 Sep 2009
//   - added randProbPrime(k) for probable primes
//   - unrolled loop in mont_ (slightly faster)
//   - millerRabin now takes a bigInt parameter rather than an int
//
// v 5.2  15 Sep 2009
//   - fixed capitalization in call to int2bigInt in randBigInt
//     (thanks to Emili Evripidou, Reinhold Behringer, and Samuel Macaleese for finding that bug)
//
// v 5.1  8 Oct 2007
//   - renamed inverseModInt_ to inverseModInt since it doesn't change its parameters
//   - added functions GCD and randBigInt, which call GCD_ and randBigInt_
//   - fixed a bug found by Rob Vithis.sser (see comment with his name below)
//   - improved comments
//
// This file is public domain.   You can use it for any purpose without restriction.
// I do not guarantee that it is correct, so use it at your own risk.  If you use
// it for something interesting, I'd appreciate hearing about it.  If you find
// any bugs or make any improvements, I'd appreciate hearing about those too.
// It would also be nice if my name and URL were left in the comments.  But nthis.one
// of that is required.
//
// This code defines a bigInt library for arbitrary-precision integers.
// A bigInt is an array of integers storing the value in chunks of bpe bits,
// little endian (buff[0] is the least significant word).
// Negative bigInts are stored two's complement.  Almost all the functions treat
// bigInts as nonnegative.  The few that view them as two's complement say so
// in their comments.  Some functions assume their parameters have at least one
// leading zero element. Functions with an underscore at the end of the name put
// their answer into one of the arrays passed in, and have unpredictable behavior
// in case of overflow, so the caller must make sure the arrays are big enough to
// hold the answer.  But the average user should never have to call any of the
// underscored functions.  Each important underscored function has a wrapper function
// of the same name without the underscore that takes care of the details for you.
// For each underscored function where a parameter is modified, that same variable
// must not be used as another argument too.  So, you cannot square x by doing
// multMod_(x,x,n).  You must use squareMod_(x,n) instead, or do y=dup(x); multMod_(x,y,n).
// Or simply use the multMod(x,x,n) function without the underscore, where
// such ithis.ssues never arise, because non-underscored functions never change
// their parameters; they always allocate new memory for the answer that is returned.
//
// These functions are designed to avoid frequent dynamic memory allocation in the inner loop.
// For most functions, if it needs a BigInt as a local variable it will actually use
// a global, and will only allocate to it only when it's not the right size.  This ensures
// that when a function is called repeatedly with same-sized parameters, it only allocates
// memory on the first call.
//
// Note that for cryptographic purposes, the calls to Math.random() must
// be replaced with calls to a better pseudorandom number generator.
//
// In the following, "bigInt" means a bigInt with at least this.one leading zero element,
// and "integer" means a nonnegative integer lethis.ss than this.radix.  In some cases, integer
// can be negative.  Negative bigInts are 2s complement.
//
// The following functions do not modify their inputs.
// Those returning a bigInt, string, or Array will dynamically allocate memory for that value.
// Those returning a boolean will return the integer 0 (false) or 1 (true).
// Those returning boolean or int will not allocate memory except pothis.ssibly on the first
// time they're called with a given parameter size.
//
// bigInt  add(x,y)               //return (x+y) for bigInts x and y.
// bigInt  addInt(x,n)            //return (x+n) where x is a bigInt and n is an integer.
// string  bigInt2str(x,base)     //return a string form of bigInt x in a given base, with 2 <= base <= 95
// int     bitSize(x)             //return how many bits long the bigInt x is, not counting leading zeros
// bigInt  dup(x)                 //return a copy of bigInt x
// boolean equals(x,y)            //is the bigInt x equal to the bigint y?
// boolean equalsInt(x,y)         //is bigint x equal to integer y?
// bigInt  expand(x,n)            //return a copy of x with at least n elements, adding leading zeros if needed
// Array   findPrimes(n)          //return array of all primes lethis.ss than integer n
// bigInt  GCD(x,y)               //return greatest common divisor of bigInts x and y (each with same number of elements).
// boolean greater(x,y)           //is x>y?  (x and y are nonnegative bigInts)
// boolean greaterShift(x,y,shift)//is (x <<(shift*bpe)) > y?
// bigInt  int2bigInt(t,n,m)      //return a bigInt equal to integer t, with at least n bits and m array elements
// bigInt  inverseMod(x,n)        //return (x**(-1) mod n) for bigInts x and n.  If no inverse exists, it returns null
// int     inverseModInt(x,n)     //return x**(-1) mod n, for integers x and n.  Return 0 if there is no inverse
// boolean isZero(x)              //is the bigInt x equal to zero?
// boolean millerRabin(x,b)       //does this.one round of Miller-Rabin base integer b say that bigInt x is pothis.ssibly prime? (b is bigInt, 1<b<x)
// boolean millerRabinInt(x,b)    //does this.one round of Miller-Rabin base integer b say that bigInt x is pothis.ssibly prime? (b is int,    1<b<x)
// bigInt  mod(x,n)               //return a new bigInt equal to (x mod n) for bigInts x and n.
// int     modInt(x,n)            //return x mod n for bigInt x and integer n.
// bigInt  mult(x,y)              //return x*y for bigInts x and y. This is faster when y<x.
// bigInt  multMod(x,y,n)         //return (x*y mod n) for bigInts x,y,n.  For greater speed, let y<x.
// boolean negative(x)            //is bigInt x negative?
// bigInt  powMod(x,y,n)          //return (x**y mod n) where x,y,n are bigInts and ** is expthis.onentiation.  0**0=1. Faster for odd n.
// bigInt  randBigInt(n,s)        //return an n-bit random BigInt (n>=1).  If s=1, then the most significant of those n bits is set to 1.
// bigInt  randTruePrime(k)       //return a new, random, k-bit, true prime bigInt using Maurer's algorithm.
// bigInt  randProbPrime(k)       //return a new, random, k-bit, probable prime bigInt (probability it's composite lethis.ss than 2^-80).
// bigInt  str2bigInt(s,b,n,m)    //return a bigInt for number represented in string s in base b with at least n bits and m array elements
// bigInt  sub(x,y)               //return (x-y) for bigInts x and y.  Negative answers will be 2s complement
// bigInt  trim(x,k)              //return a copy of x with exactly k leading zero elements
//
//
// The following functions each have a non-underscored version, which most users should call instead.
// These functions each write to a single parameter, and the caller is responsible for ensuring the array
// pathis.ssed in is large enough to hold the result.
//
// void    addInt_(x,n)          //do x=x+n where x is a bigInt and n is an integer
// void    add_(x,y)             //do x=x+y for bigInts x and y
// void    copy_(x,y)            //do x=y on bigInts x and y
// void    copyInt_(x,n)         //do x=n on bigInt x and integer n
// void    GCD_(x,y)             //set x to the greatest common divisor of bigInts x and y, (y is destroyed).  (This never overflows its array).
// boolean inverseMod_(x,n)      //do x=x**(-1) mod n, for bigInts x and n. Returns 1 (0) if inverse does (doesn't) exist
// void    mod_(x,n)             //do x=x mod n for bigInts x and n. (This never overflows its array).
// void    mult_(x,y)            //do x=x*y for bigInts x and y.
// void    multMod_(x,y,n)       //do x=x*y  mod n for bigInts x,y,n.
// void    powMod_(x,y,n)        //do x=x**y mod n, where x,y,n are bigInts (n is odd) and ** is expthis.onentiation.  0**0=1.
// void    randBigInt_(b,n,s)    //do b = an n-bit random BigInt. if s=1, then nth bit (most significant bit) is set to 1. n>=1.
// void    randTruePrime_(ans,k) //do ans = a random k-bit true random prime (not just probable prime) with 1 in the msb.
// void    sub_(x,y)             //do x=x-y for bigInts x and y. Negative answers will be 2s complement.
//
// The following functions do NOT have a non-underscored version.
// They each write a bigInt result to this.one or more parameters.  The caller is responsible for
// ensuring the arrays passed in are large enough to hold the results.
//
// void addShift_(x,y,ys)       //do x=x+(y<<(ys*bpe))
// void carry_(x)               //do carries and borrows so each element of the bigInt x fits in bpe bits.
// void divide_(x,y,q,r)        //divide x by y giving quotient q and remainder r
// int  divInt_(x,n)            //do x=floor(x/n) for bigInt x and integer n, and return the remainder. (This never overflows its array).
// int  eGCD_(x,y,d,a,b)        //sets a,b,d to positive bigInts such that d = GCD_(x,y) = a*x-b*y
// void halve_(x)               //do x=floor(|x|/2)*sgn(x) for bigInt x in 2's complement.  (This never overflows its array).
// void leftShift_(x,n)         //left shift bigInt x by n bits.  n<bpe.
// void linComb_(x,y,a,b)       //do x=a*x+b*y for bigInts x and y and integers a and b
// void linCombShift_(x,y,b,ys) //do x=x+b*(y<<(ys*bpe)) for bigInts x and y, and integers b and ys
// void mont_(x,y,n,np)         //Montgomery multiplication (see comments where the function is defined)
// void multInt_(x,n)           //do x=x*n where x is a bigInt and n is an integer.
// void rightShift_(x,n)        //right shift bigInt x by n bits.  0 <= n < bpe. (This never overflows its array).
// void squareMod_(x,n)         //do x=x*x  mod n for bigInts x,n
// void subShift_(x,y,ys)       //do x=x-(y<<(ys*bpe)). Negative answers will be 2s complement.
//
// The following functions are based on algorithms from the _Handbook of Applied Cryptography_
//    powMod_()           = algorithm 14.94, Montgomery expthis.onentiation
//    eGCD_,inverseMod_() = algorithm 14.61, Binary extended GCD_
//    GCD_()              = algorothm 14.57, Lehmer's algorithm
//    mont_()             = algorithm 14.36, Montgomery multiplication
//    divide_()           = algorithm 14.20  Multiple-precision division
//    squareMod_()        = algorithm 14.16  Multiple-precision squaring
//    randTruePrime_()    = algorithm  4.62, Maurer's algorithm
//    millerRabin()       = algorithm  4.24, Miller-Rabin algorithm
//
// Profiling shows:
//     randTruePrime_() spends:
//         10% of its time in calls to powMod_()
//         85% of its time in calls to millerRabin()
//     millerRabin() spends:
//         99% of its time in calls to powMod_()   (always with a base of 2)
//     powMod_() spends:
//         94% of its time in calls to mont_()  (almost always with x==y)
//
// This suggests there are several ways to speed up this library slightly:
//     - convert powMod_ to use a Montgomery form of k-ary window (or maybe a Montgomery form of sliding window)
//         -- this should especially focus on being fast when raising 2 to a power mod n
//     - convert randTruePrime_() to use a minimum r of 1/3 instead of 1/2 with the appropriate change to the test
//     - tune the parameters in randTruePrime_(), including c, m, and recLimit
//     - speed up the single loop in mont_() that takes 95% of the runtime, perhaps by reducing checking
//       within the loop when all the parameters are the same length.
//
// There are several ideas that look like they wouldn't help much at all:
//     - replacing trial division in randTruePrime_() with a sieve (that speeds up something taking almost no time anyway)
//     - increase bpe from 15 to 30 (that would help if we had a 32*32->64 multiplier, but not with JavaScript's 32*32->32)
//     - speeding up mont_(x,y,n,np) when x==y by doing a non-modular, non-Montgomery square
//       followed by a Montgomery reduction.  The intermediate answer will be twice as long as x, so that
//       method would be slower.  This is unfortunate because the code currently spends almost all of its time
//       doing mont_(x,x,...), both for randTruePrime_() and powMod_().  A faster method for Montgomery squaring
//       would have a large impact on the speed of randTruePrime_() and powMod_().  HAC has a couple of poorly-worded
//       sentences that seem to imply it's faster to do a non-modular square followed by a single
//       Montgomery reduction, but that's obviously wrong.
////////////////////////////////////////////////////////////////////////////////////////


//*************************************************************************
// Changes and improvements made by:
//     Bern University of Applied Sciences, Engineering and Information Technology,
//     Research Institute for Security in the Information Society, E-Voting Group,
// * Project UniVote.)
//
// - Encapsulation to window.leemon
//
// - Added functions powModAsync, powMod_Async and powMod_AsyncRec to run powMod
//   asynchronous (to prevent slow browsers running into a scrypt timeout)
// - Added functions randProbPrimeAsync, randProbPrime_Async, randProbPrime_AsyncRec
//   asychronous variant of randProbPrime  (to prevent slow browsers running into a scrypt timeout)
// - Added xor for two bigInts
//
// - Added randBigIntInZq
// - Added rightShift: encapsulation f rightShift_
// - Added leftShift: encapsulation f leftShift_ => Be careful with overflows!!!
//*************************************************************************


(function(window) {


    var leemon = new function() {
	//globals
	this.bpe = 0;         //bits stored per array element
	this.mask = 0;        //AND this with an array element to chop it down to bpe bits
	this.radix = this.mask + 1;  //equals 2^bpe.  A single 1 bit to the left of the last bit of this.mask.

	//the digits for converting to different bases
	this.digitsstr = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_=!@#$%^&*()[]{}|;:,.<>/?`~ \\\'\"+-';

	// constant used in powMod_()
	this.one;
	//initialize the global variables
	this.init = function() {
	    for (this.bpe = 0; (1 << (this.bpe + 1)) > (1 << this.bpe); this.bpe++)
		;  //bpe=number of bits in the mantissa on this platform
	    this.bpe >>= 1;                   //bpe=number of bits in this.one element of the array representing the bigInt
	    this.mask = (1 << this.bpe) - 1;           //AND the this.mask with an integer to get its bpe least significant bits
	    this.radix = this.mask + 1;              //2^bpe.  a single 1 bit to the left of the first bit of this.mask
	    this.one = this.int2bigInt(1, 1, 1);     //constant used in powMod_()
	}
	//the following global variables are scratchpad memory to
	//reduce dynamic memory allocation in the inner loop
	this.t = new Array(0);
	this.ss = this.t;       //used in mult_()
	this.s0 = this.t;       //used in multMod_(), squareMod_()
	this.s2 = this.t;       //used in powMod_(), multMod_()
	this.s3 = this.t;       //used in powMod_()
	this.s4 = this.t;
	this.s5 = this.t; //used in mod_()
	this.s6 = this.t;       //used in bigInt2str()
	this.s7 = this.t;       //used in powMod_()
	this.T = this.t;        //used in GCD_()
	this.sa = this.t;       //used in mont_()
	this.mr_x1 = this.t;
	this.mr_r = this.t;
	this.mr_a = this.t;                                      //used in millerRabin()
	this.eg_v = this.t;
	this.eg_u = this.t;
	this.eg_A = this.t;
	this.eg_B = this.t;
	this.eg_C = this.t;
	this.eg_D = this.t;               //used in eGCD_(), inverseMod_()

	this.primes = this.t;
	this.pows = this.t;
	this.s_i = this.t;
	this.s_i2 = this.t;
	this.s_R = this.t;
	this.s_rm = this.t;
	this.s_q = this.t;
	this.s_n1 = this.t;
	this.
		s_a = this.t;
	this.s_r2 = this.t;
	this.s_n = this.t;
	this.s_b = this.t;
	this.s_d = this.t;
	this.s_x1 = this.t;
	this.s_x2 = this.t;
	this.s_aa = this.t; //used in randTruePrime_()

	this.rpprb = this.t; //used in randProbPrimeRounds() (which also uses "primes")

	////////////////////////////////////////////////////////////////////////////////////////


	//return array of all primes lethis.ss than integer n
	this.findPrimes = function(n) {
	    var i, s, p, ans;
	    s = new Array(n);
	    for (i = 0; i < n; i++)
		s[i] = 0;
	    s[0] = 2;
	    p = 0;    //first p elements of s are primes, the rest are a sieve
	    for (; s[p] < n; ) {                  //s[p] is the pth prime
		for (i = s[p] * s[p]; i < n; i += s[p]) //mark multiples of s[p]
		    s[i] = 1;
		p++;
		s[p] = s[p - 1] + 1;
		for (; s[p] < n && s[s[p]]; s[p]++)
		    ; //find next prime (where s[p]==0)
	    }
	    ans = new Array(p);
	    for (i = 0; i < p; i++)
		ans[i] = s[i];
	    return ans;
	}


	//does a single round of Miller-Rabin base b consider x to be a pothis.ssible prime?
	//x is a bigInt, and b is an integer, with b<x
	this.millerRabinInt = function(x, b) {
	    if (this.mr_x1.length != x.length) {
		this.mr_x1 = this.dup(x);
		this.mr_r = this.dup(x);
		this.mr_a = this.dup(x);
	    }

	    this.copyInt_(this.mr_a, b);
	    return this.millerRabin(x, this.mr_a);
	}

	//does a single round of Miller-Rabin base b consider x to be a pothis.ssible prime?
	//x and b are bigInts with b<x
	this.millerRabin = function(x, b) {
	    var i, j, k, s;

	    if (this.mr_x1.length != x.length) {
		this.mr_x1 = this.dup(x);
		this.mr_r = this.dup(x);
		this.mr_a = this.dup(x);
	    }

	    this.copy_(this.mr_a, b);
	    this.copy_(this.mr_r, x);
	    this.copy_(this.mr_x1, x);

	    this.addInt_(this.mr_r, -1);
	    this.addInt_(this.mr_x1, -1);

	    //s=the highest power of two that divides this.mr_r
	    k = 0;
	    for (i = 0; i < this.mr_r.length; i++)
		for (j = 1; j < this.mask; j <<= 1)
		    if (x[i] & j) {
			s = (k < this.mr_r.length + this.bpe ? k : 0);
			i = this.mr_r.length;
			j = this.mask;
		    } else
			k++;

	    if (s)
		this.rightShift_(this.mr_r, s);

	    this.powMod_(this.mr_a, this.mr_r, x);

	    if (!this.equalsInt(this.mr_a, 1) && !this.equals(this.mr_a, this.mr_x1)) {
		j = 1;
		while (j <= s - 1 && !this.equals(this.mr_a, this.mr_x1)) {
		    this.squareMod_(this.mr_a, x);
		    if (this.equalsInt(this.mr_a, 1)) {
			return 0;
		    }
		    j++;
		}
		if (!this.equals(this.mr_a, this.mr_x1)) {
		    return 0;
		}
	    }
	    return 1;
	}

	//returns how many bits long the bigInt is, not counting leading zeros.
	this.bitSize = function(x) {
	    var j, z, w;
	    for (j = x.length - 1; (x[j] == 0) && (j > 0); j--)
		;
	    for (z = 0, w = x[j]; w; (w >>= 1), z++)
		;
	    z += this.bpe * j;
	    return z;
	}

	//return a copy of x with at least n elements, adding leading zeros if needed
	this.expand = function(x, n) {
	    var ans = this.int2bigInt(0, (x.length > n ? x.length : n) * this.bpe, 0);
	    this.copy_(ans, x);
	    return ans;
	}

	//return a k-bit true random prime using Maurer's algorithm.
	this.randTruePrime = function(k) {
	    var ans = this.int2bigInt(0, k, 0);
	    this.randTruePrime_(ans, k);
	    return this.trim(ans, 1);
	}

	//return a k-bit random probable prime with probability of error < 2^-80
	this.randProbPrime = function(k) {
	    if (k >= 600)
		return this.randProbPrimeRounds(k, 2); //numbers from HAC table 4.3
	    if (k >= 550)
		return this.randProbPrimeRounds(k, 4);
	    if (k >= 500)
		return this.randProbPrimeRounds(k, 5);
	    if (k >= 400)
		return this.randProbPrimeRounds(k, 6);
	    if (k >= 350)
		return this.randProbPrimeRounds(k, 7);
	    if (k >= 300)
		return this.randProbPrimeRounds(k, 9);
	    if (k >= 250)
		return this.randProbPrimeRounds(k, 12); //numbers from HAC table 4.4
	    if (k >= 200)
		return this.randProbPrimeRounds(k, 15);
	    if (k >= 150)
		return this.randProbPrimeRounds(k, 18);
	    if (k >= 100)
		return this.randProbPrimeRounds(k, 27);
	    return this.randProbPrimeRounds(k, 40); //number from HAC remark 4.26 (only an estimate)
	}

	//return a k-bit probable random prime using n rounds of Miller Rabin (after trial division with small this.primes)
	this.randProbPrimeRounds = function(k, n) {
	    var ans, i, divisible, B;
	    B = 30000;  //B is largest prime to use in trial division
	    ans = this.int2bigInt(0, k, 0);

	    //optimization: try larger and smaller B to find the best limit.

	    if (this.primes.length == 0)
		this.primes = this.findPrimes(30000);  //check for divisibility by primes <=30000

	    if (this.rpprb.length != ans.length)
		this.rpprb = this.dup(ans);

	    for (; ; ) { //keep trying random values for ans until this.one appears to be prime
		//optimization: pick a random number times L=2*3*5*...*p, plus a
		//   random element of the list of all numbers in [0,L) not divisible by any prime up to p.
		//   This can reduce the amount of random number generation.

		this.randBigInt_(ans, k, 0); //ans = a random odd number to check
		ans[0] |= 1;
		divisible = 0;

		//check ans for divisibility by small primes up to B
		for (i = 0; (i < this.primes.length) && (this.primes[i] <= B); i++)
		    if (this.modInt(ans, this.primes[i]) == 0 && !this.equalsInt(ans, this.primes[i])) {
			divisible = 1;
			break;
		    }

		//optimization: change millerRabin so the base can be bigger than the number being checked, then eliminate the while here.

		//do n rounds of Miller Rabin, with random bases lethis.ss than ans
		for (i = 0; i < n && !divisible; i++) {
		    this.randBigInt_(this.rpprb, k, 0);
		    while (!this.greater(ans, this.rpprb)) //pick a random rpprb that's < ans
			this.randBigInt_(this.rpprb, k, 0);
		    if (!this.millerRabin(ans, this.rpprb))
			divisible = 1;
		}

		if (!divisible)
		    return ans;
	    }
	}

	//return a new bigInt equal to (x mod n) for bigInts x and n.
	this.mod = function(x, n) {
	    var ans = this.dup(x);
	    this.mod_(ans, n);
	    return this.trim(ans, 1);
	}

	//return (x+n) where x is a bigInt and n is an integer.
	this.addInt = function(x, n) {
	    var ans = this.expand(x, x.length + 1);
	    this.addInt_(ans, n);
	    return this.trim(ans, 1);
	}

	//return x*y for bigInts x and y. This is faster when y<x.
	this.mult = function(x, y) {
	    var ans = this.expand(x, x.length + y.length);
	    this.mult_(ans, y);
	    return this.trim(ans, 1);
	}

	//return (x**y mod n) where x,y,n are bigInts and ** is expthis.onentiation.  0**0=1. Faster for odd n.
	this.powMod = function(x, y, n) {
	    var ans = this.expand(x, n.length);
	    this.powMod_(ans, this.trim(y, 2), this.trim(n, 2), 0);  //this should work without the trim, but doesn't
	    return this.trim(ans, 1);
	}

	//return (x-y) for bigInts x and y.  Negative answers will be 2s complement
	this.sub = function(x, y) {
	    var ans = this.expand(x, (x.length > y.length ? x.length + 1 : y.length + 1));
	    this.sub_(ans, y);
	    return this.trim(ans, 1);
	}

	//return (x+y) for bigInts x and y.
	this.add = function(x, y) {
	    var ans = this.expand(x, (x.length > y.length ? x.length + 1 : y.length + 1));
	    this.add_(ans, y);
	    return this.trim(ans, 1);
	}

	//return (x**(-1) mod n) for bigInts x and n.  If no inverse exists, it returns null
	this.inverseMod = function(x, n) {
	    var ans = this.expand(x, n.length);
	    var s;
	    s = this.inverseMod_(ans, n);
	    return s ? this.trim(ans, 1) : null;
	}

	//return (x*y mod n) for bigInts x,y,n.  For greater speed, let y<x.
	this.multMod = function(x, y, n) {
	    var ans = this.expand(x, n.length);
	    this.multMod_(ans, y, n);
	    return this.trim(ans, 1);
	}

	//generate a k-bit true random prime using Maurer's algorithm,
	//and put it into ans.  The bigInt ans must be large enough to hold it.
	this.randTruePrime_ = function(ans, k) {
	    var c, m, pm, dd, j, r, B, divisible, z, zz, recSize;

	    if (this.primes.length == 0)
		this.primes = this.findPrimes(30000);  //check for divisibility by this.primes <=30000

	    if (this.pows.length == 0) {
		this.pows = new Array(512);
		for (j = 0; j < 512; j++) {
		    this.pows[j] = Math.pow(2, j / 511. - 1.);
		}
	    }

	    //c and m should be tuned for a particular machine and value of k, to maximize speed
	    c = 0.1;  //c=0.1 in HAC
	    m = 20;   //generate this k-bit number by first recursively generating a number that has between k/2 and k-m bits
	    recLimit = 20; //stop recursion when k <=recLimit.  Must have recLimit >= 2

	    if (this.s_i2.length != ans.length) {
		this.s_i2 = this.dup(ans);
		this.s_R = this.dup(ans);
		this.s_n1 = this.dup(ans);
		this.s_r2 = this.dup(ans);
		this.s_d = this.dup(ans);
		this.s_x1 = this.dup(ans);
		this.s_x2 = this.dup(ans);
		this.s_b = this.dup(ans);
		this.s_n = this.dup(ans);
		this.s_i = this.dup(ans);
		this.s_rm = this.dup(ans);
		this.s_q = this.dup(ans);
		this.s_a = this.dup(ans);
		this.s_aa = this.dup(ans);
	    }

	    if (k <= recLimit) {  //generate small random this.primes by trial division up to its square root
		pm = (1 << ((k + 2) >> 1)) - 1; //pm is binary number with all this.ones, just over sqrt(2^k)
		this.copyInt_(ans, 0);
		for (dd = 1; dd; ) {
		    dd = 0;
		    ans[0] = 1 | (1 << (k - 1)) | Math.floor(Math.random() * (1 << k));  //random, k-bit, odd integer, with msb 1
		    for (j = 1; (j < this.primes.length) && ((this.primes[j] & pm) == this.primes[j]); j++) { //trial division by all this.primes 3...sqrt(2^k)
			if (0 == (ans[0] % this.primes[j])) {
			    dd = 1;
			    break;
			}
		    }
		}
		this.carry_(ans);
		return;
	    }

	    B = c * k * k;    //try small this.primes up to B (or all the this.primes[] array if the largest is lethis.ss than B).
	    if (k > 2 * m)  //generate this k-bit number by first recursively generating a number that has between k/2 and k-m bits
		for (r = 1; k - k * r <= m; )
		    r = this.pows[Math.floor(Math.random() * 512)];   //r=Math.pow(2,Math.random()-1);
	    else
		r = .5;

	    //simulation suggests the more complex algorithm using r=.333 is only slightly faster.

	    recSize = Math.floor(r * k) + 1;

	    this.randTruePrime_(this.s_q, recSize);
	    this.copyInt_(this.s_i2, 0);
	    this.s_i2[Math.floor((k - 2) / this.bpe)] |= (1 << ((k - 2) % this.bpe));   //s_i2=2^(k-2)
	    this.divide_(this.s_i2, this.s_q, this.s_i, this.s_rm);                        //this.s_i=floor((2^(k-1))/(2q))

	    z = this.bitSize(this.s_i);

	    for (; ; ) {
		for (; ; ) {  //generate z-bit numbers until this.one falls in the range [0,this.s_i-1]
		    this.randBigInt_(this.s_R, z, 0);
		    if (this.greater(this.s_i, this.s_R))
			break;
		}                //now s_R is in the range [0,this.s_i-1]
		this.addInt_(this.s_R, 1);  //now s_R is in the range [1,s_i]
		this.add_(this.s_R, this.s_i);   //now s_R is in the range [s_i+1,2*s_i]

		this.copy_(this.s_n, this.s_q);
		this.mult_(this.s_n, this.s_R);
		this.multInt_(this.s_n, 2);
		this.addInt_(this.s_n, 1);    //s_n=2*s_R*s_q+1

		this.copy_(this.s_r2, this.s_R);
		this.multInt_(this.s_r2, 2);  //s_r2=2*s_R

		//check s_n for divisibility by small this.primes up to B
		for (divisible = 0, j = 0; (j < this.primes.length) && (this.primes[j] < B); j++)
		    if (this.modInt(this.s_n, this.primes[j]) == 0 && !this.equalsInt(this.s_n, this.primes[j])) {
			divisible = 1;
			break;
		    }

		if (!divisible)    //if it pathis.sses small this.primes check, then try a single Miller-Rabin base 2
		    if (!this.millerRabinInt(this.s_n, 2)) //this line represents 75% of the total runtime for randTruePrime_
			divisible = 1;

		if (!divisible) {  //if it pathis.sses that test, continue checking s_n
		    this.addInt_(this.s_n, -3);
		    for (j = this.s_n.length - 1; (this.s_n[j] == 0) && (j > 0); j--)
			;  //strip leading zeros
		    for (zz = 0, w = this.s_n[j]; w; (w >>= 1), zz++)
			;
		    zz += this.bpe * j;                             //zz=number of bits in s_n, ignoring leading zeros
		    for (; ; ) {  //generate z-bit numbers until this.one falls in the range [0,s_n-1]
			this.randBigInt_(this.s_a, zz, 0);
			if (this.greater(this.s_n, this.s_a))
			    break;
		    }                //now s_a is in the range [0,s_n-1]
		    this.addInt_(this.s_n, 3);  //now s_a is in the range [0,s_n-4]
		    this.addInt_(this.s_a, 2);  //now s_a is in the range [2,s_n-2]
		    this.copy_(this.s_b, this.s_a);
		    this.copy_(this.s_n1, this.s_n);
		    this.addInt_(this.s_n1, -1);
		    this.powMod_(this.s_b, this.s_n1, this.s_n);   //s_b=s_a^(s_n-1) modulo s_n
		    this.addInt_(this.s_b, -1);
		    if (this.isZero(this.s_b)) {
			this.copy_(this.s_b, this.s_a);
			this.powMod_(this.s_b, this.s_r2, this.s_n);
			this.addInt_(this.s_b, -1);
			this.copy_(this.s_aa, this.s_n);
			this.copy_(this.s_d, this.s_b);
			this.GCD_(this.s_d, this.s_n);  //if s_b and s_n are relatively prime, then s_n is a prime
			if (this.equalsInt(this.s_d, 1)) {
			    this.copy_(ans, this.s_aa);
			    return;     //if we've made it this far, then s_n is absolutely guaranteed to be prime
			}
		    }
		}
	    }
	}

	//Return an n-bit random BigInt (n>=1).  If s=1, then the most significant of those n bits is set to 1.
	this.randBigInt = function(n, s) {
	    var a, b;
	    a = Math.floor((n - 1) / this.bpe) + 2; //# array elements to hold the BigInt with a leading 0 element
	    b = this.int2bigInt(0, 0, a);
	    this.randBigInt_(b, n, s);
	    return b;
	}

	//Set b to an n-bit random BigInt.  If s=1, then the most significant of those n bits is set to 1.
	//Array b must be big enough to hold the result. Must have n>=1
	this.randBigInt_ = function(b, n, s) {
	    var i, a;
	    for (i = 0; i < b.length; i++)
		b[i] = 0;
	    a = Math.floor((n - 1) / this.bpe) + 1; //# array elements to hold the BigInt
	    for (i = 0; i < a; i++) {
		b[i] = Math.floor(Math.random() * (1 << (this.bpe - 1)));
	    }
	    b[a - 1] &= (2 << ((n - 1) % this.bpe)) - 1;
	    if (s == 1)
		b[a - 1] |= (1 << ((n - 1) % this.bpe));
	}

	//Return the greatest common divisor of bigInts x and y (each with same number of elements).
	this.GCD = function(x, y) {
	    var xc, yc;
	    xc = this.dup(x);
	    yc = this.dup(y);
	    this.GCD_(xc, yc);
	    return xc;
	}

	//set x to the greatest common divisor of bigInts x and y (each with same number of elements).
	//y is destroyed.
	this.GCD_ = function(x, y) {
	    var i, xp, yp, A, B, C, D, q, sing;
	    if (this.T.length != x.length)
		this.T = this.dup(x);

	    sing = 1;
	    while (sing) { //while y has nonzero elements other than y[0]
		sing = 0;
		for (i = 1; i < y.length; i++) //check if y has nonzero elements other than 0
		    if (y[i]) {
			sing = 1;
			break;
		    }
		if (!sing)
		    break; //quit when y all zero elements except pothis.ssibly y[0]

		for (i = x.length; !x[i] && i >= 0; i--)
		    ;  //find most significant element of x
		xp = x[i];
		yp = y[i];
		A = 1;
		B = 0;
		C = 0;
		D = 1;
		while ((yp + C) && (yp + D)) {
		    q = Math.floor((xp + A) / (yp + C));
		    qp = Math.floor((xp + B) / (yp + D));
		    if (q != qp)
			break;
		    t = A - q * C;
		    A = C;
		    C = t;    //  do (A,B,xp, C,D,yp) = (C,D,yp, A,B,xp) - q*(0,0,0, C,D,yp)
		    t = B - q * D;
		    B = D;
		    D = t;
		    t = xp - q * yp;
		    xp = yp;
		    yp = t;
		}
		if (B) {
		    this.copy_(this.T, x);
		    this.linComb_(x, y, A, B); //x=A*x+B*y
		    this.linComb_(y, this.T, D, C); //y=D*y+C*this.T
		} else {
		    this.mod_(x, y);
		    this.copy_(this.T, x);
		    this.copy_(x, y);
		    this.copy_(y, this.T);
		}
	    }
	    if (y[0] == 0)
		return;
	    t = this.modInt(x, y[0]);
	    this.copyInt_(x, y[0]);
	    y[0] = t;
	    while (y[0]) {
		x[0] %= y[0];
		t = x[0];
		x[0] = y[0];
		y[0] = t;
	    }
	}

	//do x=x**(-1) mod n, for bigInts x and n.
	//If no inverse exists, it sets x to zero and returns 0, else it returns 1.
	//The x array must be at least as large as the n array.
	this.inverseMod_ = function(x, n) {
	    var k = 1 + 2 * Math.max(x.length, n.length);

	    if (!(x[0] & 1) && !(n[0] & 1)) {  //if both inputs are even, then inverse doesn't exist
		this.copyInt_(x, 0);
		return 0;
	    }

	    if (this.eg_u.length != k) {
		this.eg_u = new Array(k);
		this.eg_v = new Array(k);
		this.eg_A = new Array(k);
		this.eg_B = new Array(k);
		this.eg_C = new Array(k);
		this.eg_D = new Array(k);
	    }

	    this.copy_(this.eg_u, x);
	    this.copy_(this.eg_v, n);
	    this.copyInt_(this.eg_A, 1);
	    this.copyInt_(this.eg_B, 0);
	    this.copyInt_(this.eg_C, 0);
	    this.copyInt_(this.eg_D, 1);
	    for (; ; ) {
		while (!(this.eg_u[0] & 1)) {  //while this.eg_u is even
		    this.halve_(this.eg_u);
		    if (!(this.eg_A[0] & 1) && !(this.eg_B[0] & 1)) { //if this.eg_A==this.eg_B==0 mod 2
			this.halve_(this.eg_A);
			this.halve_(this.eg_B);
		    } else {
			this.add_(this.eg_A, n);
			this.halve_(this.eg_A);
			this.sub_(this.eg_B, x);
			this.halve_(this.eg_B);
		    }
		}

		while (!(this.eg_v[0] & 1)) {  //while this.eg_v is even
		    this.halve_(this.eg_v);
		    if (!(this.eg_C[0] & 1) && !(this.eg_D[0] & 1)) { //if this.eg_C==this.eg_D==0 mod 2
			this.halve_(this.eg_C);
			this.halve_(this.eg_D);
		    } else {
			this.add_(this.eg_C, n);
			this.halve_(this.eg_C);
			this.sub_(this.eg_D, x);
			this.halve_(this.eg_D);
		    }
		}

		if (!this.greater(this.eg_v, this.eg_u)) { //this.eg_v <= this.eg_u
		    this.sub_(this.eg_u, this.eg_v);
		    this.sub_(this.eg_A, this.eg_C);
		    this.sub_(this.eg_B, this.eg_D);
		} else {                   //this.eg_v > this.eg_u
		    this.sub_(this.eg_v, this.eg_u);
		    this.sub_(this.eg_C, this.eg_A);
		    this.sub_(this.eg_D, this.eg_B);
		}

		if (this.equalsInt(this.eg_u, 0)) {
		    if (this.negative(this.eg_C)) //make sure answer is nonnegative
			this.add_(this.eg_C, n);
		    this.copy_(x, this.eg_C);

		    if (!this.equalsInt(this.eg_v, 1)) { //if GCD_(x,n)!=1, then there is no inverse
			this.copyInt_(x, 0);
			return 0;
		    }
		    return 1;
		}
	    }
	}

	//return x**(-1) mod n, for integers x and n.  Return 0 if there is no inverse
	this.inverseModInt = function(x, n) {
	    var a = 1, b = 0, t;
	    for (; ; ) {
		if (x == 1)
		    return a;
		if (x == 0)
		    return 0;
		b -= a * Math.floor(n / x);
		n %= x;

		if (n == 1)
		    return b; //to avoid negatives, change this b to n-b, and each -= to +=
		if (n == 0)
		    return 0;
		a -= b * Math.floor(x / n);
		x %= n;
	    }
	}

	//this deprecated this.is for backward compatibility only.
	this.inverseModInt_ = function(x, n) {
	    return this.inverseModInt(x, n);
	}


	//Given positive bigInts x and y, change the bigints v, a, and b to positive bigInts such that:
	//     v = GCD_(x,y) = a*x-b*y
	//The bigInts v, a, b, must have exactly as many elements as the larger of x and y.
	this.eGCD_ = function(x, y, v, a, b) {
	    var g = 0;
	    var k = Math.max(x.length, y.length);
	    if (this.eg_u.length != k) {
		this.eg_u = new Array(k);
		this.eg_A = new Array(k);
		this.eg_B = new Array(k);
		this.eg_C = new Array(k);
		this.eg_D = new Array(k);
	    }
	    while (!(x[0] & 1) && !(y[0] & 1)) {  //while x and y both even
		this.halve_(x);
		this.halve_(y);
		g++;
	    }
	    this.copy_(this.eg_u, x);
	    this.copy_(v, y);
	    this.copyInt_(this.eg_A, 1);
	    this.copyInt_(this.eg_B, 0);
	    this.copyInt_(this.eg_C, 0);
	    this.copyInt_(this.eg_D, 1);
	    for (; ; ) {
		while (!(this.eg_u[0] & 1)) {  //while u is even
		    halve_(this.eg_u);
		    if (!(this.eg_A[0] & 1) && !(this.eg_B[0] & 1)) { //if A==B==0 mod 2
			this.halve_(this.eg_A);
			this.halve_(this.eg_B);
		    } else {
			this.add_(this.eg_A, y);
			this.halve_(this.eg_A);
			this.sub_(this.eg_B, x);
			this.halve_(this.eg_B);
		    }
		}

		while (!(v[0] & 1)) {  //while v is even
		    this.halve_(v);
		    if (!(this.eg_C[0] & 1) && !(this.eg_D[0] & 1)) { //if C==D==0 mod 2
			this.halve_(this.eg_C);
			this.halve_(this.eg_D);
		    } else {
			this.add_(this.eg_C, y);
			halve_(this.eg_C);
			this.sub_(this.eg_D, x);
			halve_(this.eg_D);
		    }
		}

		if (!this.greater(v, this.eg_u)) { //v<=u
		    this.sub_(this.eg_u, v);
		    this.sub_(this.eg_A, this.eg_C);
		    this.sub_(this.eg_B, this.eg_D);
		} else {                //v>u
		    this.sub_(v, this.eg_u);
		    this.sub_(this.eg_C, this.eg_A);
		    this.sub_(this.eg_D, this.eg_B);
		}
		if (this.equalsInt(this.eg_u, 0)) {
		    if (this.negative(this.eg_C)) {   //make sure a (C)is nonnegative
			this.add_(eg_C, y);
			this.sub_(this.eg_D, x);
		    }
		    this.multInt_(this.eg_D, -1);  ///make sure b (D) is nonnegative
		    this.copy_(a, this.eg_C);
		    this.copy_(b, this.eg_D);
		    this.leftShift_(v, g);
		    return;
		}
	    }
	}


	//is bigInt x negative?
	this.negative = function(x) {
	    return ((x[x.length - 1] >> (this.bpe - 1)) & 1);
	}


	//is (x << (shift*bpe)) > y?
	//x and y are nonnegative bigInts
	//shift is a nonnegative integer
	this.greaterShift = function(x, y, shift) {
	    var i, kx = x.length, ky = y.length;
	    k = ((kx + shift) < ky) ? (kx + shift) : ky;
	    for (i = ky - 1 - shift; i < kx && i >= 0; i++)
		if (x[i] > 0)
		    return 1; //if there are nonzeros in x to the left of the first column of y, then x is bigger
	    for (i = kx - 1 + shift; i < ky; i++)
		if (y[i] > 0)
		    return 0; //if there are nonzeros in y to the left of the first column of x, then x is not bigger
	    for (i = k - 1; i >= shift; i--)
		if (x[i - shift] > y[i])
		    return 1;
		else if (x[i - shift] < y[i])
		    return 0;
	    return 0;
	}

	//is x > y? (x and y both nonnegative)
	this.greater = function(x, y) {
	    var i;
	    var k = (x.length < y.length) ? x.length : y.length;

	    for (i = x.length; i < y.length; i++)
		if (y[i])
		    return 0;  //y has more digits

	    for (i = y.length; i < x.length; i++)
		if (x[i])
		    return 1;  //x has more digits

	    for (i = k - 1; i >= 0; i--)
		if (x[i] > y[i])
		    return 1;
		else if (x[i] < y[i])
		    return 0;
	    return 0;
	}

	//divide x by y giving quotient q and remainder r.  (q=floor(x/y),  r=x mod y).  All 4 are bigints.
	//x must have at least this.one leading zero element.
	//y must be nonzero.
	//q and r must be arrays that are exactly the same length as x. (Or q can have more).
	//Must have x.length >= y.length >= 2.
	this.divide_ = function(x, y, q, r) {
	    var kx, ky;
	    var i, j, y1, y2, c, a, b;
	    this.copy_(r, x);
	    for (ky = y.length; y[ky - 1] == 0; ky--)
		; //ky is number of elements in y, not including leading zeros

	    //normalize: ensure the most significant element of y has its highest bit set
	    b = y[ky - 1];
	    for (a = 0; b; a++)
		b >>= 1;
	    a = this.bpe - a;  //a is how many bits to shift so that the high order bit of y is leftmost in its array element
	    this.leftShift_(y, a);  //multiply both by 1<<a now, then divide both by that at the end
	    this.leftShift_(r, a);

	    //Rob Vithis.sser discovered a bug: the following line was originally just before the normalization.
	    for (kx = r.length; r[kx - 1] == 0 && kx > ky; kx--)
		; //kx is number of elements in normalized x, not including leading zeros

	    this.copyInt_(q, 0);                      // q=0
	    while (!this.greaterShift(y, r, kx - ky)) {  // while (leftShift_(y,kx-ky) <= r) {
		this.subShift_(r, y, kx - ky);             //   r=r-leftShift_(y,kx-ky)
		q[kx - ky]++;                       //   q[kx-ky]++;
	    }                                   // }

	    for (i = kx - 1; i >= ky; i--) {
		if (r[i] == y[ky - 1])
		    q[i - ky] = this.mask;
		else
		    q[i - ky] = Math.floor((r[i] * this.radix + r[i - 1]) / y[ky - 1]);

		//The following for(;;) loop is equivalent to the commented while loop,
		//except that the uncommented version avoids overflow.
		//The commented loop comes from HAC, which athis.ssumes r[-1]==y[-1]==0
		//  while (q[i-ky]*(y[ky-1]*this.radix+y[ky-2]) > r[i]*this.radix*this.radix+r[i-1]*this.radix+r[i-2])
		//    q[i-ky]--;
		for (; ; ) {
		    y2 = (ky > 1 ? y[ky - 2] : 0) * q[i - ky];
		    c = y2 >> this.bpe;
		    y2 = y2 & this.mask;
		    y1 = c + q[i - ky] * y[ky - 1];
		    c = y1 >> this.bpe;
		    y1 = y1 & this.mask;

		    if (c == r[i] ? y1 == r[i - 1] ? y2 > (i > 1 ? r[i - 2] : 0) : y1 > r[i - 1] : c > r[i])
			q[i - ky]--;
		    else
			break;
		}

		this.linCombShift_(r, y, -q[i - ky], i - ky);    //r=r-q[i-ky]*leftShift_(y,i-ky)
		if (this.negative(r)) {
		    this.addShift_(r, y, i - ky);         //r=r+leftShift_(y,i-ky)
		    q[i - ky]--;
		}
	    }

	    this.rightShift_(y, a);  //undo the normalization step
	    this.rightShift_(r, a);  //undo the normalization step
	}

	//do carries and borrows so each element of the bigInt x fits in this.bpe bits.
	this.carry_ = function(x) {
	    var i, k, c, b;
	    k = x.length;
	    c = 0;
	    for (i = 0; i < k; i++) {
		c += x[i];
		b = 0;
		if (c < 0) {
		    b = -(c >> this.bpe);
		    c += b * this.radix;
		}
		x[i] = c & this.mask;
		c = (c >> this.bpe) - b;
	    }
	}

	//return x mod n for bigInt x and integer n.
	this.modInt = function(x, n) {
	    var i, c = 0;
	    for (i = x.length - 1; i >= 0; i--)
		c = (c * this.radix + x[i]) % n;
	    return c;
	}

	//convert the integer t into a bigInt with at least the given number of bits.
	//the returned array stores the bigInt in bpe-bit chunks, little endian (buff[0] is least significant word)
	//Pad the array with leading zeros so that it has at least minSize elements.
	//There will always be at least this.one leading 0 element.
	this.int2bigInt = function(t, bits, minSize) {
	    var i, k, buff;
	    k = Math.ceil(bits / this.bpe) + 1;
	    k = minSize > k ? minSize : k;
	    buff = new Array(k);
	    this.copyInt_(buff, t);
	    return buff;
	}

	//return the bigInt given a string representation in a given base.
	//Pad the array with leading zeros so that it has at least minSize elements.
	//If base=-1, then it reads in a space-separated list of array elements in decimal.
	//The array will always have at least this.one leading zero, unlethis.ss base=-1.
	this.str2bigInt = function(s, base, minSize) {
	    var d, i, j, x, y, kk;
	    var k = s.length;
	    if (base == -1) { //comma-separated list of array elements in decimal
		x = new Array(0);
		for (; ; ) {
		    y = new Array(x.length + 1);
		    for (i = 0; i < x.length; i++)
			y[i + 1] = x[i];
		    y[0] = this.parseInt(s, 10);
		    x = y;
		    d = s.indexOf(',', 0);
		    if (d < 1)
			break;
		    s = s.substring(d + 1);
		    if (s.length == 0)
			break;
		}
		if (x.length < minSize) {
		    y = new Array(minSize);
		    this.copy_(y, x);
		    return y;
		}
		return x;
	    }

	    x = this.int2bigInt(0, base * k, 0);
	    for (i = 0; i < k; i++) {
		d = this.digitsstr.indexOf(s.substring(i, i + 1), 0);
		if (base <= 36 && d >= 36)  //convert lowercase to uppercase if base<=36
		    d -= 26;
		if (d >= base || d < 0) {   //stop at first illegal character
		    break;
		}
		this.multInt_(x, base);
		this.addInt_(x, d);
	    }

	    for (k = x.length; k > 0 && !x[k - 1]; k--)
		; //strip off leading zeros
	    k = minSize > k + 1 ? minSize : k + 1;
	    y = new Array(k);
	    kk = k < x.length ? k : x.length;
	    for (i = 0; i < kk; i++)
		y[i] = x[i];
	    for (; i < k; i++)
		y[i] = 0;
	    return y;
	}

	//is bigint x equal to integer y?
	//y must have lethis.ss than bpe bits
	this.equalsInt = function(x, y) {
	    var i;
	    if (x[0] != y)
		return 0;
	    for (i = 1; i < x.length; i++)
		if (x[i])
		    return 0;
	    return 1;
	}

	//are bigints x and y equal?
	//this works even if x and y are different lengths and have arbitrarily many leading zeros
	this.equals = function(x, y) {
	    var i;
	    var k = x.length < y.length ? x.length : y.length;
	    for (i = 0; i < k; i++)
		if (x[i] != y[i])
		    return 0;
	    if (x.length > y.length) {
		for (; i < x.length; i++)
		    if (x[i])
			return 0;
	    } else {
		for (; i < y.length; i++)
		    if (y[i])
			return 0;
	    }
	    return 1;
	}

	//is the bigInt x equal to zero?
	this.isZero = function(x) {
	    var i;
	    for (i = 0; i < x.length; i++)
		if (x[i])
		    return 0;
	    return 1;
	}

	//convert a bigInt into a string in a given base, from base 2 up to base 95.
	//Base -1 prints the contents of the array representing the number.
	this.bigInt2str = function(x, base) {
	    var i, t, s = "";

	    if (this.s6.length != x.length)
		this.s6 = this.dup(x);
	    else
		this.copy_(this.s6, x);

	    if (base == -1) { //return the list of array contents
		for (i = x.length - 1; i > 0; i--)
		    s += x[i] + ',';
		s += x[0];
	    }
	    else { //return it in the given base
		while (!this.isZero(this.s6)) {
		    t = this.divInt_(this.s6, base);  //t=this.s6 % base; this.s6=floor(this.s6/base);
		    s = this.digitsstr.substring(t, t + 1) + s;
		}
	    }
	    if (s.length == 0)
		s = "0";
	    return s;
	}

	//returns a duplicate of bigInt x
	this.dup = function(x) {
	    var i;
	    var buff = new Array(x.length);
	    this.copy_(buff, x);
	    return buff;
	}

	//do x=y on bigInts x and y.  x must be an array at least as big as y (not counting the leading zeros in y).
	this.copy_ = function(x, y) {
	    var i;
	    var k = x.length < y.length ? x.length : y.length;
	    for (i = 0; i < k; i++)
		x[i] = y[i];
	    for (i = k; i < x.length; i++)
		x[i] = 0;
	}

	//do x=y on bigInt x and integer y.
	this.copyInt_ = function(x, n) {
	    var i, c;
	    for (c = n, i = 0; i < x.length; i++) {
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	}

	//do x=x+n where x is a bigInt and n is an integer.
	//x must be large enough to hold the result.
	this.addInt_ = function(x, n) {
	    var i, k, c, b;
	    x[0] += n;
	    k = x.length;
	    c = 0;
	    for (i = 0; i < k; i++) {
		c += x[i];
		b = 0;
		if (c < 0) {
		    b = -(c >> this.bpe);
		    c += b * this.radix;
		}
		x[i] = c & this.mask;
		c = (c >> this.bpe) - b;
		if (!c)
		    return; //stop carrying as soon as the carry is zero
	    }
	}

	//right shift bigInt x by n bits.  0 <= n < bpe.
	this.rightShift_ = function(x, n) {
	    var i;
	    var k = Math.floor(n / this.bpe);
	    if (k) {
		for (i = 0; i < x.length - k; i++) //right shift x by k elements
		    x[i] = x[i + k];
		for (; i < x.length; i++)
		    x[i] = 0;
		n %= this.bpe;
	    }
	    for (i = 0; i < x.length - 1; i++) {
		x[i] = this.mask & ((x[i + 1] << (this.bpe - n)) | (x[i] >> n));
	    }
	    x[i] >>= n;
	}

	//do x=floor(|x|/2)*sgn(x) for bigInt x in 2's complement
	this.halve_ = function(x) {
	    var i;
	    for (i = 0; i < x.length - 1; i++) {
		x[i] = this.mask & ((x[i + 1] << (this.bpe - 1)) | (x[i] >> 1));
	    }
	    x[i] = (x[i] >> 1) | (x[i] & (this.radix >> 1));  //most significant bit stays the same
	}

	//left shift bigInt x by n bits.
	this.leftShift_ = function(x, n) {
	    var i;
	    var k = Math.floor(n / this.bpe);
	    if (k) {
		for (i = x.length; i >= k; i--) //left shift x by k elements
		    x[i] = x[i - k];
		for (; i >= 0; i--)
		    x[i] = 0;
		n %= this.bpe;
	    }
	    if (!n)
		return;
	    for (i = x.length - 1; i > 0; i--) {
		x[i] = this.mask & ((x[i] << n) | (x[i - 1] >> (this.bpe - n)));
	    }
	    x[i] = this.mask & (x[i] << n);
	}

	//do x=x*n where x is a bigInt and n is an integer.
	//x must be large enough to hold the result.
	this.multInt_ = function(x, n) {
	    var i, k, c, b;
	    if (!n)
		return;
	    k = x.length;
	    c = 0;
	    for (i = 0; i < k; i++) {
		c += x[i] * n;
		b = 0;
		if (c < 0) {
		    b = -(c >> this.bpe);
		    c += b * this.radix;
		}
		x[i] = c & this.mask;
		c = (c >> this.bpe) - b;
	    }
	}

	//do x=floor(x/n) for bigInt x and integer n, and return the remainder
	this.divInt_ = function(x, n) {
	    var i, r = 0, s;
	    for (i = x.length - 1; i >= 0; i--) {
		s = r * this.radix + x[i];
		x[i] = Math.floor(s / n);
		r = s % n;
	    }
	    return r;
	}

	//do the linear combination x=a*x+b*y for bigInts x and y, and integers a and b.
	//x must be large enough to hold the answer.
	this.linComb_ = function(x, y, a, b) {
	    var i, c, k, kk;
	    k = x.length < y.length ? x.length : y.length;
	    kk = x.length;
	    for (c = 0, i = 0; i < k; i++) {
		c += a * x[i] + b * y[i];
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	    for (i = k; i < kk; i++) {
		c += a * x[i];
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	}

	//do the linear combination x=a*x+b*(y<<(ys*bpe)) for bigInts x and y, and integers a, b and ys.
	//x must be large enough to hold the answer.
	this.linCombShift_ = function(x, y, b, ys) {
	    var i, c, k, kk;
	    k = x.length < ys + y.length ? x.length : ys + y.length;
	    kk = x.length;
	    for (c = 0, i = ys; i < k; i++) {
		c += x[i] + b * y[i - ys];
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	    for (i = k; c && i < kk; i++) {
		c += x[i];
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	}

	//do x=x+(y<<(ys*bpe)) for bigInts x and y, and integers a,b and ys.
	//x must be large enough to hold the answer.
	this.addShift_ = function(x, y, ys) {
	    var i, c, k, kk;
	    k = x.length < ys + y.length ? x.length : ys + y.length;
	    kk = x.length;
	    for (c = 0, i = ys; i < k; i++) {
		c += x[i] + y[i - ys];
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	    for (i = k; c && i < kk; i++) {
		c += x[i];
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	}

	//do x=x-(y<<(ys*bpe)) for bigInts x and y, and integers a,b and ys.
	//x must be large enough to hold the answer.
	this.subShift_ = function(x, y, ys) {
	    var i, c, k, kk;
	    k = x.length < ys + y.length ? x.length : ys + y.length;
	    kk = x.length;
	    for (c = 0, i = ys; i < k; i++) {
		c += x[i] - y[i - ys];
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	    for (i = k; c && i < kk; i++) {
		c += x[i];
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	}

	//do x=x-y for bigInts x and y.
	//x must be large enough to hold the answer.
	//negative answers will be 2s complement
	this.sub_ = function(x, y) {
	    var i, c, k, kk;
	    k = x.length < y.length ? x.length : y.length;
	    for (c = 0, i = 0; i < k; i++) {
		c += x[i] - y[i];
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	    for (i = k; c && i < x.length; i++) {
		c += x[i];
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	}

	//do x=x+y for bigInts x and y.
	//x must be large enough to hold the answer.
	this.add_ = function(x, y) {
	    var i, c, k, kk;
	    k = x.length < y.length ? x.length : y.length;
	    for (c = 0, i = 0; i < k; i++) {
		c += x[i] + y[i];
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	    for (i = k; c && i < x.length; i++) {
		c += x[i];
		x[i] = c & this.mask;
		c >>= this.bpe;
	    }
	}

	//do x=x*y for bigInts x and y.  This is faster when y<x.
	this.mult_ = function(x, y) {
	    var i;
	    if (this.ss.length != 2 * x.length)
		this.ss = new Array(2 * x.length);
	    this.copyInt_(this.ss, 0);
	    for (i = 0; i < y.length; i++)
		if (y[i])
		    this.linCombShift_(this.ss, x, y[i], i);   //this.ss=1*this.ss+y[i]*(x<<(i*bpe))
	    this.copy_(x, this.ss);
	}

	//do x=x mod n for bigInts x and n.
	this.mod_ = function(x, n) {
	    if (this.s4.length != x.length)
		this.s4 = this.dup(x);
	    else
		this.copy_(this.s4, x);
	    if (this.s5.length != x.length)
		this.s5 = this.dup(x);
	    this.divide_(this.s4, n, this.s5, x);  //x = remainder of this.s4 / n
	}

	//do x=x*y mod n for bigInts x,y,n.
	//for greater speed, let y<x.
	this.multMod_ = function(x, y, n) {
	    var i;
	    if (this.s0.length != 2 * x.length)
		this.s0 = new Array(2 * x.length);
	    this.copyInt_(this.s0, 0);
	    for (i = 0; i < y.length; i++)
		if (y[i])
		    this.linCombShift_(this.s0, x, y[i], i);   //this.s0=1*this.s0+y[i]*(x<<(i*this.bpe))
	    this.mod_(this.s0, n);
	    this.copy_(x, this.s0);
	}

	//do x=x*x mod n for bigInts x,n.
	this.squareMod_ = function(x, n) {
	    var i, j, d, c, kx, kn, k;
	    for (kx = x.length; kx > 0 && !x[kx - 1]; kx--)
		;  //ignore leading zeros in x
	    k = kx > n.length ? 2 * kx : 2 * n.length; //k=# elements in the product, which is twice the elements in the larger of x and n
	    if (this.s0.length != k)
		this.s0 = new Array(k);
	    this.copyInt_(this.s0, 0);
	    for (i = 0; i < kx; i++) {
		c = this.s0[2 * i] + x[i] * x[i];
		this.s0[2 * i] = c & this.mask;
		c >>= this.bpe;
		for (j = i + 1; j < kx; j++) {
		    c = this.s0[i + j] + 2 * x[i] * x[j] + c;
		    this.s0[i + j] = (c & this.mask);
		    c >>= this.bpe;
		}
		this.s0[i + kx] = c;
	    }
	    this.mod_(this.s0, n);
	    this.copy_(x, this.s0);
	}

	//return x with exactly k leading zero elements
	this.trim = function(x, k) {
	    var i, y;
	    for (i = x.length; i > 0 && !x[i - 1]; i--)
		;
	    y = new Array(i + k);
	    this.copy_(y, x);
	    return y;
	}

	//do x=x**y mod n, where x,y,n are bigInts and ** is expthis.onentiation.  0**0=1.
	//this is faster when n is odd.  x usually needs to have as many elements as n.
	this.powMod_ = function(x, y, n) {
	    var k1, k2, kn, np;
	    if (this.s7.length != n.length)
		this.s7 = this.dup(n);

	    //for even modulus, use a simple square-and-multiply algorithm,
	    //rather than using the more complex Montgomery algorithm.
	    if ((n[0] & 1) == 0) {
		this.copy_(this.s7, x);
		this.copyInt_(x, 1);
		while (!this.equalsInt(y, 0)) {
		    if (y[0] & 1)
			this.multMod_(x, this.s7, n);
		    this.divInt_(y, 2);
		    this.squareMod_(this.s7, n);
		}
		return;
	    }

	    //calculate np from n for the Montgomery multiplications
	    this.copyInt_(this.s7, 0);
	    for (kn = n.length; kn > 0 && !n[kn - 1]; kn--)
		;
	    np = this.radix - this.inverseModInt(this.modInt(n, this.radix), this.radix);
	    this.s7[kn] = 1;
	    this.multMod_(x, this.s7, n);   // x = x * 2**(kn*bp) mod n

	    if (this.s3.length != x.length)
		this.s3 = this.dup(x);
	    else
		this.copy_(this.s3, x);

	    for (k1 = y.length - 1; k1 > 0 & !y[k1]; k1--)
		;  //k1=first nonzero element of y
	    if (y[k1] == 0) {  //anything to the 0th power is 1
		this.copyInt_(x, 1);
		return;
	    }
	    for (k2 = 1 << (this.bpe - 1); k2 && !(y[k1] & k2); k2 >>= 1)
		;  //k2=position of first 1 bit in y[k1]
	    for (; ; ) {
		if (!(k2 >>= 1)) {  //look at next bit of y
		    k1--;
		    if (k1 < 0) {
			this.mont_(x, this.one, n, np);
			return;
		    }
		    k2 = 1 << (this.bpe - 1);
		}
		this.mont_(x, x, n, np);

		if (k2 & y[k1]) //if next bit is a 1
		    this.mont_(x, this.s3, n, np);
	    }
	}


	//do x=x*y*Ri mod n for bigInts x,y,n,
	//  where Ri = 2**(-kn*bpe) mod n, and kn is the
	//  number of elements in the n array, not
	//  counting leading zeros.
	//x array must have at least as many elemnts as the n array
	//It's OK if x and y are the same variable.
	//must have:
	//  x,y < n
	//  n is odd
	//  np = -(n^(-1)) mod this.radix
	this.mont_ = function(x, y, n, np) {
	    var i, j, c, ui, t, ks;
	    var kn = n.length;
	    var ky = y.length;

	    if (this.sa.length != kn)
		this.sa = new Array(kn);

	    this.copyInt_(this.sa, 0);

	    for (; kn > 0 && n[kn - 1] == 0; kn--)
		; //ignore leading zeros of n
	    for (; ky > 0 && y[ky - 1] == 0; ky--)
		; //ignore leading zeros of y
	    ks = this.sa.length - 1; //this.sa will never have more than this many nonzero elements.

	    //the following loop consumes 95% of the runtime for randTruePrime_() and powMod_() for large numbers
	    for (i = 0; i < kn; i++) {
		t = this.sa[0] + x[i] * y[0];
		ui = ((t & this.mask) * np) & this.mask;  //the inner "& this.mask" was needed on Safari (but not MSIE) at this.one time
		c = (t + ui * n[0]) >> this.bpe;
		t = x[i];

		//do this.sa=(this.sa+x[i]*y+ui*n)/b   where b=2**bpe.  Loop is unrolled 5-fold for speed
		j = 1;
		for (; j < ky - 4; ) {
		    c += this.sa[j] + ui * n[j] + t * y[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		    c += this.sa[j] + ui * n[j] + t * y[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		    c += this.sa[j] + ui * n[j] + t * y[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		    c += this.sa[j] + ui * n[j] + t * y[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		    c += this.sa[j] + ui * n[j] + t * y[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		}
		for (; j < ky; ) {
		    c += this.sa[j] + ui * n[j] + t * y[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		}
		for (; j < kn - 4; ) {
		    c += this.sa[j] + ui * n[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		    c += this.sa[j] + ui * n[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		    c += this.sa[j] + ui * n[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		    c += this.sa[j] + ui * n[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		    c += this.sa[j] + ui * n[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		}
		for (; j < kn; ) {
		    c += this.sa[j] + ui * n[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		}
		for (; j < ks; ) {
		    c += this.sa[j];
		    this.sa[j - 1] = c & this.mask;
		    c >>= this.bpe;
		    j++;
		}
		this.sa[j - 1] = c & this.mask;
	    }

	    if (!this.greater(n, this.sa))
		this.sub_(this.sa, n);
	    this.copy_(x, this.sa);
	}


	////////////////////////////////////
	// powMod asynchronous

	this.powModAsync = function(x, y, n, cbProgress, cbDone) {
	    var ans = this.expand(x, n.length);
	    this.powMod_Async(ans, this.trim(y, 2), this.trim(n, 2), cbProgress, cbDone);  //this should work without the trim, but doesn't
	    //return trim(ans,1);
	}

	//do x=x**y mod n, where x,y,n are bigInts and ** is exponentiation.  0**0=1.
	//this is faster when n is odd.  x usually needs to have as many elements as n.
	this.powMod_Async = function(x, y, n, cbProgress, cbDone) {
	    var k1, k2, kn, np;
	    if (this.s7.length != n.length)
		this.s7 = this.dup(n);

	    //for even modulus, use a simple square-and-multiply algorithm,
	    //rather than using the more complex Montgomery algorithm.
	    if ((n[0] & 1) == 0) {
		this.copy_(this.s7, x);
		this.copyInt_(x, 1);
		while (!this.equalsInt(y, 0)) {
		    if (y[0] & 1)
			this.multMod_(x, this.s7, n);
		    this.divInt_(y, 2);
		    this.squareMod_(this.s7, n);
		}
		cbDone(this.trim(x, 1));
	    }

	    //calculate np from n for the Montgomery multiplications
	    this.copyInt_(this.s7, 0);
	    for (kn = n.length; kn > 0 && !n[kn - 1]; kn--)
		;
	    np = this.radix - this.inverseModInt(this.modInt(n, this.radix), this.radix);
	    this.s7[kn] = 1;
	    this.multMod_(x, this.s7, n);   // x = x * 2**(kn*bp) mod n

	    if (this.s3.length != x.length)
		this.s3 = this.dup(x);
	    else
		this.copy_(this.s3, x);

	    for (k1 = y.length - 1; k1 > 0 & !y[k1]; k1--)
		;  //k1=first nonzero element of y
	    if (y[k1] == 0) {  //anything to the 0th power is 1
		this.copyInt_(x, 1);
		cbDone(this.trim(x, 1));
	    }
	    for (k2 = 1 << (this.bpe - 1); k2 && !(y[k1] & k2); k2 >>= 1)
		;  //k2=position of first 1 bit in y[k1]

	    this.powMod_AsyncRec(x, y, n, cbProgress, cbDone, k1, k2, kn, np);
	}

	this.powMod_AsyncRec = function(x, y, n, cbProgress, cbDone, k1, k2, kn, np) {
	    for (var i = 0; i < 20; i++) {
		if (!(k2 >>= 1)) {  //look at next bit of y
		    k1--;
		    if (k1 < 0) {
			this.mont_(x, this.one, n, np);
			cbDone(this.trim(x, 1));
			return;
		    }
		    k2 = 1 << (this.bpe - 1);
		}
		this.mont_(x, x, n, np);

		if (k2 & y[k1]) //if next bit is a 1
		    this.mont_(x, this.s3, n, np);
	    }
	    setTimeout(function() {
		cbProgress();
		leemon.powMod_AsyncRec(x, y, n, cbProgress, cbDone, k1, k2, kn, np);
	    }, 1);

	}


	//////////////////////////////////////////////
	// xor: return (x xor y) for bigInts x and y (the smaller bigInt is padded with zeros
	this.xor = function(x, y) {
	    var i;
	    var s = x.length < y.length ? x : y;
	    var b = x.length > y.length ? x : y;
	    var buff = new Array(b.length);

	    for (i = 0; i < s.length; i++)
		buff[i] = x[i] ^ y[i];

	    for (i = s.length; i < b.length; i++)
		buff[i] = b[i];

	    return buff;
	}

	//////////////////////////////////////////////
	// randBigIntInZq: return a random bigInt in Zq (q is a bigInt)
	this.randBigIntInZq = function(q) {

	    var n = this.bitSize(q);
	    var r;
	    do {
		r = this.randBigInt(n, 0);
	    } while (this.greater(q, r) == 0);

	    return r;
	}

	//////////////////////////////////////////////
	// concatBigInts: return a string representing the concatenation of the bigIntegers in the array bigInts to the base b
	this.concatBigInts = function(bigInts, b, separator) {
	    var i, s = [];
	    for (i = 0; i < bigInts.length; i++) {
		s.push(this.bigInt2str(bigInts[i], b));
	    }
	    return s.join(separator);
	}

	////////////////////////////////////////////////////////


	//return a k-bit random probable prime with probability of error < 2^-80
	//Asynchronous variant
	this.randProbPrimeAsync = function(k, cbDone, cbProgress) {
	    if (k >= 600)
		return this.randProbPrimeRoundsAsync(k, 2, cbDone, cbProgress); //numbers from HAC table 4.3
	    if (k >= 550)
		return this.randProbPrimeRoundsAsync(k, 4, cbDone, cbProgress);
	    if (k >= 500)
		return this.randProbPrimeRoundsAsync(k, 5, cbDone, cbProgress);
	    if (k >= 400)
		return this.randProbPrimeRoundsAsync(k, 6, cbDone, cbProgress);
	    if (k >= 350)
		return this.randProbPrimeRoundsAsync(k, 7, cbDone, cbProgress);
	    if (k >= 300)
		return this.randProbPrimeRoundsAsync(k, 9, cbDone, cbProgress);
	    if (k >= 250)
		return this.randProbPrimeRoundsAsync(k, 12, cbDone, cbProgress); //numbers from HAC table 4.4
	    if (k >= 200)
		return this.randProbPrimeRoundsAsync(k, 15, cbDone, cbProgress);
	    if (k >= 150)
		return this.randProbPrimeRoundsAsync(k, 18, cbDone, cbProgress);
	    if (k >= 100)
		return this.randProbPrimeRoundsAsync(k, 27, cbDone, cbProgress);
	    return this.randProbPrimeRoundsAsync(k, 40, cbDone, cbProgress); //number from HAC remark 4.26 (only an estimate)
	}

	//return a k-bit probable random prime using n rounds of Miller Rabin (after trial division with small this.primes)
	this.randProbPrimeRoundsAsync = function(k, n, cbDone, cbProgress) {
	    var ans, i, divisible, B;
	    B = 30000;  //B is largest prime to use in trial division
	    ans = this.int2bigInt(0, k, 0);

	    //optimization: try larger and smaller B to find the best limit.

	    if (this.primes.length == 0)
		this.primes = this.findPrimes(30000);  //check for divisibility by primes <=30000

	    if (this.rpprb.length != ans.length)
		this.rpprb = this.dup(ans);

	    this.randProbPrimeRoundsAsyncRec(k, n, ans, divisible, i, B, cbDone, cbProgress);
	}

	this.randProbPrimeRoundsAsyncRec = function(k, n, ans, divisible, i, B, cbDone, cbProgress) {
	    for (var i = 0; i < 2; i++) {  //keep trying random values for ans until this.one appears to be prime
		//optimization: pick a random number times L=2*3*5*...*p, plus a
		//   random element of the list of all numbers in [0,L) not divisible by any prime up to p.
		//   This can reduce the amount of random number generation.

		this.randBigInt_(ans, k, 0); //ans = a random odd number to check
		ans[0] |= 1;
		divisible = 0;

		//check ans for divisibility by small primes up to B
		for (i = 0; (i < this.primes.length) && (this.primes[i] <= B); i++)
		    if (this.modInt(ans, this.primes[i]) == 0 && !this.equalsInt(ans, this.primes[i])) {
			divisible = 1;
			break;
		    }

		//optimization: change millerRabin so the base can be bigger than the number being checked, then eliminate the while here.

		//do n rounds of Miller Rabin, with random bases lethis.ss than ans
		for (i = 0; i < n && !divisible; i++) {
		    this.randBigInt_(this.rpprb, k, 0);
		    while (!this.greater(ans, this.rpprb)) //pick a random rpprb that's < ans
			this.randBigInt_(this.rpprb, k, 0);
		    if (!this.millerRabin(ans, this.rpprb))
			divisible = 1;
		}

		if (!divisible) {
		    cbDone(ans);
		    return ans;
		}
	    }

	    setTimeout(function() {
		cbProgress();
		leemon.randProbPrimeRoundsAsyncRec(k, n, ans, divisible, i, B, cbDone, cbProgress);
	    }, 1);
	}
	
	
	this.rightShift = function(x,n){
	    var x2=this.dup(x)
	    this.rightShift_(x2,n);
	    return x2;
	}
	
	//be careful be overflows in x2: when x is not big enough, overflows can appear
	this.leftShift = function(x,n){
	    var x2=this.dup(x)
	    this.leftShift_(x2,n);
	    return x2;
	}

	this.init()
    }

    window.leemon = leemon;

})(window);

