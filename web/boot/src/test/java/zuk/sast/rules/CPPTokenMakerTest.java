package zuk.sast.rules;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMaker;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.fife.ui.rsyntaxtextarea.modes.CPlusPlusTokenMaker;

import javax.swing.text.Segment;

public class CPPTokenMakerTest {
    public static void main(String[] args) throws Exception {
        String code = """
                /* SDSLib 2.0 -- A C dynamic strings library
                 *
                 * Copyright (c) 2006-2015, Salvatore Sanfilippo <antirez at gmail dot com>
                 * Copyright (c) 2015, Oran Agra
                 * Copyright (c) 2015, Redis Labs, Inc
                 * All rights reserved. */
                 int a = 0;
                 // over
                 int b = 1;
                 /* 注释一下 */
                 in c = 3;
                """;
//        code = FileUtils.readFileToString(new File("C:/Users/5132/Desktop/redis/redis-unstable/redis-unstable/deps/hiredis/sds.h"), "UTF-8");
        // TokenTypes.java 类型
        TokenTypes tokenTypes;

        int tokenType = TokenTypes.NULL;
        for(String line : code.split("\n")) {
            CPPTokenMakerTest cppTokenMakerTest = new CPPTokenMakerTest();
            Segment segment = cppTokenMakerTest.createSegment(line);
            TokenMaker tm = new CPlusPlusTokenMaker();
            Token token = tm.getTokenList(segment, tokenType, 0);
            if(token.getType() == TokenTypes.COMMENT_MULTILINE){
                System.out.println("注释, " + token.getType());
            }

            tokenType = cppTokenMakerTest.printToken(token);
//            System.out.println(token);
        }

    }

    protected Segment createSegment(String code) {
        return new Segment(code.toCharArray(), 0, code.length());
    }

    protected int printToken(Token token){
        Token next = token;

        while (next !=null && next.getType() != TokenTypes.NULL) {
            String text = new String(next.getTextArray());
            int offset = next.getOffset();
            int len = next.length();
            int type = next.getType();
            String tokenImage = text.substring(offset, offset + len);
            System.out.println(type + " " + tokenImage);
//            System.out.print(TokenTypeUtil.getHtml(type, tokenImage));
//            System.out.print(tokenImage);
            next = next.getNextToken();
        }

        System.out.println();
        if(token.getType() == TokenTypes.COMMENT_MULTILINE && token.getNextToken()==null){
            return TokenTypes.COMMENT_MULTILINE;
        }
        else if(token.getType() == TokenTypes.COMMENT_MULTILINE
                && token.getNextToken()!=null
                && token.getNextToken().getLexeme()!=null
                && token.getNextToken().getLexeme().equals("*/")){
            return TokenTypes.NULL;
        }
        else {
            return TokenTypes.NULL;
        }



    }

}
