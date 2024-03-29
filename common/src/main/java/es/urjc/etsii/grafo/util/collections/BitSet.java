package es.urjc.etsii.grafo.util.collections;

import java.util.*;
import java.util.function.IntConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Fast integer set implementation based on JDK BitSet class
 */
public class BitSet extends AbstractSet<Integer> {

    /**
     * Initialize a new bitset with the given numbers
     * @param maxSize biggest number that may be stored in set
     * @param numbers numbers initially stored in set
     * @return new set
     */
    public static BitSet of(int maxSize, int... numbers){
        var set = new BitSet(maxSize);
        for(int n: numbers){
            set.add(n);
        }
        return set;
    }

    /**
     * Returns the intersection between two sets as a new set
     * @param a First set
     * @param b Second set
     * @return New set, original sets are not modified
     */
    public static BitSet intersection(BitSet a, BitSet b){
        var r = new BitSet(a);
        r.and(b); // Keep elements in R that are in B too
        return r;
    }

    /**
     * Returns a new BitSet containing all elements in range [0, n] except those present in the given set.
     * @param a set
     * @return New set, original set is not modified
     */
    public static BitSet not(BitSet a){
        var r = new BitSet(a.capacity);
        r.add(0, a.capacity);
        r.xor(a);
        return r;
    }

    /**
     * Returns the union between two sets as a new set
     * @param a First set
     * @param b Second set
     * @return New set, original sets are not modified
     */
    public static BitSet union(BitSet a, BitSet b){
        var r = new BitSet(a);
        r.or(b); // Keep elements in R that are in B too
        return r;
    }

    /**
     * Returns the difference between two sets as a new set.
     * @param a First set
     * @param b Second set
     * @return New set, with all elements in set A removing those that are in set B
     */
    public static BitSet difference(BitSet a, BitSet b){
        var r = new BitSet(a);
        r.andNot(b); // Keep elements in R that are in B too
        return r;
    }

    /**
     * Returns the symmetric difference between two sets as a new set
     * @param a First set
     * @param b Second set
     * @return New set, original sets are not modified
     */
    public static BitSet symmetricDifference(BitSet a, BitSet b){
        var r = new BitSet(a);
        r.xor(b); // Keep elements in R that are in B too
        return r;
    }


    /*
     * BitSets are packed into arrays of "words."  Currently a word is
     * a long, which consists of 64 bits, requiring 6 address bits.
     * The choice of word size is determined purely by performance concerns.
     */
    private static final int ADDRESS_BITS_PER_WORD = 6;
    private static final int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;

    /* Used to shift left or right for a partial word mask */
    private static final long WORD_MASK = 0xffffffffffffffffL;


    /**
     * The internal field corresponding to the serialField "bits".
     */
    private final long[] words;
    private final int capacity;


    /**
     * Given a bit index, return word index containing it.
     */
    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    /**
     * Creates a bit set whose initial size is large enough to explicitly
     * represent bits with indices in the range {@code 0} through
     * {@code nbits-1}. All bits are initially {@code false}.
     *
     * @param nElements the initial size of the bit set. Will be rounded to the nearest multiple of 64.
     * @throws NegativeArraySizeException if the specified initial size
     *                                    is negative
     */
    public BitSet(int nElements) {
        // nbits can't be negative; size 0 is OK
        if (nElements < 0) {
            throw new NegativeArraySizeException("nbits < 0: " + nElements);
        }

        int nWords = wordIndex(nElements - 1) + 1;
        this.capacity = nElements;
        this.words = new long[nWords];
    }

    public BitSet(BitSet other){
        this.words = other.words.clone();
        this.capacity = other.capacity;
    }

    public BitSet(int nElements, Collection<Integer> other){
        if(other instanceof BitSet set){
            this.words = set.words.clone();
            this.capacity = set.capacity;
        } else {
            int nWords = wordIndex(nElements - 1) + 1;
            this.capacity = nElements;
            this.words = new long[nWords];
            this.addAll(other);
        }
    }

    /**
     * BitSet can contain elements in range [0, capacity). Get the upper bound.
     * @return capacity, as initialized in constructor method
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Checks that fromIndex ... toIndex is a valid range of bit indices.
     */
    private void checkRange(int fromIndex, int toIndex) {
        checkCapacity(fromIndex);
        if (toIndex < 0 || toIndex > this.capacity) {
            throw new IndexOutOfBoundsException("Invalid operation, breaks capacity constraint: 0 <= %s <= %s".formatted(fromIndex, capacity));
        }
        if (fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: %s  > toIndex: %s".formatted(fromIndex, toIndex));
        }
    }

    /**
     * Sets the bit at the specified index to the complement of its
     * current value.
     *
     * @param bitIndex the index of the bit to flip
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since 1.4
     */
    public void flip(int bitIndex) {
        checkCapacity(bitIndex);

        int wordIndex = wordIndex(bitIndex);
        words[wordIndex] ^= (1L << bitIndex);
    }

    /**
     * Sets each bit from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to the complement of its current
     * value.
     *
     * @param fromIndex index of the first bit to flip
     * @param toIndex   index after the last bit to flip
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code toIndex} is negative, or {@code fromIndex} is
     *                                   larger than {@code toIndex}
     * @since 1.4
     */
    public void flip(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        if (fromIndex == toIndex) {
            return;
        }

        int startWordIndex = wordIndex(fromIndex);
        int endWordIndex = wordIndex(toIndex - 1);

        long firstWordMask = WORD_MASK << fromIndex;
        long lastWordMask = WORD_MASK >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            // Case 1: One word
            words[startWordIndex] ^= (firstWordMask & lastWordMask);
        } else {
            // Case 2: Multiple words
            // Handle first word
            words[startWordIndex] ^= firstWordMask;

            // Handle intermediate words, if any
            for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                words[i] ^= WORD_MASK;
            }

            // Handle last word
            words[endWordIndex] ^= lastWordMask;
        }
    }

    /**
     * Sets the bit at the specified index to {@code true}.
     *
     * @param bitIndex a bit index
     * @return true if an element was added, false if the set was not modified
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    public boolean add(int bitIndex) {
        checkCapacity(bitIndex);

        int wordIndex = wordIndex(bitIndex);
        var t = 1L << bitIndex;
        boolean contains = (words[wordIndex] & t) != 0;
        words[wordIndex] |= (1L << bitIndex);
        return !contains;
    }


    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        boolean changed = false;
        for (int n : c) {
            changed |= add(n);
        }
        return changed;
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param fromIndex index of the first bit to be set, inclusive
     * @param toIndex   index after the last bit to be set, exclusive
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code toIndex} is negative, or {@code fromIndex} is
     *                                   larger than {@code toIndex}
     * @since 1.4
     */
    public void add(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        if (fromIndex == toIndex) {
            return;
        }

        // Increase capacity if necessary
        int startWordIndex = wordIndex(fromIndex);
        int endWordIndex = wordIndex(toIndex - 1);

        long firstWordMask = WORD_MASK << fromIndex;
        long lastWordMask = WORD_MASK >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            // Case 1: One word
            words[startWordIndex] |= (firstWordMask & lastWordMask);
        } else {
            // Case 2: Multiple words
            // Handle first word
            words[startWordIndex] |= firstWordMask;

            // Handle intermediate words, if any
            for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                words[i] = WORD_MASK;
            }

            // Handle last word (restores invariants)
            words[endWordIndex] |= lastWordMask;
        }
    }

    /**
     * Sets the bit specified by the index to {@code false}.
     *
     * @param bitIndex the index of the bit to be cleared
     * @return true if an element was removed, false if the set was not modified
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    public boolean remove(int bitIndex) {
        checkCapacity(bitIndex);

        int wordIndex = wordIndex(bitIndex);
        var t = 1L << bitIndex;
        boolean contains = (words[wordIndex] & t) != 0;
        words[wordIndex] &= ~t;
        return contains;
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     *
     * @param fromIndex index of the first bit to be cleared
     * @param toIndex   index after the last bit to be cleared
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code toIndex} is negative, or {@code fromIndex} is
     *                                   larger than {@code toIndex}
     * @since 1.4
     */
    public void remove(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        if (fromIndex == toIndex) {
            return;
        }

        int startWordIndex = wordIndex(fromIndex);
        int endWordIndex = wordIndex(toIndex - 1);

        long firstWordMask = WORD_MASK << fromIndex;
        long lastWordMask = WORD_MASK >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            // Case 1: One word
            words[startWordIndex] &= ~(firstWordMask & lastWordMask);
        } else {
            // Case 2: Multiple words
            // Handle first word
            words[startWordIndex] &= ~firstWordMask;

            // Handle intermediate words, if any
            for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                words[i] = 0;
            }

            // Handle last word
            words[endWordIndex] &= ~lastWordMask;
        }
    }

    /**
     * Sets all of the bits in this BitSet to {@code false}.
     *
     * @since 1.4
     */
    public void clear() {
        Arrays.fill(words, 0);
    }

    /**
     * Returns the value of the bit with the specified index. The value
     * is {@code true} if the bit with the index {@code bitIndex}
     * is currently set in this {@code BitSet}; otherwise, the result
     * is {@code false}.
     *
     * @param bitIndex the bit index
     * @return the value of the bit with the specified index
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    public boolean get(int bitIndex) {
        checkCapacity(bitIndex);

        int wordIndex = wordIndex(bitIndex);
        return ((words[wordIndex] & (1L << bitIndex)) != 0);
    }

    /**
     * Returns the index of the first bit that is set to {@code true}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * <p>To iterate over the {@code true} bits in a {@code BitSet},
     * use the following loop:
     *
     * <pre> {@code
     * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
     *     // operate on index i here
     *     if (i == Integer.MAX_VALUE) {
     *         break; // or (i+1) would overflow
     *     }
     * }}</pre>
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next set bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since 1.4
     */
    public int nextSetBit(int fromIndex) {
        checkCapacity(fromIndex);

        int wordIndex = wordIndex(fromIndex);
        long word = words[wordIndex] & (WORD_MASK << fromIndex);

        while (true) {
            if (word != 0) {
                return (wordIndex * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
            }
            if (++wordIndex == this.words.length) {
                return -1;
            }
            word = words[wordIndex];
        }
    }

    private void checkCapacity(int idx) {
        if (idx < 0 || idx >= this.capacity) {
            throw new IndexOutOfBoundsException("Invalid operation, breaks capacity constraint: 0 <= %s < %s".formatted(idx, capacity));
        }
    }

    /**
     * Returns the index of the first bit that is set to {@code false}
     * that occurs on or after the specified starting index.
     * If no such bit exists then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next clear bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since 1.4
     */
    public int nextClearBit(int fromIndex) {
        checkCapacity(fromIndex);

        int wordIndex = wordIndex(fromIndex);
        long word = ~words[wordIndex] & (WORD_MASK << fromIndex);

        while (true) {
            if (word != 0) {
                return (wordIndex * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
            }
            if (++wordIndex == this.words.length) {
                return -1;
            }
            word = ~words[wordIndex];
        }
    }

    /**
     * Returns the index of the nearest bit that is set to {@code true}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * <p>To iterate over the {@code true} bits in a {@code BitSet},
     * use the following loop:
     *
     * <pre> {@code
     * for (int i = bs.length(); (i = bs.previousSetBit(i-1)) >= 0; ) {
     *     // operate on index i here
     * }}</pre>
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous set bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     * @since 1.7
     */
    public int previousSetBit(int fromIndex) {
        checkCapacity(fromIndex);

        int u = wordIndex(fromIndex);
        long word = words[u] & (WORD_MASK >>> -(fromIndex + 1));

        while (true) {
            if (word != 0) {
                return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
            }
            if (u-- == 0) {
                return -1;
            }
            word = words[u];
        }
    }

    /**
     * Returns the index of the nearest bit that is set to {@code false}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous clear bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     * @since 1.7
     */
    public int previousClearBit(int fromIndex) {
        checkCapacity(fromIndex);

        int u = wordIndex(fromIndex);
        long word = ~words[u] & (WORD_MASK >>> -(fromIndex + 1));

        while (true) {
            if (word != 0) {
                return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
            }
            if (u-- == 0) {
                return -1;
            }
            word = ~words[u];
        }
    }

    /**
     * Returns true if this {@code BitSet} contains no bits that are set
     * to {@code true}.
     *
     * @return boolean indicating whether this {@code BitSet} is empty
     * @since 1.4
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns true if the specified {@code BitSet} has any bits set to
     * {@code true} that are also set to {@code true} in this {@code BitSet}.
     *
     * @param set {@code BitSet} to intersect with
     * @return boolean indicating whether this {@code BitSet} intersects
     * the specified {@code BitSet}
     * @since 1.4
     */
    public boolean intersects(BitSet set) {
        checkSameSize(set);
        for (int i = this.words.length - 1; i >= 0; i--) {
            if ((words[i] & set.words[i]) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of bits set to {@code true} in this {@code BitSet}.
     *
     * @return the number of bits set to {@code true} in this {@code BitSet}
     * @since 1.4
     */
    public int size() {
        int sum = 0;
        for (long word : words) {
            sum += Long.bitCount(word);
        }
        return sum;
    }

    /**
     * Performs a logical <b>AND</b> of this target bit set with the
     * argument bit set. This bit set is modified so that each bit in it
     * has the value {@code true} if and only if it both initially
     * had the value {@code true} and the corresponding bit in the
     * bit set argument also had the value {@code true}.
     *
     * @param set a bit set
     */
    public void and(BitSet set) {
        if (this == set) {
            return;
        }
        checkSameSize(set);

        // Perform logical AND for each word
        for (int i = 0; i < this.words.length; i++) {
            words[i] &= set.words[i];
        }
    }

    /**
     * Performs a logical <b>OR</b> of this bit set with the bit set
     * argument. This bit set is modified so that a bit in it has the
     * value {@code true} if and only if it either already had the
     * value {@code true} or the corresponding bit in the bit set
     * argument has the value {@code true}.
     *
     * @param set a bit set
     */
    public void or(BitSet set) {
        if (this == set) {
            return;
        }
        checkSameSize(set);

        // Perform logical OR on all words
        for (int i = 0; i < this.words.length; i++) {
            words[i] |= set.words[i];
        }

    }

    /**
     * Performs a logical <b>XOR</b> of this bit set with the bit set
     * argument. This bit set is modified so that a bit in it has the
     * value {@code true} if and only if one of the following
     * statements holds:
     * <ul>
     * <li>The bit initially has the value {@code true}, and the
     *     corresponding bit in the argument has the value {@code false}.
     * <li>The bit initially has the value {@code false}, and the
     *     corresponding bit in the argument has the value {@code true}.
     * </ul>
     *
     * @param set a bit set
     */
    public void xor(BitSet set) {
        checkSameSize(set);

        // Perform logical XOR on all words
        for (int i = 0; i < this.words.length; i++) {
            words[i] ^= set.words[i];
        }

        // Clear bits outside capacity range
        long lastWordMask = WORD_MASK >>> -capacity;
        words[words.length-1] &= lastWordMask;
    }

    /**
     * Clears all of the bits in this {@code BitSet} whose corresponding
     * bit is set in the specified {@code BitSet}.
     *
     * @param set the {@code BitSet} with which to mask this
     *            {@code BitSet}
     * @since 1.2
     */
    public void andNot(BitSet set) {
        checkSameSize(set);

        // Perform logical (a & !b) on all words
        for (int i = 0; i < this.words.length; i++) {
            words[i] &= ~set.words[i];
        }

        // Clear bits outside capacity range
        long lastWordMask = WORD_MASK >>> -capacity;
        words[words.length-1] &= lastWordMask;
    }

    public void checkSameSize(BitSet set) {
        if (this.capacity != set.capacity) {
            throw new IllegalArgumentException("Different size capacity, current %s, other %s".formatted(this.words.length, set.words.length));
        }
//
//        if (this.words.length != set.words.length) {
//            throw new IllegalArgumentException("Different size sets, current %s, other %s".formatted(this.words.length, set.words.length));
//        }
    }

    /**
     * Returns the hash code value for this bit set. The hash code depends
     * only on which bits are set within this {@code BitSet}.
     *
     * <p>The hash code is defined to be the result of the following
     * calculation:
     * <pre> {@code
     * public int hashCode() {
     *     long h = 1234;
     *     long[] words = toLongArray();
     *     for (int i = words.length; --i >= 0; )
     *         h ^= words[i] * (i + 1);
     *     return (int)((h >> 32) ^ h);
     * }}</pre>
     * Note that the hash code changes if the set of bits is altered.
     *
     * @return the hash code value for this bit set
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.words);
    }

    @Override
    public Iterator<Integer> iterator() {
        return new BitSetIterator();
    }

    /**
     * Compares this object against the specified object.
     * The result is {@code true} if and only if the argument is
     * not {@code null} and is a {@code BitSet} object that has
     * exactly the same set of bits set to {@code true} as this bit
     * set. That is, for every nonnegative {@code int} index {@code k},
     * <pre>((BitSet)obj).get(k) == this.get(k)</pre>
     * must be true. IMPORTANT: The current sizes of the two bit sets ARE COMPARED FIRST.
     *
     * @param obj the object to compare with
     * @return {@code true} if the objects are the same;
     * {@code false} otherwise
     * @see #size()
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof BitSet set)) {
            return false;
        }
        if (this == obj) {
            return true;
        }

        if (words.length != set.words.length) {
            return false;
        }

        // Check words in use by both BitSets
        for (int i = 0; i < words.length; i++) {
            if (words[i] != set.words[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Cloning this {@code BitSet} produces a new {@code BitSet}
     * that is equal to it.
     * The clone of the bit set is another bit set that has exactly the
     * same bits set to {@code true} as this bit set.
     *
     * @return a clone of this bit set
     * @see #size()
     */
    public BitSet clone() {
        return new BitSet(this);
    }


    /**
     * Returns a string representation of this bit set. For every index
     * for which this {@code BitSet} contains a bit in the set
     * state, the decimal representation of that index is included in
     * the result. Such indices are listed in order from lowest to
     * highest, separated by ",&nbsp;" (a comma and a space) and
     * surrounded by braces, resulting in the usual mathematical
     * notation for a set of integers.
     *
     * <p>Example:
     * <pre>
     * BitSet drPepper = new BitSet();</pre>
     * Now {@code drPepper.toString()} returns "{@code {}}".
     * <pre>
     * drPepper.set(2);</pre>
     * Now {@code drPepper.toString()} returns "{@code {2}}".
     * <pre>
     * drPepper.set(4);
     * drPepper.set(10);</pre>
     * Now {@code drPepper.toString()} returns "{@code {2, 4, 10}}".
     *
     * @return a string representation of this bit set
     */
    public String toString() {
        final int MAX_INITIAL_CAPACITY = Integer.MAX_VALUE - 8;
        int numBits = (words.length > 128) ?
                size() : words.length * BITS_PER_WORD;
        // Avoid overflow in the case of a humongous numBits
        int initialCapacity = (numBits <= (MAX_INITIAL_CAPACITY - 2) / 6) ?
                6 * numBits + 2 : MAX_INITIAL_CAPACITY;
        StringBuilder b = new StringBuilder(initialCapacity);
        b.append('{');
        var iterator = this.iterator();
        if(iterator.hasNext()){
            b.append(iterator.next());
        }
        while (iterator.hasNext()){
            b.append(", ").append(iterator.next());
        }
        b.append('}');
        return b.toString();
    }

    @Override
    public boolean add(Integer integer) {
        return add((int) integer);
    }

    @Override
    public boolean remove(Object o) {
        return remove((int) o);
    }

    @Override
    public boolean contains(Object o) {
        return this.get((int) o);
    }

    @Override
    public Spliterator<Integer> spliterator() {
        return new BitSetSpliterator(0, -1, 0, true);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if(c instanceof BitSet set){
            long before = Arrays.hashCode(this.words);
            this.andNot(set);
            long after = Arrays.hashCode(this.words);
            return before != after;
        } else {
            return super.removeAll(c);
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if(c instanceof BitSet set){
            for (int i = 0; i < this.words.length; i++) {
                long a = this.words[i];
                long b = set.words[i];
                boolean contained = (a & b) == b;
                if(!contained){
                    return false; // short
                }
            }
            return true;
        } else {
            return super.containsAll(c);
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if(c instanceof BitSet set){
            long before = Arrays.hashCode(this.words);
            this.and(set);
            long after = Arrays.hashCode(this.words);
            return before != after;
        } else {
            return super.retainAll(c);
        }
    }

    /**
     * Returns a stream of indices for which this {@code BitSet}
     * contains a bit in the set state. The indices are returned
     * in order, from lowest to highest. The size of the stream
     * is the number of bits in the set state, equal to the value
     * returned by the {@link #size()} ()} method.
     *
     * <p>The stream binds to this bit set when the terminal stream operation
     * commences (specifically, the spliterator for the stream is
     * <a href="Spliterator.html#binding"><em>late-binding</em></a>).  If the
     * bit set is modified during that operation then the result is undefined.
     *
     * @return a stream of integers representing set indices
     * @since 1.8
     */
    public Stream<Integer> stream() {
        return StreamSupport.intStream(new BitSetSpliterator(0, -1, 0, true), false).boxed();
    }

    class BitSetSpliterator implements Spliterator.OfInt {
        private int index; // current bit index for a set bit
        private int fence; // -1 until used; then one past last bit index
        private int est;   // size estimate
        private boolean root; // true if root and not split
        // root == true then size estimate is accurate
        // index == -1 or index >= fence if fully traversed
        // Special case when the max bit set is Integer.MAX_VALUE

        BitSetSpliterator(int origin, int fence, int est, boolean root) {
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.root = root;
        }

        private int getFence() {
            int hi;
            if ((hi = fence) < 0) {
                // Round up fence to maximum cardinality for allocated words
                // This is sufficient and cheap for sequential access
                // When splitting this value is lowered
                hi = fence = (words.length >= wordIndex(Integer.MAX_VALUE))
                        ? Integer.MAX_VALUE
                        : words.length << ADDRESS_BITS_PER_WORD;
                est = size();
                index = nextSetBit(0);
            }
            return hi;
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            Objects.requireNonNull(action);

            int hi = getFence();
            int i = index;
            if (i < 0 || i >= hi) {
                // Check if there is a final bit set for Integer.MAX_VALUE
                if (i == Integer.MAX_VALUE && hi == Integer.MAX_VALUE) {
                    index = -1;
                    action.accept(Integer.MAX_VALUE);
                    return true;
                }
                return false;
            }

            index = nextSetBit(i + 1, wordIndex(hi - 1));
            action.accept(i);
            return true;
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            Objects.requireNonNull(action);

            int hi = getFence();
            int i = index;
            index = -1;

            if (i >= 0 && i < hi) {
                action.accept(i++);

                int u = wordIndex(i);      // next lower word bound
                int v = wordIndex(hi - 1); // upper word bound

                words_loop:
                for (; u <= v && i <= hi; u++, i = u << ADDRESS_BITS_PER_WORD) {
                    long word = words[u] & (WORD_MASK << i);
                    while (word != 0) {
                        i = (u << ADDRESS_BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
                        if (i >= hi) {
                            // Break out of outer loop to ensure check of
                            // Integer.MAX_VALUE bit set
                            break words_loop;
                        }

                        // Flip the set bit
                        word &= ~(1L << i);

                        action.accept(i);
                    }
                }
            }

            // Check if there is a final bit set for Integer.MAX_VALUE
            if (i == Integer.MAX_VALUE && hi == Integer.MAX_VALUE) {
                action.accept(Integer.MAX_VALUE);
            }
        }

        @Override
        public OfInt trySplit() {
            int hi = getFence();
            int lo = index;
            if (lo < 0) {
                return null;
            }

            // Lower the fence to be the upper bound of last bit set
            // The index is the first bit set, thus this spliterator
            // covers one bit and cannot be split, or two or more
            // bits
            hi = fence = (hi < Integer.MAX_VALUE || !get(Integer.MAX_VALUE))
                    ? previousSetBit(Math.min(capacity-1,hi - 1)) + 1
                    : Integer.MAX_VALUE;

            // Find the mid point
            int mid = (lo + hi) >>> 1;
            if (lo >= mid) {
                return null;
            }

            // Raise the index of this spliterator to be the next set bit
            // from the mid point
            index = nextSetBit(mid, wordIndex(hi - 1));
            root = false;

            // Don't lower the fence (mid point) of the returned spliterator,
            // traversal or further splitting will do that work
            return new BitSetSpliterator(lo, mid, est >>>= 1, false);
        }

        @Override
        public long estimateSize() {
            getFence(); // force init
            return est;
        }

        @Override
        public int characteristics() {
            // Only sized when root and not split
            return (root ? Spliterator.SIZED : 0) |
                    Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED;
        }

        @Override
        public Comparator<? super Integer> getComparator() {
            return null;
        }
    }

    /**
     * Returns the index of the first bit that is set to {@code true}
     * that occurs on or after the specified starting index and up to and
     * including the specified word index
     * If no such bit exists then {@code -1} is returned.
     *
     * @param fromIndex   the index to start checking from (inclusive)
     * @param toWordIndex the last word index to check (inclusive)
     * @return the index of the next set bit, or {@code -1} if there
     * is no such bit
     */
    private int nextSetBit(int fromIndex, int toWordIndex) {
        int u = wordIndex(fromIndex);
        // Check if out of bounds
        if (u > toWordIndex) {
            return -1;
        }

        long word = words[u] & (WORD_MASK << fromIndex);

        while (true) {
            if (word != 0) {
                return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
            }
            // Check if out of bounds
            if (++u > toWordIndex) {
                return -1;
            }
            word = words[u];
        }
    }

    private class BitSetIterator implements Iterator<Integer> {

        private int lastReturned = -1;
        private int nextIdx = nextSetBit(0);

        @Override
        public boolean hasNext() {
            return nextIdx != -1;
        }

        @Override
        public Integer next() {
            if(nextIdx == -1){
                throw new NoSuchElementException("CurrentState{lastReturned=%s, nextIdx=%s}".formatted(lastReturned, nextIdx));
            }
            lastReturned = nextIdx;
            nextIdx = nextIdx == capacity -1? -1: nextSetBit(nextIdx + 1);
            return lastReturned;
        }

        @Override
        public void remove() {
            BitSet.this.remove(lastReturned);
        }
    }
}