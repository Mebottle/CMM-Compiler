package interpreter;

import structure.*;

/**
 * Created by WeiZehao on 17/11/6.
 * 本类用于遍历输出抽象语法树
 */
public class ASTVisitor
{
    RootNode _root;

    public ASTVisitor(RootNode root)
    {
        _root = root;
    }

    public void printTree()
    {
        System.out.println("+---" + _root._type);
        for (ASTNode stmt : _root._stmts)
        {
            visitStmt(stmt);
        }
    }

    private void printTab(int layer)
    {
        for(int i = 1; i < layer; i++)
        {
            System.out.print("\t");
        }
        System.out.print("+---");
    }

    private void visitStmt(ASTNode stmt)
    {
        switch(stmt._type)
        {
            case Const.IFSTMT:
                visitIf((ASTIfStmt) stmt);
                break;
            case Const.BLOCK:
                visitBlock((ASTBlockStmt)stmt);
                break;
            case Const.WHILESTMT:
                visitWhile((ASTWhileStmt)stmt);
                break;
            case Const.BREAKSTMT:
                visitBreak((ASTBreakStmt)stmt);
                break;
            case Const.ASSIGNSTMT:
                visitAssign((ASTAssignStmt)stmt);
                break;
            case Const.DECLARATION:
                visitVarDecl((ASTDeclaration)stmt);
                break;
            case Const.READSTMT:
                visitRead((ASTReadStmt)stmt);
                break;
            case Const.WRITESTMT:
                visitWrite((ASTWriteStmt)stmt);
                break;
            case Const.FORSTMT:
                visitFor((ASTForStmt)stmt);
                break;
        }
    }

    private void visitFor(ASTForStmt forStmt)
    {
        printTab(forStmt._layer);
        System.out.println(forStmt._type);
        if(forStmt._first != null)
        {
            if(forStmt._first._type.equals(Const.DECLARATION))
                visitVarDecl((ASTDeclaration)forStmt._first);
            else
                visitAssign((ASTAssignStmt)forStmt._first);
        }
        visitExpr(forStmt._second);
        if(forStmt._third != null)
        {
            visitAssign(forStmt._third);
        }
        visitStmt(forStmt._stmt);
    }

    private void visitIf(ASTIfStmt ifStmt)
    {
        printTab(ifStmt._layer);
        System.out.println(ifStmt._type);
        visitExpr(ifStmt.get_expr());
        visitStmt(ifStmt.get_stmt());
        if(ifStmt.get_elseStmt() != null)
            visitStmt(ifStmt.get_elseStmt());
    }

    private void visitVarDecl(ASTDeclaration decl)
    {
        printTab(decl._layer);
        System.out.println(decl._type);
        visitType(decl._declType);
        for(ASTValue var : decl._vars)
        {
            visitValue(var, false);
        }
    }

    private void visitType(ASTType type)
    {
        if(type._type.equals(Const.NUMBERDECL))
        {
            ASTNumDecl numDecl = (ASTNumDecl)type;
            printTab(numDecl._layer);
            System.out.println(numDecl._declarator);
        }
        else if(type._type.equals(Const.ARRAYDECL) )
        {
            ASTArrayDecl arrayDecl = (ASTArrayDecl)type;
            if(arrayDecl._declarator != null)
            {
                printTab(arrayDecl._layer);
                System.out.print(arrayDecl._declarator + String.format("[%d]", arrayDecl._size));
            }
            System.out.println();
        }
    }

    private void visitWhile(ASTWhileStmt whileStmt)
    {
        printTab(whileStmt._layer);
        System.out.println(whileStmt._type);
        visitExpr(whileStmt._expr);
        visitStmt(whileStmt._stmt);
    }

    private void visitBreak(ASTBreakStmt breakStmt)
    {
        printTab(breakStmt._layer);
        System.out.println("break");
    }

    private void visitRead(ASTReadStmt readStmt)
    {
        printTab(readStmt._layer);
        System.out.println("read");
        visitValue(readStmt._value, true);
    }

    private void visitWrite(ASTWriteStmt writeStmt)
    {
        printTab(writeStmt._layer);
        System.out.println("write");
        visitExpr(writeStmt._expr);
    }

    private void visitBlock(ASTBlockStmt block)
    {
        printTab(block._layer);
        System.out.println("block");
        for(ASTNode stmt : block._stmts)
            visitStmt(stmt);
    }

    private void visitAssign(ASTAssignStmt assignStmt)
    {
        printTab(assignStmt._layer);
        System.out.println(String.format("op< %s >", assignStmt._op));
        visitValue(assignStmt._value, true);
        visitExpr(assignStmt._expr);
    }

    private void visitExpr(ASTExpr expr)
    {
        printTab(expr._layer);
        switch(expr._op)
        {
            case "+":
            case "-":
            case "*":
            case "/":
            case "<":
            case ">":
            case "=":
            case "!=":
            case ">=":
            case "<=":
            case "==":
                System.out.println(String.format("op< %s >", expr._op));
                break;
            default:
                if(expr._type.equals(Const.VALUEARRAY))
                    System.out.println(expr._op);
                else
                    System.out.println(expr._op);
                break;
        }
        if(expr._left != null)
            visitExpr(expr._left);
        if(expr._right != null)
            visitExpr(expr._right);
    }

    private void visitValue(ASTValue value, boolean mark)
    {
        printTab(value._layer);
        if(value._type.equals(Const.VALUEARRAY) && mark)
        {
            System.out.print(value._op + "[");
            if(value._index != null)
            {
                switch(value._index._type)
                {
                    case Const.INT:
                    case Const.DOUBLE:
                    case Const.BOOL:
                    case Const.VALUEID:
                        System.out.print(value._index._op + "]");
                        break;
                    default:
                        System.out.print(" ]");
                }
            }
            else
                System.out.print(" ]");
            System.out.println();
        }
        else
        {
            if(value._valueExpr != null)
                System.out.println(value._op + "=");
            else
                System.out.println(value._op);
        }
        if(value._valueExpr != null)
            visitExpr(value._valueExpr);
    }
}
