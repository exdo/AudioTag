package xyz.idaoteng.audiotag.api.netease;

import com.google.gson.Gson;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ApiTool {
    private static final String PRESET_KEY = "0CoJUm6Qyw8W8jud";
    private static final String IV = "0102030405060708";
    private static final String PUB_KEY = "010001";
    private static final String MODULUS = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";
    private static final String[] IP_POOL = {
            "218", "218", "66", "66", "218", "218", "60", "60", "202", "204", "66", "66",
            "66", "59", "61", "60", "222", "221", "66", "59", "60", "60", "66", "218",
            "218", "62", "63", "64", "66", "66", "122", "211"
    };
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:92.0) Gecko/20100101 Firefox/92.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 13; Redmi Note 12 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    };

    private static final Random random = new SecureRandom();

    public static Map<String, String> encryptParams(Map<String, Object> data) throws Exception {
        String secretKey = createRandomString();
        String jsonStr = new Gson().toJson(data);

        String params = aesEncrypt(jsonStr, PRESET_KEY);
        params = aesEncrypt(params, secretKey);

        String encSecKey = rsaEncrypt(secretKey);

        Map<String, String> result = new HashMap<>();
        result.put("params", params);
        result.put("encSecKey", encSecKey);
        return result;
    }

    public static String aesEncrypt(String text, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String rsaEncrypt(String text) {
        // Reverse the text
        String reversedText = new StringBuilder(text).reverse().toString();

        // Convert to hex
        String hexText = bytesToHex(reversedText.getBytes(StandardCharsets.UTF_8));

        // Big integer operations
        BigInteger biText = new BigInteger(hexText, 16);
        BigInteger biPubKey = new BigInteger(PUB_KEY, 16);
        BigInteger biModulus = new BigInteger(MODULUS, 16);

        BigInteger biResult = biText.modPow(biPubKey, biModulus);

        // Convert back to hex and pad
        String hexResult = biResult.toString(16);
        return String.format("%256s", hexResult).replace(' ', '0');
    }

    public static String createRandomString() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String getRandomIP() {
        String ip1 = IP_POOL[random.nextInt(IP_POOL.length)];
        return ip1 + "." + random.nextInt(256) +
                "." + random.nextInt(256) +
                "." + random.nextInt(256);
    }

    public static String getRandomUserAgent() {
        return USER_AGENTS[random.nextInt(USER_AGENTS.length)];
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
