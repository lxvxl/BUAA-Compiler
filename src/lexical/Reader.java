package lexical;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Reader {
    private String content;
    private int p = 0;
    private int line = 1;

    public Reader(String pathName) {
        Path path = Paths.get(pathName);
        try {
            byte[] bytes = Files.readAllBytes(path);
            content = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public char getChar() throws StringIndexOutOfBoundsException {
        if (p == content.length()) {
            return '\0';
        }
        char c = content.charAt(p);
        if (c == '\n') {
            line++;
        }
        p++;
        return c;
    }

    public void unGetChar() {
        p--;
        if (content.charAt(p) == '\n') {
            line--;
        }
    }
}
