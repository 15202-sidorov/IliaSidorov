/*
    All strings are numbered.
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;


public class StringGenerator {
    public StringGenerator( int startPoint, int endPoint ) {
        start = startPoint;
        end = endPoint;
    }

    public String checkHash( byte[] inputHash ) {
        try {
            String stringToCheck = null;
            byte[] resultedHash = null;
            MessageDigest md = MessageDigest.getInstance("MD5");
            for (int i = start; i < end; i++) {
                stringToCheck = translateNumberToString(i);
                md.update(stringToCheck.getBytes());
                resultedHash = md.digest();
                if ( resultedHash.length != inputHash.length ) {
                    continue;
                }

                boolean hashIsNotCorrect = false;
                for ( int j = 0; j < resultedHash.length; j++ ) {
                    if ( resultedHash[j] != inputHash[j] ) {
                        hashIsNotCorrect = true;
                        break;
                    }
                }

                if ( hashIsNotCorrect ) {
                    continue;
                }

                System.out.println("FOUND HASH");
                return stringToCheck;
            }
            System.out.println("HASH IS NOT FOUND");
            return null;
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /*
        Get string out of a corresponding number
    */
    private static String translateNumberToString(long number) {
        long i;
        long sum = 0;
        for (i = BASE; sum + i <= number; i *= BASE) {
            sum += i;
        }
        long zeroCount = (long) (Math.log(i) / Math.log(BASE));
        long numberWithZeros = number - sum;
        String numberToBase = Long.toString(numberWithZeros, BASE);
        long x = Long.parseLong(numberToBase);
        String formatString = "%0" + zeroCount + "d";
        String numberWithZerosStr = String.format(formatString, x);
        StringBuilder sb = new StringBuilder("");
        for (i = 0; i < numberWithZerosStr.length(); i++) {
            switch (numberWithZerosStr.charAt((int) i)) {
                case '0':
                    sb.append(charsAvailable[0]);
                    break;
                case '1':
                    sb.append(charsAvailable[1]);
                    break;
                case '2':
                    sb.append(charsAvailable[2]);
                    break;
                case '3':
                    sb.append(charsAvailable[3]);
                    break;
            }
        }
        return sb.toString();
    }

    private int start = 0;
    private int end = 0;

    private final static char[] charsAvailable = {'A','B','G','T'};
    private final static int BASE = 4; // four letters in system
}
