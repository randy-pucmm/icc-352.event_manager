package pucmm.application.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtil {

    private static final int COST = 12;

    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray());
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified;
    }
}
