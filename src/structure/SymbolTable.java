package structure;

import java.util.HashMap;
/**
 * Created by WeiZehao on 17/11/22.
 * 本类是符号表
 */
public class SymbolTable
{
    public SymbolTable _pre;

    private HashMap<String, SymbolNode> _symbolTable;

    public SymbolTable()
    {
        _symbolTable = new HashMap<>();
    }

    public boolean hasId(String id)
    {
        return _symbolTable.containsKey(id);
    }

    public String getType(String id)
    {
        if(hasId(id))
        {
            return _symbolTable.get(id)._type;
        }
        else
            return Const.STERROR;
    }

    public void addIntId(String id)
    {
        SymbolNode node = new SymbolNode(Const.INTID, id);
        node._valueNode = new IntId();
        _symbolTable.put(id, node);
    }

    public void setIntIdValue(String id, int value)
    {
        ((IntId)(_symbolTable.get(id)._valueNode))._value = value;
    }

    public int getIntIdValue(String id)
    {
        return ((IntId)(_symbolTable.get(id)._valueNode))._value;
    }

    public void addDoubleID(String id)
    {
        SymbolNode node = new SymbolNode(Const.DOUBLEID, id);
        node._valueNode = new DoubleId();
        _symbolTable.put(id, node);
    }

    public void setDoubleIdValue(String id, double value)
    {
        ((DoubleId)(_symbolTable.get(id)._valueNode))._value = value;
    }

    public double getDoubleIdValue(String id)
    {
        return ((DoubleId)(_symbolTable.get(id)._valueNode))._value;
    }

    public void addIntArray(String id, int size)
    {
        SymbolNode node = new SymbolNode(Const.INTARRAY, id);
        node._valueNode = new IntArray(size);
        _symbolTable.put(id, node);
    }

    public void setIntArrayValue(String id, int index, int value)
    {
        ((IntArray)(_symbolTable.get(id)._valueNode))._value[index] = value;
    }

    public int getIntArrayValue(String id, int index)
    {
        return ((IntArray)(_symbolTable.get(id)._valueNode))._value[index];
    }

    public int getIntArraySize(String id)
    {
        return ((IntArray)(_symbolTable.get(id)._valueNode))._size;
    }

    public void addDoubleArray(String id, int size)
    {
        SymbolNode node = new SymbolNode(Const.DOUBLEARRAY, id);
        node._valueNode = new DoubleArray(size);
        _symbolTable.put(id, node);
    }

    public void setDoubleArrayValue(String id, int index, double value)
    {
        ((DoubleArray)(_symbolTable.get(id)._valueNode))._value[index] = value;
    }

    public double getDoubleArrayValue(String id, int index)
    {
        return ((DoubleArray)(_symbolTable.get(id)._valueNode))._value[index];
    }

    public int getDoubleArraySize(String id)
    {
        return ((DoubleArray)(_symbolTable.get(id)._valueNode))._size;
    }

}
