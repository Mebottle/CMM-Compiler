package gui;

import interpreter.CMMLexer;
import structure.Token;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.awt.Color;

import javax.swing.text.*;

public class HighLight {
    // 每个字符的宽度
    private int charWidth;
    // 每个字符的高度
    private int charHeight;
    // 划定界限
    private static final String DELIM = "[] (); {}. \n\t";
    // 字体
    private static final String FAMILY = "Courier New";
    // 字体大小
    private static final int SIZE = 14;
    // 关键字颜色
//    private static final Color KEYWORDS_FOREGROUND = new Color(185, 120, 50);
    private static final Color KEYWORDS_FOREGROUND = new Color(0, 100, 255);
    // 变量颜色
    private static final Color VARIABLE_FOREGROUND = new Color(142, 80, 168);
    // 数字颜色
    private static final Color NUMBER_FOREGROUND = new Color(118, 149, 186);
    // 错误颜色
    private static final Color ERROR_FOREGROUND = new Color(203, 58, 6);
    // 默认字体颜色
    private static final Color DEFAULT_FORGROUND = new Color(0, 0, 0);
    // 样式上下文
    private static final StyleContext styleContext = new StyleContext();
    // 第一行字符的Y坐标
    private static final int FIRST_POSITION_Y = 20;
    // 第一行以后每行字符的长度
    private static final int LINE_LENGTH_Y = 17;
    // 关键字集合
    private static final HashSet<String> keywords = new HashSet<String>();
    private ArrayList<Token> displayTokens;
    Style s = null;

    private CMMLexer lexer;

    public HighLight() {
        // 添加默认样式
        addStyle("none");
        // 添加关键字样式
        addStyle("keywords", KEYWORDS_FOREGROUND);
        // 添加变量样式
        addStyle("variable", VARIABLE_FOREGROUND);
        // 添加字符串样式
        addStyle("number", NUMBER_FOREGROUND);
        addStyle("error", ERROR_FOREGROUND);
        // 添加关键字
        keywords.add("int");
        keywords.add("double");
        keywords.add("real");
        keywords.add("bool");
        keywords.add("string");
        keywords.add("if");
        keywords.add("else");
        keywords.add("while");
        keywords.add("read");
        keywords.add("write");
        keywords.add("true");
        keywords.add("false");
        keywords.add("for");
        keywords.add("break");
    }

    public void WriteStringToFile(String filePath, String text) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filePath));
            pw.println(text);
            pw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 将text填充文本到doc中
     *
     * @param text 要填充到StyledDocument中的文本
     * @param doc  填充的StyledDocument对象
     */
    public void markStyle(String text, StyledDocument doc) throws IOException, InvocationTargetException, InterruptedException, BadLocationException {
        WriteStringToFile("temp.cmm", text);
        lexer = new CMMLexer("temp.cmm");
        displayTokens = new ArrayList<Token>();
        while (true) {
            Token token = lexer.getToken();
            if (token.getType().equals("EOF")) {
                break;
            }
            displayTokens.add(token);
        }
        for (Token token : displayTokens) {
            s = null;
            if (keywords.contains(token.getContent()) && token.getType().equals("RESERVED")) {
                s = styleContext.getStyle("keywords");
                StyleConstants.setBold(s, true);
            } else if (token.getType().equals("ID")) {
                s = styleContext.getStyle("variable");
                StyleConstants.setBold(s, true);
            } else if (token.getType().equals("INTEGER") || token.getType().equals("FLOAT")) {
                s = styleContext.getStyle("number");
                StyleConstants.setBold(s, true);
            } else if (token.getType().equals("ERROR")) {
                s = styleContext.getStyle("error");
                StyleConstants.setBold(s, true);

                StyleConstants.setUnderline(s, true);

            } else {
                s = styleContext.getStyle("none");
            }
            OffsetGetter offsetGetter = new OffsetGetter("temp.cmm");
            int offsets = offsetGetter.getOffset(token.getRow(), token.getCol());
            doc.remove(offsets, token.getContent().length());
            doc.insertString(offsets, token.getContent(), s);

        }
    }

    protected void addStyle(String key) {
        addStyle(key, DEFAULT_FORGROUND, SIZE, FAMILY);
    }

    protected void addStyle(String key, Color color) {
        addStyle(key, color, SIZE, FAMILY);
    }

    protected void addStyle(String key, Color color, int size, String fam) {
        Style s = styleContext.addStyle(key, null);
        if (color != null)
            StyleConstants.setForeground(s, color);
        if (size > 0)
            StyleConstants.setFontSize(s, size);
        if (fam != null)
            StyleConstants.setFontFamily(s, fam);
    }
}