package interpreter;

import structure.Token;
import structure.Const;
import java.util.ArrayList;

/**
 * Created by WeiZehao on 17/10/22.
 * 本类是语法分析器
 */

public class CMMParser
{
    private Token _curToken;
    private Token _lastToken;
    private CMMLexer _lexer;
    private RootNode _root;
    private final static int EQUALITY = 1;
    private final static int INEQUALITY = 2;
    private final static int ADDSUB = 3;
    private final static int MULDIV = 4;
    private boolean _hasError = false;

    public CMMParser(String path)
    {
        _lexer = new CMMLexer(path);
    }

    /**
     * 获取下一个token
     */
    private void nextToken() throws LexError
    {
        _lastToken = _curToken;
        _curToken = _lexer.getToken();
        if(_curToken.getType().equals("ERROR"))
        {

            System.out.printf("line %d, position %d : %s\n",
                    _curToken.getRow(), _curToken.getCol(), _curToken.getContent());
            treatWithLexError();
        }
    }

    /**
     * 处理词法错误
     */
    private void treatWithLexError() throws LexError
    {
        _hasError = true;
        Token token;
        while(true)
        {
            token = _lexer.getToken();
            if(token.getType().equals("EOF"))
                break;
            if(token.getType().equals("ERROR"))
                System.out.printf("line %d, position %d : %s\n",token.getRow(),token.getCol(),token.getContent());
        }
        _lexer.close();
        throw new LexError();
    }

    /**
     * 匹配token
     */
    private void match(String type) throws CMMError
    {
        if(_curToken.getType().equals(type))
            nextToken();
        else if(_curToken.getContent().equals(type))
            nextToken();
        else
            error(String.format("expect: %s", type));
    }

    /**
     * 输出语法错误
     */
    private void error(String msg) throws SyntaxError
    {
        if(msg.equals("expect: ;") || msg.equals("Declaration is not allowed here"))
            System.out.printf("line %d, position %d : Syntax error< %s >\n",
                    _lastToken.getRow(), _lastToken.getCol(), msg);
        else
            System.out.printf("line %d, position %d : Syntax error< %s >\n",
                    _curToken.getRow(), _curToken.getCol(), msg);
        throw new SyntaxError();
    }

    /**
     * 处理语法错误
     */
    private void treatCMMError()
    {
        _hasError = true;
        while(true)
        {
            if(_curToken.getContent().equals("if")
                    || _curToken.getContent().equals("int")
                    || _curToken.getContent().equals("double")
                    || _curToken.getContent().equals("while")
                    || _curToken.getContent().equals("break")
                    || _curToken.getContent().equals("ID")
                    || _curToken.getContent().equals("read")
                    || _curToken.getContent().equals("write")
                    || _curToken.getContent().equals("{")
                    || _curToken.getContent().equals("}")
                    || _curToken.getContent().equals("EOF"))
                break;
            try
            {
                nextToken();
            }
            catch (LexError lexError)
            {
                treatCMMError();
            }
        }
    }

    private void treatCMMError2()
    {
        _hasError = true;
        while(true)
        {
            if(_curToken.getContent().equals("if")
                    || _curToken.getContent().equals("int")
                    || _curToken.getContent().equals("double")
                    || _curToken.getContent().equals("while")
                    || _curToken.getContent().equals("break")
                    || _curToken.getContent().equals("ID")
                    || _curToken.getContent().equals("read")
                    || _curToken.getContent().equals("write")
                    || _curToken.getContent().equals("{")
                    || _curToken.getContent().equals("EOF"))
                break;
            try
            {
                nextToken();
            }
            catch (LexError lexError)
            {
                treatCMMError();
            }
        }
    }

    /**
     * Program -> Stmt {Stmt}
     * 该函数会捕获除空文件以外的错误并进行错误恢复
     */
    public RootNode start() throws CMMError
    {
        try
        {
            nextToken();
        }
        catch (LexError lexError)
        {
            treatCMMError();
        }
        _root = new RootNode(Const.ROOT, _curToken.getRow(), 1);
        if(_curToken.getContent().equals("EOF"))
            error("The program should not be empty");
        while(!(_curToken.getContent().equals("EOF")))
        {
            try
            {
                _root.addStmt(Stmt(2));
            }
            catch (CMMError cmmError)
            {
                treatCMMError();
                if(_curToken.getContent().equals("}"))
                    treatCMMError2();
            }
        }
        if(!_hasError)
        {
            System.out.println("Complete Syntax Analysis : Valid!");
            return _root;
        }
        else
        {
            System.out.println("Complete Syntax Analysis: Failed.");
            return null;
        }
    }

    /**
     * Stmt -> VarDecl | IfStmt | WhileStmt | BreakStmt
     *       | AssignStmt | ReadStmt | WriteStmt | StmtBlock
     */
    private ASTNode Stmt(int layer) throws CMMError
    {
        if(_curToken.getContent().equals("if"))
            return IfStmt(layer);
        else if(_curToken.getContent().equals("int")
                || _curToken.getContent().equals("double"))
            return VarDecl(layer);
        else if(_curToken.getContent().equals("while"))
            return WhileStmt(layer);
        else if(_curToken.getContent().equals("for"))
            return ForStmt(layer);
        else if(_curToken.getContent().equals("break"))
            return BreakStmt(layer);
        else if(_curToken.getType().equals("ID"))
            return AssignStmt(layer, true);
        else if(_curToken.getContent().equals("read"))
            return ReadStmt(layer);
        else if(_curToken.getContent().equals("write"))
            return WriteStmt(layer);
        else if(_curToken.getContent().equals("{"))
            return StmtBlock(layer);
        else
        {
            error("unexpected: " + _curToken.getContent());
            return new ASTError(Const.SYNTAXERROR, _curToken.getRow(), layer);
        }
    }

    /**
     * ForSubStmt -> IfStmt | WhileStmt | BreakStmt | AssignStmt | ReadStmt | WriteStmt | Block | ForStmt
     * ForStmt -> for ( VarDecl | AssignStmt | ε ; Expr ; Value = Expr | ε ) ForSubStmt
     */
    private ASTForStmt ForStmt(int layer) throws CMMError
    {
        ASTForStmt forStmt = new ASTForStmt(Const.FORSTMT, _curToken.getRow(), layer);

        match("for");
        match("(");
        if(_curToken.getType().equals("ID"))
        {
            forStmt._first = AssignStmt(layer+1, true);
        }
        else if(_curToken.getContent().equals("int") || _curToken.getContent().equals("double"))
        {
            forStmt._first = VarDecl(layer+1);
        }
        else
            match(";");

        forStmt._second = Expr(0, layer+1);
        match(";");

        if(_curToken.getContent().equals(")"))
            match(")");
        else
        {
            forStmt._third = AssignStmt(layer+1, false);
            match(")");
        }

        ASTNode stmt;
        try
        {
           stmt = Stmt(layer+1);
        }
        catch(SyntaxError error)
        {
            treatCMMError();
            stmt = Stmt(layer+1);
        }
        if(stmt._type.equals(Const.DECLARATION))
        {
            error("Declaration is not allowed here");
            stmt._type = Const.SYNTAXERROR;
        }
        forStmt._stmt = stmt;

        return forStmt;
    }

    /**
     * IfSubStmt -> IfStmt | WhileStmt | AssignStmt | ReadStmt | WriteStmt | Block | BreakStmt
     * IfStmt -> if ( Expr ) IfSubStmt [ else IfSubStmt ]
     */
    private ASTIfStmt IfStmt(int layer) throws CMMError
    {
        ASTIfStmt ifStmt = new ASTIfStmt(Const.IFSTMT, _curToken.getRow(), layer);

        match("if");
        match("(");
        ifStmt.set_expr(Expr(0, layer+1));
        match(")");
        ASTNode stmt;
        try
        {
             stmt = Stmt(layer+1);
        }
        catch(CMMError error)
        {
            treatCMMError();
            stmt = Stmt(layer+1);
        }
        if(stmt._type.equals(Const.DECLARATION))
        {
            error("Declaration is not allowed here");
            stmt._type = Const.SYNTAXERROR;
        }
        ifStmt.set_stmt(stmt);
        if(_curToken.getContent().equals("else"))
        {
            match("else");
            ASTNode stmt2;
            try
            {
                stmt2 = Stmt(layer+1);
            }
            catch(CMMError error)
            {
                treatCMMError();
                stmt2 = Stmt(layer+1);
            }
            if(stmt2._type.equals(Const.DECLARATION))
            {
                error("Declaration is not allowed here");
                stmt2._type = Const.SYNTAXERROR;
            }
            ifStmt.set_elseStmt(stmt2);
        }

        return ifStmt;
    }

    /**
     * StmtBlock -> { {Stmt} }
     * 错误恢复：如果block中某语句发生语法错误，跳过该语句，分析下一条语句
     */
    private ASTBlockStmt StmtBlock(int layer) throws CMMError
    {
        ASTBlockStmt blockStmt = new ASTBlockStmt(Const.BLOCK, _curToken.getRow(), layer);

        match("{");
        while(!(_curToken.getContent().equals("}")))
        {
            if(_curToken.getContent().equals("EOF"))
            {
                error("Block error, expect: }");
            }
            try
            {
                blockStmt.addStmt(Stmt(layer+1));
            }
            catch(CMMError error)
            {
                treatCMMError();
                if(_curToken.getContent().equals("}"))
                {
                    match("}");
                    treatCMMError();
                    return blockStmt;
                }
            }
        }
        match("}");

        return blockStmt;
    }

    /**
     * VarDecl -> Type VarDecl2 ;
     * Type -> int | double
     * VarDecl2 -> VarList1 | Type2 VarList2
     */
    private ASTDeclaration VarDecl(int layer) throws CMMError
    {
        ASTDeclaration declaration = new ASTDeclaration(_curToken.getRow(), layer);
        String declarator = _curToken.getContent();
        declaration._line = _curToken.getRow();
        match(declarator);

        ASTType type = Type2(layer+1);
        if(type == null)
        {
            type = new ASTNumDecl(_curToken.getRow(), (layer+1));
            declaration.set_declType(type);
            declaration.set_vars(VarList1(layer+1));
        }
        else
        {
            declaration.set_declType(type);
            declaration.set_vars(VarList2(layer+1));
        }
        type._declarator = declarator;

        match(";");

        return declaration;
    }

    /**
     * Type2 -> [ integer ] Type2 | ε
     */
    private ASTArrayDecl Type2(int layer) throws CMMError
    {
        if(_curToken.getContent().equals("["))
        {
            match("[");
            ASTArrayDecl arrayDecl = new ASTArrayDecl(_curToken.getRow(), layer);
            String content = _curToken.getContent();

            match("INTEGER");
            int size = Integer.parseInt(content);
            arrayDecl.set_size(size);
            match("]");

            return arrayDecl;
        }
        else if(_curToken.getType().equals("ID"))
        {
            return null;
        }
        else
        {
            error("Declaration error");
            return null;
        }
    }

    /**
     * VarList1 -> ident [ = Expr ] { , ident [ = Expr ]}
     */
    private ArrayList<ASTValue> VarList1(int layer) throws CMMError
    {
        ArrayList<ASTValue> vars = new ArrayList<>();
        if(_curToken.getType().equals("ID"))
        {
            ASTValue ident = new ASTValue(Const.VALUEID, _curToken.getRow(), layer, _curToken.getContent());
            match("ID");
            if(_curToken.getContent().equals("="))
            {
                match("=");
                ident._valueExpr = Expr(0, layer+1);
            }
            vars.add(ident);
            while(_curToken.getContent().equals(","))
            {
                match(",");
                String content = _curToken.getContent();
                int line = _curToken.getRow();
                match("ID");
                ASTValue ident2 = new ASTValue(Const.VALUEID, line, layer, content);
                if(_curToken.getContent().equals("="))
                {
                    match("=");
                    ident2._valueExpr = Expr(0, layer+1);
                }
                vars.add(ident2);
            }
        }
        else
        {
            error("Declaration error");
        }

        return vars;
    }

    /**
     * VarList2 -> ident {, ident}
     */
    private ArrayList<ASTValue> VarList2(int layer) throws CMMError
    {
        ArrayList<ASTValue> vars = new ArrayList<>();
        if(_curToken.getType().equals("ID"))
        {
            ASTValue ident = new ASTValue(Const.VALUEARRAY, _curToken.getRow(), layer, _curToken.getContent());
            match("ID");
            vars.add(ident);
            while(_curToken.getContent().equals(","))
            {
                match(",");
                String content = _curToken.getContent();
                int line = _curToken.getRow();
                match("ID");
                ASTValue ident2 = new ASTValue(Const.VALUEARRAY, line, layer, content);
                vars.add(ident2);
            }
        }
        else
        {
            error("Declaration error");
        }

        return vars;
    }

    /**
     * WhileSubStmt -> IfStmt | WhileStmt | BreakStmt | AssignStmt | ReadStmt | WriteStmt | Block
     * WhileStmt -> while ( Expr ) WhileSubStmt
     */
    private ASTWhileStmt WhileStmt(int layer) throws CMMError
    {
        ASTWhileStmt whileStmt = new ASTWhileStmt(Const.WHILESTMT, _curToken.getRow(), layer);

        match("while");
        match("(");
        whileStmt.set_expr(Expr(0, layer+1));
        match(")");
        ASTNode stmt;
        try
        {
            stmt = Stmt(layer+1);
        }
        catch(CMMError error)
        {
            treatCMMError();
            stmt = Stmt(layer+1);
        }
        if(stmt._type.equals(Const.DECLARATION))
            error("Declaration is not allowed here");
        whileStmt.set_stmt(stmt);

        return whileStmt;
    }

    /**
     * BreakStmt -> break ;
     */
    private ASTBreakStmt BreakStmt(int layer) throws CMMError
    {
        ASTBreakStmt breakStmt = new ASTBreakStmt(Const.BREAKSTMT, _curToken.getRow(), layer);

        match("break");
        match(";");

        return breakStmt;
    }

    /**
     * ReadStmt -> ( Value ) ;
     */
    private ASTReadStmt ReadStmt(int layer) throws CMMError
    {
        ASTReadStmt readStmt = new ASTReadStmt(Const.READSTMT, _curToken.getRow(), layer);

        match("read");
        match("(");
        readStmt.set_value(Value(layer+1));
        match(")");
        match(";");

        return readStmt;
    }

    /**
     * WriteStmt -> write ( Expr )
     */
    private ASTWriteStmt WriteStmt(int layer) throws CMMError
    {
        ASTWriteStmt writeStmt = new ASTWriteStmt(Const.WRITESTMT, _curToken.getRow(), layer);

        match("write");
        match("(");
        writeStmt.set_expr(Expr(0, layer+1));
        match(")");
        match(";");

        return writeStmt;
    }

    /**
     * AssignStmt -> Value = Expr ;
     */
    private ASTAssignStmt AssignStmt(int layer, boolean mark) throws CMMError
    {
        ASTAssignStmt assignStmt = new ASTAssignStmt(Const.ASSIGNSTMT, _curToken.getRow(), layer);

        assignStmt.set_value(Value(layer+1));
        match("=");
        assignStmt.set_expr(Expr(0, layer+1));
        if(mark)
            match(";");

        return assignStmt;
    }

    /**
     * Value -> ident | ident [ Expr ]
     */
    private ASTValue Value(int layer) throws CMMError
    {
        ASTValue astValue;

        if(_curToken.getType().equals("ID"))
        {
            String ident = _curToken.getContent();
            int line = _curToken.getRow();
            match("ID");
            if(_curToken.getContent().equals("["))
            {
                match("[");
                ASTExpr index = Expr(0, layer+1);
                match("]");
                astValue = new ASTValue(Const.VALUEARRAY, line, layer, ident, index);
            }
            else
            {
                astValue = new ASTValue(Const.VALUEID, line, layer, ident);
            }
        }
        else
        {
            astValue = new ASTValue(Const.SYNTAXERROR, _curToken.getRow(), layer);
            error("value error, expect identifier");
        }

        return astValue;
    }

    /**
     * Constant -> integer | float | true | false
     */
    private ASTConstant Constant(int layer) throws CMMError
    {
        ASTConstant astConstant = null;

        if(_curToken.getType().equals("INTEGER"))
        {
            astConstant = new ASTConstant(Const.INT, _curToken.getRow(), layer, _curToken.getContent());
            match("INTEGER");
        }
        else if(_curToken.getType().equals("FLOAT"))
        {
            astConstant = new ASTConstant(Const.DOUBLE, _curToken.getRow(), layer, _curToken.getContent());
            match("FLOAT");
        }
        else if(_curToken.getContent().equals("true"))
        {
            astConstant = new ASTConstant(Const.BOOL, _curToken.getRow(), layer, _curToken.getContent());
            match("true");
        }
        else if(_curToken.getContent().equals("false"))
        {
            astConstant = new ASTConstant(Const.BOOL, _curToken.getRow(), layer, _curToken.getContent());
            match("false");
        }
        else
        {
            error("constant error");
        }

        return astConstant;
    }

    /**
     * Expr(n) -> Expr(n) OP(n) Expr(n+1) | Expr(n+1)
     * 以上是一个不同优先级的表达式的产生式的通式
     * (n)代表表达式或操作符的优先级为n
     * @param priority 优先级
     */
    private ASTExpr Expr(int priority, int layer) throws CMMError
    {
        ASTExpr astExpr;
        ASTExpr expr_p;

        expr_p = UnaryExpr(layer);

        int new_prio;
        while(_curToken.getType().equals("OP")
                && ((new_prio = get_priority(_curToken.getContent())) >= priority))
        {
            astExpr = new ASTExpr(Const.EXPRESSION, _curToken.getRow(), layer);
            astExpr.set_op(_curToken.getContent());
            match("OP");
            expr_p.add_layer();
            astExpr.set_left(expr_p);
            astExpr.set_right(Expr(new_prio+1, layer+1));

            expr_p = astExpr;
        }

        return expr_p;
    }

    /**
     * 处理一元表达式：-Expr | Constant | ident
     */
    private ASTExpr UnaryExpr(int layer) throws CMMError
    {
        ASTExpr expr;
        if(_curToken.getContent().equals("-"))
        {
            expr = new ASTExpr(Const.UNARYEXPR, _curToken.getRow(), layer);
            expr.set_op("-");
            match("-");
            expr.set_left(UnaryExpr(layer+1));
        }
        else
        {
            expr = MainExpr(layer);
        }

        return expr;
    }

    private ASTExpr MainExpr(int layer) throws CMMError
    {
        ASTExpr expr;
        if(_curToken.getType().equals("ID"))
        {
            expr = Value(layer);
        }
        else if(_curToken.getType().equals("INTEGER")
                | _curToken.getType().equals("FLOAT")
                | _curToken.getContent().equals("true")
                | _curToken.getContent().equals("false"))
        {
            expr = Constant(layer);
        }
        else if(_curToken.getContent().equals("("))
        {
            match("(");
            expr = Expr(0, layer);
            match(")");
        }
        else
        {
            expr = new ASTExpr(Const.SYNTAXERROR, _curToken.getRow(), layer);
            error("expression error");
        }

        return expr;
    }

    /**
     * 获得当前操作符的优先级
     * @param op 操作符
     * @return 优先级
     */
    private int get_priority(String op)
    {
        if(op.equals("!=") | op.equals("=="))
            return EQUALITY;
        else if(op.equals(">") | op.equals(">=")
                | op.equals("<") | op.equals("<="))
            return INEQUALITY;
        else if(op.equals("+") | op.equals("-"))
            return ADDSUB;
        else
            return MULDIV;
    }

}
