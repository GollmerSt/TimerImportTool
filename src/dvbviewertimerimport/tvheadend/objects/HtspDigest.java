package dvbviewertimerimport.tvheadend.objects;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HtspDigest {

	/**
	 * 32 bytes randomized data used to generate authentication digests
	 * 
	 * @return
	 */
	private final byte[] challenge;

	public HtspDigest(byte[] challenge) {
		this.challenge = challenge;
	}

	public byte[] getHtspDigest(String user, String password) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] passwdBytes = password.getBytes();
		byte[] toConvert = Arrays.copyOf(passwdBytes, passwdBytes.length + this.challenge.length);
		System.arraycopy(this.challenge, 0, toConvert, passwdBytes.length, this.challenge.length);

		return messageDigest.digest(toConvert);
	}
}
