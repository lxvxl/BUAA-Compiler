package intermediateCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Inst {
    int count = 0;

    void toMips();
    List<String> usedReg();
    List<String> getParams();
    Inst generateEquivalentInst(HashMap<String, String> regMap);
    String getResult();
    int num();
    Inst replace(int n, String funcName);

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
        return param.matches("^%\\d+_.*$");
    }

    static boolean isGlobalParam(String param) {
        return param != null && param.charAt(0) == '@';
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

    static String transformParam(String param, int n, String funcName) {
        if (param == null) {
            return null;
        } else if (isStackParam(param)) {
            return param + '@' + funcName;
        } else if (isTempParam(param)) {
            return param + '@' + funcName + '@' + n;
        } else {
            return param;
        }
    }

    static String transformLabel(String label, int n, String funcName) {
        return label + '@' + funcName + '@' + n;
    }

    static String transformArrName(String arrName, int n, String funcName, Map<String, String> arrMap) {
        if (arrMap.containsKey(arrName)) {
            return arrMap.get(arrName);
        } else {
            return transformParam(arrName, n, funcName);
        }
    }
}
