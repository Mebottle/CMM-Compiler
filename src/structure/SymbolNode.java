package structure;

/**
 * Created by WeiZehao on 17/11/22.
 * 本类定义符号表中的符号节点
 */

class SymbolNode
{
    String _id;
    String _type;
    SymbolValueNode _valueNode;
    public SymbolNode(String type, String id)
    {
        _id = id;
        _type = type;
    }
}

interface SymbolValueNode
{}

class IntId implements SymbolValueNode
{
    int _value = 0;
}

class DoubleId implements SymbolValueNode
{
    double _value = 0.0;
}

class IntArray implements SymbolValueNode
{
    int[] _value;
    int _size;
    public IntArray(int size)
    {
        _value = new int[size];
        _size = size;
    }
}

class DoubleArray implements SymbolValueNode
{
    double[] _value;
    int _size;
    public DoubleArray(int size)
    {
        _value = new double[size];
        _size = size;
    }
}
