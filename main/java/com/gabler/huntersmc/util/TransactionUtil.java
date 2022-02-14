package com.gabler.huntersmc.util;

import java.util.UUID;

public class TransactionUtil {

    public static String rollupTransactionIdLogHeader() {
        return "[Transaction " + UUID.randomUUID().toString().substring(0, 8) + "] ";
    }
}
