package simka.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DashboardForm extends JFrame {

    private final String username;
    private final JPanel pnlContent = new JPanel(new BorderLayout());

    public DashboardForm(String username) {
        this.username = username;

        setTitle("SIMKA - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        pnlContent.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        setContentPane(pnlContent);
        setJMenuBar(buatMenuBar());

        tampil(buatPanelDashboard());
    }

    // ===== Menu bar (mengikuti diagram struktur navigasi) =====
    private JMenuBar buatMenuBar() {
        JMenuBar bar = new JMenuBar();

        bar.add(item("Dashboard", () -> tampil(buatPanelDashboard())));

        JMenu master = new JMenu("Data Master");
        master.add(item("Data Karyawan", () -> tampil(new KaryawanPanel())));
        master.add(item("Data Jabatan", () -> tampil(new JabatanPanel())));
        bar.add(master);

        JMenu transaksi = new JMenu("Transaksi");
        transaksi.add(item("Absensi", () -> tampil(buatPlaceholder("Absensi"))));
        transaksi.add(item("Pengajuan Cuti", () -> tampil(buatPlaceholder("Pengajuan Cuti"))));
        transaksi.add(item("Persetujuan", () -> tampil(buatPlaceholder("Persetujuan"))));
        transaksi.add(item("Penggajian", () -> tampil(buatPlaceholder("Penggajian"))));
        bar.add(transaksi);

        bar.add(item("Laporan", () -> tampil(buatPlaceholder("Laporan"))));
        bar.add(item("Logout", this::logout));

        return bar;
    }

    private JMenuItem item(String teks, Runnable aksi) {
        JMenuItem mi = new JMenuItem(teks);
        mi.addActionListener(e -> aksi.run());
        return mi;
    }

    // ===== Mengganti isi area konten =====
    private void tampil(JComponent panel) {
        pnlContent.removeAll();
        pnlContent.add(panel, BorderLayout.CENTER);
        pnlContent.revalidate();
        pnlContent.repaint();
    }

    // ===== Halaman dashboard =====
    private JPanel buatPanelDashboard() {
        JPanel root = new JPanel(new BorderLayout(0, 20));

        JLabel judul = new JLabel("Dashboard");
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 26f));
        JLabel selamat = new JLabel("Selamat datang, " + username);

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.add(judul);
        header.add(selamat);

        // Angka masih 0, nanti diisi dari database
        JPanel kartu = new JPanel(new GridLayout(1, 4, 16, 16));
        kartu.add(buatKartu("Total Karyawan", "0"));
        kartu.add(buatKartu("Hadir Hari Ini", "0"));
        kartu.add(buatKartu("Cuti Menunggu", "0"));
        kartu.add(buatKartu("Total Gaji", "Rp 0"));

        root.add(header, BorderLayout.NORTH);
        root.add(kartu, BorderLayout.CENTER);
        return root;
    }

    private JPanel buatKartu(String judul, String nilai) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JLabel lblJudul = new JLabel(judul, SwingConstants.CENTER);
        JLabel lblNilai = new JLabel(nilai, SwingConstants.CENTER);
        lblNilai.setFont(lblNilai.getFont().deriveFont(Font.BOLD, 28f));

        p.add(lblJudul);
        p.add(lblNilai);
        return p;
    }

    // ===== Halaman sementara untuk modul yang belum dibuat =====
    private JPanel buatPlaceholder(String nama) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel judul = new JLabel(nama);
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 26f));
        JLabel info = new JLabel("Modul ini belum dibuat.", SwingConstants.CENTER);
        info.setForeground(Color.GRAY);
        p.add(judul, BorderLayout.NORTH);
        p.add(info, BorderLayout.CENTER);
        return p;
    }

    private void logout() {
        int pilih = JOptionPane.showConfirmDialog(this, "Yakin ingin keluar?",
                "Logout", JOptionPane.YES_NO_OPTION);
        if (pilih == JOptionPane.YES_OPTION) {
            new LoginForm().setVisible(true);
            dispose();
        }
    }
}