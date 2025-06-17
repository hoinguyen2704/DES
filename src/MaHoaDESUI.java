import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MaHoaDESUI extends JFrame {
    // Components bên Mã hóa
    private JTextField txtEncFile, txtEncOutName, txtEncKeyFile, txtEncKeyText;
    private JCheckBox chkEncDebug;
    private JButton btnEncBrowseFile, btnEncRandomKeyFile, btnEncEncryptFile;
    private JTextArea taEncPlainFile, taEncInputText, taEncResultText;
    private JButton btnEncSaveResult, btnEncEncryptText, btnEncRandomKeyText;
    JFileChooser fileChooser;

    // Components bên Giải mã
    private JTextField txtDecFile, txtDecOutName, txtDecKeyFile, txtDecKeyText;
    private JCheckBox chkDecDebug;
    private JButton btnDecBrowseFile, btnDecDecryptFile;
    private JTextArea taDecCipherFile, taDecResultFile, taDecResultText;
    private JButton btnDecLoadCipher, btnDecDecryptText;

    public MaHoaDESUI() {
        super("MaHoaDES");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // --- PANEL MÃ HÓA ---
        JPanel panelEnc = new JPanel(new GridBagLayout());
        panelEnc.setBorder(BorderFactory.createTitledBorder("Mã hóa"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        // Row 0: File:
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        panelEnc.add(new JLabel("File:"), c);
        txtEncFile = new JTextField(20);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panelEnc.add(txtEncFile, c);
        btnEncBrowseFile = new JButton("Files...");
        c.gridx = 2;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.anchor = GridBagConstraints.WEST;
        panelEnc.add(btnEncBrowseFile, c);
        fileChooser = new JFileChooser();
        // Sự kiện chọn file: cập nhật txtEncFile và txtEncOutName
        btnEncBrowseFile.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                txtEncFile.setText(selectedFile.getAbsolutePath());
                String fileHex = "";
                try {
                    fileHex = new String(java.nio.file.Files.readAllBytes(selectedFile.toPath())).trim();
                } catch (java.io.IOException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi đọc file: " + ex.getMessage());
                    return;
                }
                // Hiển thị tên file (không có đường dẫn) lên txtEncOutName, thêm hậu tố
                // ".txt"
                String fileName = selectedFile.getName();
                int dotIdx = fileName.lastIndexOf('.');
                String baseName = (dotIdx > 0) ? fileName.substring(0, dotIdx) : fileName;
                String outName = baseName + "_encrypted.txt";
                txtEncOutName.setText(outName);
                taEncPlainFile.setText(fileHex);
            }
        });

        // Row 1: Tên file mã hóa:
        c.gridy = 1;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        panelEnc.add(new JLabel("Tên file sau khi mã hóa:"), c);
        txtEncOutName = new JTextField(20);
        c.gridx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        panelEnc.add(txtEncOutName, c);
        c.gridwidth = 1;

        // Row 2: Khóa
        c.gridy = 2;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        panelEnc.add(new JLabel("Khóa:"), c);
        txtEncKeyFile = new JTextField("133457799BBCDFF1", 20);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panelEnc.add(txtEncKeyFile, c);

        // Row 3: Debug, Random Key, Encrypt File
        c.gridy = 3;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        // chkEncDebug = new JCheckBox("Chế độ debug");
        // panelEnc.add(chkEncDebug, c);
        btnEncRandomKeyFile = new JButton("Khóa ngẫu nhiên");
        btnEncRandomKeyFile.addActionListener(e -> {
            String randomKey = generateRandomKey();
            txtEncKeyFile.setText(randomKey);
            txtDecKeyFile.setText(randomKey);
        });
        c.gridx = 1;
        panelEnc.add(btnEncRandomKeyFile, c);
        btnEncEncryptFile = new JButton("Mã hóa file");

        c.gridx = 2;
        c.anchor = GridBagConstraints.EAST;
        btnEncEncryptFile.addActionListener(e -> {
            String inputFilePath = txtEncFile.getText().trim();
            String key = txtEncKeyFile.getText().trim().toUpperCase();
            if (inputFilePath.isEmpty() || key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn file và nhập khóa!");
                return;
            }
            java.io.File inputFile = new java.io.File(inputFilePath);
            if (!inputFile.exists()) {
                JOptionPane.showMessageDialog(this, "File không tồn tại!");
                return;
            }
            try {
                // Đọc nội dung file dưới dạng ASCII
                String fileContent = new String(java.nio.file.Files.readAllBytes(inputFile.toPath())).trim();
                taEncPlainFile.setText(fileContent);
                
                // Tạo đối tượng DESData với nội dung ASCII
                DESData data = new DESData(fileContent, key, true);
                
                // Chuyển đổi văn bản ASCII sang hex và chia thành các khối
                List<String> blocks = DES.splitHexToBlocks(data.getTextHex());
                StringBuilder encryptedHexBuilder = new StringBuilder();
                
                for (String block : blocks) {
                    // Nếu block < 16 ký tự, padding bằng '0'
                    if (block.length() < 16) {
                        block = String.format("%-16s", block).replace(' ', '0');
                    }
                    DESData blockData = new DESData(block, key);
                    String encryptedBlock = blockData.encrypt();
                    encryptedHexBuilder.append(encryptedBlock);
                }
                
                String encryptedHex = encryptedHexBuilder.toString();
                taEncResultText.setText(encryptedHex);

                // Lưu ra file thông thường
                String outName = txtEncOutName.getText();
                java.io.File outFile = new java.io.File(inputFile.getParentFile(), outName);
                try (java.io.FileWriter fw = new java.io.FileWriter(outFile)) {
                    fw.write(encryptedHex);
                }
                
                // Lưu ra file bảo mật (kèm khóa và checksum)
                String secureFileName = outName + ".secure";
                java.io.File secureFile = new java.io.File(inputFile.getParentFile(), secureFileName);
                DESData encryptedData = new DESData(encryptedHex, key);
                encryptedData.saveToFile(secureFile.getAbsolutePath());
                
                JOptionPane.showMessageDialog(this, 
                    "Đã mã hóa và lưu file:\n" + 
                    "- File thông thường: " + outFile.getAbsolutePath() + "\n" +
                    "- File bảo mật (kèm khóa): " + secureFile.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi mã hóa file: " + ex.getMessage());
            }
        });
        panelEnc.add(btnEncEncryptFile, c);

        // Row 4: Separator
        c.gridy = 4;
        c.gridx = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        panelEnc.add(new JSeparator(), c);
        c.gridwidth = 1;
        c.weightx = 0;
        // Row 5: Tiêu đề "Mã hóa văn bản"
        c.gridy = 5;
        c.gridx = 0;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        JLabel lblEncTextSection = new JLabel("Mã hóa văn bản");
        lblEncTextSection.setFont(lblEncTextSection.getFont().deriveFont(Font.BOLD));
        panelEnc.add(lblEncTextSection, c);
        c.gridwidth = 1;

        // Row 6: Bản rõ (file)
        c.gridy = 6;
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        panelEnc.add(new JLabel("Bản rõ:"), c);
        taEncPlainFile = new JTextArea(5, 20);
        JScrollPane spEncPlainFile = new JScrollPane(taEncPlainFile);
        c.gridx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 0.3;
        panelEnc.add(spEncPlainFile, c);
        c.gridwidth = 1;
        c.weighty = 0;

        // // --- SECTION MÃ HÓA VĂN BẢN ---
        // // Row 6: Input text
        // c.gridy=6; c.gridx=0; c.anchor=GridBagConstraints.NORTHWEST;
        // c.fill=GridBagConstraints.NONE;
        // panelEnc.add(new JLabel("Bản rõ (văn bản):"), c);
        // taEncInputText = new JTextArea(5,20);
        // JScrollPane spEncInputText = new JScrollPane(taEncInputText);
        // c.gridx=1; c.gridwidth=2; c.fill=GridBagConstraints.BOTH; c.weighty=0.3;
        // panelEnc.add(spEncInputText, c);
        // c.gridwidth=1; c.weighty=0;

        // Row 7: Khóa (text)
        c.gridy = 7;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        panelEnc.add(new JLabel("Khóa:"), c);
        txtEncKeyText = new JTextField("133457799BBCDFF1", 20);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panelEnc.add(txtEncKeyText, c);
        btnEncRandomKeyText = new JButton("Khóa ngẫu nhiên");
        btnEncRandomKeyText.addActionListener(e -> {
            String randomKey = generateRandomKey();
            txtEncKeyText.setText(randomKey);
            txtDecKeyText.setText(randomKey);
        });
        c.gridx = 2;
        c.anchor = GridBagConstraints.WEST;
        panelEnc.add(btnEncRandomKeyText, c);

        // Row 8: Kết quả (text)
        c.gridy = 8;
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        panelEnc.add(new JLabel("Kết quả:"), c);
        taEncResultText = new JTextArea(5, 20);
        JScrollPane spEncResultText = new JScrollPane(taEncResultText);
        c.gridx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0.3;
        panelEnc.add(spEncResultText, c);
        c.gridwidth = 1;
        c.weighty = 0;

        // Row 9: Buttons lưu và mã hóa text
        c.gridy = 9;
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        btnEncSaveResult = new JButton("Lưu vào file");
        panelEnc.add(btnEncSaveResult, c);
        btnEncSaveResult.addActionListener(e -> {
            String encryptedText = taEncResultText.getText();
            if (encryptedText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu để lưu!");
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu kết quả mã hóa");
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                try (java.io.FileWriter fw = new java.io.FileWriter(fileToSave)) {
                    fw.write(encryptedText);
                    JOptionPane.showMessageDialog(this, "Đã lưu thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi lưu file: " + ex.getMessage());
                }
            }
        });
        btnEncEncryptText = new JButton("Mã hóa văn bản");
        btnEncEncryptText.addActionListener(e -> maHoaVanBan());
        c.gridx = 2;
        c.anchor = GridBagConstraints.EAST;
        panelEnc.add(btnEncEncryptText, c);

        // --- PANEL GIẢI MÃ (tương tự) ---
        JPanel panelDec = new JPanel(new GridBagLayout());
        panelDec.setBorder(BorderFactory.createTitledBorder("Giải mã"));
        c.insets = new Insets(5, 5, 5, 5);

        // Row 0: File:
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        panelDec.add(new JLabel("File:"), c);
        txtDecFile = new JTextField(20);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panelDec.add(txtDecFile, c);
        btnDecBrowseFile = new JButton("Files...");
        JFileChooser fileChooser = new JFileChooser();
        btnDecBrowseFile.addActionListener(e -> {
            fileChooser.setDialogTitle("Chọn file để giải mã");
            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToOpen = fileChooser.getSelectedFile();
                txtDecFile.setText(fileToOpen.getAbsolutePath());
                String fileName = fileToOpen.getName();
                int dotIdx = fileName.lastIndexOf('.');
                String baseName = (dotIdx > 0) ? fileName.substring(0, dotIdx) : fileName;
                String outName = baseName + "_decrypted.txt";
                txtDecOutName.setText(outName);

            }
        });
        c.gridx = 2;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.anchor = GridBagConstraints.WEST;
        panelDec.add(btnDecBrowseFile, c);

        // Row 1: Tên file giải mã
        c.gridy = 1;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        panelDec.add(new JLabel("Tên file giải mã:"), c);
        txtDecOutName = new JTextField(20);

        c.gridx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        panelDec.add(txtDecOutName, c);
        c.gridwidth = 1;

        // Row 2: Khóa
        c.gridy = 2;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        panelDec.add(new JLabel("Khóa:"), c);
        txtDecKeyFile = new JTextField("133457799BBCDFF1", 20);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panelDec.add(txtDecKeyFile, c);

        // Row 3: Debug + Giải mã file
        c.gridy = 3;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        // chkDecDebug = new JCheckBox("Chế độ debug");
        // panelDec.add(chkDecDebug, c);
        btnDecDecryptFile = new JButton("Giải mã file");
        btnDecDecryptFile.addActionListener(e -> {
            String inputFilePath = txtDecFile.getText().trim();
            String key = txtDecKeyFile.getText().trim().toUpperCase();
            if (inputFilePath.isEmpty() || key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn file và nhập khóa!");
                return;
            }
            java.io.File inputFile = new java.io.File(inputFilePath);
            if (!inputFile.exists()) {
                JOptionPane.showMessageDialog(this, "File không tồn tại!");
                return;
            }
            try {
                // Đọc nội dung file (hex)
                String fileHex = new String(java.nio.file.Files.readAllBytes(inputFile.toPath())).trim();
                List<String> blocks = DES.splitHexToBlocks(fileHex);
                StringBuilder decryptedHexBuilder = new StringBuilder();
                for (String block : blocks) {
                    // Nếu block < 16 ký tự, padding bằng '0'
                    int[] cipherBinary = DES.textToBinary(block);
                    int[] keyBinary = DES.textToBinary(key);
                    int[] decryptedBinary = DES.decryptText(cipherBinary, keyBinary);
                    String decryptedBlock = DES.binaryToHex(decryptedBinary);
                    decryptedHexBuilder.append(decryptedBlock);
                }
                
                String decryptedHex = decryptedHexBuilder.toString();
                
                // Chuyển đổi kết quả hex sang ASCII
                String decryptedAscii = "";
                try {
                    decryptedAscii = DES.hexToText(decryptedHex);
                } catch (Exception ex) {
                    decryptedAscii = "(Không thể chuyển đổi sang ASCII)";
                }
                
                // Hiển thị cả hex và ASCII
                taDecResultText.setText(decryptedAscii + "\n\n(HEX: " + decryptedHex + ")");
                taDecCipherFile.setText(fileHex);
                
                // Lưu ra file mới
                String outName = txtDecOutName.getText();
                java.io.File outFile = new java.io.File(inputFile.getParentFile(), outName);
                try (java.io.FileWriter fw = new java.io.FileWriter(outFile)) {
                    // Lưu kết quả dưới dạng ASCII nếu có thể, nếu không thì lưu dạng hex
                    if (!decryptedAscii.equals("(Không thể chuyển đổi sang ASCII)")) {
                        fw.write(decryptedAscii);
                    } else {
                        fw.write(decryptedHex);
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Đã giải mã và lưu file: " + outFile.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi giải mã file: " + ex.getMessage());
            }
        });
        c.gridx = 2;
        c.anchor = GridBagConstraints.EAST;
        panelDec.add(btnDecDecryptFile, c);

        // Row 4: Separator
        c.gridy = 4;
        c.gridx = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        panelDec.add(new JSeparator(), c);
        c.gridwidth = 1;
        c.weightx = 0;

        // Row 5: Tiêu đề "Giải mã văn bản"
        c.gridy = 5;
        c.gridx = 0;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        JLabel lblDecTextSection = new JLabel("Giải mã văn bản");
        lblDecTextSection.setFont(lblDecTextSection.getFont().deriveFont(Font.BOLD));
        panelDec.add(lblDecTextSection, c);
        c.gridwidth = 1;

        // Row 6: Bản mã (file)
        c.gridy = 6;
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        panelDec.add(new JLabel("Bản mã:"), c);
        taDecCipherFile = new JTextArea(5, 20);
        JScrollPane spDecCipherFile = new JScrollPane(taDecCipherFile);
        c.gridx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0.3;
        panelDec.add(spDecCipherFile, c);
        c.gridwidth = 1;
        c.weighty = 0;

        // --- SECTION GIẢI MÃ VĂN BẢN ---
        // Row 6: Input Cipher text
        // c.gridy=6; c.gridx=0; c.anchor=GridBagConstraints.NORTHWEST;
        // c.fill=GridBagConstraints.NONE;
        // panelDec.add(new JLabel("Bản mã (văn bản):"), c);
        // taDecResultFile = new JTextArea(5,20);
        // JScrollPane spDecResultFile = new JScrollPane(taDecResultFile);
        // c.gridx=1; c.gridwidth=2; c.fill=GridBagConstraints.BOTH; c.weighty=0.3;
        // panelDec.add(spDecResultFile, c);
        // c.gridwidth=1; c.weighty=0;

        // Row 7: Khóa (text)
        c.gridy = 7;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        panelDec.add(new JLabel("Khóa:"), c);
        txtDecKeyText = new JTextField("133457799BBCDFF1", 20);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panelDec.add(txtDecKeyText, c);

        // Row 8: Kết quả
        c.gridy = 8;
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.NONE;
        panelDec.add(new JLabel("Kết quả:"), c);
        taDecResultText = new JTextArea(5, 20);
        JScrollPane spDecResultText = new JScrollPane(taDecResultText);
        c.gridx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0.3;
        panelDec.add(spDecResultText, c);
        c.gridwidth = 1;
        c.weighty = 0;

        // Row 9: Buttons Giải mã văn bản
        c.gridy = 9;
        c.gridx = 2;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        btnDecDecryptText = new JButton("Giải mã văn bản");
        panelDec.add(btnDecDecryptText, c);
        btnDecDecryptText.addActionListener(e -> giaiMaVanBan());
        // --- TỔNG HỢP VÀO SPLIT PANE ---
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelEnc, panelDec);
        split.setResizeWeight(0.5);
        getContentPane().add(split, BorderLayout.CENTER);
    }

    private void maHoaVanBan() {
        String plaintext = taEncPlainFile.getText();
        String key = txtEncKeyText.getText().toUpperCase();
        if (plaintext.isEmpty() || key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập bản rõ và khóa!");
            return;
        }
        
        // Tạo đối tượng DESData với văn bản ASCII
        DESData data = new DESData(plaintext, key, true);
        
        // Chuyển đổi văn bản ASCII sang hex và chia thành các khối
        List<String> blocks = DES.splitHexToBlocks(data.getTextHex());
        StringBuilder encryptedHexBuilder = new StringBuilder();
        
        for (String block : blocks) {
            // Padding nếu block < 16 ký tự
            if (block.length() < 16) {
                block = String.format("%-16s", block).replace(' ', '0');
            }
            DESData blockData = new DESData(block, key);
            String encryptedBlock = blockData.encrypt();
            encryptedHexBuilder.append(encryptedBlock);
        }
        
        String encryptedHex = encryptedHexBuilder.toString();
        taEncResultText.setText(encryptedHex);
        
        // Hiển thị thông tin về văn bản gốc
        JOptionPane.showMessageDialog(this, 
            "Mã hóa thành công!\n" +
            "Văn bản ASCII: " + plaintext + "\n" +
            "Biểu diễn HEX: " + data.getTextHex() + "\n" +
            "Kết quả mã hóa: " + encryptedHex);
    }

    private void giaiMaVanBan() {
        String ciphertext = taDecCipherFile.getText().toUpperCase();
        String key = txtDecKeyText.getText().toUpperCase();
        if (ciphertext.isEmpty() || key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập bản mã và khóa!");
            return;
        }
        
        // Đảm bảo ciphertext là hex hợp lệ
        if (!ciphertext.matches("^[0-9A-F]+$")) {
            JOptionPane.showMessageDialog(this, "Bản mã phải ở dạng HEX (0-9, A-F)!", 
                                         "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<String> blocks = DES.splitHexToBlocks(ciphertext);
        StringBuilder decryptedHexBuilder = new StringBuilder();
        
        for (String block : blocks) {
            DESData data = new DESData(block, key);
            String decryptedBlock = data.decrypt();
            decryptedHexBuilder.append(decryptedBlock);
        }
        
        String decryptedHex = decryptedHexBuilder.toString();
        
        // Tạo đối tượng DESData từ kết quả giải mã để lấy biểu diễn ASCII
        DESData resultData = new DESData(decryptedHex, key);
        String decryptedAscii = resultData.getTextAscii();
        
        // Hiển thị kết quả chủ yếu là ASCII, với HEX ở dưới
        taDecResultText.setText(decryptedAscii + "\n\n(HEX: " + decryptedHex + ")");
        
        // Hiển thị thông báo thành công với thông tin chi tiết
        JOptionPane.showMessageDialog(this, 
            "Giải mã thành công!\n" +
            "Bản mã HEX: " + ciphertext + "\n" +
            "Kết quả giải mã (ASCII): " + decryptedAscii);
    }

    private String giaiMaVanBan2() {
        String ciphertext = taDecCipherFile.getText().toUpperCase();
        String key = txtEncKeyText.getText().toUpperCase();
        if (ciphertext.isEmpty() || key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập bản mã và khóa!");
            return "";
        }
        List<String> blocks = DES.splitHexToBlocks(ciphertext);
        StringBuilder decryptedHexBuilder = new StringBuilder();
        for (String block : blocks) {
            // Padding nếu block < 16 ký tự
            int[] cipherBinary = DES.textToBinary(block);
            int[] keyBinary = DES.textToBinary(key);
            int[] decryptedBinary = DES.decryptText(cipherBinary, keyBinary);
            String decryptedBlock = DES.binaryToHex(decryptedBinary);
            decryptedHexBuilder.append(decryptedBlock);
        }
        String decryptedHex = decryptedHexBuilder.toString();
        taDecResultText.setText(decryptedHex);
        return decryptedHex;
    }

    private String getFilePath(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private String getSaveFilePath(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu kết quả");
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private String getKeyFilePath(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file khóa");
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private String getKeyText(JTextField textField) {
        String key = textField.getText().trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập khóa!");
            return null;
        }
        return key;
    }

    private String generateRandomKey() {
        String hexChars = "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            int idx = (int) (Math.random() * hexChars.length());
            sb.append(hexChars.charAt(idx));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MaHoaDESUI().setVisible(true);
        });
    }
}
