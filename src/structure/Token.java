package structure;

/**
 * Created by WeiZehao on 17/9/30.
 * structure.Token
 */
public class Token
{
    private int _col;
    private int _row;
    private String _type;       // 终结符类型
    private String _content;    // 终结符实际内容

    public Token(String type, String content, int row, int col)
    {
        _type = type;
        _content = content;
        _row = row;
        _col = col;
    }

    public String getContent()
    {
        return _content;
    }

    public String getType()
    {
        return _type;
    }

    public int getCol()
    {
        return _col;
    }

    public int getRow()
    {
        return _row;
    }
}
