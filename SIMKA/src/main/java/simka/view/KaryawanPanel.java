package simka.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import simka.config.Koneksi;

/**
 * Panel CRUD Data Karyawan (dengan pilihan jabatan dan pencarian).
 * Memakai pola yang sama dengan JabatanPanel.
 */
public class KaryawanPanel extends JPanel {

    /** Item untuk combo jabatan: menyimpan id, menampilkan nama. */
    private record ItemJabatan(int id, String nama) {
        @Override
        public String toString() {
            return nama;
        }
    }

    /** Data form yang sudah lolos validasi. */
    private record DataForm(String nik, String nama, ItemJabatan jabatan,
                            String alamat, String telepon, LocalDate tanggal) {
    }

    private final JTextField txtNik = new JTextField(18);
    private final JTextField txtNama = new JTextField(18);
    private final JComboBox<ItemJabatan> cmbJabatan = new JComboBox<>();
    private final JTextField txtAlamat = new JTextField(24);
    private final JTextField txtTelepon = new JTextField(24);
    private final JTextField txtTanggal = new JTextField(24);

    private final JTextField txtCari = new JTextField(24);
    private final JButton btnCari = new JButton("Cari");
    private final JButton btnReset = new JButton("Reset");

    private final JButton btnSimpan = new JButton("Simpan");
    private final JButton btnUbah = new JButton("Ubah");
    private final JButton btnHapus = new JButton("Hapus");
    private final JButton btnBatal = new JButton("Batal");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "NIK", "Nama", "Jabatan", "Alamat", "No. Telepon", "Tanggal Masuk"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabel = new JTable(model);

    // null = tidak ada baris yang dipilih (mode tambah data baru)
    private Integer idTerpilih = null;

    public KaryawanPanel() {
        setLayout(new BorderLayout(0, 12));

        JLabel judul = new JLabel("Data Karyawan");
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 26f));

        JPanel atas = new JPanel(new BorderLayout(0, 12));
        atas.add(judul, BorderLayout.NORTH);
        atas.add(buatForm(), BorderLayout.CENTER);

        // Baris pencarian + tabel
        JPanel cari = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cari.add(new JLabel("Cari (NIK / nama / jabatan):"));
        cari.add(txtCari);
        cari.add(btnCari);
        cari.add(btnReset);

        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setRowHeight(24);
        tabel.getColumnModel().getColumn(0).setMaxWidth(60);

        JPanel tengah = new JPanel(new BorderLayout(0, 8));
        tengah.add(cari, BorderLayout.NORTH);
        tengah.add(new JScrollPane(tabel), BorderLayout.CENTER);

        add(atas, BorderLayout.NORTH);
        add(tengah, BorderLayout.CENTER);

        // Klik baris tabel -> isi form
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int baris = tabel.getSelectedRow();
            if (baris >= 0) {
                isiFormDariBaris(baris);
            }
        });

        btnSimpan.addActionListener(e -> simpan());
        btnUbah.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
        btnBatal.addActionListener(e -> bersihkan());
        btnCari.addActionListener(e -> muatData(txtCari.getText().trim()));
        txtCari.addActionListener(e -> muatData(txtCari.getText().trim()));   // Enter di kolom cari
        btnReset.addActionListener(e -> {
            txtCari.setText("");
            muatData("");
        });

        txtTanggal.setText(LocalDate.now().toString());
        muatJabatan();
        muatData("");
        aturTombol();
    }

    private JPanel buatForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Form Karyawan"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.anchor = GridBagConstraints.WEST;

        // Kolom kiri
        tambahBaris(form, c, 0, 0, "NIK", txtNik);
        tambahBaris(form, c, 1, 0, "Nama", txtNama);
        tambahBaris(form, c, 2, 0, "Jabatan", cmbJabatan);
        cmbJabatan.setPrototypeDisplayValue(new ItemJabatan(0, "XXXXXXXXXXXXXXXXXX"));

        // Kolom kanan
        tambahBaris(form, c, 0, 2, "Alamat", txtAlamat);
        tambahBaris(form, c, 1, 2, "No. Telepon", txtTelepon);
        tambahBaris(form, c, 2, 2, "Tanggal Masuk (yyyy-MM-dd)", txtTanggal);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tombol.add(btnSimpan);
        tombol.add(btnUbah);
        tombol.add(btnHapus);
        tombol.add(btnBatal);

        c.gridx = 1; c.gridy = 3; c.gridwidth = 3;
        form.add(tombol, c);

        return form;
    }

    private void tambahBaris(JPanel form, GridBagConstraints c, int baris, int kolom,
                             String label, java.awt.Component komponen) {
        c.gridwidth = 1;
        c.gridy = baris;
        c.gridx = kolom;
        form.add(new JLabel(label), c);
        c.gridx = kolom + 1;
        form.add(komponen, c);
    }

    // ===== Data =====
    private void muatJabatan() {
        cmbJabatan.removeAllItems();
        String sql = "SELECT id_jabatan, nama_jabatan FROM jabatan ORDER BY nama_jabatan";
        try (Statement st = Koneksi.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                cmbJabatan.addItem(new ItemJabatan(rs.getInt(1), rs.getString(2)));
            }
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void muatData(String kata) {
        model.setRowCount(0);
        String sql = "SELECT k.id_karyawan, k.nik, k.nama, j.nama_jabatan, k.alamat, "
                + "k.no_telepon, k.tanggal_masuk "
                + "FROM karyawan k JOIN jabatan j ON k.id_jabatan = j.id_jabatan "
                + "WHERE k.nik LIKE ? OR k.nama LIKE ? OR j.nama_jabatan LIKE ? "
                + "ORDER BY k.id_karyawan";
        try (PreparedStatement ps = Koneksi.getConnection().prepareStatement(sql)) {
            String pola = "%" + kata + "%";
            ps.setString(1, pola);
            ps.setString(2, pola);
            ps.setString(3, pola);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id_karyawan"),
                        rs.getString("nik"),
                        rs.getString("nama"),
                        rs.getString("nama_jabatan"),
                        rs.getString("alamat"),
                        rs.getString("no_telepon"),
                        rs.getDate("tanggal_masuk").toLocalDate()
                    });
                }
            }
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void isiFormDariBaris(int baris) {
        idTerpilih = (Integer) model.getValueAt(baris, 0);
        txtNik.setText((String) model.getValueAt(baris, 1));
        txtNama.setText((String) model.getValueAt(baris, 2));

        // Nama jabatan unik, jadi bisa dicocokkan lewat nama
        String namaJabatan = (String) model.getValueAt(baris, 3);
        for (int i = 0; i < cmbJabatan.getItemCount(); i++) {
            if (cmbJabatan.getItemAt(i).nama().equals(namaJabatan)) {
                cmbJabatan.setSelectedIndex(i);
                break;
            }
        }

        Object alamat = model.getValueAt(baris, 4);
        Object telepon = model.getValueAt(baris, 5);
        txtAlamat.setText(alamat == null ? "" : alamat.toString());
        txtTelepon.setText(telepon == null ? "" : telepon.toString());
        txtTanggal.setText(model.getValueAt(baris, 6).toString());
        aturTombol();
    }

    // ===== Validasi: mengembalikan DataForm jika valid, null jika tidak =====
    private DataForm validasi() {
        String nik = txtNik.getText().trim();
        String nama = txtNama.getText().trim();
        String alamat = txtAlamat.getText().trim();
        String telepon = txtTelepon.getText().trim();
        String tanggalTeks = txtTanggal.getText().trim();
        ItemJabatan jabatan = (ItemJabatan) cmbJabatan.getSelectedItem();

        if (nik.isEmpty()) {
            return gagal("NIK tidak boleh kosong", txtNik);
        }
        if (!nik.matches("[A-Za-z0-9]{1,20}")) {
            return gagal("NIK hanya boleh huruf dan angka (maksimal 20 karakter)", txtNik);
        }
        if (nama.isEmpty()) {
            return gagal("Nama tidak boleh kosong", txtNama);
        }
        if (nama.length() > 100) {
            return gagal("Nama maksimal 100 karakter", txtNama);
        }
        if (jabatan == null) {
            return gagal("Belum ada jabatan. Isi dulu di menu Data Master > Data Jabatan", cmbJabatan);
        }
        if (alamat.length() > 255) {
            return gagal("Alamat maksimal 255 karakter", txtAlamat);
        }
        if (!telepon.isEmpty() && !telepon.matches("\\+?\\d{8,15}")) {
            return gagal("No. telepon harus berupa angka 8-15 digit (boleh diawali +)", txtTelepon);
        }

        LocalDate tanggal;
        try {
            tanggal = LocalDate.parse(tanggalTeks);
        } catch (DateTimeParseException ex) {
            return gagal("Tanggal masuk harus berformat yyyy-MM-dd, contoh: 2026-10-26", txtTanggal);
        }

        return new DataForm(nik, nama, jabatan, alamat, telepon, tanggal);
    }

    private DataForm gagal(String pesan, java.awt.Component fokus) {
        peringatan(pesan);
        fokus.requestFocusInWindow();
        return null;
    }

    // ===== CRUD =====
    private void simpan() {
        DataForm d = validasi();
        if (d == null) {
            return;
        }
        String sql = "INSERT INTO karyawan (nik, nama, id_jabatan, alamat, no_telepon, tanggal_masuk) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Koneksi.getConnection().prepareStatement(sql)) {
            isiParameter(ps, d);
            ps.executeUpdate();
            info("Data karyawan berhasil disimpan");
            bersihkan();
            muatData(txtCari.getText().trim());
        } catch (SQLIntegrityConstraintViolationException ex) {
            if (ex.getErrorCode() == 1062) {
                peringatan("NIK sudah terdaftar");
            } else {
                error(ex);
            }
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void ubah() {
        if (idTerpilih == null) {
            peringatan("Pilih karyawan di tabel terlebih dahulu");
            return;
        }
        DataForm d = validasi();
        if (d == null) {
            return;
        }
        String sql = "UPDATE karyawan SET nik=?, nama=?, id_jabatan=?, alamat=?, no_telepon=?, "
                + "tanggal_masuk=? WHERE id_karyawan=?";
        try (PreparedStatement ps = Koneksi.getConnection().prepareStatement(sql)) {
            isiParameter(ps, d);
            ps.setInt(7, idTerpilih);
            ps.executeUpdate();
            info("Data karyawan berhasil diubah");
            bersihkan();
            muatData(txtCari.getText().trim());
        } catch (SQLIntegrityConstraintViolationException ex) {
            if (ex.getErrorCode() == 1062) {
                peringatan("NIK sudah dipakai karyawan lain");
            } else {
                error(ex);
            }
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void hapus() {
        if (idTerpilih == null) {
            peringatan("Pilih karyawan di tabel terlebih dahulu");
            return;
        }
        int pilih = JOptionPane.showConfirmDialog(this,
                "Hapus karyawan \"" + txtNama.getText().trim() + "\"?",
                "Konfirmasi hapus", JOptionPane.YES_NO_OPTION);
        if (pilih != JOptionPane.YES_OPTION) {
            return;
        }
        String sql = "DELETE FROM karyawan WHERE id_karyawan=?";
        try (PreparedStatement ps = Koneksi.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTerpilih);
            ps.executeUpdate();
            info("Data karyawan berhasil dihapus");
            bersihkan();
            muatData(txtCari.getText().trim());
        } catch (SQLIntegrityConstraintViolationException ex) {
            peringatan("Karyawan tidak bisa dihapus karena masih dipakai data absensi, cuti, atau gaji");
        } catch (SQLException ex) {
            error(ex);
        }
    }

    /** Parameter 1-6 sama untuk INSERT dan UPDATE. */
    private void isiParameter(PreparedStatement ps, DataForm d) throws SQLException {
        ps.setString(1, d.nik());
        ps.setString(2, d.nama());
        ps.setInt(3, d.jabatan().id());
        ps.setString(4, d.alamat().isEmpty() ? null : d.alamat());
        ps.setString(5, d.telepon().isEmpty() ? null : d.telepon());
        ps.setDate(6, java.sql.Date.valueOf(d.tanggal()));
    }

    // ===== Bantuan tampilan =====
    private void bersihkan() {
        txtNik.setText("");
        txtNama.setText("");
        txtAlamat.setText("");
        txtTelepon.setText("");
        txtTanggal.setText(LocalDate.now().toString());
        if (cmbJabatan.getItemCount() > 0) {
            cmbJabatan.setSelectedIndex(0);
        }
        tabel.clearSelection();
        idTerpilih = null;
        aturTombol();
        txtNik.requestFocusInWindow();
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