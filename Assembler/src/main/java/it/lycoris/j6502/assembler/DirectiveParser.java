package it.lycoris.j6502.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class responsible for parsing assembler directives.
 * Designed to process raw data injections like .BYTE or .WORD pseudo-opcodes,
 * including symbol resolution and ASCII string extraction.
 */
public class DirectiveParser {

    /**
     * Parses a .BYTE directive payload into a list of raw bytes.
     * Handles ASCII strings enclosed in quotes, hexadecimal values prefixed with '$',
     * binary values prefixed with '%', standard decimal integers, and mapped constants.
     *
     * @param payload     The raw string argument of the directive (e.g., "\"HELLO\",$0A,N_E5").
     * @param symbolTable The symbol table containing resolved equates and constants.
     * @return A list of parsed Byte objects ready to be injected into the binary output.
     * @throws IllegalArgumentException If a numeric token is malformed or an unknown symbol is encountered.
     */
    public List<Byte> parseByteDirective(String payload, Map<String, Integer> symbolTable) {
        List<Byte> outputBytes = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean isInsideString = false;

        if (payload == null || payload.isEmpty()) {
            return outputBytes;
        }

        for (int i = 0; i < payload.length(); i++) {
            char currentChar = payload.charAt(i);

            if (currentChar == '"') {
                isInsideString = !isInsideString;
                continue;
            }

            // If we are parsing a string literal, add chars as raw ASCII bytes
            if (isInsideString) {
                outputBytes.add((byte) currentChar);
            } else {
                if (currentChar == ',') {
                    this.parseAndAppendToken(currentToken.toString(), outputBytes, symbolTable);
                    currentToken.setLength(0);
                } else if (!Character.isWhitespace(currentChar)) {
                    currentToken.append(currentChar);
                }
            }
        }

        // Parse any remaining token after the last comma
        if (!currentToken.isEmpty()) {
            this.parseAndAppendToken(currentToken.toString(), outputBytes, symbolTable);
        }

        return outputBytes;
    }

    /**
     * Resolves a token and appends its evaluated byte value to the list.
     *
     * @param token       The string token representing a number or symbol.
     * @param outputBytes The target list where the parsed byte will be added.
     * @param symbolTable The symbol table context.
     */
    private void parseAndAppendToken(String token, List<Byte> outputBytes, Map<String, Integer> symbolTable) {
        String cleanToken = token.trim();
        if (cleanToken.isEmpty()) {
            return;
        }

        int parsedValue = this.resolveTokenValue(cleanToken, symbolTable);

        // Ensure the value fits within a single byte boundary considering signed/unsigned contexts
        if (parsedValue < -128 || parsedValue > 255) {
            throw new IllegalArgumentException("Value out of bounds for a .BYTE directive: " + parsedValue);
        }

        outputBytes.add((byte) (parsedValue & 0xFF));
    }

    /**
     * Resolves a token to its numeric value.
     *
     * @param token       The string token to evaluate.
     * @param symbolTable The table containing parsed labels and constants.
     * @return The evaluated integer value.
     */
    private int resolveTokenValue(String token, Map<String, Integer> symbolTable) {
        // 1. Symbol Table Lookup
        if (symbolTable != null && symbolTable.containsKey(token)) {
            return symbolTable.get(token);
        }

        // 2. Hexadecimal Parsing ($FF)
        if (token.startsWith("$")) {
            return Integer.parseInt(token.substring(1), 16);
        }

        // 3. Hexadecimal Parsing (0xFF)
        if (token.toLowerCase().startsWith("0x")) {
            return Integer.parseInt(token.substring(2), 16);
        }

        // 4. Binary Parsing (%1010)
        if (token.startsWith("%")) {
            return Integer.parseInt(token.substring(1), 2);
        }

        // 5. Decimal Parsing Fallback
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid numeric token or undefined symbol encountered: [" + token + "]", exception);
        }
    }
}