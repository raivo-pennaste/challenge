package my;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static my.Globals.*;

/**
 * Expected a beautiful code? Nope! FYI: this was specially uglified for speed...
 */
public class Challenge {

    static {
        // prepare Threads
        for (int i = 0; i < MAX_THROTTLE; i++) {
            Runnable runnable = Challenge::runner;
            Thread thread = new Thread(runnable);
            threads[i] = thread;
        }
    }

    public static void main(String args[]) throws IOException {
        String result = "";
        // do your magic
        if (args.length < 2) {
            System.out.println("Params not correct");
            return;
        }

        long startTime = System.currentTimeMillis();
        result = process(args[0], args[1]);

        long stop = System.currentTimeMillis() - startTime;
        System.out.print(stop);
        System.out.println(result);
    }


    private static long wordToBits(String word) {
        long lowPart = 0;
        long highPart = 0;
        for (int i = 0; i < word.length(); i++) {
            long bit = BITMAP_OF_COMPRESSED_ALPHABET[word.charAt(i)];
            if ((lowPart & bit) != 0) {
                highPart |= bit;
            }
            lowPart |= bit;
        }
        return (highPart << 25) | lowPart;
    }

    private static void wordToBits2(String word, long bittedWord) {
        long lowPart = 0;
        long highPart = 0;
        int trimmedLength = 0;
        for (int i = 0; i < word.length(); i++) {
            long bit = BITMAP_OF_COMPRESSED_ALPHABET[word.charAt(i)];
            if ((lowPart & bit) != 0) {
                highPart |= bit;
            }
            if (bit != 0) {
                trimmedLength++;
                lowPart |= bit;
            }
        }
        long bits = (highPart << 25) | lowPart;
        // makes a BIG test a lot slower!
        if (bittedWord < (bittedWord | bits)) {
            return;
        }
        dictionaryBitmap.add(bits);
        dictionaryWordTrimmedLength.add(trimmedLength);
        dictionary.add(word);
    }

    private static void addWordToHistogram(String word, int[] customHistogram) {
        for (int i = 0; i < word.length(); i++) {
            customHistogram[COMPRESSED_ALPHABET_MAPPING[word.charAt(i)]]++;
        }
    }

    private static void prepareInitialfilteredCandidates(long bittedWord) {
        for (int i = 0; i < dictionaryBitmap.size(); i++) {
            long bits = dictionaryBitmap.get(i);
            if (bittedWord < (bittedWord | bits)) {
                continue;
            }
            INITIAL_CANDIDATES.add(i);
        }
    }

    static String process(String dictionaryPath, String word) throws IOException {
        THE_WORD = word;
        long bittedWord = wordToBits(word);

        if (dictionary.isEmpty()) {
            processDictionary(dictionaryPath, word.getBytes(StandardCharsets.ISO_8859_1).length, bittedWord);
        }

        prepareInitialfilteredCandidates(bittedWord);
        processCandidates();
        for (Sentence sentence : RESULT_SENTENCES) {
            RESULT.append(",");
            for (int i = 0; i < sentence.values.length; i++) {
                if (i > 0) {
                    RESULT.append(' ');
                }
                RESULT.append(dictionary.get(sentence.values[i]));
            }
        }
        return RESULT.toString();

    }

    static void processDictionary(String dictionaryPath, int wordExpandedSize, long bittedWord) throws IOException {
        File file = new File(dictionaryPath);
        if (!file.exists()) {
            System.err.println("Dictionary not found");
            return;
        }
        int length = (int) file.length();
        if (length == 0) {
            System.out.println("Dictionary file empty");
            return;
        }

        byte[] buf = new byte[length];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(buf, 0, length);
            parseDictionaryBuf(buf, wordExpandedSize, bittedWord);
        }
    }

    private static void processCandidates() {
        if (INITIAL_CANDIDATES.isEmpty()) {
            return;
        }
        workIndex.set(INITIAL_CANDIDATES.size() - 1);
        if (INITIAL_CANDIDATES.size() < 300) {
            runner();
        } else {
            int coreCount = MAX_THROTTLE;
//            if (candidates.size() < 400) {
//                coreCount >>= 1;
//            }

            for (int i = 0; i < coreCount; i++) {
                threads[i].start();
            }

            for (int i = 0; i < coreCount; i++) {
                Thread thread = threads[i];
                if (thread.isAlive()) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private static void runner() {
        Solver solver = new Solver(); // per CPU
        while (true) {
            int index = workIndex.getAndDecrement();
            if (index < 0) break;
            Integer candidate = INITIAL_CANDIDATES.get(index);
            int[] currentWordHistogram = solver.storage[0].histogram;
            System.arraycopy(EMPTY_HISTOGRAM, 0, currentWordHistogram, 0, ALPHABET_LENGTH); // clear histogram
            addWordToHistogram(THE_WORD, currentWordHistogram);
            solver.subCandidate(0, candidate, INITIAL_CANDIDATES);
        }
    }


    private static void parseDictionaryBuf(byte[] buf, int wordExpandedSize, long bittedWord) {
        int startPos = 0;
        int endPos = 0;
        int size = buf.length;

        boolean eof;
        do {
            byte b = buf[endPos];
            endPos++;
            eof = endPos >= size;
            if (b == 13 || b == 10 || eof) {
                int newLength = endPos - startPos - 1;
                if (newLength > 0) {
                    if (newLength <= wordExpandedSize) {
                        byte[] newWord = new byte[newLength];
                        System.arraycopy(buf, startPos, newWord, 0, newLength);
                        String w = new String(newWord, StandardCharsets.ISO_8859_1);
                        wordToBits2(w, bittedWord);
                    }
                }
                startPos = endPos;
            }
        } while (!eof);
    }


}
