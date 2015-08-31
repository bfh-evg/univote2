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

		this.MOCK = false;

		/**
		 * The urls of the certificate authority.
		 */
		this.URL_UNICERT_CERTIFICATE_AUTHORITY = '/unicert-authentication/certificate-request/';
		this.URL_PARAMETERS = '/voting-client2/parameters/';

		/**
		 * The url of the public board.
		 */
		this.URL_UNIBOARD_GET = '/uniboard/messages/query';
		this.URL_UNIBOARD_POST = '/uniboard/messages/post';


		if (this.MOCK) {
			this.URL_UNICERT_CERTIFICATE_AUTHORITY = 'http://uni.vote/certificate-request.php';
			this.URL_UNIBOARD_GET = 'http://uni.vote/elections.php';
			this.URL_UNIBOARD_POST = 'http://uni.vote/uniBoardPOST.php';
		}

		/**
		 * The home site.
		 */
		this.HOME_SITE = 'index.xhtml';

		/**
		 * The default base for BigInteger representations as string.
		 */
		this.BASE = 10;

		/**
		 * Sign used for concatenation (used only to creat proof/signature
		 * of verification key during registration)
		 */
		this.CONCAT_SEPARATOR = "|";

		/**
		 * Pre- and postfix used for secret key padding. Important: As the padded
		 * secret key is converted into a bigInt only leemon's base64 charset can
		 * be used (0-9, A-Z, a-z, _ and =)
		 */
		this.PRIVATE_KEY_PREFIX = "=====BEGIN_UNICERT_PRIVATE_KEY=====";
		this.PRIVATE_KEY_POSTFIX = "=====END_UNICERT_PRIVATE_KEY=====";

		/**
		 * Pre- and postfix used for padding the encrypted secret key generated by UniCert
		 */
		this.ENC_PRIVATE_KEY_PREFIX = "-----BEGIN ENCRYPTED UNICERT KEY-----";
		this.ENC_PRIVATE_KEY_POSTFIX = "-----END ENCRYPTED UNICERT KEY-----";

		/**
		 * Size of prefix and postfix
		 * Used for the one-time-pad to encrypt secret key.
		 *
		 * => size of pre- and postfix = 411 < 512
		 */
		this.PRIVATE_KEY_ONE_TIME_PAD_PREPOSTFIX_SIZE = 512;

		/**
		 * The size of the symmetric key used to encrpyt the secret key (PBKDF2)
		 */
		this.SYM_KEY_SIZE = 128;

		/**
		 * The number of iterations of the PBKDF2
		 */
		this.PWD_KEY_DERIVATION_ITERATION = 1000;

		/**
		 * The underlying hash function of the PBKDF2
		 */
		this.PWD_KEY_DERIVATION_HASHER = CryptoJS.algo.SHA1;

		/**
		 * UniVote Board setting: p, q and g for Schnorr signature.
		 */
		this.BOARD_SETTING = {
			P: "178011905478542266528237562450159990145232156369120674273274450314442865788737020770612695252123463079567156784778466449970650770920727857050009668388144034129745221171818506047231150039301079959358067395348717066319802262019714966524135060945913707594956514672855690606794135837542707371727429551343320695239",
			Q: "864205495604807476120572616017955259175325408501",
			G: "174068207532402095185811980123523436538604490794561350978495831040599953488455823147851597408940950725307797094915759492368300574252438761037084473467180148876118103083043754985190983472601550494691329488083395492313850000361646482644608492304078721818959999056496097769368017749273708962006689187956744210730",
			PK: "66958355597810698489471425362307177643942027542459889847139582549668126282135390777783858460746726000662328230820010568386790445146483265529575179386210607141523872577665302631137940868775056108711129683088056561604904097738690582247866539343707184146771244022706478825219472668923353222121740341182394461614"
		};

		/**
		 * EC setting: p, q and g for Schnorr signature.
		 */
		this.EC_SETTING = {
			P: "178011905478542266528237562450159990145232156369120674273274450314442865788737020770612695252123463079567156784778466449970650770920727857050009668388144034129745221171818506047231150039301079959358067395348717066319802262019714966524135060945913707594956514672855690606794135837542707371727429551343320695239",
			Q: "864205495604807476120572616017955259175325408501",
			G: "174068207532402095185811980123523436538604490794561350978495831040599953488455823147851597408940950725307797094915759492368300574252438761037084473467180148876118103083043754985190983472601550494691329488083395492313850000361646482644608492304078721818959999056496097769368017749273708962006689187956744210730",
			PK: "56046417983892736553802144126938027620541527748149798291772741324585401896629333631946067035048636852193599009683854669888084829275709016527803283969382064059277595434720007311044291973015468412076193860820267069585136814954876118357235567344941207775471304204747723868227800093861417983672494051114178304212"
		};

		this.CS = {
			RC0e: {
				TYPE: "Residue Classes",
				BASE: this.BASE,
				P: "167",
				Q: "83",
				G: "4"
			},
			RC1e: {
				TYPE: "Residue Classes",
				BASE: this.BASE,
				P: "89884656743115795386465259539451236680898848947115328636715040578866337902750481566354238661203768010560056939935696678829394884407208311246423715319737062188883946712432742638151109800623047059726541476042502884419075341171231440736956555270413618581675255342293149119973622969239858152417678164812113740223",
				Q: "44942328371557897693232629769725618340449424473557664318357520289433168951375240783177119330601884005280028469967848339414697442203604155623211857659868531094441973356216371319075554900311523529863270738021251442209537670585615720368478277635206809290837627671146574559986811484619929076208839082406056870111",
				G: "4"
			},
			RC2e: {
				TYPE: "Residue Classes",
				BASE: this.BASE,
				P: "16158503035655503650357438344334975980222051334857742016065172713762327569433945446598600705761456731844358980460949009747059779575245460547544076193224141560315438683650498045875098875194826053398028819192033784138396109321309878080919047169238085235290822926018152521443787945770532904303776199561965192760957166694834171210342487393282284747428088017663161029038902829665513096354230157075129296432088558362971801859230928678799175576150822952201848806616643615613562842355410104862578550863465661734839271290328348967522998634176499319107762583194718667771801067716614802322659239302476074096777926805529798824879",
				Q: "8079251517827751825178719172167487990111025667428871008032586356881163784716972723299300352880728365922179490230474504873529889787622730273772038096612070780157719341825249022937549437597413026699014409596016892069198054660654939040459523584619042617645411463009076260721893972885266452151888099780982596380478583347417085605171243696641142373714044008831580514519451414832756548177115078537564648216044279181485900929615464339399587788075411476100924403308321807806781421177705052431289275431732830867419635645164174483761499317088249659553881291597359333885900533858307401161329619651238037048388963402764899412439",
				G: "4"
			},
			RC3e: {
				TYPE: "Residue Classes",
				BASE: this.BASE,
				P: "2904802997684979031429751266652287185343487588181447618330743076143601865498555112868668022266559203625663078877490258721995264797270023560831442836093516200516055819853220249422024925494525813600122382903520906197364840270012052413988292184690761146180604389522384946371612875869038489784405654789562755666546621759776892408153190790080930100123746284224075121257652224788593802068214369290495086275786967073127915183202957500434821866026609283416272645553951861415817069299793203345162979862593723584529770402506155104819505875374380008547680367117472878708136497428006654308479264979152338818509590797044264172530642931949135881728647441773319439777155807723223165099627191170008146028545375587766944080959493647795765768349350646133842732758718957895411577422317390130051445859016247698037520949742756905563488653739484537428521855358075060657961012278379620619506576459855478234203189721457470807178553957231283671210463",
				Q: "1452401498842489515714875633326143592671743794090723809165371538071800932749277556434334011133279601812831539438745129360997632398635011780415721418046758100258027909926610124711012462747262906800061191451760453098682420135006026206994146092345380573090302194761192473185806437934519244892202827394781377833273310879888446204076595395040465050061873142112037560628826112394296901034107184645247543137893483536563957591601478750217410933013304641708136322776975930707908534649896601672581489931296861792264885201253077552409752937687190004273840183558736439354068248714003327154239632489576169409254795398522132086265321465974567940864323720886659719888577903861611582549813595585004073014272687793883472040479746823897882884174675323066921366379359478947705788711158695065025722929508123849018760474871378452781744326869742268714260927679037530328980506139189810309753288229927739117101594860728735403589276978615641835605231",
				G: "4"
			},
			RC0s: {
				TYPE: "Residue Classes",
				BASE: this.BASE,
				P: "149",
				Q: "37",
				G: "16"
			},
			RC1s: {
				TYPE: "Residue Classes",
				BASE: this.BASE,
				P: "89884656743115795386465259539451236680898848947115328636715040578866337902750481566354238661203768010560056939935696678829394884407208311246423715319737062188883946712432742638151109800623047059726541476042502884419075341171231440736956555270413618581675255529365358698328774708775703215219351545329613875969",
				Q: "730750818665451459101842416358141509827966271787",
				G: "43753966268956158683794141044609048074944399463497118601009260015278907944793396888872654797436679156171704835263342098747229841982963550871557447683404359446377648645751856913829280577934384831381295103182368037001170314531189658120206052644043469275562473160989451140877931368137440524162645073654512304068"
			},
			RC2s: {
				TYPE: "Residue Classes",
				BASE: this.BASE,
				P: "16158503035655503650357438344334975980222051334857742016065172713762327569433945446598600705761456731844358980460949009747059779575245460547544076193224141560315438683650498045875098875194826053398028819192033784138396109321309878080919047169238085235290822926018152521443787945770532904303776199561965192760957166694834171210342487393282284747428088017663161029038902829665513096354230157075129296432088558362971801859230928678799175576150822952201848806616643615613562842355410104862578550863465661734839271290328348967522998634176499319107762601824041814772893165831522227453224035124084988448041816607879141260367",
				Q: "13479973333575319897333507543509815336818572211270286240551805124797",
				G: "1134269898971939660256221417602992673757781560247338745711142004292707499263615663726956407344787191388627204394785328286520316952457371640119709459567156265272656919807409699971484184844404437839488942735405277198676036283721356819677333140642790964300984664518053443525909642640603162099914341539824434934715022408665363634880726847516892393401614383985819689883140616831792350484976314212608052796942951089533668814348814636566690462232705866142760699021764820760170288154471669270258911504614068561280584855398438862525973273228514639148263645084849683718631964199688562411013834474496797602932228487527202996447"
			},
			RC3s: {
				TYPE: "Residue Classes",
				BASE: this.BASE,
				P: "2904802997684979031429751266652287185343487588181447618330743076143601865498555112868668022266559203625663078877490258721995264797270023560831442836093516200516055819853220249422024925494525813600122382903520906197364840270012052413988292184690761146180604389522384946371612875869038489784405654789562755666546621759776892408153190790080930100123746284224075121257652224788593802068214369290495086275786967073127915183202957500434821866026609283416272645553951861415817069299793203345162979862593723584529770402506155104819505875374380008547680367117472878708136497428006654308479264979152338818509590797044264172530642931949135881728647441773319439777155807723223165099627191170008146028545375587766944080959493647795765768349350646133842732758718957895411577422317390130051445859016247698037520949742756905563488653739484537428521855358075060716204433164749666917562781919225495884397992008274673412368259180131087873830227",
				Q: "57896044618658097711785492504343953926634992332820282019728792003956564820063",
				G: "929201257310104332188591408019152129579241746486819445694307394243636863240095504743093120730529545201697713049145946900827236099646074823853068234153087337964364835389953295566321414313091913366225524533661658716002930284204999593624436384275650019661121320642809090585202424291614730617743399939604380759971315152248498812228036889780065062471230154677956427260036586710411937258219627969440814693562205456348916958258759592201670386533152318109435823730464203365018502119976697469575407233818073043766012496812833666218850668460811529129373410522906961500960262552792104927047576848063338611368945105165083640940027319781115994012389226504753822421159371219051181093034982843594503162887752985308303928798690699505264113123684970126578784098482558068892187359052293562177396159556752699248399982523452738392367969537592558599460631573044287048348447357227399839277391551954675708968106856319809492705559609560299469015227"
			},
			H1: {
				BIT_LENGTH: 160,
				STANDARD: 'SHA-1'
			},
			H2: {
				BIT_LENGTH: 224,
				STANDARD: 'SHA-224'
			},
			H3: {
				BIT_LENGTH: 256,
				STANDARD: 'SHA-256'
			},
			H4: {
				BIT_LENGTH: 384,
				STANDARD: 'SHA-384'
			},
			H5: {
				BIT_LENGTH: 512,
				STANDARD: 'SHA-512'
			}
		};
	}
	window.uvConfig = new Config();

})(window);
