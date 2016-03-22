package com.seele;

/**
 * Created by flb on 16/3/21.
 */

import org.jf.smali.smaliTreeWalker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.apache.commons.io.IOUtils;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.smali.LexerErrorInterface;
import org.jf.smali.smaliFlexLexer;
import org.jf.smali.smaliParser;

public class SmaliMod {
    public static boolean assembleSmaliFile(String smali, DexBuilder dexBuilder, boolean verboseErrors, boolean printTokens, File smaliFile)
            throws IOException, RuntimeException, RecognitionException {
        InputStream is = new ByteArrayInputStream(smali.getBytes());
        return assembleSmaliFile(is, dexBuilder, verboseErrors, printTokens, smaliFile);
    }

    public static boolean assembleSmaliFile(InputStream is, DexBuilder dexBuilder, boolean verboseErrors, boolean printTokens, File smaliFile)
            throws IOException, RecognitionException {
        File tmp = File.createTempFile("BRUT", ".bak");
        tmp.deleteOnExit();

        OutputStream os = new FileOutputStream(tmp);
        IOUtils.copy(is, os);
        os.close();

        return assembleSmaliFile(tmp, dexBuilder, verboseErrors, printTokens);
    }

    public static boolean assembleSmaliFile(File smaliFile, DexBuilder dexBuilder, boolean verboseErrors, boolean printTokens)
            throws IOException, RecognitionException {
        InputStream is = new FileInputStream(smaliFile);
        InputStreamReader reader = new InputStreamReader(is, "UTF-8");

        LexerErrorInterface lexer = new smaliFlexLexer(reader);
        ((smaliFlexLexer) lexer).setSourceFile(smaliFile);
        CommonTokenStream tokens = new CommonTokenStream((TokenSource) lexer);

        if (printTokens) {
            tokens.getTokens();

            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                if (token.getChannel() != 99) {
                    System.out.println(smaliParser.tokenNames[token.getType()] + ": " + token.getText());
                }
            }
        }
        smaliParser parser = new smaliParser(tokens);
        parser.setVerboseErrors(verboseErrors);

        smaliParser.smali_file_return result = parser.smali_file();

        if ((parser.getNumberOfSyntaxErrors() > 0) || (lexer.getNumberOfSyntaxErrors() > 0)) {
            return false;
        }

        CommonTree t = result.getTree();

        CommonTreeNodeStream treeStream = new CommonTreeNodeStream(t);
        treeStream.setTokenStream(tokens);

        smaliTreeWalker dexGen = new smaliTreeWalker(treeStream);

        dexGen.setVerboseErrors(verboseErrors);
        dexGen.setDexBuilder(dexBuilder);
        dexGen.smali_file();

        return dexGen.getNumberOfSyntaxErrors() == 0;
    }
}
