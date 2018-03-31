package gui;

import interpreter.CMMFrame;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.io.InputStream;


public class GUIInputStream extends InputStream {
    private JTextArea tf;
    private String str = null;
    private int pos = 0;

    public GUIInputStream(JTextArea jtf) {
        tf = jtf;
    }

    //每次按下Enter都会激活readline，然后调用这里

    @Override
    public int read() {
        if (pos == 0) {
            str = null;
        }
        //input到末尾之后,返回EOF
        if (str != null && pos == str.length()) {
            str = null;
            return -1;
        }
        //没有任何input,线程挂起，等待输入。输入完毕后，等待主线程激活
        while (str == null || pos >= str.length()) {
            try {
                synchronized (CMMFrame.object) {
                    CMMFrame.object.wait();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return str.charAt(pos++);
    }

    public void method() throws BadLocationException {
        int endpos = tf.getCaret().getMark();
        int startpos = tf.getText().substring(0, endpos - 1).lastIndexOf('\n') + 1;
        str = tf.getText(startpos, endpos - startpos);
        pos = 0;
    }

}
