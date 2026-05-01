package it.lycoris.j6502.assembler;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class responsible for parsing assembler directives.
 * Designed to process raw data injections like .BYTE or .WORD pseudo-opcodes.
 */
public class DirectiveParser {

    /**
     * Parses a .BYTE directive payload into a list of raw bytes.
     * Handles ASCII strings enclosed in quotes, hexadecimal values prefixed with '$',
     * binary values prefixed with '%', and standard decimal integers.
     *
     * @param payload The raw string argument of the directive (e.g., "\"HELLO\",$0A,0").
     * @return A list of parsed Byte objects ready to be injected into the binary output.
     * @throws IllegalArgumentException If a numeric token is malformed.
     */
    public List<Byte> parseByteDirective(String payload) {
        List<Byte> outputBytes = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean isInsideString = false;

        if (payload == null || payload.isEmpty()) return outputBytes;

        for (int i = 0; i < payload.length(); i++) {
            char currentChar = payload.charAt(i);

            if (currentChar == '"') {
                isInsideString = !isInsideString;
                continue;
            }

            if (isInsideString) {
                outputBytes.add((byte) currentChar);
            } else {
                if (currentChar == ',') {
                    this.parseAndAppendNumericToken(currentToken.toString(), outputBytes);
                    currentToken.setLength(0);
                } else if (!Character.isWhitespace(currentChar)) {
                    currentToken.append(currentChar);
                }
            }
        }

        if (!currentToken.isEmpty()) this.parseAndAppendNumericToken(currentToken.toString(), outputBytes);

        return outputBytes;
    }

    /**
     * Converts a string token into a byte based on its numeric prefix.
     *
     * @param token       The string token representing a number.
     * @param outputBytes The target list where the parsed byte will be added.
     */
    private void parseAndAppendNumericToken(String token, List<Byte> outputBytes) {
        String cleanToken = token.trim();
        if (cleanToken.isEmpty()) return;

        int parsedValue;
        try {
            if (cleanToken.startsWith("$")) parsedValue = Integer.parseInt(cleanToken.substring(1), 16);
            else if (cleanToken.startsWith("%")) parsedValue = Integer.parseInt(cleanToken.substring(1), 2);
            else parsedValue = Integer.parseInt(cleanToken);

            outputBytes.add((byte) (parsedValue & 0xFF));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid numeric token in directive: " + cleanToken, exception);
        }
    }
}