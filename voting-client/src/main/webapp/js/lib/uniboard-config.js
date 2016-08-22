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
 */


(function (window) {

	function Config() {

		/**
		 * The url of the public board.
		 */
		this.URL_UNIBOARD_GET = '/uniboard-ws-univote/messages/query';
		this.URL_UNIBOARD_POST = '/uniboard-ws-univote/messages/post';


		/**
		 * The default base for BigInteger representations as string.
		 */
		this.BASE = 10;

		/**
		 * The hash function used all over the client.
		 */
		this.HASH_FUNCTION = CryptoJS.SHA256; // CryptoJS.SHA512;

		/**
		 * UniVote Board setting: pk, p, q and g for Schnorr signature.
		 */
		this.BOARD_SETTING = {
			P: "178011905478542266528237562450159990145232156369120674273274450314442865788737020770612695252123463079567156784778466449970650770920727857050009668388144034129745221171818506047231150039301079959358067395348717066319802262019714966524135060945913707594956514672855690606794135837542707371727429551343320695239",
			Q: "864205495604807476120572616017955259175325408501",
			G: "174068207532402095185811980123523436538604490794561350978495831040599953488455823147851597408940950725307797094915759492368300574252438761037084473467180148876118103083043754985190983472601550494691329488083395492313850000361646482644608492304078721818959999056496097769368017749273708962006689187956744210730",
			PK: "66958355597810698489471425362307177643942027542459889847139582549668126282135390777783858460746726000662328230820010568386790445146483265529575179386210607141523872577665302631137940868775056108711129683088056561604904097738690582247866539343707184146771244022706478825219472668923353222121740341182394461614"
		};

		/**
		 * Signature setting of known authors of posts. If the author is not available here, his/her signature isnt validated.
		 * Requries pk, p, q and g for Schnorr signature.
		 */
		this.KNOWN_AUTHORS = [{
				P: "178011905478542266528237562450159990145232156369120674273274450314442865788737020770612695252123463079567156784778466449970650770920727857050009668388144034129745221171818506047231150039301079959358067395348717066319802262019714966524135060945913707594956514672855690606794135837542707371727429551343320695239",
				Q: "864205495604807476120572616017955259175325408501",
				G: "174068207532402095185811980123523436538604490794561350978495831040599953488455823147851597408940950725307797094915759492368300574252438761037084473467180148876118103083043754985190983472601550494691329488083395492313850000361646482644608492304078721818959999056496097769368017749273708962006689187956744210730",
				PK: "56046417983892736553802144126938027620541527748149798291772741324585401896629333631946067035048636852193599009683854669888084829275709016527803283969382064059277595434720007311044291973015468412076193860820267069585136814954876118357235567344941207775471304204747723868227800093861417983672494051114178304212"
			}];
	}
	window.ubConfig = new Config();

})(window);

