package simka.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import simka.config.Koneksi;

/**
 * Panel CRUD Data Jabatan.
 * Pola ini dipakai sebagai cetakan untuk modul lain (karyawan, dll).
 */
public class JabatanPanel extends JPanel {

    private final JTextField txtNama = new JTextField(22);
    private final JTextField txtGaji = new JTextField(22);

    private final JButton btnSimpan = new JButton("Simpan");
    private final JButton btnUbah = new JButton("Ubah");
    private final JButton btnHapus = new JButton("Hapus");
    private final JButton btnBatal = new JButton("Batal");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nama Jabatan", "Gaji Pokok"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabel = new JTable(model);

    // null = tidak ada baris yang dipilih (mode tambah data baru)
    private Integer idTerpilih = null;

    public JabatanPanel() {
        setLayout(new BorderLayout(0, 16));

        JLabel judul = new JLabel("Data Jabatan");
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 26f));

        JPanel atas = new JPanel(new BorderLayout(0, 12));
        atas.add(judul, BorderLayout.NORTH);
        atas.add(buatForm(), BorderLayout.CENTER);

        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setRowHeight(24);
        tabel.getColumnModel().getColumn(0).setMaxWidth(80);

        NumberFormat rupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
        rupiah.setMinimumFractionDigits(0);
        rupiah.setMaximumFractionDigits(0);
        tabel.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(RIGHT);
            }

            @Override
            protected void setValue(Object value) {
                setText(value instanceof Number ? rupiah.format(value) : "");
            }
        });

        add(atas, BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        // Klik baris tabel -> isi form
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int baris = tabel.getSelectedRow();
            if (baris >= 0) {
                idTerpilih = (Integer) model.getValueAt(baris, 0);
                txtNama.setText((String) model.getValueAt(baris, 1));
                BigDecimal gaji = (BigDecimal) model.getValueAt(baris, 2);
                txtGaji.setText(gaji.stripTrailingZeros().toPlainString());
                aturTombol();
            }
        });

        btnSimpan.addActionListener(e -> simpan());
        btnUbah.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
        btnBatal.addActionListener(e -> bersihkan());

        muatData();
        aturTombol();
    }

    private JPanel buatForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Form Jabatan"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("Nama Jabatan"), c);
        c.gridx = 1;
        form.add(txtNama, c);

        c.gridx = 0; c.gridy = 1;
        form.add(new JLabel("Gaji Pokok (Rp)"), c);
        c.gridx = 1;
        form.add(txtGaji, c);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tombol.add(btnSimpan);
        tombol.add(btnUbah);
        tombol.add(btnHapus);
        tombol.add(btnBatal);

        c.gridx = 1; c.gridy = 2;
        form.add(tombol, c);

        return form;
    }

    // ===== Data =====
    private void muatData() {
        model.setRowCount(0);
        String sql = "SELECT id_jabatan, nama_jabatan, gaji_pokok FROM jabatan ORDER BY id_jabatan";
        try (Statement st = Koneksi.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getBigDecimal(3)});
            }
        } catch (SQLException ex) {
            error(ex);
        }
    }

    // ===== Validasi: mengembalikan gaji jika valid, null jika tidak =====
    private BigDecimal validasi() {
        String nama = txtNama.getText().trim();
        String gajiTeks = txtGaji.getText().trim();

        if (nama.isEmpty()) {
            peringatan("Nama jabatan tidak boleh kosong");
            txtNama.requestFocusInWindow();
            return null;
        }
        if (nama.length() > 50) {
            peringatan("Nama jabatan maksimal 50 karakter");
            txtNama.requestFocusInWindow();
            return null;
        }
        if (gajiTeks.isEmpty()) {
            peringatan("Gaji pokok tidak boleh kosong");
            txtGaji.requestFocusInWindow();
            return null;
        }
        if (!gajiTeks.matches("\\d{1,10}(\\.\\d{1,2})?")) {
            peringatan("Gaji pokok harus berupa angka positif, tanpa titik/koma pemisah ribuan.\nContoh: 4500000");
            txtGaji.requestFocusInWindow();
            return null;
        }
        return new BigDecimal(gajiTeks);
    }

    // ===== CRUD =====
    private void simpan() {
        BigDecimal gaji = validasi();
        if (gaji == null) {
            return;
        }
        String sql = "INSERT INTO jabatan (nama_jabatan, gaji_pokok) VALUES (?, ?)";
        try (PreparedStatement ps = Koneksi.getConnection().prepareStatement(sql)) {
            ps.setString(1, txtNama.getText().trim());
            ps.setBigDecimal(2, gaji);
            ps.executeUpdate();
            info("Jabatan berhasil disimpan");
            bersihkan();
            muatData();
        } catch (SQLIntegrityConstraintViolationException ex) {
            peringatan("Nama jabatan sudah ada");
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void ubah() {
        if (idTerpilih == null) {
            peringatan("Pilih jabatan di tabel terlebih dahulu");
            return;
        }
        BigDecimal gaji = validasi();
        if (gaji == null) {
            return;
        }
        String sql = "UPDATE jabatan SET nama_jabatan=?, gaji_pokok=? WHERE id_jabatan=?";
        try (PreparedStatement ps = Koneksi.getConnection().prepareStatement(sql)) {
            ps.setString(1, txtNama.getText().trim());
            ps.setBigDecimal(2, gaji);
            ps.setInt(3, idTerpilih);
            ps.executeUpdate();
            info("Jabatan berhasil diubah");
            bersihkan();
            muatData();
        } catch (SQLIntegrityConstraintViolationException ex) {
            peringatan("Nama jabatan sudah ada");
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void hapus() {
        if (idTerpilih == null) {
            peringatan("Pilih jabatan di tabel terlebih dahulu");
            return;
        }
        int pilih = JOptionPane.showConfirmDialog(this,
                "Hapus jabatan \"" + txtNama.getText().trim() + "\"?",
                "Konfirmasi hapus", JOptionPane.YES_NO_OPTION);
        if (pilih != JOptionPane.YES_OPTION) {
            return;
        }
        String sql = "DELETE FROM jabatan WHERE id_jabatan=?";
        try (PreparedStatement ps = Koneksi.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTerpilih);
            ps.executeUpdate();
            info("Jabatan berhasil dihapus");
            bersihkan();
            muatData();
        } catch (SQLIntegrityConstraintViolationException ex) {
            peringatan("Jabatan tidak bisa dihapus karena masih dipakai data karyawan");
        } catch (SQLException ex) {
            error(ex);
        }
    }

    // ===== Bantuan tampilan =====
    private void bersihkan() {
        txtNama.setText("");
        txtGaji.setText("");
        tabel.clearSelection();
        idTerpilih = null;
        aturTombol();
        txtNama.requestFocusInWindow();
    }

    private void aturTombol() {
        boolean adaPilihan = idTerpilih != null;
        btnSimpan.setEnabled(!adaPilihan);
        btnUbah.setEnabled(adaPilihan);
        btnHapus.setEnabled(adaPilihan);
    }

    private void info(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void peringatan(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private void error(SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Terjadi kesalahan database:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}