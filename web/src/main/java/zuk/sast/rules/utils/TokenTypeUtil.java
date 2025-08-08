package zuk.sast.rules.utils;

import org.apache.commons.text.StringEscapeUtils;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import java.util.HashMap;
import java.util.Map;

public class TokenTypeUtil {
    public static Map<Integer, String> tokenMap = new HashMap<>();
    public static String NULL = "NULL";
    static {
        tokenMap.put(TokenTypes.NULL, NULL); //0
        tokenMap.put(TokenTypes.COMMENT_EOL, NULL);
        tokenMap.put(TokenTypes.COMMENT_MULTILINE, NULL);
        tokenMap.put(TokenTypes.COMMENT_DOCUMENTATION, NULL);
        tokenMap.put(TokenTypes.COMMENT_KEYWORD, "keyword");

        tokenMap.put(TokenTypes.COMMENT_MARKUP, NULL); //5
        tokenMap.put(TokenTypes.RESERVED_WORD, "keyword");
        tokenMap.put(TokenTypes.RESERVED_WORD_2, "keyword");
        tokenMap.put(TokenTypes.FUNCTION, "function"); //函数
        tokenMap.put(TokenTypes.LITERAL_BOOLEAN, NULL);

        tokenMap.put(TokenTypes.LITERAL_NUMBER_DECIMAL_INT, "number"); //10
        tokenMap.put(TokenTypes.LITERAL_NUMBER_FLOAT, NULL);
        tokenMap.put(TokenTypes.LITERAL_NUMBER_HEXADECIMAL, NULL);
        tokenMap.put(TokenTypes.LITERAL_STRING_DOUBLE_QUOTE, "String");
        tokenMap.put(TokenTypes.LITERAL_CHAR, NULL);

        tokenMap.put(TokenTypes.LITERAL_BACKQUOTE, NULL); //15
        tokenMap.put(TokenTypes.DATA_TYPE, "keyword"); //16 数据类型
        tokenMap.put(TokenTypes.VARIABLE, NULL);
        tokenMap.put(TokenTypes.REGEX, NULL);
        tokenMap.put(TokenTypes.ANNOTATION, NULL);

        tokenMap.put(TokenTypes.IDENTIFIER, NULL); //20 标识符
        tokenMap.put(TokenTypes.WHITESPACE, NULL);
        tokenMap.put(TokenTypes.SEPARATOR, NULL); //22
        tokenMap.put(TokenTypes.OPERATOR, NULL);
        tokenMap.put(TokenTypes.PREPROCESSOR, NULL);

        tokenMap.put(TokenTypes.MARKUP_TAG_DELIMITER, NULL); //25
        tokenMap.put(TokenTypes.MARKUP_TAG_NAME, NULL);
        tokenMap.put(TokenTypes.MARKUP_TAG_ATTRIBUTE, NULL);
        tokenMap.put(TokenTypes.MARKUP_TAG_ATTRIBUTE_VALUE, NULL);
        tokenMap.put(TokenTypes.MARKUP_COMMENT, NULL);

        tokenMap.put(TokenTypes.MARKUP_DTD, NULL);
        tokenMap.put(TokenTypes.MARKUP_PROCESSING_INSTRUCTION, NULL);
        tokenMap.put(TokenTypes.MARKUP_CDATA_DELIMITER, NULL);
        tokenMap.put(TokenTypes.MARKUP_CDATA, NULL);
        tokenMap.put(TokenTypes.MARKUP_ENTITY_REFERENCE, NULL);

        tokenMap.put(TokenTypes.ERROR_IDENTIFIER, NULL);
        tokenMap.put(TokenTypes.ERROR_NUMBER_FORMAT, NULL);
        tokenMap.put(TokenTypes.ERROR_STRING_DOUBLE, NULL);
        tokenMap.put(TokenTypes.ERROR_CHAR, NULL);
        tokenMap.put(TokenTypes.DEFAULT_NUM_TOKEN_TYPES, NULL);

    }

    public static String getHtml(Integer type, String text) {
        String value = tokenMap.get(type);
        text = StringEscapeUtils.escapeHtml4(text);
//        if(value.equals(NULL)){
//            return "<span class = 'token punctuation'>" + text + "</span>";
//        }
//        else {
//            return "<span class = 'token " + value + "'>" + text + "</span>";
//        }
        return "<span class = 'token hl_"+type+"'>" + text + "</span>";
    }
}
