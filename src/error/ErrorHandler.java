package error;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.TreeSet;

public class ErrorHandler {

    private record Error(int lineNum, char type) {
        @Override
        public String toString() {
            return lineNum + " " + type;
        }
    }

    private static final TreeSet<Error> errors = new TreeSet<>(new Comparator<Error>() {
        @Override
        public int compare(Error o1, Error o2) {
            return o1.lineNum - o2.lineNum;
        }
    });

    public static void putError(int lineNum, String reason) {
        System.out.printf("%d行发生异常，原因是：%s", lineNum, reason);
    }

    public static void putError(int lineNum, char type) {
        errors.add(new Error(lineNum, type));
    }

    public static void outputErrors(BufferedWriter writer) throws IOException {
        for (Error error : errors) {
            writer.write(error.toString());
            writer.write('\n');
        }
    }
}
