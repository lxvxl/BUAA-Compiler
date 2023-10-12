package ident;

import logger.Logger;

import java.util.HashMap;
import java.util.LinkedList;

public class SymbolTable {
    private static final LinkedList<HashMap<String, AbstractIdent>> tableStack = new LinkedList<>();
    private static boolean isAdvanceBlockIn = false;

    static {
        tableStack.addFirst(new HashMap<>());
    }

    public static void addIdent(AbstractIdent ident) throws RepeatDefException {
        if (tableStack.getFirst().containsKey(ident.getName())) {
            throw new RepeatDefException();
        }
        tableStack.getFirst().put(ident.getName(), ident);
        Logger.write(ident.toString());
    }

    public static void blockIn() {
        if (isAdvanceBlockIn) {
            isAdvanceBlockIn = false;
            return;
        }
        tableStack.addFirst(new HashMap<>());
    }

    public static void advanceBlockIn() {
        tableStack.addFirst(new HashMap<>());
        isAdvanceBlockIn = true;
    }

    public static void blockOut() {
        tableStack.removeFirst();
    }

    /**
     * 若未找到符号，返回null
     */
    public static AbstractIdent searchIdent(String name) {
        for (HashMap<String, AbstractIdent> map : tableStack) {
            if (map.containsKey(name)) {
                return map.get(name);
            }
        }
        return null;
    }
}
