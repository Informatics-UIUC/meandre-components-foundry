
package org.seasr.meandre.support.generic.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public abstract class Crypto {

    static final byte[] HEX_CHAR_TABLE = {
        (byte)'0', (byte)'1', (byte)'2', (byte)'3',
        (byte)'4', (byte)'5', (byte)'6', (byte)'7',
        (byte)'8', (byte)'9', (byte)'a', (byte)'b',
        (byte)'c', (byte)'d', (byte)'e', (byte)'f'
    };

    public static String getHexString(byte[] raw) throws UnsupportedEncodingException
    {
        byte[] hex = new byte[2 * raw.length];
        int index = 0;

        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex, "ASCII");
    }

    public static String getSHA1Hash(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        sha1hash = md.digest();

        return getHexString(sha1hash);
    }

    /**
     * Creates an MD5 checksum for a file
     *
     * @param file The file
     * @return The byte[] containing the MD5 checksum
     * @throws IOException Thrown when an I/O error occurs
     */
    public static byte[] createMD5Checksum(File file) throws IOException {
        MessageDigest complete;
        InputStream fis = new FileInputStream(file);

        try {
            byte[] buffer = new byte[4096];

            try {
                complete = MessageDigest.getInstance("MD5");
            }
            catch (NoSuchAlgorithmException e) {
                return null;
            }

            int numRead;
            do {
                if ((numRead = fis.read(buffer)) > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead > 0);
        }
        finally {
            fis.close();
        }

        return complete.digest();
    }

    /**
     * Creates an MD5 checksum for a data block
     * @param data The data
     * @return The checksum
     */
    public static byte[] createMD5Checksum(byte[] data) {
        MessageDigest complete;

        try {
            complete = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            return null;
        }

        complete.update(data);

        return complete.digest();
    }

}
