/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Cody Kletter
 */
public class TextCompressor {
    // constants
    public static final int CODE_LENGTH = 12;
    public static final int RADIX = 256;
    private static void compress() {
        // read in the entire text
        String text = BinaryStdIn.readString();
        int length = text.length();
        int index = 0;
        // calculate max codes available
        int maxCode = (int) Math.pow(2, CODE_LENGTH) - 1;
        int nextCode = RADIX + 1;
        TST prefixes = new TST();
        // write out single character codes into TST
        for (int i = 0; i < RADIX; i++) {
            prefixes.insert(String.valueOf((char) i), i);
        }
        // linear pass through the text
        while (index < length) {
            // find the longest prefix in our codebase from the current index
            String prefix = prefixes.getLongestPrefix(text, index);
            // find the code associated with that prefix and write it out
            int code = prefixes.lookup(prefix);
            BinaryStdOut.write(code, CODE_LENGTH);
            // lookahead to next character if have available codes and not out of bounds
            if (nextCode <= maxCode && index + prefix.length() + 1 < length) {
                // go to our lookahead string and add it to our codebase, then increment our current code
                String lookahead = text.substring(index, index + prefix.length() + 1);
                prefixes.insert(lookahead, nextCode);
                nextCode++;
            }
            // move to next index in the text
            index += prefix.length();
        }
        BinaryStdOut.close();
    }

    private static void expand() {
        int nextCode = RADIX + 1;
        // map for codes to prefixes
        int maxCode = (int) Math.pow(2, CODE_LENGTH);
        String[] prefixes = new String[maxCode + 1];
        // add ascii characters to map
        for (int i = 0; i < RADIX; i++) {
            prefixes[i] = String.valueOf((char) i);
        }
        // get initial code
        int code = BinaryStdIn.readInt(CODE_LENGTH);
        // loop until no more codes to read
        while (!BinaryStdIn.isEmpty()) {
            String prefix = prefixes[code];
            BinaryStdOut.write(prefix);
            int lookaheadCode = BinaryStdIn.readInt(CODE_LENGTH);
            String lookaheadString;
            // edge case, if lookahead code does not exist
            if (prefixes[lookaheadCode] == null) {
                lookaheadString = prefix + prefix.charAt(0);
            }
            // set our lookahead string to the next code written out
            else {
                lookaheadString = prefixes[lookaheadCode];
            }
            // if we still have codes available, add the next prefix to our map
            // increment our current code to be added by one
            if (nextCode <= maxCode) {
                prefixes[nextCode] = prefix + lookaheadString.charAt(0);
                nextCode++;
            }
            // set current code to what was previously our lookahead code
            code = lookaheadCode;
        }
        // write final code to expanded file
        BinaryStdOut.write(prefixes[code]);
        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
