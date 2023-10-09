import lexical.LexicalManager;

import java.io.BufferedWriter;
import java.io.FileWriter;

import syntaxAnalyser.TreeNode;
import syntaxAnalyser.nodes.CompUnit;

public class Compiler {
    public static void main(String[] args) throws Exception {
        //Reader reader = new Reader("testfile.txt");
        //LexicalAnalyser lexicalAnalyser = new LexicalAnalyser(reader);
        //Logger.open();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("output.txt"));
        LexicalManager lm = new LexicalManager("testfile.txt");
        TreeNode root = CompUnit.parse(lm);
        root.compile(bufferedWriter);
        bufferedWriter.close();
    }

}
