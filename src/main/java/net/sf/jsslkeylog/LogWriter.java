package net.sf.jsslkeylog;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.crypto.SecretKey;

/**
 * Utility class that contains methods for writing to the logfile. Note that
 * these methods get copied into instrumented classes; therefore, they should
 * not refer to fields or other non-API classes.
 */
public class LogWriter {

	public static final String LOGFILE_PROPERTY_NAME = "net.sf.jsslkeylog.logfilename";

	public static void logRSA(byte[] encryptedPreMasterSecret, SecretKey preMasterSecret) {
		logRSA(encryptedPreMasterSecret, preMasterSecret.getEncoded());
	}

	public static void logRSA(byte[] encryptedPreMasterSecret, byte[] preMasterSecret) {
		logLine("RSA " + hex(encryptedPreMasterSecret).substring(0, 16) + " " + hex(preMasterSecret));
	}

	public static void logClientRandom(byte[] clientRandom, SecretKey masterSecret) {
		logClientRandom(clientRandom, masterSecret.getEncoded());
	}

	public static void logClientRandom(byte[] clientRandom, byte[] masterSecret) {
		logLine("CLIENT_RANDOM " + hex(clientRandom) + " " + hex(masterSecret));
	}

	private static String hex(byte[] encoded) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < encoded.length; i++) {
			int b = encoded[i] & 0xFF;
			sb.append(b < 0x10 ? "0" : "").append(Integer.toHexString(b));
		}
		return sb.toString();
	}

	public static void logLine(String line) {
		String logfile = System.getProperty(LOGFILE_PROPERTY_NAME);
		// yes, I know, bad idea to synchonize on a String value, but since
		// this method gets copied into other classes (in different class
		// loaders) via instrumentation, I don't have any other "global"
		// object available to synchronize against.
		synchronized (logfile) {
			try {
				FileOutputStream fos = new FileOutputStream(logfile, true);
				try {
					fos.write((line + "\r\n").getBytes("ISO-8859-1"));
				} finally {
					fos.close();
				}
			} catch (IOException ex) {
				InternalError t = new InternalError("Unable to log SSL Key Log");
				t.initCause(ex);
				throw t;
			}
		}
	}
}
