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
 *  @author Zach Blick, YOUR NAME HERE
 */
public class TextCompressor {
    public static final int CODE_LENGTH = 12;
    public static final int RADIX = 128;
    private static void compress() {
        String text = BinaryStdIn.readString();
        int length = text.length();
        int index = 0;
        int maxCode = (int) Math.pow(2, CODE_LENGTH);
        int nextCode = RADIX + 1;
        TST prefixes = new TST();
        // write out single character codes into TST
        for (int i = 0; i < RADIX; i++) {
            prefixes.insert(String.valueOf((char) i), i);
        }
        // linear pass through the text
        while (index < length) {
            String prefix = prefixes.getLongestPrefix(text, index);
            int code = prefixes.lookup(prefix);
            BinaryStdOut.write(code, CODE_LENGTH);
            // lookahead to next character if have available codes
            if (nextCode <= maxCode && index + prefix.length() + 1 < length) {
                String lookahead = text.substring(index, index + prefix.length() + 1);
                 prefixes.insert(lookahead, nextCode);
                 nextCode++;
            }
            index += prefix.length();
        }
        BinaryStdOut.close();
    }

    private static void expand() {
        int nextCode = RADIX + 1;
        // map for codes to prefixes
        int maxCode = (int) Math.pow(2, CODE_LENGTH);
        String[] prefixes = new String[maxCode];
        // add ascii characters to map
        for (int i = 0; i < RADIX; i++) {
            prefixes[i] = String.valueOf((char) i);
        }
        int code = BinaryStdIn.readInt(CODE_LENGTH);
        // loop until no more codes to read
        while (!BinaryStdIn.isEmpty()) {
            String prefix = prefixes[code];
            BinaryStdOut.write(prefix);
            int lookaheadCode = BinaryStdIn.readInt(CODE_LENGTH);
            String lookaheadString = prefixes[lookaheadCode];
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
