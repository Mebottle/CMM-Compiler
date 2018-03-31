package interpreter;

import structure.Token;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by WeiZehao on 17/9/30.
 * 词法分析器
 */
public class CMMLexer
{
    private FileReader _reader;
    private char _candidate;
    private int _row = 0;
    private int _col = 0;
    private static final char EOF = (char)4;  // 控制字符EOT，表示传输结束

    public CMMLexer(String path)
    {
        try
        {
            _reader = new FileReader(path);
            // 读第一个字符
            nextChar();
            _row++;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    void close()
    {
        try
        {
            _reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 获取一个词法单元
     * 每次调用结束后，_candidate通过nextChar()方法，均指向下一个将被验证的字符，故直接进入判断
     * 每次调用结束后，返回读取的token，且此时读取指针已经指向下一个token的开头字符
     * @return token
     */
    public Token getToken()
    {
        int offset = 0;
        StringBuffer strb;

        ignoreBlank();

        // 识别'/' 或 注释
        while(_candidate == '/')
        {
            int currentRow = _row;
            int currentCol = _col;
            nextChar();
            // 读取到注释
            if(_candidate == '/')
            {
                // 忽略注释内容
                while(true)
                {
                    nextChar();
                    if(_candidate == '\n' || _candidate == EOF)
                    {
                        nextChar();
                        _row++;
                        _col = 1;
                        break;
                    }
                }
                ignoreBlank();
            }
            // 读取到注释
            else if(_candidate == '*')
            {
                nextChar(); // 先读取*后的第一个字符
                while(true)
                {
                    // 识别注释结束符号
                    if(_candidate == '*')
                    {
                        nextChar();
                        // 若'*'后读取到'/'，则注释结束
                        if(_candidate == '/')
                        {
                            nextChar();
                            break;
                        }
                    }
                    // 未读取到注释结束符号，报错
                    else if(_candidate == EOF)
                    {
                        return new Token("ERROR","/*", currentRow, currentCol);
                    }
                    else if(_candidate == '\n')
                    {
                        nextChar();
                        _row++;
                        _col = 1;
                    }
                    // 在识别到注释结束符号前，忽略所有读到的字符
                    else
                        nextChar();
                }
                ignoreBlank();
            }
            // 读取到普通'/'
            else
                return new Token("OP","/",_row,_col - 1);
        }

        ignoreBlank();

        switch (_candidate)
        {
            case '+':
                nextChar();
                return new Token("OP","+",_row,_col - 1);
            case '-':
                nextChar();
                return new Token("OP","-",_row,_col - 1);
            case '*':
                nextChar();
                return new Token("OP","*",_row,_col - 1);
            case '(':
                nextChar();
                return new Token("(","(",_row,_col - 1);
            case ')':
                nextChar();
                return new Token(")",")",_row,_col - 1);
            case '{':
                nextChar();
                return new Token("{","{",_row,_col - 1);
            case '}':
                nextChar();
                return new Token("}","}",_row,_col - 1);
            case '[':
                nextChar();
                return new Token("[","[",_row,_col - 1);
            case ']':
                nextChar();
                return new Token("]","]",_row,_col - 1);
            case ',':
                nextChar();
                return new Token(",",",",_row,_col - 1);
            case ':':
                nextChar();
                return new Token(":", ":",_row,_col - 1);
            case ';':
                nextChar();
                return new Token(";",";",_row,_col - 1);
            case '!':
            {
                nextChar();
                if(_candidate == '=')
                {
                    nextChar();
                    return new Token("OP","!=",_row,_col - 2);
                }
                else
                    return new Token("ERROR", "!",_row,_col - 1);
            }
            case '=':
            {
                nextChar();
                if(_candidate == '=')
                {
                    nextChar();
                    return new Token("OP","==",_row,_col - 2);
                }
                else
                    return new Token("OP","=",_row,_col - 1);
            }
            case '<':
            {
                nextChar();
                if(_candidate == '=')
                {
                    nextChar();
                    return new Token("OP","<=",_row,_col - 2);
                }
                else
                    return new Token("OP","<",_row,_col - 1);
            }
            case '>':
            {
                nextChar();
                if(_candidate == '=')
                {
                    nextChar();
                    return new Token("OP",">=",_row,_col - 2);
                }
                else
                    return new Token("OP",">",_row,_col - 1);
            }
            default:
                break;
        }
        // 识别标识符或者保留字
        if(Character.isAlphabetic(_candidate) || _candidate == '_')
        {
            strb = new StringBuffer();
            do
            {
                strb.append(_candidate);
                nextChar();
                offset++;
            }while(Character.isAlphabetic(_candidate)
                    || Character.isDigit(_candidate)
                    || _candidate == '_');
            String str = strb.toString();
            // 识别为保留字
            if(str.equals("true")
                    || str.equals("false")
                    || str.equals("if")
                    || str.equals("else")
                    || str.equals("while")
                    || str.equals("read")
                    || str.equals("write")
                    || str.equals("int")
                    || str.equals("double")
                    || str.equals("break"))
            {
                return new Token("RESERVED",str,_row,_col - offset);
            }
            // 若标识符以'_'结尾则报错
            else if(str.charAt(str.length() - 1) == '_')
            {
                return new Token("ERROR",str,_row,_col - offset);
            }
            // 识别为标识符
            else
            {
                return new Token("ID",str,_row,_col - offset);
            }
        }
        // 识别数字
        else if(Character.isDigit(_candidate))
        {
            strb = new StringBuffer();
            boolean hasDot = false;

            /*
             * 识别[1-9][0-9]* 或 0 或 16进制数
             */

            // 识别'[1-9][0-9]*'
            if(_candidate != '0')
            {
                do
                {
                    strb.append(_candidate);
                    nextChar();
                    offset++;
                }while(Character.isDigit(_candidate));
            }
            // 识别0
            else
            {
                strb.append(_candidate);
                nextChar();
                offset++;
                // 若0后是x或X，识别16进制数
                if(_candidate == 'x' || _candidate == 'X')
                {
                    strb.append(_candidate);
                    nextChar();
                    offset++;
                    if(isHexChar(_candidate))
                    {
                        do
                        {
                            strb.append(_candidate);
                            nextChar();
                            offset++;
                        }while(isHexChar(_candidate));
                        return new Token("HEXADECIMAL",strb.toString(),_row,_col - offset);
                    }
                    else
                        return new Token("ERROR",strb.toString(),_row,_col - offset);
                }
            }

            /*
             * 识别'.[0-9]*' 或 空
             */
            if(_candidate == '.')
            {
                hasDot = true;
                strb.append(_candidate);
                nextChar();
                offset++;
                while(Character.isDigit(_candidate))
                {
                    strb.append(_candidate);
                    nextChar();
                    offset++;
                }
            }

            // 识别数字成功
            if(hasDot)
                return new Token("FLOAT",strb.toString(),_row,_col - offset);
            else
                return new Token("INTEGER",strb.toString(),_row,_col - offset);
        }
        // 正常读到文件结尾
        else if(_candidate == EOF)
            return new Token("EOF","EOF",_row,_col - offset);
        else
        {
            char wrongChar = _candidate;
            nextChar();
            return new Token("ERROR",String.valueOf(wrongChar),_row,_col - 1);
        }
    }

    /**
     * 从文件中读取下一个字符
     */
    private void nextChar()
    {
        try
        {
            int num = _reader.read();
            _col++;
            if(num != -1)
            {
                _candidate = (char)num;
            }
            else
                _candidate = EOF;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 忽略空格\t\n等
     */
    private void ignoreBlank()
    {
        while(true)
        {
            // 如果为空格或TAB键或回车符继续读下一个
            if(_candidate == ' ' || _candidate == '\t' || _candidate == '\r')
            {
                nextChar();
            }
            // 如果读到行尾，换行
            else if(_candidate == '\n')
            {
                nextChar();
                _row++;
                _col = 1;
            }
            else
                break;
        }
    }

    /**
     * 判断字符是否属于十六进制数中允许出现的字符
     * @param c 字符
     * @return boolean
     */
    private boolean isHexChar(char c)
    {
        if(Character.isDigit(c)
                || c == 'a'
                || c == 'A'
                || c == 'b'
                || c == 'B'
                || c == 'c'
                || c == 'C'
                || c == 'd'
                || c == 'D'
                || c == 'e'
                || c == 'E'
                || c == 'f'
                || c == 'F')
            return true;
        else
            return false;
    }
}
