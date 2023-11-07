package intermediateCode;

public interface Inst {
    public void toMips();

    public static boolean isInt(String param) {
        try {
            Integer.parseInt(param);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
