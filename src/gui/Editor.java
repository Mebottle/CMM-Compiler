package gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Editor extends JTextPane implements DocumentListener {
    private StyleContext context;
    private HighLight highlight = new HighLight();

    public void paint(Graphics g) {
        super.paint(g);
    }

    public Editor() {
        setDocument(new DefaultStyledDocument());
        getDocument().addDocumentListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
//        update();
    }

    private void update() {
        context = new StyleContext();
        StyledDocument oDoc = getStyledDocument();
        StyledDocument nDoc = new DefaultStyledDocument();
        try {
            String text = oDoc.getText(0, oDoc.getLength());
            nDoc.insertString(0, text, null);

            highlight.markStyle(text, nDoc);
            oDoc.removeDocumentListener(this);
            nDoc.addDocumentListener(this);

            int off = getCaretPosition();
            setDocument(nDoc);
            setStyledDocument(nDoc);
            DocumentListener[] listeners = ((DefaultStyledDocument) oDoc).getDocumentListeners();
            for (int i = 0; i < listeners.length; i++) {
                nDoc.addDocumentListener(listeners[i]);
            }
            setCaretPosition(off);
        } catch (BadLocationException | IOException | InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            oDoc = null;
        }
    }
}