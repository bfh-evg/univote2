/*
 * UniVote2
 *
 *  UniVote2(tm): An Internet-based, verifiable e-voting system for student elections in Switzerland
 *  Copyright (c) 2015 Bern University of Applied Sciences (BFH),
 *  Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *  Quellgasse 21, CH-2501 Biel, Switzerland
 *
 *  Licensed under Dual License consisting of:
 *  1. GNU Affero General Public License (AGPL) v3
 *  and
 *  2. Commercial license
 *
 *
 *  1. This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  2. Licensees holding valid commercial licenses for UniVote2 may use this file in
 *   accordance with the commercial license agreement provided with the
 *   Software or, alternatively, in accordance with the terms contained in
 *   a written agreement between you and Bern University of Applied Sciences (BFH),
 *   Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *   Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 *   For further information contact <e-mail: univote@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
 */
package ch.bfh.univote2.common.crypto;

import ch.bfh.unicrypt.helper.math.MathUtil;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModPrime;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import ch.bfh.univote2.common.message.CryptoSetting;
import java.math.BigInteger;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class CryptoProvider {

	public static CryptoSetting getCryptoSetting(int securtityLevel) {
		String encSetting;
		String sigSetting;

		switch (securtityLevel) {
			case 0:
				encSetting = "RC0e";
				sigSetting = "RC0s";
				break;
			case 1:
				encSetting = "RC1e";
				sigSetting = "RC1s";
				break;
			case 2:
				encSetting = "RC2e";
				sigSetting = "RC2s";
				break;
			case 3:
				encSetting = "RC3e";
				sigSetting = "RC3s";
				break;
			case 4:
				encSetting = "RC4e";
				sigSetting = "RC4s";
				break;
			case 5:
				encSetting = "RC5e";
				sigSetting = "RC5s";
				break;
			default:
				encSetting = "RC5e";
				sigSetting = "RC5s";
		}
		return new CryptoSetting(encSetting, sigSetting);
	}

	public static CryptoSetup getEncryptionSetup(String encryptionClassName) {
		CyclicGroup group;
		switch (encryptionClassName) {
			case "RC0e":
				group = GStarModSafePrime.getFirstInstance(8);
				return new CryptoSetup(group, group.getElement(MathUtil.FOUR));
			case "RC1e":
				group = GStarModSafePrime.getFirstInstance(1024);
				return new CryptoSetup(group, group.getElement(MathUtil.FOUR));
			case "RC2e":
				group = GStarModSafePrime.getFirstInstance(2048);
				return new CryptoSetup(group, group.getElement(MathUtil.FOUR));
			case "RC3e":
				group = GStarModSafePrime.getFirstInstance(3072);
				return new CryptoSetup(group, group.getElement(MathUtil.FOUR));
			case "RC4e":
				group = GStarModSafePrime.getFirstInstance(7680);
				return new CryptoSetup(group, group.getElement(MathUtil.FOUR));
			case "RC5e":
				group = GStarModSafePrime.getFirstInstance(15360);
				return new CryptoSetup(group, group.getElement(MathUtil.FOUR));
			default:
				group = GStarModSafePrime.getFirstInstance(3072);
				return new CryptoSetup(group, group.getElement(MathUtil.FOUR));
		}
	}

	public static CryptoSetup getSignatureSetup(String signatureClassName) {
		CyclicGroup group;
		switch (signatureClassName) {
			case "RC0s":
				group = GStarModPrime.getFirstInstance(8, 6);
				return new CryptoSetup(group, group.getElement(new BigInteger("16")));
			case "RC1s":
				group = GStarModPrime.getFirstInstance(1024, 160);
				return new CryptoSetup(group, group.getElement(new BigInteger("4375396626895615868379414104460904807494"
						+ "43994634971186010092600152789079447933968888726547974366791561717048352633420987472298419829"
						+ "63550871557447683404359446377648645751856913829280577934384831381295103182368037001170314531"
						+ "189658120206052644043469275562473160989451140877931368137440524162645073654512304068")));
			case "RC2s":
				group = GStarModPrime.getFirstInstance(2048, 224);
				return new CryptoSetup(group, group.getElement(new BigInteger("1134269898971939660256221417602992673757"
						+ "78156024733874571114200429270749926361566372695640734478719138862720439478532828652031695245"
						+ "73716401197094595671562652726569198074096999714841848444044378394889427354052771986760362837"
						+ "21356819677333140642790964300984664518053443525909642640603162099914341539824434934715022408"
						+ "66536363488072684751689239340161438398581968988314061683179235048497631421260805279694295108"
						+ "95336688143488146365666904622327058661427606990217648207601702881544716692702589115046140685"
						+ "61280584855398438862525973273228514639148263645084849683718631964199688562411013834474496797"
						+ "602932228487527202996447")));
			case "RC3s":
				group = GStarModPrime.getFirstInstance(3072, 256);
				return new CryptoSetup(group, group.getElement(new BigInteger("9292012573101043321885914080191521295792"
						+ "41746486819445694307394243636863240095504743093120730529545201697713049145946900827236099646"
						+ "07482385306823415308733796436483538995329556632141431309191336622552453366165871600293028420"
						+ "49995936244363842756500196611213206428090905852024242916147306177433999396043807599713151522"
						+ "48498812228036889780065062471230154677956427260036586710411937258219627969440814693562205456"
						+ "34891695825875959220167038653315231810943582373046420336501850211997669746957540723381807304"
						+ "37660124968128336662188506684608115291293734105229069615009602625527921049270475768480633386"
						+ "11368945105165083640940027319781115994012389226504753822421159371219051181093034982843594503"
						+ "16288775298530830392879869069950526411312368497012657878409848255806889218735905229356217739"
						+ "61595567526992483999825234527383923679695375925585994606315730442870483484473572273998392773"
						+ "91551954675708968106856319809492705559609560299469015227")));
			default:
				group = GStarModPrime.getFirstInstance(3072, 256);
				return new CryptoSetup(group, group.getElement(new BigInteger("9292012573101043321885914080191521295792"
						+ "41746486819445694307394243636863240095504743093120730529545201697713049145946900827236099646"
						+ "07482385306823415308733796436483538995329556632141431309191336622552453366165871600293028420"
						+ "49995936244363842756500196611213206428090905852024242916147306177433999396043807599713151522"
						+ "48498812228036889780065062471230154677956427260036586710411937258219627969440814693562205456"
						+ "34891695825875959220167038653315231810943582373046420336501850211997669746957540723381807304"
						+ "37660124968128336662188506684608115291293734105229069615009602625527921049270475768480633386"
						+ "11368945105165083640940027319781115994012389226504753822421159371219051181093034982843594503"
						+ "16288775298530830392879869069950526411312368497012657878409848255806889218735905229356217739"
						+ "61595567526992483999825234527383923679695375925585994606315730442870483484473572273998392773"
						+ "91551954675708968106856319809492705559609560299469015227")));
		}
	}
}
