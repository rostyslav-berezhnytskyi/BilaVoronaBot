package com.telegram.bilavorona.util;

public interface CommandValidator {
    boolean checkCom(long chatId, String[] commandParts, int size, String errorText);
}
