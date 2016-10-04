package ch.bfh.univote2.admin;



import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.StringToByteArray;
import ch.bfh.unicrypt.helper.converter.classes.string.ByteArrayToString;
import ch.bfh.unicrypt.helper.converter.interfaces.Converter;
import ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import java.nio.charset.Charset;
import java.util.Scanner;

public class VoterTest {

	private static final Scanner CONSOLE = new Scanner(System.in);
	private static final HashAlgorithm HASH_ALGORITHM = HashAlgorithm.SHA256;
	private static final Converter<String, ByteArray> STRING_TO_BYTEARRAY = StringToByteArray.getInstance(Charset.forName("UTF-8"));
	private static final Converter<ByteArray, String> BYREARRAY_TO_STRING = ByteArrayToString.getInstance(ByteArrayToString.Radix.HEX);

	public static void main(String[] args) {
		System.out.print("Voter ID: ");
		String voterId = CONSOLE.nextLine();
		String voterHash = BYREARRAY_TO_STRING.convert(HASH_ALGORITHM.getHashValue(STRING_TO_BYTEARRAY.convert(voterId)));
		System.out.println("Voter Hash: " + voterHash);
	}
}
