package my;

public class Sentence implements Comparable<Sentence> {
    Integer[] values;

    @Override
    public int compareTo(Sentence o) {
        Integer[] values2 = o.values;

        int length1 = values.length;
        int length2 = values2.length;
        int length = Math.min(length1, length2);
        for (int i = 0; i < length; i++) {
            int compare = Integer.compare(values[i], values2[i]);
            if (compare != 0) {
                return compare;
            }
        }
        return Integer.compare(length1, length2);
    }

    @Override
    public boolean equals(Object o) {
        Integer[] values2 = ((Sentence) o).values;

        int length1 = values.length;
        int length2 = values2.length;
        if (length1 != length2) return false;
        for (int i = 0; i < length1; i++) {
            if (!values[i].equals(values2[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 11111;
        for (int i = 0; i < values.length; i++) {
            hash ^= values[i];
            hash += 170;
        }
        return hash;
    }

    public Sentence setValues(Integer[] values) {
        this.values = values;
        return this;
    }
}
