/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.hashcash.execution;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastDateFormat;

public class HashCash {

    public static final int DefaultVersion = 1;
    private static final int hashLength = 160;
    // NB: java.util.SimpleDateFormat is not thread-safe
    private static final FastDateFormat[] FORMATS = {
        FastDateFormat.getInstance("yyMMdd", TimeZone.getTimeZone("GMT")),
        FastDateFormat.getInstance("yyMMddHHmmss", TimeZone.getTimeZone("GMT")),
        FastDateFormat.getInstance("yyMMddHHmm", TimeZone.getTimeZone("GMT"))
    };

    public HashCash() {

    }

    public static boolean validate(String cash) throws NoSuchAlgorithmException {

        String[] parts = cash.split(":");

        if ((parts.length != 6) && (parts.length != 7)) {
            throw new IllegalArgumentException("Improperly formed HashCash");
        }

        int version = Integer.parseInt(parts[0]);
        if (version < 0 || version > 1) {
            throw new IllegalArgumentException("The version is not supported");
        }

        if ((version == 0 && parts.length != 6)
                || (version == 1 && parts.length != 7)) {
            throw new IllegalArgumentException("Improperly formed HashCash");
        }

        int index = 1;
        int claimedBits = (version == 1) ? Integer.parseInt(parts[index++]) : 0;
        Date date = parseDate(parts[index++]);
        if (date == null) {
            throw new IllegalArgumentException("Improperly formed Date");
        }
        String resource = parts[index++];
        Map<String, List<String>> extensions = deserializeExtensions(parts[index++]);

        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(cash.getBytes());
        int computedBits = numberOfLeadingZeros(md.digest());
        return true;
    }

    private static Date parseDate(String dateString) {
        if (dateString != null) {
            try {
                // try each date format starting with the most common one
                for (DateParser format : FORMATS) {
                    try {
                        return format.parse(dateString);
                    } catch (ParseException ex) {
                        /* gulp */ }
                }
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // Private utility functions
    /**
     * Actually tries various combinations to find a valid hash. Form is of
     * prefix + randomhex + ":" + randomhex
     *
     * @throws NoSuchAlgorithmException If SHA1 is not a supported Message
     * Digest
     */
    private static String generateCash(String prefix, int value, MessageDigest md)
            throws NoSuchAlgorithmException {
        SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
        byte[] tmpBytes = new byte[8];
        rnd.nextBytes(tmpBytes);
        long random = bytesToLong(tmpBytes);
        rnd.nextBytes(tmpBytes);
        long counter = bytesToLong(tmpBytes);

        prefix = prefix + Long.toHexString(random) + ":";

        String temp;
        int tempValue;
        byte[] bArray;
        do {
            counter++;
            temp = prefix + Long.toHexString(counter);
            md.reset();
            md.update(temp.getBytes());
            bArray = md.digest();
            tempValue = numberOfLeadingZeros(bArray);
        } while (tempValue < value);

        return temp;
    }

    /**
     * Converts a 8 byte array of unsigned bytes to an long
     *
     * @param b an array of 8 unsigned bytes
     */
    private static long bytesToLong(byte[] b) {
        long l = 0;
        l |= b[0] & 0xFF;
        l <<= 8;
        l |= b[1] & 0xFF;
        l <<= 8;
        l |= b[2] & 0xFF;
        l <<= 8;
        l |= b[3] & 0xFF;
        l <<= 8;
        l |= b[4] & 0xFF;
        l <<= 8;
        l |= b[5] & 0xFF;
        l <<= 8;
        l |= b[6] & 0xFF;
        l <<= 8;
        l |= b[7] & 0xFF;
        return l;
    }

    /**
     * Serializes the extensions with (key, value) seperated by semi-colons and
     * values seperated by commas
     */
    private static String serializeExtensions(Map<String, List<String>> extensions) {
        if (null == extensions || extensions.isEmpty()) {
            return "";
        }

        StringBuffer result = new StringBuffer();
        List<String> tempList;
        boolean first = true;

        for (String key : extensions.keySet()) {
            if (key.contains(":") || key.contains(";") || key.contains("=")) {
                throw new IllegalArgumentException("Extension key contains an illegal character. " + key);
            }
            if (!first) {
                result.append(";");
            }
            first = false;
            result.append(key);
            tempList = extensions.get(key);

            if (null != tempList) {
                result.append("=");
                for (int i = 0; i < tempList.size(); i++) {
                    if (tempList.get(i).contains(":") || tempList.get(i).contains(";") || tempList.get(i).contains(",")) {
                        throw new IllegalArgumentException("Extension value contains an illegal character. " + tempList.get(i));
                    }
                    if (i > 0) {
                        result.append(",");
                    }
                    result.append(tempList.get(i));
                }
            }
        }
        return result.toString();
    }

    /**
     * Inverse of {@link #serializeExtensions(Map)}
     */
    private static Map<String, List<String>> deserializeExtensions(String extensions) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        if (null == extensions || extensions.length() == 0) {
            return result;
        }

        String[] items = extensions.split(";");

        for (String item : items) {
            String[] parts = item.split("=", 2);
            if (parts.length == 1) {
                result.put(parts[0], null);
            } else {
                result.put(parts[0], Arrays.asList(parts[1].split(",")));
            }
        }

        return result;
    }

    /**
     * Counts the number of leading zeros in a byte array.
     */
    private static int numberOfLeadingZeros(byte[] values) {
        int result = 0;
        int temp = 0;
        for (int i = 0; i < values.length; i++) {

            temp = numberOfLeadingZeros(values[i]);

            result += temp;
            if (temp != 8) {
                break;
            }
        }

        return result;
    }

    /**
     * Returns the number of leading zeros in a bytes binary represenation
     */
    private static int numberOfLeadingZeros(byte value) {
        if (value < 0) {
            return 0;
        }
        if (value < 1) {
            return 8;
        } else if (value < 2) {
            return 7;
        } else if (value < 4) {
            return 6;
        } else if (value < 8) {
            return 5;
        } else if (value < 16) {
            return 4;
        } else if (value < 32) {
            return 3;
        } else if (value < 64) {
            return 2;
        } else if (value < 128) {
            return 1;
        } else {
            return 0;
        }
    }

}
