
package com.mcserver.util;

import java.util.Scanner;


public class HashGeneration {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String in = scanner.nextLine();
        String hash = AuthUtils.toHashed(in);
        System.out.println("pass: " + in);
        System.out.println("hash: " + hash);
        System.out.println("check: " + AuthUtils.verifyPassword(in, hash));
    }
}
