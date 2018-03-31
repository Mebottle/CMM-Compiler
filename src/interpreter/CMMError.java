package interpreter;

/**
 * Created by WeiZehao on 17/10/30.
 * 本类用于定义CMM错误
 */

/*
 * CMM错误类
 */
class CMMError extends Exception
{
    CMMError()
    {
        super();
    }
}

/*
 * 运行时错误类
 */
class RuntimeError extends CMMError
{

}

/*
 * 语义错误类
 */
class SemanticError extends CMMError
{

}

/*
 * 语法错误类
 */
class SyntaxError extends CMMError
{

}

/*
 * 词法错误类
 */
class LexError extends CMMError
{
}
