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
            LOG.error("Input file {} does not exists", inputFile.getName());
            System.exit(1);
        }

        List<TokenLine> tokens = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                TokenLine token = tokenizeLine(line, lineNumber);
                if (!token.isEmpty()) tokens.add(token);
            }
        }

        LOG.info("Lexing completed: found {} lines", tokens.size());
        return tokens;
    }

    public TokenLine tokenizeLine(String rawLine, int lineNumber) {
        // 1. Remove comments (everything after ';' is a comment)
        String cleanLine = rawLine;
        int commentIndex = cleanLine.indexOf(';');
        if (commentIndex != -1) cleanLine = cleanLine.substring(0, commentIndex);

        // 2. Trim initial and final spaces (If the line is empty skip it)
        cleanLine = cleanLine.trim();
        if (cleanLine.isEmpty()) return new TokenLine(null, null, null, lineNumber, rawLine);

        // Standardization (Converting all to uppercase)
        cleanLine = cleanLine.toUpperCase();

        String label = null;
        String mnemonic = null;
        String operand = null;

        // 3. Check the label (Labels ends with ':')
        if (cleanLine.contains(":")) {
            int colonIndex = cleanLine.indexOf(':');
            label = cleanLine.substring(0, colonIndex + 1).trim();
            cleanLine = cleanLine.substring(colonIndex + 1).trim();
        }

        if (cleanLine.isEmpty()) {
            return new TokenLine(label, null, null, lineNumber, rawLine);
        }

        // 4. Splitting the rest of the line
        String[] parts = cleanLine.split("\\s+", 2);
        mnemonic = parts[0];

        if (parts.length > 1) operand = parts[1].replaceAll("\\s+", "");

        return new TokenLine(label, mnemonic, operand, lineNumber, rawLine);
    }
}
