package interpreter;

import structure.SymbolTable;
import structure.Const;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by WeiZehao on 17/11/30.
 * 本类用于根据语法树和语义执行程序
 */
class CMMSemanticAnalyser
{
    private RootNode _root;
    private SymbolTable _firstTable;
    private boolean _mark = false;

    CMMSemanticAnalyser(RootNode root)
    {
        _root = root;
        _firstTable = new SymbolTable();
    }

    void start() throws RuntimeError
    {
        for(ASTNode stmt : _root._stmts)
        {
            visitStmt(stmt, _firstTable);
        }
    }

    /**
     * 输出运行时错误
     */
    private void error(int line, String msg) throws RuntimeError
    {
        System.out.printf("line %d : Runtime error< %s >\n", line, msg);
        throw new RuntimeError();
    }

    private void visitStmt(ASTNode stmt, SymbolTable symbolTable) throws RuntimeError
    {
        switch(stmt._type)
        {
            case Const.IFSTMT:
                visitIf((ASTIfStmt)stmt, symbolTable);
                break;
            case Const.BLOCK:
                visitBlock((ASTBlockStmt)stmt, symbolTable);
                break;
            case Const.WHILESTMT:
                visitWhile((ASTWhileStmt)stmt, symbolTable);
                break;
            case Const.ASSIGNSTMT:
                visitAssign((ASTAssignStmt)stmt, symbolTable);
                break;
            case Const.DECLARATION:
                visitVarDecl((ASTDeclaration)stmt, symbolTable);
                break;
            case Const.READSTMT:
                visitRead((ASTReadStmt)stmt, symbolTable);
                break;
            case Const.WRITESTMT:
                visitWrite((ASTWriteStmt)stmt, symbolTable);
                break;
            case Const.BREAKSTMT:
                visitBreak();
                break;
            case Const.FORSTMT:
                SymbolTable new_table = new SymbolTable();
                new_table._pre = symbolTable;
                visitFor((ASTForStmt)stmt, new_table);
        }
    }

    private void visitBreak()
    {
        _mark = true;
    }

    private void visitBlock(ASTBlockStmt block, SymbolTable symbolTable) throws RuntimeError
    {
        SymbolTable new_table = new SymbolTable();
        new_table._pre = symbolTable;
        for(ASTNode stmt : block._stmts)
        {
            visitStmt(stmt, new_table);
            if(_mark)
                return;
        }
    }

    private void visitIf(ASTIfStmt ifStmt, SymbolTable symbolTable) throws RuntimeError
    {
        ExprValue result = visitExpr(ifStmt.get_expr(), symbolTable);
        double bool = getBool(result);
        if(bool != 0)
            visitStmt(ifStmt.get_stmt(), symbolTable);
        else
        {
            if(ifStmt.get_elseStmt() != null)
                visitStmt(ifStmt.get_elseStmt(), symbolTable);
        }
    }

    private double getBool(ExprValue result)
    {
        double bool = 0;
        switch(result._type)
        {
            case Const.BOOL:
                bool = ((ExprBool)result)._value;
                break;
            case Const.INT:
                bool = ((ExprInt)result)._value;
                break;
            case Const.DOUBLE:
                bool = ((ExprDouble)result)._value;
        }
        return bool;
    }

    private void visitWhile(ASTWhileStmt whileStmt, SymbolTable symbolTable) throws RuntimeError
    {
        while(getBool(visitExpr(whileStmt._expr, symbolTable)) != 0)
        {
            if(whileStmt._stmt._type.equals(Const.BREAKSTMT))
                break;
            visitStmt(whileStmt._stmt, symbolTable);
            if(_mark)
                break;
        }
        _mark = false;
    }

    private void visitFor(ASTForStmt forStmt, SymbolTable symbolTable) throws RuntimeError
    {
        if(forStmt._first != null)
        {
            if(forStmt._first._type.equals(Const.DECLARATION))
                visitVarDecl((ASTDeclaration)forStmt._first, symbolTable);
            else
                visitAssign((ASTAssignStmt)forStmt._first, symbolTable);
        }

        while(getBool(visitExpr(forStmt._second, symbolTable)) != 0)
        {
            if(forStmt._stmt._type.equals(Const.BREAKSTMT))
                break;
            visitStmt(forStmt._stmt, symbolTable);
            if(forStmt._third != null)
                visitAssign(forStmt._third, symbolTable);
            if(_mark)
                break;
        }
        _mark = false;
    }

    private void visitAssign(ASTAssignStmt assignStmt, SymbolTable symbolTable) throws RuntimeError
    {
        ASTValue id = assignStmt._value;
        String id_name = id._op;
        SymbolTable symbolTable_new = getSymbolTable(id_name, symbolTable);
        String type = symbolTable_new.getType(id_name);
        switch(type)
        {
            case Const.INTID:
                symbolTable_new.setIntIdValue(id_name, (int)getValue(visitExpr(assignStmt._expr, symbolTable)));
                break;
            case Const.DOUBLEID:
                symbolTable_new.setDoubleIdValue(id_name, getValue(visitExpr(assignStmt._expr, symbolTable)));
                break;
            case Const.INTARRAY:
                ExprValue index = visitExpr(id._index, symbolTable);
                if(index._type.equals(Const.INT))
                {
                    int ind = ((ExprInt)index)._value;
                    int size = symbolTable_new.getIntArraySize(id_name);
                    if(ind > -1 && ind < size)
                        symbolTable_new.setIntArrayValue(id_name, ind, (int)getValue(visitExpr(assignStmt._expr, symbolTable)));
                    else
                        error(id._line, "Array index out of range");
                }
                else
                    error(id._line, "Array index should be integer");
                break;
            case Const.DOUBLEARRAY:
                ExprValue index2 = visitExpr(id._index, symbolTable);
                if(index2._type.equals(Const.INT))
                {
                    int ind = ((ExprInt)index2)._value;
                    int size = symbolTable_new.getDoubleArraySize(id_name);
                    if(ind > -1 && ind < size)
                        symbolTable_new.setDoubleArrayValue(id_name, ind, getValue(visitExpr(assignStmt._expr, symbolTable)));
                    else
                        error(id._line, "Array index out of range");
                }
                else
                    error(id._line, "Array index should be integer");
                break;
        }
    }

    private void visitVarDecl(ASTDeclaration declarationStmt, SymbolTable symbolTable) throws RuntimeError
    {
        String type = declarationStmt._declType._type;
        String declarator = declarationStmt._declType._declarator;
        for(ASTValue var : declarationStmt._vars)
        {
            String id_name = var._op;
            if(type.equals(Const.NUMBERDECL))
            {
                switch(declarator)
                {
                    case "int":
                        symbolTable.addIntId(id_name);
                        if(var._valueExpr != null)
                            symbolTable.setIntIdValue(id_name, (int)getValue(visitExpr(var._valueExpr, symbolTable)));
                        break;
                    case "double":
                        symbolTable.addDoubleID(id_name);
                        if(var._valueExpr != null)
                            symbolTable.setDoubleIdValue(id_name, getValue(visitExpr(var._valueExpr, symbolTable)));
                        break;
                }
            }
            else
            {
                switch(declarator)
                {
                    case "int":
                        symbolTable.addIntArray(id_name, ((ASTArrayDecl)declarationStmt._declType)._size);
                        break;
                    case "double":
                        symbolTable.addDoubleArray(id_name, ((ASTArrayDecl)declarationStmt._declType)._size);
                        break;
                }
            }
        }
    }

    private void visitRead(ASTReadStmt readStmt, SymbolTable symbolTable) throws RuntimeError
    {
        ASTValue id = readStmt._value;
        Scanner s = new Scanner(System.in);
        String content = s.nextLine();
        if(isNumeric(content))
        {
            SymbolTable symbolTable_new = getSymbolTable(id._op, symbolTable);
            String type = symbolTable_new.getType(id._op);
            switch(type)
            {
                case Const.INTID:
                    symbolTable_new.setIntIdValue(id._op, (int)Double.parseDouble(content));
                    break;
                case Const.DOUBLEID:
                    symbolTable_new.setDoubleIdValue(id._op, Double.parseDouble(content));
                    break;
                case Const.INTARRAY:
                    ExprValue index = visitExpr(id._index, symbolTable);
                    if(index._type.equals(Const.INT))
                    {
                        int ind = ((ExprInt)index)._value;
                        int size = symbolTable_new.getIntArraySize(id._op);
                        if(ind > -1 && ind < size)
                            symbolTable_new.setIntArrayValue(id._op, ind, (int)Double.parseDouble(content));
                        else
                            error(id._line, "Array index out of range");
                    }
                    else
                        error(id._line, "Array index should be integer");
                    break;
                case Const.DOUBLEARRAY:
                    ExprValue index2 = visitExpr(id._index, symbolTable);
                    if(index2._type.equals(Const.INT))
                    {
                        int ind = ((ExprInt)index2)._value;
                        int size = symbolTable_new.getDoubleArraySize(id._op);
                        if(ind > -1 && ind < size)
                            symbolTable_new.setDoubleArrayValue(id._op, ind, Double.parseDouble(content));
                        else
                            error(id._line, "Array index out of range");
                    }
                    else
                        error(id._line, "Array index should be integer");
                    break;
            }
        }
        else
        {
            error(id._line, "Enter error, the enter should be integer or float");
        }
    }

    private boolean isNumeric(String str)
    {
        Pattern pattern = Pattern.compile("([+-])?[0-9]+(\\.[0-9]+)?");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    private void visitWrite(ASTWriteStmt writeStmt, SymbolTable symbolTable) throws RuntimeError
    {
        ExprValue result = visitExpr(writeStmt._expr, symbolTable);
        switch(result._type)
        {
            case Const.INT:
                System.out.println(((ExprInt)result)._value);
                break;
            case Const.DOUBLE:
                System.out.println(((ExprDouble)result)._value);
                break;
            case Const.BOOL:
                System.out.println(((ExprBool)result)._value);
                break;
        }
    }

    private ExprValue visitExpr(ASTExpr expr, SymbolTable symbolTable) throws RuntimeError
    {
        switch(expr._type)
        {
            case Const.INT:
                return new ExprInt(Const.INT, Integer.parseInt(expr._op));
            case Const.DOUBLE:
                return new ExprDouble(Const.DOUBLE, Double.parseDouble(expr._op));
            case Const.BOOL:
                if(expr._op.equals("true"))
                    return new ExprBool(Const.BOOL, 1);
                else
                    return new ExprBool(Const.BOOL, 0);
            case Const.VALUEID:
                symbolTable = getSymbolTable(expr._op, symbolTable);
                String type = symbolTable.getType(expr._op);
                switch(type)
                {
                    case Const.INTID:
                        return new ExprInt(Const.INT, symbolTable.getIntIdValue(expr._op));
                    case Const.DOUBLEID:
                        return new ExprDouble(Const.DOUBLE, symbolTable.getDoubleIdValue(expr._op));
                }
                break;
            case Const.VALUEARRAY:
                SymbolTable symbolTable_new = getSymbolTable(expr._op, symbolTable);
                String type2 = symbolTable_new.getType(expr._op);
                switch(type2)
                {
                    case Const.INTARRAY:
                        ExprValue index = visitExpr(((ASTValue)expr)._index, symbolTable);
                        if(index._type.equals(Const.INT))
                        {
                            int ind = ((ExprInt)index)._value;
                            int size = symbolTable_new.getIntArraySize(expr._op);
                            if(ind > -1 && ind < size)
                                return new ExprInt(Const.INT, symbolTable_new.getIntArrayValue(expr._op, ind));
                            else
                                error(expr._line, "Array index out of range");
                        }
                        else
                            error(expr._line, "Array index should be integer");
                        break;
                    case Const.DOUBLEARRAY:
                        ExprValue index2 = visitExpr(((ASTValue)expr)._index, symbolTable);
                        if(index2._type.equals(Const.INT))
                        {
                            int ind = ((ExprInt)index2)._value;
                            int size = symbolTable_new.getDoubleArraySize(expr._op);
                            if(ind > -1 && ind < size)
                                return new ExprDouble(Const.DOUBLE, symbolTable_new.getDoubleArrayValue(expr._op, ind));
                            else
                                error(expr._line, "Array index out of range");
                        }
                        else
                            error(expr._line, "Array index should be integer");
                }
                break;
            case Const.UNARYEXPR:
                ExprValue result = visitExpr(expr._left, symbolTable);
                if(result._type.equals(Const.INT))
                {
                    ((ExprInt)result)._value = -((ExprInt)result)._value;
                    return result;
                }
                else if(result._type.equals(Const.DOUBLE))
                {
                    ((ExprDouble)result)._value = -((ExprDouble)result)._value;
                    return result;
                }
                else
                {
                    return new ExprInt(Const.INT, -((ExprBool)result)._value);
                }
            case Const.EXPRESSION:
                ExprValue left = visitExpr(expr._left, symbolTable);
                ExprValue right = visitExpr(expr._right, symbolTable);
                switch(expr._op)
                {
                    case "!=":
                        if(getValue(left) != getValue(right))
                            return new ExprBool(Const.BOOL, 1);
                        else
                            return new ExprBool(Const.BOOL, 0);
                    case "==":
                        if(getValue(left) == getValue(right))
                            return new ExprBool(Const.BOOL, 1);
                        else
                            return new ExprBool(Const.BOOL, 0);
                    case ">":
                        if(getValue(left) > getValue(right))
                            return new ExprBool(Const.BOOL, 1);
                        else
                            return new ExprBool(Const.BOOL, 0);
                    case ">=":
                        if(getValue(left) >= getValue(right))
                            return new ExprBool(Const.BOOL, 1);
                        else
                            return new ExprBool(Const.BOOL, 0);
                    case "<":
                        if(getValue(left) < getValue(right))
                            return new ExprBool(Const.BOOL, 1);
                        else
                            return new ExprBool(Const.BOOL, 0);
                    case "<=":
                        if(getValue(left) <= getValue(right))
                            return new ExprBool(Const.BOOL, 1);
                        else
                            return new ExprBool(Const.BOOL, 0);
                    case "+":
                        if(left._type.equals(Const.DOUBLE) || right._type.equals(Const.DOUBLE))
                            return new ExprDouble(Const.DOUBLE, getValue(left) + getValue(right));
                        else
                            return new ExprInt(Const.INT, (int)getValue(left) + (int)getValue(right));
                    case "-":
                        if(left._type.equals(Const.DOUBLE) || right._type.equals(Const.DOUBLE))
                            return new ExprDouble(Const.DOUBLE, getValue(left) - getValue(right));
                        else
                            return new ExprInt(Const.INT, (int)getValue(left) - (int)getValue(right));
                    case "*":
                        if(left._type.equals(Const.DOUBLE) || right._type.equals(Const.DOUBLE))
                            return new ExprDouble(Const.DOUBLE, getValue(left) * getValue(right));
                        else
                            return new ExprInt(Const.INT, (int)getValue(left) * (int)getValue(right));
                    case "/":
                        if(getValue(right) == 0)
                        {
                            error(expr._line, "Divisor should not be zero");
                            break;
                        }
                        if(left._type.equals(Const.DOUBLE) || right._type.equals(Const.DOUBLE))
                            return new ExprDouble(Const.DOUBLE, getValue(left) / getValue(right));
                        else
                        {
                            int number = (int)getValue(left) / (int)getValue(right);
                            return new ExprInt(Const.INT, number);
                        }
                }
        }
        return new ExprError(Const.SEMANTICERROR);
    }

    private double getValue(ExprValue value)
    {
        switch(value._type)
        {
            case Const.INT:
                return ((ExprInt)value)._value;
            case Const.DOUBLE:
                return ((ExprDouble)value)._value;
            case Const.BOOL:
                return ((ExprBool)value)._value;
            default:
                return -1;
        }
    }

    private SymbolTable getSymbolTable(String id, SymbolTable symbolTable)
    {
        while(symbolTable != null)
        {
            if(symbolTable.hasId(id))
                return symbolTable;
            else
                symbolTable = symbolTable._pre;
        }
        return null;
    }
}

abstract class ExprValue
{
    String _type;
    ExprValue(String type)
    {
        _type = type;
    }
}

class ExprInt extends ExprValue
{
    int _value;
    ExprInt(String type, int value)
    {
        super(type);
        _value = value;
    }
}

class ExprDouble extends ExprValue
{
    double _value;
    ExprDouble(String type, double value)
    {
        super(type);
        _value = value;
    }
}

class ExprBool extends ExprValue
{
    int _value;

    ExprBool(String type, int value) {
        super(type);
        _value = value;
    }
}

class ExprError extends ExprValue
{
    ExprError(String type)
    {
        super(type);
    }
}
