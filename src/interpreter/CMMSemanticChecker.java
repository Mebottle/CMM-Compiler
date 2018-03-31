package interpreter;

import structure.SymbolTable;
import structure.Const;
import java.util.Stack;

/**
 * Created by WeiZehao on 17/11/22.
 * 本类用于语义检查
 */
class CMMSemanticChecker
{
    private RootNode _root;
    private SymbolTable _firstTable;
    private Stack<ASTNode> _breakableStmt;
    private boolean _hasError = false;

    CMMSemanticChecker(RootNode root)
    {
        _root = root;
        _firstTable = new SymbolTable();
        _breakableStmt = new Stack<>();
    }

    boolean start()
    {
        for(ASTNode stmt : _root._stmts)
        {
            try
            {
                checkStmt(stmt, _firstTable);
            }
            catch(SemanticError error)
            {

            }
        }
        if(_hasError)
        {
            System.out.println("Complete Semantic Analysis: Failed.");
            return false;
        }
        else
        {
            System.out.println("Complete Semantic Analysis: Valid!");
            return true;
        }
    }

    /**
     * 输出语义错误
     */
    private void error(int line, String msg) throws SemanticError
    {
        _hasError = true;
        System.out.printf("line %d : Semantic error< %s >\n", line, msg);
        throw new SemanticError();
    }

    private void checkStmt(ASTNode stmt, SymbolTable symbolTable) throws SemanticError
    {
        switch(stmt._type)
        {
            case Const.IFSTMT:
                checkIf((ASTIfStmt) stmt, symbolTable);
                break;
            case Const.BLOCK:
                checkBlock((ASTBlockStmt)stmt, symbolTable);
                break;
            case Const.WHILESTMT:
                checkWhile((ASTWhileStmt)stmt, symbolTable);
                break;
            case Const.ASSIGNSTMT:
                checkAssign((ASTAssignStmt)stmt, symbolTable);
                break;
            case Const.DECLARATION:
                checkVarDecl((ASTDeclaration)stmt, symbolTable);
                break;
            case Const.READSTMT:
                checkRead((ASTReadStmt)stmt, symbolTable);
                break;
            case Const.WRITESTMT:
                checkWrite((ASTWriteStmt)stmt, symbolTable);
                break;
            case Const.BREAKSTMT:
                checkBreak((ASTBreakStmt)stmt);
                break;
            case Const.FORSTMT:
                SymbolTable new_table = new SymbolTable();
                new_table._pre = symbolTable;
                checkFor((ASTForStmt)stmt, new_table);
        }
    }

    private void checkBreak(ASTBreakStmt breakStmt) throws SemanticError
    {
        if(_breakableStmt.isEmpty())
            error(breakStmt._line, "Break is not allowed here");
    }

    private void checkIf(ASTIfStmt ifStmt, SymbolTable symbolTable) throws SemanticError
    {
        try
        {
            checkExpr(ifStmt.get_expr(), symbolTable);
        }
        catch(SemanticError error)
        {

        }
        try
        {
            checkStmt(ifStmt.get_stmt(), symbolTable);
        }
        catch(SemanticError error)
        {

        }
        if(ifStmt.get_elseStmt() != null)
            checkStmt(ifStmt.get_elseStmt(), symbolTable);
    }

    private void checkBlock(ASTBlockStmt block, SymbolTable symbolTable) throws SemanticError
    {
        SymbolTable new_table = new SymbolTable();
        new_table._pre = symbolTable;
        for(ASTNode stmt : block._stmts)
        {
            try
            {
                checkStmt(stmt, new_table);
            }
            catch(SemanticError error)
            {

            }
        }

    }

    private void checkWhile(ASTWhileStmt whileStmt, SymbolTable symbolTable) throws SemanticError
    {
        try
        {
            checkExpr(whileStmt._expr, symbolTable);
        }
        catch(SemanticError error)
        {

        }
        _breakableStmt.push(whileStmt);
        checkStmt(whileStmt._stmt, symbolTable);
        _breakableStmt.pop();
    }

    private void checkFor(ASTForStmt forStmt, SymbolTable symbolTable) throws SemanticError
    {
        if(forStmt._first != null)
        {
            try
            {
                if(forStmt._first._type.equals(Const.DECLARATION))
                    checkVarDecl((ASTDeclaration)forStmt._first, symbolTable);
                else
                    checkAssign((ASTAssignStmt)forStmt._first, symbolTable);
            }
            catch(SemanticError error)
            {

            }
        }

        try
        {
            checkExpr(forStmt._second, symbolTable);
        }
        catch(SemanticError error)
        {

        }

        if(forStmt._third != null)
        {
            try
            {
                checkAssign(forStmt._third, symbolTable);
            }
            catch(SemanticError error)
            {

            }
        }

        _breakableStmt.push(forStmt);
        checkStmt(forStmt._stmt, symbolTable);
        _breakableStmt.pop();
    }

    private void checkAssign(ASTAssignStmt assignStmt, SymbolTable symbolTable) throws SemanticError
    {
        ASTValue id = assignStmt._value;
        String id_name = id._op;
        if(isInSymbolTable(id_name, symbolTable))
        {
            SymbolTable symbolTable_new = getSymbolTable(id_name, symbolTable);
            String type = symbolTable_new.getType(id_name);
            switch(type)
            {
                case Const.INTID:
                    if(!id._type.equals(Const.VALUEID))
                    {
                        error(id._line, id_name + " is not an array identifier");
                        break;
                    }
                    symbolTable_new.setIntIdValue(id_name, (int)getValue(checkExpr(assignStmt._expr, symbolTable)));
                    break;
                case Const.DOUBLEID:
                    if(!id._type.equals(Const.VALUEID))
                    {
                        error(id._line, id_name + " is not an array identifier");
                        break;
                    }
                    symbolTable_new.setDoubleIdValue(id_name, getValue(checkExpr(assignStmt._expr, symbolTable)));
                    break;
                case Const.INTARRAY:
                    if(!id._type.equals(Const.VALUEARRAY))
                    {
                        error(id._line, id_name + " is an array identifier, you must appoint an index");
                        break;
                    }
                    ExprValue index = checkExpr(id._index, symbolTable);
                    if(index._type.equals(Const.INT))
                    {
                        int ind = ((ExprInt)index)._value;
                        int size = symbolTable_new.getIntArraySize(id_name);
                        if(ind > -1 && ind < size)
                            symbolTable_new.setIntArrayValue(id_name, ind, (int)getValue(checkExpr(assignStmt._expr, symbolTable)));
                        else
                            error(id._line, "Array index out of range");
                    }
                    else
                        error(id._line, "Array index should be integer");
                    break;
                case Const.DOUBLEARRAY:
                    if(!id._type.equals(Const.VALUEARRAY))
                    {
                        error(id._line, id_name + " is an array identifier, you must appoint an index");
                        break;
                    }
                    ExprValue index2 = checkExpr(id._index, symbolTable);
                    if(index2._type.equals(Const.INT))
                    {
                        int ind = ((ExprInt)index2)._value;
                        int size = symbolTable_new.getDoubleArraySize(id_name);
                        if(ind > -1 && ind < size)
                            symbolTable_new.setDoubleArrayValue(id_name, ind, getValue(checkExpr(assignStmt._expr, symbolTable)));
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
            error(id._line, "Cannot assign a value to an undefined variable");
        }
    }

    private void checkVarDecl(ASTDeclaration declarationStmt, SymbolTable symbolTable) throws SemanticError
    {
        String type = declarationStmt._declType._type;
        String declarator = declarationStmt._declType._declarator;
        for(ASTValue var : declarationStmt._vars)
        {
            String id_name = var._op;
            if(symbolTable.hasId(id_name))
                error(var._line, "Identifier duplicated");
            else
            {
                if(type.equals(Const.NUMBERDECL))
                {
                    switch(declarator)
                    {
                        case "int":
                            symbolTable.addIntId(id_name);
                            if(var._valueExpr != null)
                                symbolTable.setIntIdValue(id_name, (int)getValue(checkExpr(var._valueExpr, symbolTable)));
                            break;
                        case "double":
                            symbolTable.addDoubleID(id_name);
                            if(var._valueExpr != null)
                                symbolTable.setDoubleIdValue(id_name, getValue(checkExpr(var._valueExpr, symbolTable)));
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
    }

    private void checkRead(ASTReadStmt readStmt, SymbolTable symbolTable) throws SemanticError
    {
        ASTValue id = readStmt._value;
        if(!isInSymbolTable(id._op, symbolTable))
        {
            error(id._line, "Cannot use undefined variable in read statement");
        }
        SymbolTable symbolTable_new = getSymbolTable(id._op, symbolTable);
        String type = symbolTable_new.getType(id._op);
        switch(type)
        {
            case Const.INTID:
            case Const.DOUBLEID:
                break;
            case Const.INTARRAY:
                ExprValue index = checkExpr(id._index, symbolTable);
                if(index._type.equals(Const.INT))
                {
                    int ind = ((ExprInt)index)._value;
                    int size = symbolTable_new.getIntArraySize(id._op);
                    if(ind < 0 || ind >= size)
                        error(id._line, "Array index out of range");
                }
                else
                    error(id._line, "Array index should be integer");
                break;
            case Const.DOUBLEARRAY:
                ExprValue index2 = checkExpr(id._index, symbolTable);
                if(index2._type.equals(Const.INT))
                {
                    int ind = ((ExprInt)index2)._value;
                    int size = symbolTable_new.getDoubleArraySize(id._op);
                    if(ind < 0 || ind >= size)
                        error(id._line, "Array index out of range");
                }
                else
                    error(id._line, "Array index should be integer");
                break;
        }
    }

    private void checkWrite(ASTWriteStmt writeStmt, SymbolTable symbolTable) throws SemanticError
    {
        checkExpr(writeStmt._expr, symbolTable);
    }

    private ExprValue checkExpr(ASTExpr expr, SymbolTable symbolTable) throws SemanticError
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
                if(isInSymbolTable(expr._op, symbolTable))
                {
                    symbolTable = getSymbolTable(expr._op, symbolTable);
                    String type = symbolTable.getType(expr._op);
                    switch(type)
                    {
                        case Const.INTID:
                            return new ExprInt(Const.INT, symbolTable.getIntIdValue(expr._op));
                        case Const.DOUBLEID:
                            return new ExprDouble(Const.DOUBLE, symbolTable.getDoubleIdValue(expr._op));
                        default:
                            error(expr._line, expr._op + " is an array, you must appoint an index");
                            break;
                    }
                }
                else
                    error(expr._line, expr._op + " is undefined");

                break;
            case Const.VALUEARRAY:
                if(isInSymbolTable(expr._op, symbolTable))
                {
                    SymbolTable symbolTable_new = getSymbolTable(expr._op, symbolTable);
                    String type2 = symbolTable_new.getType(expr._op);
                    switch(type2)
                    {
                        case Const.INTARRAY:
                            ExprValue index = checkExpr(((ASTValue)expr)._index, symbolTable);
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
                            ExprValue index2 = checkExpr(((ASTValue)expr)._index, symbolTable);
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
                            break;
                        default:
                            error(expr._line, expr._op + " is not an array");
                    }
                }
                else
                    error(expr._line, expr._op + " is undefined");
                break;
            case Const.UNARYEXPR:
                ExprValue result = checkExpr(expr._left, symbolTable);
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
                ExprValue left = checkExpr(expr._left, symbolTable);
                ExprValue right = checkExpr(expr._right, symbolTable);
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
            case Const.SYNTAXERROR:
                break;
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

    private boolean isInSymbolTable(String id, SymbolTable symbolTable)
    {
        while(symbolTable != null)
        {
            if(symbolTable.hasId(id))
                return true;
            else
                symbolTable = symbolTable._pre;
        }
        return false;
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
