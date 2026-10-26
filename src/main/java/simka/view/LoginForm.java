package simka.view;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import simka.config.Koneksi;

public class LoginForm extends JFrame {

    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JButton btnLogin = new JButton("Login");

    public LoginForm() {
        setTitle("SIMKA - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel judul = new JLabel("SIMKA", SwingConstants.CENTER);
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 30f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        root.add(judul, c);

        JLabel sub = new JLabel("Aplikasi Absensi Karyawan", SwingConstants.CENTER);
        c.gridy = 1;
        root.add(sub, c);

        c.gridwidth = 1;
        c.gridy = 2; c.gridx = 0;
        root.add(new JLabel("Username"), c);
        c.gridx = 1;
        root.add(txtUsername, c);

        c.gridy = 3; c.gridx = 0;
        root.add(new JLabel("Password"), c);
        c.gridx = 1;
        root.add(txtPassword, c);

        c.gridy = 4; c.gridx = 0; c.gridwidth = 2;
        root.add(btnLogin, c);

        setContentPane(root);
        getRootPane().setDefaultButton(btnLogin);   // tekan Enter = klik Login
        btnLogin.addActionListener(e -> prosesLogin());

        pack();
        setLocationRelativeTo(null);
    }

    private void prosesLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password wajib diisi",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT id FROM users WHERE username=? AND password=SHA2(?,256)";
        try (PreparedStatement ps = Koneksi.getConnection().prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    new DashboardForm(user).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Username atau password salah",
                            "Login gagal", JOptionPane.ERROR_MESSAGE);
                    txtPassword.setText("");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Tidak bisa terhubung ke database.\nPastikan MySQL di XAMPP menyala dan schema.sql sudah dijalankan.\n\n"
                    + ex.getMessage(),
                    "Error database", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
