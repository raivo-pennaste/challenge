package my;

import java.util.ArrayList;
import java.util.List;

import static my.Globals.*;

public class Solver {

    Storage[] storage;
    private static final Object syncObject = new Object();

    public Solver() {
        this.storage = new Storage[MAX_ACCEPTABLE_WORD_LENGTH];
        for (int i = 0; i < storage.length; i++) {
            this.storage[i] = new Storage();
        }
    }

    private static long histogramToBits(int[] histogram) {
        long lowPart = 0;
        long highPart = 0;
        long bit = 2;
        // ignore first bit (ignore first character in a compressed alphabet)
        for (int j = 1; j < histogram.length; j++) {
            int i = histogram[j];
            if (i > 0) {
                lowPart |= bit;
                if (i > 1) {
                    highPart |= bit;
                }
            }
            bit <<= 1;
        }
        return ((highPart << 25) | lowPart);
    }

    private static List<Integer> filterCandidates(long bittedWord, List<Integer> candidates, int maxLength) {
        List<Integer> result = new ArrayList<>();
        for (int candidate : candidates) {
            long bits = dictionaryBitmap.get(candidate);
            if (bittedWord < (bittedWord | bits)) {
                continue;
            }
            if (dictionaryWordTrimmedLength.get(candidate) <= maxLength) {
                result.add(candidate);
            }
        }
        return result;
    }

    public void subCandidate(int depth, int candidate, List<Integer> filteredCandidates) {
        Storage activeStorage = this.storage[depth];
        int[] newHistogram = this.storage[depth + 1].histogram;
        System.arraycopy(EMPTY_HISTOGRAM, 0, newHistogram, 0, ALPHABET_LENGTH); // clear histogram
        String candidateWord = dictionary.get(candidate);
        int[] reducedHistogram = reduceHistogram(candidateWord, activeStorage.histogram, newHistogram);
        if (reducedHistogram == null) {
            return; // get out of recursion
        }
        activeStorage.candidate = candidate;
        long bittedWord = histogramToBits(reducedHistogram);
        if (bittedWord == 0) {
            // solved
            List<Integer> sentenceFromIntegers = new ArrayList<>(depth);
            for (int i = 0; i <= depth; i++) {
                sentenceFromIntegers.add(this.storage[i].candidate);
            }
            sentenceFromIntegers.sort(null);
            Sentence sentence = new Sentence().setValues(sentenceFromIntegers.toArray(EMPTY_INTEGER_OBJECT_ARRAY));
            synchronized (syncObject) {
                RESULT_SENTENCES.add(sentence);
            }
        } else {
            List<Integer> subCandidates = filterCandidates(bittedWord, filteredCandidates, candidateWord.length());
//            List<Integer> subCandidates = new ArrayList<>();
//            int maxLength = candidateWord.length();
//            for (int candidate2 : filteredCandidates) {
//                long bits = dictionaryBitmap.get(candidate2);
//                if (bittedWord < (bittedWord | bits)) {
//                    continue;
//                }
//                if (dictionaryWordTrimmedLength.get(candidate2) <= maxLength) {
//                    subCandidates.add(candidate2);
//                }
//            }

            for (Integer subCandidate : subCandidates) {
                subCandidate(depth + 1, subCandidate, subCandidates);
            }
        }
    }

    private static int[] reduceHistogram(String word, int[] inputHistogram, int[] outputHistogram) {
        System.arraycopy(inputHistogram, 0, outputHistogram, 0, ALPHABET_LENGTH); // outputHistogram = inputHistogram
        for (int i = 0; i < word.length(); i++) {
            int pos = COMPRESSED_ALPHABET_MAPPING[word.charAt(i)];
            if (pos == 0) {
                continue;
            }
            int count = outputHistogram[pos];
            count--;
            if (count < 0) {
                return null;
            }
            outputHistogram[pos] = count;
        }
        return outputHistogram;
    }

    class Storage {
        Integer candidate;
        int[] histogram = new int[ALPHABET_LENGTH];
    }

}
