package org.json;

import java.io.*;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 *
 * @author JSON.org
 * @version 2014-05-03
 */
public class JSONTokener {
    /**
     * current read character position on the current line.
     */
    private long character;
    /**
     * flag to indicate if the end of the input has been found.
     */
    private boolean eof;
    /**
     * current read index of the input.
     */
    private long index;
    /**
     * current line of the input.
     */
    private long line;
    /**
     * previous character read from the input.
     */
    private char previous;
    /**
     * Reader for the input.
     */
    private final Reader reader;
    /**
     * flag to indicate that a previous character was requested.
     */
    private boolean usePrevious;
    /**
     * the number of characters read in the previous line.
     */
    private long characterPreviousLine;


    /**
     * Construct a JSONTokener from a Reader. The caller must close the Reader.
     *
     * @param reader A reader.
     */
    public JSONTokener(final Reader reader) {
        this.reader = reader.markSupported()
                ? reader
                : new BufferedReader(reader);
        eof = false;
        usePrevious = false;
        previous = 0;
        index = 0;
        character = 1;
        characterPreviousLine = 0;
        line = 1;
    }


    /**
     * Construct a JSONTokener from an InputStream. The caller must close the input stream.
     *
     * @param inputStream The source.
     */
    public JSONTokener(final InputStream inputStream) {
        this(new InputStreamReader(inputStream));
    }


    /**
     * Construct a JSONTokener from a string.
     *
     * @param s A source string.
     */
    public JSONTokener(final String s) {
        this(new StringReader(s));
    }


    /**
     * Back up one character. This provides a sort of lookahead capability,
     * so that you can test for a digit or letter before attempting to parse
     * the next number or identifier.
     *
     * @throws JSONException Thrown if trying to step back more than 1 step
     *                       or if already at the start of the string
     */
    public void back() throws JSONException {
        if (usePrevious || index <= 0) {
            throw new JSONException("Stepping back two steps is not supported");
        }
        decrementIndexes();
        usePrevious = true;
        eof = false;
    }

    /**
     * Decrements the indexes for the {@link #back()} method based on the previous character read.
     */
    private void decrementIndexes() {
        index--;
        if (previous == '\r' || previous == '\n') {
            line--;
            character = characterPreviousLine;
        } else if (character > 0) {
            character--;
        }
    }

    /**
     * Get the hex value of a character (base16).
     *
     * @param c A character between '0' and '9' or between 'A' and 'F' or
     *          between 'a' and 'f'.
     * @return An int between 0 and 15, or -1 if c was not a hex digit.
     */
    public static int dehexchar(final char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - ('A' - 10);
        }
        if (c >= 'a' && c <= 'f') {
            return c - ('a' - 10);
        }
        return -1;
    }

    /**
     * Checks if the end of the input has been reached.
     *
     * @return true if at the end of the file and we didn't step back
     */
    public boolean end() {
        return eof && !usePrevious;
    }


    /**
     * Determine if the source string still contains characters that next()
     * can consume.
     *
     * @return true if not yet at the end of the source.
     * @throws JSONException thrown if there is an error stepping forward
     *                       or backward while checking for more data.
     */
    public boolean more() throws JSONException {
        if (usePrevious) {
            return true;
        }
        try {
            reader.mark(1);
        } catch (final IOException e) {
            throw new JSONException("Unable to preserve stream position", e);
        }
        try {
            // -1 is EOF, but next() can not consume the null character '\0'
            if (reader.read() <= 0) {
                eof = true;
                return false;
            }
            reader.reset();
        } catch (final IOException e) {
            throw new JSONException("Unable to read the next character from the stream", e);
        }
        return true;
    }


    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     * @throws JSONException Thrown if there is an error reading the source string.
     */
    public char next() throws JSONException {
        final int c;
        if (usePrevious) {
            usePrevious = false;
            c = previous;
        } else {
            try {
                c = reader.read();
            } catch (final IOException exception) {
                throw new JSONException(exception);
            }
        }
        if (c <= 0) { // End of stream
            eof = true;
            return 0;
        }
        incrementIndexes(c);
        previous = (char) c;
        return previous;
    }

    /**
     * Increments the internal indexes according to the previous character
     * read and the character passed as the current character.
     *
     * @param c the current character read.
     */
    private void incrementIndexes(final int c) {
        if (c > 0) {
            index++;
            if (c == '\r') {
                line++;
                characterPreviousLine = character;
                character = 0;
            } else if (c == '\n') {
                if (previous != '\r') {
                    line++;
                    characterPreviousLine = character;
                }
                character = 0;
            } else {
                character++;
            }
        }
    }

    /**
     * Consume the next character, and check that it matches a specified
     * character.
     *
     * @param c The character to match.
     * @return The character.
     * @throws JSONException if the character does not match.
     */
    public char next(final char c) throws JSONException {
        final char n = next();
        if (n != c) {
            if (n > 0) {
                throw syntaxError("Expected '" + c + "' and instead saw '" +
                        n + "'");
            }
            throw syntaxError("Expected '" + c + "' and instead saw ''");
        }
        return n;
    }


    /**
     * Get the next n characters.
     *
     * @param n The number of characters to take.
     * @return A string of n characters.
     * @throws JSONException Substring bounds error if there are not
     *                       n characters remaining in the source string.
     */
    public String next(final int n) throws JSONException {
        if (n == 0) {
            return "";
        }

        final char[] chars = new char[n];
        int pos = 0;

        while (pos < n) {
            chars[pos] = next();
            if (end()) {
                throw syntaxError("Substring bounds error");
            }
            pos += 1;
        }
        return new String(chars);
    }


    /**
     * Get the next char in the string, skipping whitespace.
     *
     * @return A character, or 0 if there are no more characters.
     * @throws JSONException Thrown if there is an error reading the source string.
     */
    public char nextClean() throws JSONException {
        for (; ; ) {
            final char c = next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }


    /**
     * Return the characters up to the next close quote character.
     * Backslash processing is done. The formal JSON format does not
     * allow strings in single quotes, but an implementation is allowed to
     * accept them.
     *
     * @param quote The quoting character, either
     *              <code>"</code>&nbsp;<small>(double quote)</small> or
     *              <code>'</code>&nbsp;<small>(single quote)</small>.
     * @return A String.
     * @throws JSONException Unterminated string.
     */
    public String nextString(final char quote) throws JSONException {
        char c;
        final StringBuilder sb = new StringBuilder();
        for (; ; ) {
            c = next();
            switch (c) {
                case 0:
                case '\n':
                case '\r':
                    throw syntaxError("Unterminated string");
                case '\\':
                    c = next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'u':
                            try {
                                sb.append((char) Integer.parseInt(next(4), 16));
                            } catch (final NumberFormatException e) {
                                throw syntaxError("Illegal escape.", e);
                            }
                            break;
                        case '"':
                        case '\'':
                        case '\\':
                        case '/':
                            sb.append(c);
                            break;
                        default:
                            throw syntaxError("Illegal escape.");
                    }
                    break;
                default:
                    if (c == quote) {
                        return sb.toString();
                    }
                    sb.append(c);
            }
        }
    }


    /**
     * Get the text up but not including the specified character or the
     * end of line, whichever comes first.
     *
     * @param delimiter A delimiter character.
     * @return A string.
     * @throws JSONException Thrown if there is an error while searching
     *                       for the delimiter
     */
    public String nextTo(final char delimiter) throws JSONException {
        final StringBuilder sb = new StringBuilder();
        for (; ; ) {
            final char c = next();
            if (c == delimiter || c == 0 || c == '\n' || c == '\r') {
                if (c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }


    /**
     * Get the text up but not including one of the specified delimiter
     * characters or the end of line, whichever comes first.
     *
     * @param delimiters A set of delimiter characters.
     * @return A string, trimmed.
     * @throws JSONException Thrown if there is an error while searching
     *                       for the delimiter
     */
    public String nextTo(final String delimiters) throws JSONException {
        char c;
        final StringBuilder sb = new StringBuilder();
        for (; ; ) {
            c = next();
            if (delimiters.indexOf(c) >= 0 || c == 0 ||
                    c == '\n' || c == '\r') {
                if (c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }


    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     *
     * @return An object.
     * @throws JSONException If syntax error.
     */
    public Object nextValue() throws JSONException {
        char c = nextClean();
        final String string;

        switch (c) {
            case '"':
            case '\'':
                return nextString(c);
            case '{':
                back();
                return new JSONObject(this);
            case '[':
                back();
                return new JSONArray(this);
        }

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        final StringBuilder sb = new StringBuilder();
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = next();
        }
        if (!eof) {
            back();
        }

        string = sb.toString().trim();
        if ("".equals(string)) {
            throw syntaxError("Missing value");
        }
        return JSONObject.stringToValue(string);
    }


    /**
     * Skip characters until the next character is the requested character.
     * If the requested character is not found, no characters are skipped.
     *
     * @param to A character to skip to.
     * @return The requested character, or zero if the requested character
     * is not found.
     * @throws JSONException Thrown if there is an error while searching
     *                       for the to character
     */
    public char skipTo(final char to) throws JSONException {
        char c;
        try {
            final long startIndex = index;
            final long startCharacter = character;
            final long startLine = line;
            reader.mark(1000000);
            do {
                c = next();
                if (c == 0) {
                    // in some readers, reset() may throw an exception if
                    // the remaining portion of the input is greater than
                    // the mark size (1,000,000 above).
                    reader.reset();
                    index = startIndex;
                    character = startCharacter;
                    line = startLine;
                    return 0;
                }
            } while (c != to);
            reader.mark(1);
        } catch (final IOException exception) {
            throw new JSONException(exception);
        }
        back();
        return c;
    }

    /**
     * Make a JSONException to signal a syntax error.
     *
     * @param message The error message.
     * @return A JSONException object, suitable for throwing
     */
    public JSONException syntaxError(final String message) {
        return new JSONException(message + toString());
    }

    /**
     * Make a JSONException to signal a syntax error.
     *
     * @param message  The error message.
     * @param causedBy The throwable that caused the error.
     * @return A JSONException object, suitable for throwing
     */
    public JSONException syntaxError(final String message, final Throwable causedBy) {
        return new JSONException(message + toString(), causedBy);
    }

    /**
     * Make a printable string of this JSONTokener.
     *
     * @return " at {index} [character {character} line {line}]"
     */
    @Override
    public String toString() {
        return " at " + index + " [character " + character + " line " +
                line + "]";
    }
}
