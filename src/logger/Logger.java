package logger;

import java.io.File;
import java.io.FileOutputStream;

public class Logger {
    private static boolean open;
    private static FileOutputStream outStream;
    private static int depth;
    public static void open(){
        try {
            open = true;
            File outFile = new File("log.txt");
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            outStream = new FileOutputStream(outFile);
            depth = 0;
        } catch (Exception e) {
            System.out.println("在初始化日志模块中发生了错误");
        }
    }
    public static void write(String string){
        if (!open) {
            return;
        }
        try {
            if (string.charAt(0) == 's') {
                for (int i = 0; i < depth; i++) {
                    outStream.write('\t');
                }
                outStream.write(string.substring(1).getBytes());
                outStream.write('\n');
                depth++;
            } else if (string.charAt(0) == 'e') {
                depth--;
                for (int i = 0; i < depth; i++) {
                    outStream.write('\t');
                }
                outStream.write(string.substring(1).getBytes());
                outStream.write('\n');
            } else {
                for (int i = 0; i < depth; i++) {
                    outStream.write('\t');
                }
                outStream.write(string.getBytes());
                outStream.write('\n');
            }
        } catch (Exception e) {
            System.out.println("在写入日志时发生了错误");
        }
    }
}