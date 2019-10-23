package acc.capstone;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
//import java.util.stream.IntStream;
import java.util.stream.IntStream;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Hash {

	private static final int ITERATIONS = 10_000;
	private static final int KEY_LENGTH = 256;
	

	// https://www.owasp.org/index.php/Hashing_Java
	private static byte[] owaspHash(final char[] password, final byte[] salt, final int iterations,
			final int keyLength) {

		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
			SecretKey key = skf.generateSecret(spec);
			byte[] res = key.getEncoded();
			return res;

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	public static String encode(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static String generateSalt() {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[4];
		random.nextBytes(salt);
		return encode(salt);
	}

	public static String hash(String password, String salt) {
		return encode(owaspHash(password.toCharArray(), Base64.getDecoder().decode(salt), ITERATIONS, KEY_LENGTH));
	}

	 public static void main(String[] args) { 
		 String salt = generateSalt(); 
		 System.out.println(salt + "**" + hash("greatWessex", salt));  
	 }
}
