import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.io.*;
// import org.apache.poi.xwpf.usermodel.XWPFDocument;
// import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class MaHoaDESUI extends JFrame {
    // Các hằng số cho các loại bảng mã
    private static final String[] ENCODING_OPTIONS = {
            "UTF-8", "ASCII", "HEX", "BIN"
    };
    JFileChooser fileChooser;
    // Components bên Mã hóa
    private JTextField txtEncFile, txtEncOutName, txtEncKeyFile, txtEncKeyText;
    private JCheckBox chkEncDebug;
    private JButton btnEncBrowseFile, btnEncRandomKeyFile, btnEncEncryptFile;
    private JTextArea taEncPlainFile, taEncInputText, taEncResultText;
    private JButton btnEncSaveResult, btnEncEncryptText, btnEncRandomKeyText;
    // Components bên Giải mã
    private JTextField txtDecFile, txtDecOutName, txtDecKeyFile, txtDecKeyText;
    private JCheckBox chkDecDebug;
    private JButton btnDecBrowseFile, btnDecDecryptFile;
    private JTextArea taDecCipherFile, taDecResultFile, taDecResultText;
    private JButton btnDecLoadCipher, btnDecDecryptText;
    // Đối tượng DESData hiện tại cho việc giải mã
    private DESData currentDecryptData;
    // Thêm biến thành viên cho nút Reset
    private JButton btnReset;
    // Thêm biến thành viên cho các combobox chọn bảng mã
    private JComboBox<String> cboEncInputEncoding, cboEncOutputEncoding;
    private JComboBox<String> cboDecInputEncoding, cboDecOutputEncoding;

    public MaHoaDESUI() {
        setTitle("Mã hóa và Giải mã DES");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Đảm bảo sử dụng UTF-8 cho toàn bộ ứng dụng
        System.setProperty("file.encoding", "UTF-8");
        try {
            java.lang.reflect.Field charset = java.nio.charset.Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Bỏ qua nếu không thể thiết lập
        }

        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MaHoaDESUI().setVisible(true);
        });
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
            fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn file để mã hóa");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(java.io.File f) {
                    String name = f.getName().toLowerCase();
                    return f.isDirectory() || name.endsWith(".txt") || name.endsWith(".docx");
                }

                @Override
                public String getDescription() {
                    return "Supported Files (*.txt, *.docx)";
                }
            });

            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                txtEncFile.setText(selectedFile.getAbsolutePath());
                // Đọc nội dung file
                try {
                    String fileContent = readFileContent(selectedFile);
                    taEncPlainFile.setText(fileContent);

                    // Hiển thị tên file (không có đường dẫn) lên txtEncOutName, thêm hậu tố ".txt"
                    String fileName = selectedFile.getName();
                    int dotIdx = fileName.lastIndexOf('.');
                    String baseName = (dotIdx > 0) ? fileName.substring(0, dotIdx) : fileName;
                    String outName = baseName + "_encrypted.txt";
                    txtEncOutName.setText(outName);
                } catch (java.io.IOException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi đọc file: " + ex.getMessage());
                } catch (NoClassDefFoundError ex) {
                    JOptionPane.showMessageDialog(this, "Thiếu thư viện Apache POI. Vui lòng thêm poi-ooxml.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi không xác định khi đọc file: " + ex.getMessage());
                }
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

                // Lấy thông tin bảng mã
                String inputEncoding = (String) cboEncInputEncoding.getSelectedItem();
                String outputEncoding = (String) cboEncOutputEncoding.getSelectedItem();

                // Đọc nội dung file
                String fileContent = readFileContent(inputFile);
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
                String extension = inputFile.getName().toLowerCase().endsWith(".txt") ? ".txt" : ".docx";
                if (outName.isEmpty()) {
                    String baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
                    outName = baseName + "_encrypted" + extension;
                    txtEncOutName.setText(outName);
                }
                java.io.File outFile = new java.io.File(inputFile.getParentFile(), outName);
                try (java.io.FileWriter fw = new java.io.FileWriter(outFile)) {
                    fw.write(encryptedHex);
                }

                // Lưu ra file bảo mật (kèm khóa, bảng mã và checksum)
                String secureFileName = outName + ".secure";
                java.io.File secureFile = new java.io.File(inputFile.getParentFile(), secureFileName);
                DESData encryptedData = new DESData(encryptedHex, key, inputEncoding, outputEncoding);
                encryptedData.saveToFile(secureFile.getAbsolutePath());

                JOptionPane.showMessageDialog(this,
                        "Đã mã hóa và lưu file:\n" +
                                "- File thông thường: " + outFile.getAbsolutePath() + "\n" +
                                "- File bảo mật (kèm khóa và bảng mã): " + secureFile.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi mã hóa file: " + ex.getMessage());
            }
        });
        panelEnc.add(btnEncEncryptFile, c);

        // Row 4: Bảng mã đầu vào và đầu ra
        c.gridy = 4;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        panelEnc.add(new JLabel("Bảng mã đầu vào:"), c);

        // Sử dụng biến thành viên thay vì tạo biến cục bộ mới
        cboEncInputEncoding = new JComboBox<>(ENCODING_OPTIONS);
        cboEncInputEncoding.setSelectedItem("UTF-8");
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        panelEnc.add(cboEncInputEncoding, c);

        c.gridx = 2;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        panelEnc.add(new JLabel("Bảng mã đầu ra:"), c);

        // Sử dụng biến thành viên thay vì tạo biến cục bộ mới
        cboEncOutputEncoding = new JComboBox<>(ENCODING_OPTIONS);
        cboEncOutputEncoding.setSelectedItem("HEX");
        c.gridx = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        panelEnc.add(cboEncOutputEncoding, c);
        c.weightx = 0;

        // Row 5: Separator
        c.gridy = 5;
        c.gridx = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        panelEnc.add(new JSeparator(), c);
        c.gridwidth = 1;
        c.weightx = 0;
        // Row 6: Tiêu đề "Mã hóa văn bản"
        c.gridy = 6;
        c.gridx = 0;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        JLabel lblEncTextSection = new JLabel("MÃ HÓA VĂN BẢN");
        lblEncTextSection.setFont(new Font(lblEncTextSection.getFont().getName(), Font.BOLD, 16));
        panelEnc.add(lblEncTextSection, c);
        c.gridwidth = 1;

        // Row 7: Bản rõ (file)
        c.gridy = 7;
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
        // Row 8: Khóa (text)
        c.gridy = 8;
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

        // Row 9: Kết quả (text)
        c.gridy = 9;
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

        // Row 10: Buttons lưu và mã hóa text
        c.gridy = 10;
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        btnEncSaveResult = new JButton("Lưu vào file");
        panelEnc.add(btnEncSaveResult, c);
        btnEncSaveResult.addActionListener(e -> {
            String encryptedText = taEncResultText.getText();
            String plaintext = taEncPlainFile.getText();
            String key = txtEncKeyText.getText().trim().toUpperCase();

            if (encryptedText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu để lưu!");
                return;
            }
            if (plaintext.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có bản rõ để tạo checksum!");
                return;
            }
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập khóa!");
                return;
            }

            // Lưu file .txt chỉ chứa ciphertext
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu file ciphertext (.txt)");
            fileChooser.setSelectedFile(new java.io.File("ciphertext.txt"));
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                // Thêm phần mở rộng .txt nếu chưa có
                if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                    fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".txt");
                }
                try {
                    // Lấy thông tin bảng mã
                    String inputEncoding = (String) cboEncInputEncoding.getSelectedItem();
                    String outputEncoding = (String) cboEncOutputEncoding.getSelectedItem();

                    // Lưu file thông thường chứa văn bản đã mã hóa
                    try (java.io.FileWriter fw = new java.io.FileWriter(fileToSave)) {
                        fw.write(encryptedText);
                    }

                    // Lưu file .secure
                    String secureFilePath = fileToSave.getAbsolutePath();
                    if (!secureFilePath.endsWith(".secure")) {
                        secureFilePath += ".secure";
                    }
                    DESData encryptedData = new DESData(encryptedText, key, inputEncoding, outputEncoding);
                    encryptedData.saveToFile(secureFilePath);

                    JOptionPane.showMessageDialog(this,
                            "Đã lưu thành công!\n" +
                                    "- File thông thường: " + fileToSave.getAbsolutePath() + "\n" +
                                    "- File bảo mật (.secure): " + secureFilePath);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi lưu file: " + ex.getMessage());
                }
            }
        });

        c.gridx = 2;
        c.anchor = GridBagConstraints.EAST;
        btnEncEncryptText = new JButton("Mã hóa văn bản");
        btnEncEncryptText.addActionListener(e -> maHoaVanBan());
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
        fileChooser = new JFileChooser();
        btnDecBrowseFile.addActionListener(e -> {
            String key = txtDecKeyFile.getText().trim().toUpperCase();
            fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn file để giải mã");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(java.io.File f) {
                    String name = f.getName().toLowerCase();
                    return f.isDirectory() || name.endsWith(".txt") || name.endsWith(".docx")
                            || name.endsWith(".secure");
                }

                @Override
                public String getDescription() {
                    return "Supported Files (*.txt, *.docx, *.secure)";
                }
            });

            int userSelection = fileChooser.showOpenDialog(MaHoaDESUI.this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToOpen = fileChooser.getSelectedFile();
                txtDecFile.setText(fileToOpen.getAbsolutePath());

                try {
                    // Kiểm tra xem file có phải là file bảo mật không
                    String fileContent;
                    if (fileToOpen.getName().toLowerCase().endsWith(".secure")) {
                        // File bảo mật - đọc như text thông thường
                        fileContent = new String(java.nio.file.Files.readAllBytes(fileToOpen.toPath()),
                                StandardCharsets.UTF_8).trim();
                    } else {
                        // File thông thường - sử dụng phương thức đọc phù hợp
                        fileContent = readFileContent(fileToOpen);
                    }
                    String[] parts = fileContent.split("\\|");

                    if (parts.length >= 2 && fileToOpen.getName().toLowerCase().endsWith(".secure")) {
                        // Đây có thể là file bảo mật
                        try {
                            // Đọc file theo định dạng bảo mật và lưu vào đối tượng DESData
                            DESData data = DESData.loadFromFile(fileToOpen.getAbsolutePath(), key);

                            // Lưu đối tượng DESData vào biến thành viên để sử dụng sau này
                            currentDecryptData = data;

                            // Hiển thị nội dung văn bản lên taDecCipherFile
                            taDecCipherFile.setText(data.getTextHex());

                            // Hiển thị khóa đọc được lên txtDecKeyFile và txtDecKeyText
                            // Cập nhật tên file đầu ra
                            String fileName = fileToOpen.getName();
                            // Loại bỏ phần mở rộng .secure nếu có
                            if (fileName.toLowerCase().endsWith(".secure")) {
                                fileName = fileName.substring(0, fileName.length() - 7);
                            }
                            // Loại bỏ phần mở rộng .txt nếu có
                            if (fileName.toLowerCase().endsWith(".txt")) {
                                fileName = fileName.substring(0, fileName.length() - 4);
                            }
                            String outName = fileName + "_decrypted.txt";
                            txtDecOutName.setText(outName);
                            JOptionPane.showMessageDialog(this,
                                    "Đã tải file bảo mật thành công!\n");
                        } catch (IllegalArgumentException ex) {
                            txtDecFile.setText("");
                            // Nếu file không đúng định dạng hoặc khóa đã bị sửa
                            JOptionPane.showMessageDialog(this,
                                    "Lỗi khi đọc file bảo mật: " + ex.getMessage(),
                                    "Lỗi tính toàn vẹn", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        // Đây là file thông thường
                        // Tạo đối tượng DESData với nội dung file và khóa mặc định
                        String defaultKey = txtDecKeyFile.getText().trim();
                        currentDecryptData = new DESData(fileContent, defaultKey);

                        // Hiển thị nội dung văn bản lên taDecCipherFile
                        taDecCipherFile.setText(fileContent);

                        // Cập nhật tên file đầu ra
                        String fileName = fileToOpen.getName();
                        // Loại bỏ phần mở rộng .txt nếu có
                        if (fileName.toLowerCase().endsWith(".txt")) {
                            fileName = fileName.substring(0, fileName.length() - 4);
                        }
                        String outName = fileName + "_decrypted.txt";
                        txtDecOutName.setText(outName);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi đọc file: " + ex.getMessage());
                } catch (NoClassDefFoundError ex) {
                    JOptionPane.showMessageDialog(this, "Thiếu thư viện Apache POI. Vui lòng thêm poi-ooxml.");
                }
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
            // Kiểm tra xem đã chọn file chưa
            String filePath = txtDecFile.getText();
            if (filePath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn file để giải mã!");
                return;
            }

            // Kiểm tra xem đã nhập khóa chưa
            String key = txtDecKeyFile.getText().trim();
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập khóa!");
                return;
            }

            // Tạo đối tượng File từ đường dẫn
            java.io.File inputFile = new java.io.File(filePath);
            if (!inputFile.exists() || !inputFile.isFile()) {
                JOptionPane.showMessageDialog(this, "File không tồn tại!");
                return;
            }

            try {
                // Sử dụng đối tượng DESData hiện tại nếu có, nếu không thì tạo mới
                if (currentDecryptData == null || !key.equals(currentDecryptData.getKey())) {
                    // Đọc nội dung file (hex)
                    String fileHex = taDecCipherFile.getText().trim();
                    currentDecryptData = new DESData(fileHex, key);
                }

                // Nếu trường khóa có thể chỉnh sửa, cập nhật khóa nếu đã thay đổi
                if (txtDecKeyFile.isEditable() && !key.equals(currentDecryptData.getKey())) {
                    currentDecryptData.setKey(key);
                }

                // Giải mã sử dụng đối tượng DESData
                String decryptedHex = currentDecryptData.decrypt();

                // Kiểm tra xem decryptedHex có phải là hex hợp lệ không
                if (!decryptedHex.matches("^[0-9A-F]*$")) {
                    JOptionPane.showMessageDialog(this, "Kết quả giải mã không phải là hex hợp lệ!",
                            "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Chuyển đổi hex sang bytes
                byte[] decryptedBytes = new byte[decryptedHex.length() / 2];
                for (int i = 0; i < decryptedBytes.length; i++) {
                    int index = i * 2;
                    int v = Integer.parseInt(decryptedHex.substring(index, index + 2), 16);
                    decryptedBytes[i] = (byte) v;
                }

                // Chuyển đổi sang UTF-8
                String decryptedAscii;
                try {
                    decryptedAscii = new String(decryptedBytes, StandardCharsets.UTF_8);
                    // Cắt bớt dấu space thừa ở đầu và cuối văn bản
                    decryptedAscii = decryptedAscii.trim();
                } catch (Exception ex) {
                    // Nếu không thể chuyển đổi, sử dụng ISO-8859-1 (Latin-1)
                    decryptedAscii = new String(decryptedBytes, StandardCharsets.ISO_8859_1).trim();
                }

                // Hiển thị kết quả
                taDecResultText.setText(decryptedAscii);

                // Xử lý tên file đầu ra
                String outName = txtDecOutName.getText();
                if (outName.isEmpty()) {
                    // Tạo tên file từ tên file đầu vào
                    String fileName = inputFile.getName();
                    // Loại bỏ phần mở rộng .txt nếu có
                    if (fileName.toLowerCase().endsWith(".txt")) {
                        fileName = fileName.substring(0, fileName.length() - 4);
                    }
                    // Loại bỏ phần mở rộng .secure nếu có
                    if (fileName.toLowerCase().endsWith(".secure")) {
                        fileName = fileName.substring(0, fileName.length() - 7);
                    }
                    outName = fileName + "_decrypted.txt";
                    txtDecOutName.setText(outName);
                }

                // Lưu ra file mới
                java.io.File outFile = new java.io.File(inputFile.getParentFile(), outName);
                try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(outFile), StandardCharsets.UTF_8)) {
                    writer.write(decryptedAscii);
                }

                JOptionPane.showMessageDialog(this, "Đã giải mã và lưu file: " + outFile.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi giải mã file: " + ex.getMessage());
            }
        });
        c.gridx = 2;
        c.anchor = GridBagConstraints.EAST;
        panelDec.add(btnDecDecryptFile, c);

        // Row 4: Bảng mã đầu vào và đầu ra
        c.gridy = 4;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        panelDec.add(new JLabel("Bảng mã đầu vào:"), c);

        // Sử dụng biến thành viên thay vì tạo biến cục bộ mới
        cboDecInputEncoding = new JComboBox<>(ENCODING_OPTIONS);
        cboDecInputEncoding.setSelectedItem("HEX");
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        panelDec.add(cboDecInputEncoding, c);

        c.gridx = 2;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        panelDec.add(new JLabel("Bảng mã đầu ra:"), c);

        // Sử dụng biến thành viên thay vì tạo biến cục bộ mới
        cboDecOutputEncoding = new JComboBox<>(ENCODING_OPTIONS);
        cboDecOutputEncoding.setSelectedItem("UTF-8");
        c.gridx = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        panelDec.add(cboDecOutputEncoding, c);
        c.weightx = 0;

        // Row 5: Separator
        c.gridy = 5;
        c.gridx = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        panelDec.add(new JSeparator(), c);
        c.gridwidth = 1;
        c.weightx = 0;

        // Row 6: Tiêu đề "Giải mã văn bản"
        c.gridy = 6;
        c.gridx = 0;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        JLabel lblDecTextSection = new JLabel("GIẢI MÃ VĂN BẢN");
        lblDecTextSection.setFont(new Font(lblDecTextSection.getFont().getName(), Font.BOLD, 16));
        panelDec.add(lblDecTextSection, c);
        c.gridwidth = 1;

        // Row 7: Bản mã (file)
        c.gridy = 7;
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

        // Row 8: Khóa (text)
        c.gridy = 8;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        panelDec.add(new JLabel("Khóa:"), c);
        txtDecKeyText = new JTextField("133457799BBCDFF1", 20);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panelDec.add(txtDecKeyText, c);

        // Row 9: Kết quả
        c.gridy = 9;
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

        // Row 10: Buttons Giải mã văn bản và Lưu kết quả
        c.gridy = 10;
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        JButton btnDecSaveResult = new JButton("Lưu kết quả");
        panelDec.add(btnDecSaveResult, c);
        btnDecSaveResult.addActionListener(e -> {
            String decryptedText = taDecResultText.getText();
            if (decryptedText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu để lưu!");
                return;
            }

            // Cắt bớt dấu space thừa ở đầu và cuối văn bản
            decryptedText = decryptedText.trim();

            fileChooser.setDialogTitle("Lưu kết quả giải mã");
            fileChooser.setSelectedFile(new java.io.File("decrypted_text.txt"));
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();

                // Thêm phần mở rộng .txt nếu người dùng không nhập
                if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                    fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".txt");
                }

                try {
                    // Lưu văn bản đã giải mã ra file với UTF-8
                    try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                            new java.io.FileOutputStream(fileToSave), StandardCharsets.UTF_8)) {
                        writer.write(decryptedText);
                    }

                    JOptionPane.showMessageDialog(this,
                            "Đã lưu kết quả giải mã thành công!\n" +
                                    "File: " + fileToSave.getAbsolutePath() + "\n" +
                                    "Mã hóa: UTF-8");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi lưu file: " + ex.getMessage());
                }
            }
        });

        c.gridx = 2;
        c.anchor = GridBagConstraints.EAST;
        btnDecDecryptText = new JButton("Giải mã văn bản");
        panelDec.add(btnDecDecryptText, c);
        btnDecDecryptText.addActionListener(e -> {
            // Nếu trường khóa bị vô hiệu hóa, sử dụng khóa hiện tại
            if (!txtDecKeyText.isEditable()) {
                giaiMaVanBan();
            } else {
                // Nếu trường khóa có thể chỉnh sửa, kiểm tra xem đã nhập khóa chưa
                String key = txtDecKeyText.getText().trim();
                String plainText = taDecCipherFile.getText().trim();
                if (key.isEmpty() || plainText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ khóa và bản mã!");
                    return;
                }
                giaiMaVanBan();
            }
        });
        // --- TỔNG HỢP VÀO SPLIT PANE ---
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelEnc, panelDec);
        split.setResizeWeight(0.5);
        getContentPane().add(split, BorderLayout.CENTER);

        // Thêm nút Reset vào phía dưới giao diện
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnReset = new JButton("Reset Tất Cả");
        btnReset.setFont(new Font(btnReset.getFont().getName(), Font.BOLD, 14));
        btnReset.setBackground(new Color(255, 100, 100));
        btnReset.setForeground(Color.WHITE);
        btnReset.addActionListener(e -> resetAll());
        bottomPanel.add(btnReset);

        // Thêm panel chứa nút Reset vào phía dưới của giao diện
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    private void maHoaVanBan() {
        String plaintext = taEncPlainFile.getText().trim();
        String key = txtEncKeyText.getText().toUpperCase().trim();

        if (plaintext.isEmpty() || key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập bản rõ và khóa!");
            return;
        }

        // Lấy bảng mã đầu vào và đầu ra
        String inputEncoding = (String) cboEncInputEncoding.getSelectedItem();
        String outputEncoding = (String) cboEncOutputEncoding.getSelectedItem();

        try {
            // Chuyển đổi bản rõ sang HEX (nếu chưa phải HEX)
            String textHex;
            if (!"HEX".equals(inputEncoding)) {
                textHex = convertEncoding(plaintext, inputEncoding, "HEX");
            } else {
                textHex = plaintext;
            }

            // Chia thành các khối 16 ký tự hex (64 bit)
            List<String> blocks = DES.splitHexToBlocks(textHex);
            StringBuilder encryptedHexBuilder = new StringBuilder();

            for (String block : blocks) {
                // Padding nếu block < 16 ký tự
                if (block.length() < 16) {
                    block = String.format("%-16s", block).replace(' ', '0');
                }

                // Chuyển đổi hex sang binary
                int[] plaintextBinary = DES.textToBinary(block);
                int[] keyBinary = DES.textToBinary(key);

                // Mã hóa
                int[] encryptedBinary = DES.encryptText(plaintextBinary, keyBinary);

                // Chuyển đổi binary sang hex
                String encryptedBlock = DES.binaryToHex(encryptedBinary);
                encryptedHexBuilder.append(encryptedBlock);
            }

            String encryptedHex = encryptedHexBuilder.toString();

            // Chuyển đổi kết quả sang bảng mã đầu ra
            String encryptedResult;
            if (!"HEX".equals(outputEncoding)) {
                encryptedResult = convertEncoding(encryptedHex, "HEX", outputEncoding);
            } else {
                encryptedResult = encryptedHex;
            }

            taEncResultText.setText(encryptedResult);

            // Hiển thị thông tin về văn bản gốc
            JOptionPane.showMessageDialog(this,
                    "Mã hóa thành công!\n" +
                            "Bảng mã đầu vào: " + inputEncoding + "\n" +
                            "Bảng mã đầu ra: " + outputEncoding);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi mã hóa: " + ex.getMessage());
        }
    }

    private String readFileContent(java.io.File file) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".txt") || fileName.contains("encrypted")) {
            // Đọc file text thông thường
            try {
                return new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
            } catch (IOException ex) {
                // Thử với ISO-8859-1 nếu UTF-8 không thành công
                return new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1).trim();
            }
        }
        // else if (fileName.endsWith(".docx") && !fileName.contains("encrypted")) {
        // // Đọc file Word .docx
        // try (FileInputStream fis = new FileInputStream(file);
        // XWPFDocument doc = new XWPFDocument(fis)) {
        // StringBuilder content = new StringBuilder();
        // for (XWPFParagraph p : doc.getParagraphs()) {
        // String paragraphText = p.getText();
        // if (paragraphText != null && !paragraphText.trim().isEmpty()) {
        // content.append(paragraphText).append("\n");
        // }
        // }
        // return content.toString().trim();
        // } catch (Exception ex) {
        // throw new IOException("Không thể đọc file .docx: " + ex.getMessage(), ex);
        // }
        // }
        else if (fileName.endsWith(".secure")) {
            // Đọc file bảo mật
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
            String[] parts = content.split("\\|");
            if (parts.length >= 3) {
                return parts[parts.length - 1]; // Trả về textHex (phần cuối)
            }
            return "";
        } else {
            // File không được hỗ trợ, thử đọc như text
            try {
                return new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
            } catch (IOException ex) {
                return new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
            }
        }
    }

    private void giaiMaVanBan() {
        String ciphertext = taDecCipherFile.getText().trim();
        String key = txtDecKeyText.getText().toUpperCase().trim();

        if (ciphertext.isEmpty() || key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập bản mã và khóa!");
            return;
        }

        // Lấy bảng mã đầu vào và đầu ra
        String inputEncoding = (String) cboDecInputEncoding.getSelectedItem();
        String outputEncoding = (String) cboDecOutputEncoding.getSelectedItem();

        try {
            // Chuyển đổi bản mã sang HEX (nếu chưa phải HEX)
            String cipherHex;
            if (!"HEX".equals(inputEncoding)) {
                cipherHex = convertEncoding(ciphertext, inputEncoding, "HEX");
            } else {
                cipherHex = ciphertext;
            }

            // Đảm bảo cipherHex là hex hợp lệ
            if (!cipherHex.matches("^[0-9A-F]+$")) {
                JOptionPane.showMessageDialog(this, "Bản mã phải ở dạng HEX (0-9, A-F) sau khi chuyển đổi!",
                        "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Chia thành các khối 16 ký tự hex (64 bit)
            List<String> blocks = DES.splitHexToBlocks(cipherHex);
            StringBuilder decryptedHexBuilder = new StringBuilder();

            for (String block : blocks) {
                // Padding nếu block < 16 ký tự
                if (block.length() < 16) {
                    block = String.format("%-16s", block).replace(' ', '0');
                }

                // Chuyển đổi hex sang binary
                int[] cipherBinary = DES.textToBinary(block);
                int[] keyBinary = DES.textToBinary(key);

                // Giải mã
                int[] decryptedBinary = DES.decryptText(cipherBinary, keyBinary);

                // Chuyển đổi binary sang hex
                String decryptedBlock = DES.binaryToHex(decryptedBinary);
                decryptedHexBuilder.append(decryptedBlock);
            }

            String decryptedHex = decryptedHexBuilder.toString();

            // Chuyển đổi kết quả sang bảng mã đầu ra
            String decryptedResult;
            if (!"HEX".equals(outputEncoding)) {
                decryptedResult = convertEncoding(decryptedHex, "HEX", outputEncoding);
            } else {
                decryptedResult = decryptedHex;
            }

            taDecResultText.setText(decryptedResult);

            // Hiển thị thông tin về văn bản giải mã
            JOptionPane.showMessageDialog(this,
                    "Giải mã thành công!\n" +
                            "Bảng mã đầu vào: " + inputEncoding + "\n" +
                            "Bảng mã đầu ra: " + outputEncoding);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi giải mã: " + ex.getMessage());
        }
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

    // Phương thức kiểm tra xem văn bản có đọc được không
    private boolean isPrintableAscii(String text) {
        // Kiểm tra xem văn bản có chứa quá nhiều ký tự không in được không
        int nonPrintableCount = 0;
        for (char c : text.toCharArray()) {
            if (c < 32 || c > 126) {
                nonPrintableCount++;
            }
        }
        // Nếu hơn 20% ký tự không in được, coi như văn bản không đọc được
        return nonPrintableCount < text.length() * 0.2;
    }

    // Thêm phương thức để chọn mã hóa khi lưu kết quả
    private void saveDecryptedTextWithEncoding() {
        String decryptedText = taDecResultText.getText();
        if (decryptedText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để lưu!");
            return;
        }

        // Tạo dialog để chọn mã hóa
        String[] encodings = { "UTF-8", "ISO-8859-1", "windows-1252" };
        String selectedEncoding = (String) JOptionPane.showInputDialog(
                this,
                "Chọn mã hóa để lưu file:",
                "Chọn mã hóa",
                JOptionPane.QUESTION_MESSAGE,
                null,
                encodings,
                encodings[0]);

        if (selectedEncoding == null) {
            return; // Người dùng đã hủy
        }

        // Tách phần văn bản theo mã hóa đã chọn
        String textToSave = "";
        if (selectedEncoding.equals("UTF-8") && decryptedText.contains("UTF-8: ")) {
            int start = decryptedText.indexOf("UTF-8: ") + 7;
            int end = decryptedText.indexOf("\n\n", start);
            if (end > start) {
                textToSave = decryptedText.substring(start, end).trim();
            }
        } else if (selectedEncoding.equals("ISO-8859-1") && decryptedText.contains("ISO-8859-1: ")) {
            int start = decryptedText.indexOf("ISO-8859-1: ") + 12;
            int end = decryptedText.indexOf("\n\n", start);
            if (end > start) {
                textToSave = decryptedText.substring(start, end).trim();
            }
        } else if (selectedEncoding.equals("windows-1252") && decryptedText.contains("Windows-1252: ")) {
            int start = decryptedText.indexOf("Windows-1252: ") + 14;
            int end = decryptedText.indexOf("\n\n", start);
            if (end > start) {
                textToSave = decryptedText.substring(start, end).trim();
            }
        }

        if (textToSave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không thể trích xuất văn bản với mã hóa đã chọn!");
            return;
        }

        // Hiển thị hộp thoại lưu file
        fileChooser.setDialogTitle("Lưu kết quả giải mã");
        fileChooser.setSelectedFile(new java.io.File("decrypted_text.txt"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            // Thêm phần mở rộng .txt nếu người dùng không nhập
            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".txt");
            }

            try {
                // Lưu văn bản đã giải mã ra file với mã hóa đã chọn
                try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(fileToSave), selectedEncoding)) {
                    writer.write(textToSave);
                }

                JOptionPane.showMessageDialog(this,
                        "Đã lưu kết quả giải mã thành công!\n" +
                                "File: " + fileToSave.getAbsolutePath() + "\n" +
                                "Mã hóa: " + selectedEncoding);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu file: " + ex.getMessage());
            }
        }
    }

    // Thêm phương thức resetAll để reset tất cả các trường về trạng thái ban đầu
    private void resetAll() {
        // Hiển thị hộp thoại xác nhận
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn reset tất cả các trường về trạng thái ban đầu không?",
                "Xác nhận Reset", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        currentDecryptData = null;
        // Reset các trường bên Mã hóa
        txtEncFile.setText("");
        txtEncOutName.setText("");
        txtEncKeyFile.setText("");
        txtEncKeyText.setText("");
        taEncPlainFile.setText("");
        cboEncInputEncoding.setSelectedItem("UTF-8");
        cboEncOutputEncoding.setSelectedItem("HEX");
        // taEncInputText đã bị comment out, không sử dụng
        // taEncInputText.setText("");
        taEncResultText.setText("");

        // Reset các trường bên Giải mã
        txtDecFile.setText("");
        txtDecOutName.setText("");
        txtDecKeyFile.setText("");
        txtDecKeyText.setText("");
        taDecCipherFile.setText("");
        // taDecResultFile đã bị comment out, không sử dụng
        // taDecResultFile.setText("");
        taDecResultText.setText("");
        cboDecInputEncoding.setEnabled(rootPaneCheckingEnabled);
        cboDecInputEncoding.setSelectedItem("HEX");
        cboDecOutputEncoding.setSelectedItem("UTF-8");
        // Cho phép chỉnh sửa các trường khóa
        txtDecKeyFile.setEditable(true);
        txtDecKeyText.setEditable(true);

        // Reset đối tượng DESData hiện tại

        // Thông báo đã reset thành công
        JOptionPane.showMessageDialog(this, "Đã reset tất cả các trường về trạng thái ban đầu!");
    }

    // Thêm các phương thức chuyển đổi giữa các bảng mã
    private String convertEncoding(String input, String fromEncoding, String toEncoding) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        try {
            // Chuyển đổi từ bảng mã nguồn sang bytes
            byte[] bytes;

            switch (fromEncoding) {
                case "UTF-8":
                    bytes = input.getBytes(StandardCharsets.UTF_8);
                    break;
                case "ASCII":
                    bytes = input.getBytes(StandardCharsets.US_ASCII);
                    break;
                case "HEX":
                    // Chuyển từ HEX sang bytes
                    bytes = hexToBytes(input);
                    break;
                case "BIN":
                    // Chuyển từ BIN sang bytes
                    bytes = binToBytes(input);
                    break;
                case "DEC":
                    // Chuyển từ DEC sang bytes
                    bytes = decToBytes(input);
                    break;
                default:
                    return input;
            }

            // Chuyển đổi từ bytes sang bảng mã đích
            switch (toEncoding) {
                case "UTF-8":
                    return new String(bytes, StandardCharsets.UTF_8);
                case "ASCII":
                    return new String(bytes, StandardCharsets.US_ASCII);
                case "HEX":
                    // Chuyển từ bytes sang HEX
                    return bytesToHex(bytes);
                case "BIN":
                    // Chuyển từ bytes sang BIN
                    return bytesToBin(bytes);
                case "DEC":
                    // Chuyển từ bytes sang DEC
                    return bytesToDec(bytes);
                default:
                    return input;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi chuyển đổi bảng mã: " + e.getMessage(),
                    "Lỗi chuyển đổi", JOptionPane.ERROR_MESSAGE);
            return input;
        }
    }

    // Chuyển từ HEX sang bytes
    private byte[] hexToBytes(String hex) {
        // Loại bỏ khoảng trắng và các ký tự không phải hex
        hex = hex.replaceAll("[^0-9A-Fa-f]", "").toUpperCase();

        // Đảm bảo độ dài chuỗi hex là chẵn
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int index = i * 2;
            int value = Integer.parseInt(hex.substring(index, index + 2), 16);
            bytes[i] = (byte) value;
        }
        return bytes;
    }

    // Chuyển từ bytes sang HEX
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    // Chuyển từ BIN sang bytes
    private byte[] binToBytes(String bin) {
        // Loại bỏ khoảng trắng và các ký tự không phải 0, 1
        bin = bin.replaceAll("[^01]", "");

        // Đảm bảo độ dài chuỗi bin là bội số của 8
        while (bin.length() % 8 != 0) {
            bin = "0" + bin;
        }

        byte[] bytes = new byte[bin.length() / 8];
        for (int i = 0; i < bytes.length; i++) {
            int index = i * 8;
            String byteStr = bin.substring(index, index + 8);
            bytes[i] = (byte) Integer.parseInt(byteStr, 2);
        }
        return bytes;
    }

    // Chuyển từ bytes sang BIN
    private String bytesToBin(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
            sb.append(" "); // Thêm khoảng trắng giữa các byte để dễ đọc
        }
        return sb.toString().trim();
    }

    // Chuyển từ DEC sang bytes
    private byte[] decToBytes(String dec) {
        // Loại bỏ khoảng trắng và các ký tự không phải số
        String[] values = dec.split("\\s+");
        byte[] bytes = new byte[values.length];

        for (int i = 0; i < values.length; i++) {
            try {
                int value = Integer.parseInt(values[i]);
                bytes[i] = (byte) (value & 0xFF);
            } catch (NumberFormatException e) {
                // Bỏ qua các giá trị không phải số
            }
        }
        return bytes;
    }

    // Chuyển từ bytes sang DEC
    private String bytesToDec(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(b & 0xFF);
            sb.append(" "); // Thêm khoảng trắng giữa các byte để dễ đọc
        }
        return sb.toString().trim();
    }
}
