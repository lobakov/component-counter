package com.github.lobakov.componentcounter.util;

public class CredentialParser {

    private static final int CREDENTIALS_LENGTH = 3;
    private static final String DELIMITER = ":";

    public static String[] parse(String credentials) {
        String[] result = credentials.split(DELIMITER);
        if (result.length != CREDENTIALS_LENGTH) {
            throw new RuntimeException("Invalid credentials: login:pass:otp expected");
        }
        return result;
    }
}
