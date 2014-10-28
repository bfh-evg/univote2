/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote.voteclient.beans.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 *
 * @author philipp
 */
public class RandomDistribution {
	
	private static final SecureRandom random = RandomDistribution.init();
	
	private static SecureRandom init() {
		SecureRandom random;
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch( final NoSuchAlgorithmException e ) {
			random = new SecureRandom();
		}
		random.nextBoolean();
		return random;
	}
	
	/*
	 * Returns the requested amount of random bytes.
	 * 
	 * @param size Number of bytes.
	 * @returm A new byte array filled up with random bytes.
	 */
	public static byte[] getRandomValue( int size ) {
		size = Math.max(1, size);
		byte[] bytes = new byte[size];
		random.nextBytes(bytes);
		return bytes;
	}
}
