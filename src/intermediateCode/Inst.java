package intermediateCode;

import java.util.HashMap;
import java.util.List;

public interface Inst {
    int count = 0;

    void toMips();
    List<String> usedReg();
    List<String> getParams();
    Inst generateEquivalentInst(HashMap<String, String> regMap);
    String getResult();
    int num();

    static String getEquivalentReg(HashMap<String, String> regMap, String param) {
        while (regMap.containsKey(param) && param != null && !param.equals(regMap.get(param))) {
            param = regMap.get(param);
        }
        return param;
    }

    static boolean isInt(String param) {
        try {
            Integer.parseInt(param);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean isStackParam(String param) {
        int count = 0;
        for (int i = 0; i < param.length(); i++) {
            if (param.charAt(i) == '_') {
                count++;
            }
        }
        return param.charAt(0)=='%' && count > 0;
    }

    static boolean isGlobalParam(String param) {
        return param.charAt(0) == '@';
    }

    static boolean isTempParam(String param) {
        return !isInt(param) && !isGlobalParam(param) && !isStackParam(param);
    }

    static boolean isImmediate(String param) {
        try{
            Short.parseShort(param);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
