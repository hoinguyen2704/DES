public class DESData {
    private String textAscii;  // Văn bản dạng ASCII
    private String textHex;    // Biểu diễn hex của văn bản
    private String key;        // Khóa (dạng hex)
    private String checksum;   // Checksum để kiểm tra tính toàn vẹn

    // Constructor với đầu vào là text ASCII
    public DESData(String textAscii, String key, boolean isAscii) {
        if (isAscii) {
            this.textAscii = textAscii;
            this.textHex = DES.textToHex(textAscii);
        } else {
            this.textHex = textAscii; // Đầu vào đã là hex
            try {
                this.textAscii = DES.hexToText(textAscii);
            } catch (Exception e) {
                this.textAscii = ""; // Không thể chuyển đổi sang ASCII
            }
        }
        this.key = key;
        this.checksum = generateChecksum();
    }
    
    // Constructor với đầu vào là hex
    public DESData(String textHex, String key) {
        this.textHex = textHex;
        try {
            this.textAscii = DES.hexToText(textHex);
        } catch (Exception e) {
            this.textAscii = ""; // Không thể chuyển đổi sang ASCII
        }
        this.key = key;
        this.checksum = generateChecksum();
    }

    // Tạo checksum từ dữ liệu và khóa
    private String generateChecksum() {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            String dataToHash = textHex + key;
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
    
    // Lưu dữ liệu vào file
    public void saveToFile(String filePath) throws java.io.IOException {
        java.io.File file = new java.io.File(filePath);
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file), "UTF-8")) {
            // Format: [CHECKSUM]|[KEY]|[DATA]
            writer.write(checksum + "|" + key + "|" + textHex);
        }
    }
    
    // Đọc dữ liệu từ file
    public static DESData loadFromFile(String filePath) throws java.io.IOException, IllegalArgumentException {
        java.io.File file = new java.io.File(filePath);
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), "UTF-8").trim();
        
        String[] parts = content.split("\\|");
        if (parts.length != 3) {
            throw new IllegalArgumentException("File không đúng định dạng");
        }
        
        String storedChecksum = parts[0];
        String key = parts[1];
        String textHex = parts[2];
        
        DESData data = new DESData(textHex, key);
        
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
}

