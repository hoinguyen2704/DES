import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class DES {
    // Constants for DES algorithm
    private static final int[] IP = {
            58, 50, 42, 34, 26, 18, 10, 2,
            60, 52, 44, 36, 28, 20, 12, 4,
            62, 54, 46, 38, 30, 22, 14, 6,
            64, 56, 48, 40, 32, 24, 16, 8,
            57, 49, 41, 33, 25, 17, 9, 1,
            59, 51, 43, 35, 27, 19, 11, 3,
            61, 53, 45, 37, 29, 21, 13, 5,
            63, 55, 47, 39, 31, 23, 15, 7
    };

    private static final int[] FP = {
            40, 8, 48, 16, 56, 24, 64, 32,
            39, 7, 47, 15, 55, 23, 63, 31,
            38, 6, 46, 14, 54, 22, 62, 30,
            37, 5, 45, 13, 53, 21, 61, 29,
            36, 4, 44, 12, 52, 20, 60, 28,
            35, 3, 43, 11, 51, 19, 59, 27,
            34, 2, 42, 10, 50, 18, 58, 26,
            33, 1, 41, 9, 49, 17, 57, 25
    };

    private static final int[] E = {
            32, 1, 2, 3, 4, 5,
            4, 5, 6, 7, 8, 9,
            8, 9, 10, 11, 12, 13,
            12, 13, 14, 15, 16, 17,
            16, 17, 18, 19, 20, 21,
            20, 21, 22, 23, 24, 25,
            24, 25, 26, 27, 28, 29,
            28, 29, 30, 31, 32, 1
    };

    private static final int[][] S_BOX = {
            {
                    14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7,
                    0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8,
                    4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0,
                    15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13
            },
            {
                    15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10,
                    3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5,
                    0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15,
                    13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9
            },
            {
                    10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8,
                    13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1,
                    13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7,
                    1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12
            },
            {
                    7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15,
                    13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9,
                    10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4,
                    3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14
            },
            {
                    2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9,
                    14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6,
                    4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14,
                    11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3
            },
            {
                    12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11,
                    10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8,
                    9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6,
                    4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13
            },
            {
                    4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1,
                    13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6,
                    1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2,
                    6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12
            },
            {
                    13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7,
                    1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2,
                    7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8,
                    2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11
            }
    };

    private static final int[] P = {
            16, 7, 20, 21, 29, 12, 28, 17,
            1, 15, 23, 26, 5, 18, 31, 10,
            2, 8, 24, 14, 32, 27, 3, 9,
            19, 13, 30, 6, 22, 11, 4, 25
    };

    private static final int[] PC1 = {
            57, 49, 41, 33, 25, 17, 9,
            1, 58, 50, 42, 34, 26, 18,
            10, 2, 59, 51, 43, 35, 27,
            19, 11, 3, 60, 52, 44, 36,
            63, 55, 47, 39, 31, 23, 15,
            7, 62, 54, 46, 38, 30, 22,
            14, 6, 61, 53, 45, 37, 29,
            21, 13, 5, 28, 20, 12, 4
    };

    private static final int[] PC2 = {
            14, 17, 11, 24, 1, 5,
            3, 28, 15, 6, 21, 10,
            23, 19, 12, 4, 26, 8,
            16, 7, 27, 20, 13, 2,
            41, 52, 31, 37, 47, 55,
            30, 40, 51, 45, 33, 48,
            44, 49, 39, 56, 34, 53,
            46, 42, 50, 36, 29, 32
    };

    private static final int[] SHIFT_SCHEDULE = {
            1, 1, 2, 2, 2, 2, 2, 2,
            1, 2, 2, 2, 2, 2, 2, 1
    };

    public static void main(String[] args) {
        String inputText = "0123456789ABCDEh";
        String keyText = "133457799BBCDFF1"; // Example key text

        // Convert input text and key to binary
        int[] plaintext = textToBinary(inputText);
        int[] key = textToBinary(keyText);

        System.out.println("Input text: " + inputText);
        System.out.println("Key: " + keyText);

        // Encrypt
        int[] encryptedText = encryptText(plaintext, key);
        String encryptedHex = binaryToHex(encryptedText);
        System.out.println("Encrypted hex: " + encryptedHex);

        // Decrypt
        int[] decryptedText = decryptText(encryptedText, key);
        String decryptedHex = binaryToHex(decryptedText);
        System.out.println("Decrypted hex: " + decryptedHex);
    }

    static int[] textToBinary(String text) {
        int len = text.length();
        // Each hex character is 4 bits, so 16 hex chars = 64 bits
        if (len < 16) {
            // Pad with '0' to the left if not enough for 64 bits
            StringBuilder sb = new StringBuilder();
            sb.append(text);
            for (int i = 0; i < 16 - len; i++) {
                sb.append('0');
            }
            text = sb.toString();
            len = 16;
        }
        int[] binary = new int[len * 4]; // 4 bits per hex character
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            try {
                int value = Integer.parseInt(String.valueOf(c), 16);
                for (int j = 0; j < 4; j++) {
                    binary[i * 4 + j] = (value >> (3 - j)) & 1;
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Input string must be hexadecimal. Invalid character: '" + c + "'");
            }
        }
        return binary;
    }

    public static List<String> processInput(String input) {
        List<String> blocks = new ArrayList<>();
        int blockSize = 8; // DES dùng khối 8 byte

        for (int i = 0; i < input.length(); i += blockSize) {
            String block = input.substring(i, Math.min(i + blockSize, input.length()));
            if (block.length() < blockSize) {
                block = String.format("%-8s", block).replace(' ', '\0'); // Thêm padding
            }
            blocks.add(block);
        }
        return blocks;
    }

    private static String binaryToText(int[] binary) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < binary.length; i += 4) {
            int value = 0;
            for (int j = 0; j < 4; j++) {
                value = (value << 1) | binary[i + j];
            }
            text.append(Integer.toHexString(value).toUpperCase());
        }
        return text.toString();
    }

    static int[] encryptText(int[] plaintext, int[] key) {
        int[] encrypted = new int[plaintext.length];
        for (int i = 0; i < plaintext.length; i += 64) {
            int[] block = new int[64];
            System.arraycopy(plaintext, i, block, 0, 64);
            int[] encryptedBlock = desEncrypt(block, key);
            System.arraycopy(encryptedBlock, 0, encrypted, i, 64);
        }
        return encrypted;
    }

    static int[] decryptText(int[] encryptedText, int[] key) {
        int[] decrypted = new int[encryptedText.length];
        for (int i = 0; i < encryptedText.length; i += 64) {
            int[] block = new int[64];
            System.arraycopy(encryptedText, i, block, 0, 64);
            int[] decryptedBlock = desDecrypt(block, key);
            System.arraycopy(decryptedBlock, 0, decrypted, i, 64);
        }
        return decrypted;
    }

    private static int[] permute(int[] input, int[] table) {
        int[] output = new int[table.length];
        for (int i = 0; i < table.length; i++) {
            output[i] = input[table[i] - 1];
        }
        return output;
    }

    private static int[][] generateSubkeys(int[] key) {
        int[][] subkeys = new int[16][48];
        int[] permutedKey = permute(key, PC1);

        int[] C = new int[28];
        int[] D = new int[28];
        System.arraycopy(permutedKey, 0, C, 0, 28);
        System.arraycopy(permutedKey, 28, D, 0, 28);

        for (int i = 0; i < 16; i++) {
            C = leftShift(C, SHIFT_SCHEDULE[i]);
            D = leftShift(D, SHIFT_SCHEDULE[i]);
            int[] CD = concatenate(C, D);
            subkeys[i] = permute(CD, PC2);
        }
        return subkeys;
    }

    private static int[] feistelFunction(int[] R, int[] subkey) {
        int[] expandedR = permute(R, E);
        int[] xorResult = xor(expandedR, subkey);
        int[] sBoxResult = applySBox(xorResult);
        return permute(sBoxResult, P);
    }

    public static int[] desEncrypt(int[] plaintext, int[] key) {
        int[][] subkeys = generateSubkeys(key);
        int[] permutedText = permute(plaintext, IP);

        int[] L = new int[32];
        int[] R = new int[32];
        System.arraycopy(permutedText, 0, L, 0, 32);
        System.arraycopy(permutedText, 32, R, 0, 32);

        for (int i = 0; i < 16; i++) {
            int[] newR = xor(L, feistelFunction(R, subkeys[i]));
            L = R;
            R = newR;
        }

        int[] finalText = concatenate(R, L);
        return permute(finalText, FP);
    }

    public static int[] desDecrypt(int[] encryptedText, int[] key) {
        int[][] subkeys = generateSubkeys(key);
        reverseArray(subkeys);

        int[] permutedText = permute(encryptedText, IP);

        int[] L = new int[32];
        int[] R = new int[32];
        System.arraycopy(permutedText, 0, L, 0, 32);
        System.arraycopy(permutedText, 32, R, 0, 32);

        for (int i = 0; i < 16; i++) {
            int[] newR = xor(L, feistelFunction(R, subkeys[i]));
            L = R;
            R = newR;
        }

        int[] finalText = concatenate(R, L);
        return permute(finalText, FP);
    }

    private static int[] xor(int[] a, int[] b) {
        int[] result = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] ^ b[i];
        }
        return result;
    }

    private static int[] leftShift(int[] bits, int shifts) {
        int[] result = new int[bits.length];
        System.arraycopy(bits, shifts, result, 0, bits.length - shifts);
        System.arraycopy(bits, 0, result, bits.length - shifts, shifts);
        return result;
    }

    private static int[] concatenate(int[] a, int[] b) {
        int[] result = new int[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private static void reverseArray(int[][] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int[] temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    private static int[] applySBox(int[] input) {
        int[] output = new int[32];
        for (int i = 0; i < 8; i++) {
            int row = (input[i * 6] << 1) | input[i * 6 + 5];
            int col = (input[i * 6 + 1] << 3) | (input[i * 6 + 2] << 2) | (input[i * 6 + 3] << 1) | input[i * 6 + 4];
            int val = S_BOX[i][row * 16 + col];
            for (int j = 0; j < 4; j++) {
                output[i * 4 + j] = (val >> (3 - j)) & 1;
            }
        }
        return output;
    }

    static String binaryToHex(int[] binary) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < binary.length; i += 4) {
            int value = 0;
            for (int j = 0; j < 4; j++) {
                value = (value << 1) | binary[i + j];
            }
            hex.append(Integer.toHexString(value).toUpperCase());
        }
        return hex.toString();
    }

    public static String hexToText(String hex) {
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        
        // Chuyển đổi hex sang bytes
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            bytes[i] = (byte) v;
        }
        
        // Thử với nhiều mã hóa khác nhau
        Map<String, String> results = new HashMap<>();
        
        // Thử với UTF-8
        try {
            String utf8Text = new String(bytes, "UTF-8");
            if (isPrintableText(utf8Text)) {
                return utf8Text.trim();
            }
            results.put("UTF-8", utf8Text.trim());
        } catch (Exception e) {
            results.put("UTF-8", "[Lỗi]");
        }
        
        // Thử với ISO-8859-1
        try {
            String isoText = new String(bytes, "ISO-8859-1");
            if (isPrintableText(isoText)) {
                return isoText.trim();
            }
            results.put("ISO-8859-1", isoText.trim());
        } catch (Exception e) {
            results.put("ISO-8859-1", "[Lỗi]");
        }
        
        // Thử với windows-1252
        try {
            String winText = new String(bytes, "windows-1252");
            if (isPrintableText(winText)) {
                return winText.trim();
            }
            results.put("windows-1252", winText.trim());
        } catch (Exception e) {
            results.put("windows-1252", "[Lỗi]");
        }
        
        // Nếu không có kết quả nào tốt, trả về kết quả UTF-8 hoặc ISO-8859-1
        return results.getOrDefault("UTF-8", results.getOrDefault("ISO-8859-1", ""));
    }

    // Phương thức kiểm tra xem văn bản có đọc được không
    private static boolean isPrintableText(String text) {
        // Đối với tiếng Việt, chúng ta cần kiểm tra khác
        int nonPrintableCount = 0;
        for (char c : text.toCharArray()) {
            // Cho phép các ký tự Unicode tiếng Việt và các ký tự điều khiển cơ bản
            if (c < 32 && c != '\n' && c != '\r' && c != '\t') {
                nonPrintableCount++;
            }
        }
        // Nếu hơn 10% ký tự không in được, coi như văn bản không đọc được
        return nonPrintableCount < text.length() * 0.1;
    }

    public static String textToHex(String text) {
        StringBuilder hexString = new StringBuilder();
        
        try {
            // Luôn sử dụng UTF-8 để chuyển đổi văn bản sang hex
            byte[] bytes = text.getBytes("UTF-8");
            for (byte b : bytes) {
                hexString.append(String.format("%02X", b & 0xFF));
            }
        } catch (Exception e) {
            // Nếu có lỗi, sử dụng phương pháp dự phòng
            for (char ch : text.toCharArray()) {
                hexString.append(String.format("%02X", (int) ch));
            }
        }
        
        return hexString.toString();
    }

    public static List<String> splitHexToBlocks(String hexText) {
        List<String> blocks = new ArrayList<>();
        int len = hexText.length();
        while (len % 16 != 0) {
            hexText =  hexText + "0"; // Padding bằng 0 nếu độ dài không chia hết cho 16
            len++;
        }
        for (int i = 0; i < len; i += 16) {
            String block = hexText.substring(i, Math.min(i + 16, len));
            blocks.add(block);
        }
        return blocks;
    }

}




