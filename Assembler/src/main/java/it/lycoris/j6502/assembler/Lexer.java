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
                if (!token.isEmpty()) tokens.add(token);
            }
        }

        LOG.info("Lexing completed: found {} lines", tokens.size());
        return tokens;
    }

    public TokenLine tokenizeLine(String rawLine, int lineNumber) {
        String cleanLine = rawLine;

        // 1. Safe comment removal (ignores ';' inside strings)
        int commentIndex = -1;
        boolean inQuotes = false;
        for (int i = 0; i < cleanLine.length(); i++) {
            char c = cleanLine.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            if (!inQuotes && c == ';') {
                commentIndex = i;
                break;
            }
        }

        if (commentIndex != -1) cleanLine = cleanLine.substring(0, commentIndex);

        cleanLine = cleanLine.trim();
        if (cleanLine.isEmpty()) return new TokenLine(null, null, null, lineNumber, rawLine);

        String label = null;
        String mnemonic = null;
        String operand = null;

        // 2. Safe label extraction (ignores ':' inside string literals)
        int colonIndex = -1;
        inQuotes = false;
        for (int i = 0; i < cleanLine.length(); i++) {
            char c = cleanLine.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            if (!inQuotes && c == ':') {
                colonIndex = i;
                break;
            }
        }

        if (colonIndex != -1) {
            label = cleanLine.substring(0, colonIndex + 1).trim().toUpperCase();
            cleanLine = cleanLine.substring(colonIndex + 1).trim();
        }

        if (cleanLine.isEmpty()) return new TokenLine(label, null, null, lineNumber, rawLine);

        // 3. Split Mnemonic and Operand safely
        String[] parts = cleanLine.split("\\s+", 2);
        mnemonic = parts[0].toUpperCase();

        if (parts.length > 1) {
            operand = parts[1].trim();
            operand = this.sanitizeOperand(operand);
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
