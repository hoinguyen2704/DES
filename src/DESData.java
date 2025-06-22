public class DESData {
    private String textAscii; // Văn bản dạng ASCII
    private String textHex; // Biểu diễn hex của văn bản
    private String key; // Khóa (dạng hex)
    private String checksum; // Checksum để kiểm tra tính toàn vẹn
    private String inputEncoding; // Bảng mã đầu vào
    private String outputEncoding; // Bảng mã đầu ra
    private String subCheckKey;

    public String getSubCheckKey() {
        return subCheckKey;
    }

    public void setSubCheckKey(String subCheckKey) {
        this.subCheckKey = subCheckKey;
    }

    // Constructor với đầu vào là text ASCII
    public DESData(String textAscii, String key, boolean isAscii) {
        if (isAscii) {
            this.textAscii = textAscii;
            this.textHex = DES.textToHex(textAscii); // Sử dụng UTF-8
        } else {
            this.textHex = textAscii; // Đầu vào đã là hex
            try {
                // Chuyển đổi hex sang UTF-8
                byte[] bytes = new byte[textAscii.length() / 2];
                for (int i = 0; i < bytes.length; i++) {
                    int index = i * 2;
                    int v = Integer.parseInt(textAscii.substring(index, index + 2), 16);
                    bytes[i] = (byte) v;
                }
                this.textAscii = new String(bytes, "UTF-8");
            } catch (Exception e) {
                this.textAscii = ""; // Không thể chuyển đổi sang UTF-8
            }
        }
        this.key = key;
        this.inputEncoding = isAscii ? "UTF-8" : "HEX";
        this.outputEncoding = "HEX";
        this.checksum = generateChecksum();
    }

    // Constructor với đầu vào là hex
    public DESData(String textHex, String key) {
        this(textHex, key, "UTF-8", "HEX");
    }

    // Constructor với đầu vào là hex và thông tin bảng mã
    public DESData(String textHex, String subCheckKey, String key, String inputEncoding, String outputEncoding) {
        this.textHex = textHex;
        try {
            this.textAscii = DES.hexToText(textHex);
        } catch (Exception e) {
            this.textAscii = ""; // Không thể chuyển đổi sang ASCII
        }
        this.key = key;
        this.inputEncoding = inputEncoding;
        this.outputEncoding = outputEncoding;
        this.subCheckKey = subCheckKey;
        this.checksum = generateChecksum();
    }

    public DESData(String textHex, String key, String inputEncoding, String outputEncoding) {
        this.textHex = textHex;
        try {
            this.textAscii = DES.hexToText(textHex);
        } catch (Exception e) {
            this.textAscii = ""; // Không thể chuyển đổi sang ASCII
        }
        this.key = key;
        this.inputEncoding = inputEncoding;
        this.outputEncoding = outputEncoding;
        this.checksum = generateChecksum();
    }

    // Tạo checksum từ dữ liệu và khóa
    private String generateChecksum() {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            String dataToHash = textHex + subCheckKey + key + inputEncoding + outputEncoding;
            byte[] hashBytes = md.digest(dataToHash.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString().substring(0, 16); // Lấy 16 ký tự đầu của hash
        } catch (Exception e) {
            return "";
        }
    }

    // Kiểm tra tính hợp lệ của dữ liệu và khóa
    public boolean isValid() {
        String currentChecksum = generateChecksum();
        return currentChecksum.equals(checksum);
    }

    // Các getter và setter
    public String getTextAscii() {
        return textAscii;
    }

    public void setTextAscii(String textAscii) {
        this.textAscii = textAscii;
        this.textHex = DES.textToHex(textAscii);
        this.checksum = generateChecksum();
    }

    public String getTextHex() {
        return textHex;
    }

    public void setTextHex(String textHex) {
        this.textHex = textHex;
        try {
            this.textAscii = DES.hexToText(textHex);
        } catch (Exception e) {
            this.textAscii = ""; // Không thể chuyển đổi sang ASCII
        }
        this.checksum = generateChecksum();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        this.checksum = generateChecksum();
    }

    public String getChecksum() {
        return checksum;
    }

    public String getInputEncoding() {
        return inputEncoding;
    }

    public void setInputEncoding(String inputEncoding) {
        this.inputEncoding = inputEncoding;
        this.checksum = generateChecksum();
    }

    public String getOutputEncoding() {
        return outputEncoding;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
        this.checksum = generateChecksum();
    }

    // Lưu dữ liệu vào file
    public void saveToFile(String filePath) throws java.io.IOException {
        java.io.File file = new java.io.File(filePath);
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file), "UTF-8")) {
            // Obfuscate khóa trước khi lưu
            String obfuscatedKey = obfuscateKey(key);
            // Format: [CHECKSUM]|[OBFUSCATED_KEY]|[INPUT_ENCODING]|[OUTPUT_ENCODING]|[DATA]
            writer.write(checksum + "|" + obfuscatedKey + "|" + inputEncoding + "|" + outputEncoding + "|" + textHex);
        }
    }

    // Đọc dữ liệu từ file
    public static DESData loadFromFile(String filePath) throws java.io.IOException, IllegalArgumentException {
        java.io.File file = new java.io.File(filePath);
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), "UTF-8").trim();

        String[] parts = content.split("\\|");
        if (parts.length < 5) {
            // Tương thích ngược với định dạng cũ (không có thông tin bảng mã)
            if (parts.length == 3) {
                String storedChecksum = parts[0];
                String obfuscatedKey = parts[1];
                String textHex = parts[2];

                // Deobfuscate khóa
                String key = deobfuscateKey(obfuscatedKey, textHex);

                DESData data = new DESData(textHex, key);

                // Kiểm tra tính hợp lệ
                if (!data.checksum.equals(storedChecksum)) {
                    throw new IllegalArgumentException("Dữ liệu hoặc khóa đã bị sửa đổi");
                }

                return data;
            } else {
                throw new IllegalArgumentException("File không đúng định dạng");
            }
        }

        String storedChecksum = parts[0];
        String obfuscatedKey = parts[1];
        String inputEncoding = parts[2];
        String outputEncoding = parts[3];
        String textHex = parts[4];

        // Deobfuscate khóa
        String key = deobfuscateKey(obfuscatedKey, textHex);

        DESData data = new DESData(textHex, obfuscatedKey, key, inputEncoding, outputEncoding);

        // Kiểm tra tính hợp lệ
        if (!data.checksum.equals(storedChecksum)) {
            throw new IllegalArgumentException("Dữ liệu hoặc khóa đã bị sửa đổi");
        }

        return data;
    }

    // Phương thức mã hóa
    public String encrypt() {
        int[] plaintextBinary = DES.textToBinary(textHex);
        int[] keyBinary = DES.textToBinary(key);
        int[] encryptedBinary = DES.encryptText(plaintextBinary, keyBinary);
        return DES.binaryToHex(encryptedBinary);
    }

    // Phương thức giải mã
    public String decrypt() {
        int[] cipherBinary = DES.textToBinary(textHex);
        int[] keyBinary = DES.textToBinary(key);
        int[] decryptedBinary = DES.decryptText(cipherBinary, keyBinary);
        return DES.binaryToHex(decryptedBinary);
    }

    // Mã hóa và trả về kết quả dưới dạng DESData
    public DESData encryptToData() {
        String encryptedHex = encrypt();
        return new DESData(encryptedHex, key);
    }

    // Giải mã và trả về kết quả dưới dạng DESData
    public DESData decryptToData() {
        String decryptedHex = decrypt();
        return new DESData(decryptedHex, key);
    }

    // Thêm phương thức để obfuscate khóa
    private String obfuscateKey(String key) {
        try {
            // Tạo một salt từ 8 ký tự đầu của textHex
            String salt = textHex.substring(0, Math.min(8, textHex.length()));

            // XOR key với salt
            StringBuilder obfuscatedKey = new StringBuilder();
            for (int i = 0; i < key.length(); i++) {
                char keyChar = key.charAt(i);
                char saltChar = salt.charAt(i % salt.length());
                // XOR và chuyển về dạng hex
                char xorResult = (char) (keyChar ^ saltChar);
                obfuscatedKey.append(String.format("%02X", (int) xorResult));
            }
            return obfuscatedKey.toString();
        } catch (Exception e) {
            // Nếu có lỗi, trả về key gốc
            return key;
        }
    }

    // Thêm phương thức để deobfuscate khóa
    private static String deobfuscateKey(String obfuscatedKey, String textHex) {
        try {
            // Lấy salt từ 8 ký tự đầu của textHex
            String salt = textHex.substring(0, Math.min(8, textHex.length()));

            // Chuyển obfuscatedKey từ hex về bytes
            StringBuilder deobfuscatedKey = new StringBuilder();
            for (int i = 0; i < obfuscatedKey.length(); i += 2) {
                if (i + 2 <= obfuscatedKey.length()) {
                    int value = Integer.parseInt(obfuscatedKey.substring(i, i + 2), 16);
                    char saltChar = salt.charAt((i / 2) % salt.length());
                    // XOR để khôi phục ký tự gốc
                    char originalChar = (char) (value ^ saltChar);
                    deobfuscatedKey.append(originalChar);
                }
            }
            return deobfuscatedKey.toString();
        } catch (Exception e) {
            // Nếu có lỗi, trả về key đã obfuscate
            return obfuscatedKey;
        }
    }
}
