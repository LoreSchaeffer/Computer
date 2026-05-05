package it.lycoris.j6502.assembler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private static final Logger LOG = LoggerFactory.getLogger(Lexer.class);

    public List<TokenLine> tokenizeFile(File inputFile) throws IOException {
        if (!inputFile.exists() || !inputFile.isFile()) {
            LOG.error("Input file {} does not exist", inputFile.getName());
            System.exit(1);
        }

        List<TokenLine> tokens = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                TokenLine token = this.tokenizeLine(line, lineNumber);
                if (!token.isEmpty()) {
                    tokens.add(token);
                }
            }
        }

        LOG.info("Lexing completed: found {} valid lines", tokens.size());
        return tokens;
    }

    public TokenLine tokenizeLine(String rawLine, int lineNumber) {
        // Sanitize invisible Non-Breaking Spaces often caused by browser copy-pasting
        String cleanLine = rawLine.replace('\u00A0', ' ');

        // 1. Safe comment removal (ignores ';' inside strings)
        int commentIndex = -1;
        boolean inQuotes = false;
        for (int i = 0; i < cleanLine.length(); i++) {
            char currentChar = cleanLine.charAt(i);
            if (currentChar == '"') inQuotes = !inQuotes;
            if (!inQuotes && currentChar == ';') {
                commentIndex = i;
                break;
            }
        }

        if (commentIndex != -1) {
            cleanLine = cleanLine.substring(0, commentIndex);
        }

        cleanLine = cleanLine.trim();
        if (cleanLine.isEmpty()) {
            return new TokenLine(null, null, null, lineNumber, rawLine);
        }

        String label = null;

        // 2. Safe label extraction (ignores ':' inside string literals)
        int colonIndex = -1;
        inQuotes = false;
        for (int i = 0; i < cleanLine.length(); i++) {
            char currentChar = cleanLine.charAt(i);
            if (currentChar == '"') inQuotes = !inQuotes;
            if (!inQuotes && currentChar == ':') {
                colonIndex = i;
                break;
            }
        }

        if (colonIndex != -1) {
            label = cleanLine.substring(0, colonIndex + 1).trim().toUpperCase();
            cleanLine = cleanLine.substring(colonIndex + 1).trim();
        }

        if (cleanLine.isEmpty()) {
            return new TokenLine(label, null, null, lineNumber, rawLine);
        }

        // 3. Robust Equate Check (Explicit '=' parsing)
        int equalsIndex = -1;
        inQuotes = false;
        for (int i = 0; i < cleanLine.length(); i++) {
            char c = cleanLine.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            if (!inQuotes && c == '=') {
                equalsIndex = i;
                break;
            }
        }

        // Prevent treating .BYTE values containing '=' as equates
        if (equalsIndex != -1 && !cleanLine.toUpperCase().startsWith(".BYTE")) {
            String mnemonic = cleanLine.substring(0, equalsIndex).trim().toUpperCase();
            String operand = "=" + cleanLine.substring(equalsIndex + 1).trim();
            return new TokenLine(label, mnemonic, operand, lineNumber, rawLine);
        }

        // 4. Standard Instruction (Mnemonic + Operand)
        String[] parts = cleanLine.split("\\s+", 2);
        String mnemonic = parts[0].toUpperCase();
        String operand = null;

        if (parts.length > 1) {
            operand = this.sanitizeOperand(parts[1].trim());
        }

        return new TokenLine(label, mnemonic, operand, lineNumber, rawLine);
    }

    private String sanitizeOperand(String rawOperand) {
        StringBuilder builder = new StringBuilder();
        boolean insideQuotes = false;

        for (char c : rawOperand.toCharArray()) {
            if (c == '"') insideQuotes = !insideQuotes;
            if (!insideQuotes && Character.isWhitespace(c)) continue;
            builder.append(c);
        }

        return builder.toString();
    }
}