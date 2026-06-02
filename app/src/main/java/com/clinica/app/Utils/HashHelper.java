package com.clinica.app.Utils;

import java.security.MessageDigest;

public class HashHelper {
    private static String hash_key = "dMgAys0gW8bMZixub2I0rbyb";

    public static String gerarHash(String senha) {
        try {
            if (senha == null) senha = "";

            String senhaComSalt = hash_key + senha;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(senhaComSalt.getBytes("UTF-8"));

            StringBuilder hex = new StringBuilder();

            for (byte b : hashBytes) {
                String hexByte = Integer.toHexString(0xff & b);

                if (hexByte.length() == 1) {
                    hex.append('0');
                }

                hex.append(hexByte);
            }

            return hex.toString();

        } catch (Exception e) {
            return senha;
        }
    }

    public static boolean validarSenha(String senhaDigitada, String senhaSalva) {
        if (senhaDigitada == null || senhaSalva == null) {
            return false;
        }

        String hashDigitado = gerarHash(senhaDigitada);

        return hashDigitado.equals(senhaSalva);
    }
}
