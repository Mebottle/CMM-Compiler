package gui;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class OffsetGetter {
    private File sourceFile;
    private Map<Integer,Integer> LineOffset = new HashMap<>();

    // 通过行号和列号计算Offset
    public int getOffset(int rol, int col){
        return LineOffset.get(rol)+col-1;
    }

    // 计算Offset
    private void calOffsets() throws IOException {
        FileReader in = new FileReader(sourceFile);
        LineNumberReader reader = new LineNumberReader(in);
        String s = reader.readLine();
        int lines = 0;
        int offset = 0;
        while (s != null) {
            lines++;
            LineOffset.put((Integer) lines,offset);
            offset+=s.length()+1;
            s = reader.readLine();
        }
        reader.close();
        in.close();
    }

    public OffsetGetter(String filename) throws IOException {
        sourceFile = new File(filename);
        calOffsets();
    }
}
