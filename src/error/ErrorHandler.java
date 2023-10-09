package error;

public class ErrorHandler {
    public static void putError(int lineNum, String reason) {
        System.out.printf("%d行发生异常，原因是：%s", lineNum, reason);
    }
}
