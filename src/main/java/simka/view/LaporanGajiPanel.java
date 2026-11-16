package simka.view;

import java.awt.*;
import javax.swing.*;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;
import simka.report.LaporanGaji;

public class LaporanGajiPanel extends JPanel {

    private final JTextField tPeriode = new JTextField(10);
    private final JPanel area = new JPanel(new BorderLayout());

    public LaporanGajiPanel() {
        setLayout(new BorderLayout(0, 8));

        JLabel judul = new JLabel("Laporan Gaji");
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 26f));

        JButton btnLihat = new JButton("Tampilkan Laporan");
        JButton btnPdf = new JButton("Simpan PDF");

        JPanel baris = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        baris.add(new JLabel("Periode (yyyy-MM, kosong = semua):"));
        baris.add(tPeriode);
        baris.add(btnLihat);
        baris.add(btnPdf);

        JPanel atas = new JPanel(new BorderLayout());
        atas.add(judul, BorderLayout.NORTH);
        atas.add(baris, BorderLayout.CENTER);

        add(atas, BorderLayout.NORTH);
        add(area, BorderLayout.CENTER);

        btnLihat.addActionListener(e -> muat());
        tPeriode.addActionListener(e -> muat());
        btnPdf.addActionListener(e -> LaporanGaji.simpanPdf(this, tPeriode.getText()));

        muat();   // langsung tampilkan semua laporan saat panel dibuka
    }

    private void tampilkan(Component c) {
        area.removeAll();
        area.add(c, BorderLayout.CENTER);
        area.revalidate();
        area.repaint();
    }

    private void muat() {
        final String periode = tPeriode.getText();
        tampilkan(new JLabel("Memuat laporan...", SwingConstants.CENTER));

        new SwingWorker<JasperPrint, Void>() {
            @Override protected JasperPrint doInBackground() throws Exception {
                return LaporanGaji.buat(periode);
            }

            @Override protected void done() {
                try {
                    tampilkan(new JRViewer(get()));
                } catch (Exception ex) {
                    Throwable t = ex.getCause() != null ? ex.getCause() : ex;
                    tampilkan(new JLabel("Gagal membuat laporan: " + t.getMessage(), SwingConstants.CENTER));
                }
            }
        }.execute();
    }
}