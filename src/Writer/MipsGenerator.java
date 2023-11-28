package Writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MipsGenerator {
    private static final List<String> insts = new ArrayList<>();

    public static void addInst(String line) {
        insts.add(line);
    }

    public static void addLabel(String label) {
        if (insts.isEmpty()) {
            insts.add(label);
            return;
        }
        String preInst = insts.get(insts.size() - 2).trim();
        if (preInst.charAt(0) == 'j' && preInst.charAt(1) == ' ') {
            if (preInst.split(" ")[1].equals(label.substring(0, label.length() - 1))) {
                insts.remove(insts.size() - 2);
            }
        }
        insts.add(label);
    }

    public static void outputInsts() {
        try {
            Writer writer = new BufferedWriter(new FileWriter("mips.txt"));
            for (String inst : insts) {
                writer.write(inst + '\n');
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
