package interpreter;

import gui.GUIPrintStream;
import javax.swing.*;

class CMMEntry
{
    void start(String path, JTextArea consoleArea, JTextArea analyseArea)
    {
        CMMParser cmmParser = new CMMParser(path);
        ASTVisitor astVisitor;
        try
        {
            setSysOut(SYSOUT.CONSOLE, consoleArea, analyseArea);
            RootNode root = cmmParser.start();

            if(root != null)
            {
                setSysOut(SYSOUT.ANALYSE, consoleArea, analyseArea);
                astVisitor = new ASTVisitor(root);
                astVisitor.printTree();

                setSysOut(SYSOUT.CONSOLE, consoleArea, analyseArea);
                CMMSemanticChecker checker = new CMMSemanticChecker(root);
                boolean result = checker.start();

                if(result)
                {
                    System.out.println("运行结果如下：");
                    CMMSemanticAnalyser analyser = new CMMSemanticAnalyser(root);
                    analyser.start();
                }
            }
        }
        catch (CMMError cmmError)
        {

        }
        catch (Exception e)
        {

        }
    }

    private enum SYSOUT {
        CONSOLE,
        ANALYSE,
    }

    private static void setSysOut(SYSOUT type, JTextArea consoleArea, JTextArea analyseArea) throws Exception {
        if (type == SYSOUT.CONSOLE) {
            System.setOut(new GUIPrintStream(System.out, consoleArea));
        } else if (type == SYSOUT.ANALYSE) {
            System.setOut(new GUIPrintStream(System.out, analyseArea));
        } else {
            throw new Exception("SYSOUT TYPE ERROR!");
        }
    }
}
