package de.lmu.bio.calcium.io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

public class JSONStringWriter extends StringWriter {

    private int indent = 0;

    @Override
    public void write(int c) {
        if (((char)c) == '[' || ((char)c) == '{') {
            super.write(c);
            super.write('\n');
            indent++;
            writeIndentation();
        } else if (((char)c) == ',') {
            super.write(c);
            super.write('\n');
            writeIndentation();
        } else if (((char)c) == ']' || ((char)c) == '}') {
            super.write('\n');
            indent--;
            writeIndentation();
            super.write(c);
        } else if (((char)c) == ':') {
            super.write(c);
            super.write(" ");
        } else {
            super.write(c);
        }

    }

    private void writeIndentation() {
        for (int i = 0; i < indent; i++) {
            super.write("   ");
        }
    }
}