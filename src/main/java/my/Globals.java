package my;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Globals {

    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];

    static final int MAX_ACCEPTABLE_WORD_LENGTH = 100;
    static final String ALPHABET = "^ABCDEFGHIJKLMNOPQRSTUVWXYZŠÕÄÖÜŽ";
    static final int ALPHABET_LENGTH = ALPHABET.length();
    static final int COMPRESSED_ALPHABET_SIZE = 65536;
    static final int[] COMPRESSED_ALPHABET_MAPPING = new int[COMPRESSED_ALPHABET_SIZE];
    static final long[] BITMAP_OF_COMPRESSED_ALPHABET = new long[COMPRESSED_ALPHABET_SIZE];
    static final long[] BITMAP = new long[ALPHABET_LENGTH];

    static final AtomicInteger workIndex = new AtomicInteger();
    static final StringBuffer RESULT = new StringBuffer(16777216);
    static final int MAX_THROTTLE = 8;
    static final int[] EMPTY_HISTOGRAM = new int[ALPHABET_LENGTH];
    static final List<Integer> INITIAL_CANDIDATES = new ArrayList<>();

    static List<String> dictionary = new ArrayList<>(110000);
    static List<Long> dictionaryBitmap = new ArrayList<>(110000);
    static List<Integer> dictionaryWordTrimmedLength = new ArrayList<>(110000);
    static Thread[] threads = new Thread[MAX_THROTTLE];

    static String THE_WORD; // make it global
    static final Set<Sentence> RESULT_SENTENCES = new HashSet<>();

    static {
        String lowCaseAlphabet = ALPHABET.toLowerCase();
        conv(ALPHABET);
        conv(lowCaseAlphabet);
        conv(new String(ALPHABET.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
        conv(new String(lowCaseAlphabet.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));

        COMPRESSED_ALPHABET_MAPPING[208] = COMPRESSED_ALPHABET_MAPPING['Š'];
        COMPRESSED_ALPHABET_MAPPING[240] = COMPRESSED_ALPHABET_MAPPING['š'];
        COMPRESSED_ALPHABET_MAPPING[254] = COMPRESSED_ALPHABET_MAPPING['ž'];
        // TODO add Ž

        for (int i = 0; i < COMPRESSED_ALPHABET_SIZE; i++) {
            BITMAP_OF_COMPRESSED_ALPHABET[i] = BITMAP[COMPRESSED_ALPHABET_MAPPING[i]];
        }
    }

    private static void conv(String alphas) {
        long bitValue = 2; // ignore the first bit
        for (int i = 1; i < ALPHABET_LENGTH; i++) {
            COMPRESSED_ALPHABET_MAPPING[alphas.charAt(i)] = i;
            BITMAP[i] = bitValue;
            bitValue <<= 1;
        }
    }

}
