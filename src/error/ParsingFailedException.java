package error;

public class ParsingFailedException extends Exception {
    public ParsingFailedException() {
        super();
    }

    public ParsingFailedException(String msg) {
        super(msg);
    }
}
