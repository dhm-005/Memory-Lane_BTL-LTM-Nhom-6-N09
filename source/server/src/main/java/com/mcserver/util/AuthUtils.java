package com.mcserver.util;

import com.mcserver.model.User;
import java.io.IOException;

import org.mindrot.jbcrypt.BCrypt;

public class AuthUtils {
    public static String toHashed(String pw){
        return BCrypt.hashpw(pw, BCrypt.gensalt(12));
    }
    
    public static boolean verifyPassword(String pw, String hashedPw) {
        return BCrypt.checkpw(pw, hashedPw);
    }
    
}
