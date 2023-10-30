package Writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Output {
    private static BufferedWriter writer = null;

    public static void output(String line){
        try {
            if (writer == null) {
                writer = new BufferedWriter(new FileWriter("mips.txt"));
            }
            writer.write(line + '\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
