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


(function(window) {

	function Config() {

		
		
		/**
		 * The url of the public board.
		 */
		this.URL_UNIBOARD			= 'http://localhost:8080/uniboard/messages/query';


		/**
		 * The home site.
		 */
		this.HOME_SITE					= 'index.xhtml';

		/**
		 * Signs used for concat.
		 */
		this.CONCAT_SEPARATOR			= "|";
		this.CONCAT_DELIMINATOR_L		= "(";
		this.CONCAT_DELIMINATOR_R		= ")";

//		/**
//		 * Pre- and postfix used for secret key padding. Important: As the padded
//		 * secret key is converted into a bigInt only leemon's base64 charset can
//		 * be used (0-9, A-Z, a-z, _ and =)
//		 */
//		this.PRIVATE_KEY_PREFIX			= "=====BEGIN_UNIVOTE_PRIVATE_KEY=====";
//		this.PRIVATE_KEY_POSTFIX		= "=====END_UNIVOTE_PRIVATE_KEY=====";
//
//		/**
//		 * Pre- and postfix used for padding the encrypted secret key.
//		 */
//		this.ENC_PRIVATE_KEY_PREFIX		= "-----BEGIN ENCRYPTED UNIVOTE VOTING KEY-----";
//		this.ENC_PRIVATE_KEY_POSTFIX	= "-----END ENCRYPTED UNIVOTE VOTING KEY-----";
//
//		/**
//		 * Size of the one-time-pad to encrypt secret key.
//		 *
//		 * => (size of q) + (size of pre- and postfix) = 256 + 411 < 1024
//		 */
//		this.PRIVATE_KEY_ONE_TIME_PAD_SIZE = 1024;
		
		/**
		 * Pre- and postfix used for secret key padding. Important: As the padded
		 * secret key is converted into a bigInt only leemon's base64 charset can
		 * be used (0-9, A-Z, a-z, _ and =)
		 */
		this.PRIVATE_KEY_PREFIX			= "=====BEGIN_UNICERT_PRIVATE_KEY=====";
		this.PRIVATE_KEY_POSTFIX		= "=====END_UNICERT_PRIVATE_KEY=====";

		/**
		 * Pre- and postfix used for padding the encrypted secret key.
		 */
		this.ENC_PRIVATE_KEY_PREFIX		= "-----BEGIN ENCRYPTED UNICERT KEY-----";
		this.ENC_PRIVATE_KEY_POSTFIX	= "-----END ENCRYPTED UNICERT KEY-----";

		/**
		 * Size of prefix and postfix
                 * Used for the one-time-pad to encrypt secret key.
		 *
		 * => size of pre- and postfix = 411 < 512
		 */
		this.PRIVATE_KEY_ONE_TIME_PAD_PREPOSTFIX_SIZE = 512;

		/**
		 * Cryptographic parameters.
		 *
		 * -> Base refers only to the bigInt representation of Schnorr, Elgamal
		 *    and RSA parameters.
		 */
		this.BASE = 10;

		/**
		 * p, q and q for Schnorr signature.
		 */
		this.SCHNORR = {
			P: "161931481198080639220214033595931441094586304918402813506510547237223787775475425991443924977419330663170224569788019900180050114468430413908687329871251101280878786588515668012772798298511621634145464600626619548823238185390034868354933050128115662663653841842699535282987363300852550784188180264807606304297",
			Q: "65133683824381501983523684796057614145070427752690897588060462960319251776021",
			G: "109291242937709414881219423205417309207119127359359243049468707782004862682441897432780127734395596275377218236442035534825283725782836026439537687695084410797228793004739671835061419040912157583607422965551428749149162882960112513332411954585778903685207256083057895070357159920203407651236651002676481874709"
		}

		/**
		 * p, q and g for Elgamal encryption.
		 */
		this.ELGAMAL = {
			P: "127557310857026250526155290716175721659501699151591799276600227376716505297573619294610035498965642711634086243287869889860211239877645998908773071410481719856828493012051757158513651215977686324747806475706581177754781891491034188437985448668758765692160128854525678725065063346126289455727622203325341952627",
			Q: "63778655428513125263077645358087860829750849575795899638300113688358252648786809647305017749482821355817043121643934944930105619938822999454386535705240859928414246506025878579256825607988843162373903237853290588877390945745517094218992724334379382846080064427262839362532531673063144727863811101662670976313",
			G: "4"
		}

//		/**
//		 * n and pk for RSA.
//		 */
//		this.RSA = {
//			N:	"92558986469241318288832740329307338833681686020741869464706500295501628894304148797311330831090507436975452081963984558360067050004210261767623583861598169322415200514128217638599351725489038709307386471895685520372416415885231246876440100529917690267503573299999732612304403746086647757871473345737464088331",
//			PK:	"65537"
//		};

		/**
		 * To prevent script timeouts the havy computations can be run asynchronously.
		 * This is mainly used for IE < 9, but is also a nice feature for all other
		 * browsers as the user get a feedback about the process of the computations.
		 */
		this.COMPUTE_ASYNCHRONOUSLY		= true;

		/**
		 * The secret key can be uploaded by file or manually through copy/paste.
		 * Whether or not the file upload is displayed depends on the html5 support
		 * of the browser. But this feature makes only sense if the secret key is
		 * retreived by file download. Otherwise use this flag to force the
		 * manually upload always.
		 */
		this.UPLOAD_SK_MANUALLY_ALWAYS	= true;


	}
	window.uvConfig = new Config();

})(window);
