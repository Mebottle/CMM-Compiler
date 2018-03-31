package interpreter;

import structure.Const;
import java.util.ArrayList;

/**
 * Created by WeiZehao on 17/10/22.
 * This class is to define the nodes of AST
 */
abstract class ASTNode
{
    String _type;
    int _layer;
    int _line;

    public ASTNode(String type, int line, int layer)
    {
        _type = type;
        _line = line;
        _layer = layer;
    }
}

class RootNode extends ASTNode
{
    ArrayList<ASTNode> _stmts;

    public RootNode(String type, int line, int layer)
    {
        super(type, line, layer);
        _stmts = new ArrayList<>();
    }

    public void addStmt(ASTNode stmt)
    {
        _stmts.add(stmt);
    }
}

class ASTForStmt extends ASTNode
{
    ASTNode _first;
    ASTExpr _second;
    ASTAssignStmt _third;
    ASTNode _stmt;

    public ASTForStmt(String type, int line, int layer)
    {
        super(type, line, layer);
    }
}

class ASTExpr extends ASTNode
{
    String _op;
    ASTExpr _left;
    ASTExpr _right;

    public ASTExpr(String type, int line, int layer)
    {
        super(type, line, layer);
    }

    public void set_op(String op)
    {
        _op = op;
    }

    public void set_left(ASTExpr left)
    {
        _left = left;
    }

    public void set_right(ASTExpr right)
    {
        _right = right;
    }

    public void add_layer()
    {
        _layer++;
        if(_left != null)
            _left.add_layer();
        if(_right != null)
            _right.add_layer();
    }
}

class ASTValue extends ASTExpr
{
    ASTExpr _index;
    ASTExpr _valueExpr;

    public ASTValue(String type, int line, int layer)
    {
        super(type, line, layer);
    }

    public ASTValue(String type, int line, int layer, String op)
    {
        super(type, line, layer);
        set_op(op);
    }

    public ASTValue(String type, int line, int layer, String op, ASTExpr index)
    {
        super(type, line, layer);
        set_op(op);
        _index = index;
    }
}

class ASTConstant extends ASTExpr
{
    public ASTConstant(String type, int line, int layer, String op)
    {
        super(type, line, layer);
        set_op(op);
    }
}

class ASTDeclaration extends ASTNode
{
    ASTType _declType;
    ArrayList<ASTValue> _vars;

    public ASTDeclaration(int line, int layer)
    {
        super(Const.DECLARATION, line, layer);
    }

    public void set_declType(ASTType type)
    {
        _declType = type;
    }

    public void set_vars(ArrayList<ASTValue> vars)
    {
        _vars = vars;
    }
}

abstract class ASTType extends ASTNode
{
    String _declarator;

    public ASTType(String type, int line, int layer)
    {
        super(type, line, layer);
    }
}

class ASTNumDecl extends ASTType
{
    public ASTNumDecl(int line, int layer)
    {
        super(Const.NUMBERDECL, line, layer);
    }
}

class ASTArrayDecl extends ASTType
{
    int _size;

    public ASTArrayDecl(int line, int layer)
    {
        super(Const.ARRAYDECL, line, layer);
    }

    public void set_size(int size)
    {
        _size = size;
    }
}

class ASTWhileStmt extends ASTNode
{
    ASTExpr _expr;
    ASTNode _stmt;

    public ASTWhileStmt(String type, int line, int layer)
    {
        super(type, line, layer);
    }

    public void set_expr(ASTExpr expr)
    {
        _expr = expr;
    }

    public void set_stmt(ASTNode stmt)
    {
        _stmt = stmt;
    }
}

class ASTIfStmt extends ASTNode
{
    ASTExpr _expr;
    ASTNode _stmt;
    ASTNode _elseStmt;

    public ASTIfStmt(String type, int line, int layer)
    {
        super(type, line, layer);
    }

    public ASTExpr get_expr()
    {
        return _expr;
    }

    public ASTNode get_stmt()
    {
        return _stmt;
    }

    public ASTNode get_elseStmt()
    {
        return _elseStmt;
    }

    public void set_expr(ASTExpr expr)
    {
        _expr = expr;
    }

    public void set_stmt(ASTNode stmt)
    {
        _stmt = stmt;
    }

    public void set_elseStmt(ASTNode elseStmt)
    {
        _elseStmt = elseStmt;
    }
}

class ASTBreakStmt extends ASTNode
{
    public ASTBreakStmt(String type, int line, int layer)
    {
        super(type, line, layer);
    }
}

class ASTBlockStmt extends ASTNode
{
    ArrayList<ASTNode> _stmts;

    public ASTBlockStmt(String type, int line, int layer)
    {
        super(type, line, layer);
        _stmts = new ArrayList<>();
    }

    public void addStmt(ASTNode stmt)
    {
        _stmts.add(stmt);
    }
}

class ASTAssignStmt extends ASTNode
{
    String _op;
    ASTValue _value;
    ASTExpr _expr;

    public ASTAssignStmt(String type, int line, int layer)
    {
        super(type, line, layer);
        _op = "=";
    }

    public void set_value(ASTValue value)
    {
        _value = value;
    }

    public void set_expr(ASTExpr expr)
    {
        _expr = expr;
    }
}

class ASTReadStmt extends ASTNode
{
    ASTValue _value;

    public ASTReadStmt(String type, int line, int layer)
    {
        super(type, line, layer);
    }

    public void set_value(ASTValue value)
    {
        _value = value;
    }
}

class ASTWriteStmt extends ASTNode
{
    ASTExpr _expr;

    public ASTWriteStmt(String type, int line, int layer)
    {
        super(type, line, layer);
    }

    public void set_expr(ASTExpr expr)
    {
        _expr = expr;
    }
}

class ASTError extends ASTNode
{
    public ASTError(String type, int line, int layer)
    {
        super(type, line, layer);
    }
}
