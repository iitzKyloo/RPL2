package simka.report;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import simka.config.Koneksi;

public class LaporanGaji {

    /** Membuat laporan (dipakai oleh viewer di dalam aplikasi dan oleh ekspor PDF). */
    public static JasperPrint buat(String periode) throws Exception {
        try (InputStream in = LaporanGaji.class.getResourceAsStream("/report/laporan_gaji.jrxml")) {
            if (in == null) throw new IllegalStateException(
                    "File /report/laporan_gaji.jrxml tidak ditemukan di src/main/resources/report.");
            JasperReport jr = JasperCompileManager.compileReport(in);
            Map<String, Object> param = new HashMap<>();
            param.put("PERIODE", (periode == null || periode.isBlank()) ? null : periode.trim());
            Connection c = Koneksi.getConnection();   // jangan di-close, dipakai bersama
            return JasperFillManager.fillReport(jr, param, c);
        }
    }

    /** Simpan sebagai PDF ke lokasi pilihan, lalu langsung dibuka. */
    public static void simpanPdf(Component parent, String periode) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("laporan_gaji.pdf"));
        if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        try {
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".pdf")) f = new File(f.getAbsolutePath() + ".pdf");
            JasperExportManager.exportReportToPdfFile(buat(periode), f.getAbsolutePath());
            Desktop.getDesktop().open(f);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Gagal menyimpan PDF: " + ex.getMessage());
        }
    }
}