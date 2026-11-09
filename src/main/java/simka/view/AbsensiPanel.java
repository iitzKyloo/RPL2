package simka.view;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import simka.dao.AbsensiDao;
import simka.dao.KaryawanLookupDao;
import simka.model.Absensi;
import simka.model.Option;

public class AbsensiPanel extends JPanel {
    private final AbsensiDao dao = new AbsensiDao();
    private final KaryawanLookupDao lookup = new KaryawanLookupDao();

    private final JComboBox<Option> cKaryawan = new JComboBox<>();
    private final JComboBox<String> cStatus = new JComboBox<>(new String[]{"Hadir", "Izin", "Sakit", "Alpha"});
    private final JTextField tTgl = new JTextField(LocalDate.now().toString());
    private final JTextField tJam = new JTextField(LocalTime.now().withNano(0).toString());
    private final JTable table = new JTable();
    private List<Absensi> data = List.of();

    public AbsensiPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel title = new JLabel("Absensi");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));

        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));
        form.add(new JLabel("Karyawan")); form.add(cKaryawan);
        form.add(new JLabel("Status")); form.add(cStatus);
        form.add(new JLabel("Tanggal (yyyy-MM-dd)")); form.add(tTgl);
        form.add(new JLabel("Jam Masuk (HH:mm:ss)")); form.add(tJam);

        JButton btnCatat = new JButton("Catat Absensi");
        JButton btnHapus = new JButton("Hapus");
        JButton btnNow = new JButton("Isi Waktu Sekarang");
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btn.add(btnCatat); btn.add(btnHapus); btn.add(btnNow);

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.add(title, BorderLayout.NORTH);
        north.add(form, BorderLayout.CENTER);
        north.add(btn, BorderLayout.SOUTH);

        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnNow.addActionListener(e -> {
            tTgl.setText(LocalDate.now().toString());
            tJam.setText(LocalTime.now().withNano(0).toString());
        });
        btnCatat.addActionListener(e -> catat());
        btnHapus.addActionListener(e -> hapus());
        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { refresh(); }
        });
        refresh();
    }

    private void catat() {
        Option k = (Option) cKaryawan.getSelectedItem();
        if (k == null) { pesan("Belum ada data karyawan."); return; }
        try {
            dao.simpan(k.id(), LocalDate.parse(tTgl.getText().trim()),
                    LocalTime.parse(tJam.getText().trim()), (String) cStatus.getSelectedItem());
            muatTabel();
        } catch (DateTimeParseException ex) {
            pesan("Format tanggal yyyy-MM-dd dan jam HH:mm:ss.");
        } catch (DuplicateKeyException ex) {
            pesan("Karyawan tersebut sudah tercatat absen pada tanggal yang sama.");
        } catch (DataAccessException ex) {
            pesan("Gagal menyimpan: " + ex.getMostSpecificCause().getMessage());
        }
    }

    private void hapus() {
        int r = table.getSelectedRow();
        if (r < 0) { pesan("Pilih data di tabel terlebih dahulu."); return; }
        Absensi a = data.get(table.convertRowIndexToModel(r));
        if (JOptionPane.showConfirmDialog(this, "Hapus absensi " + a.nama() + "?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { dao.hapus(a.id()); muatTabel(); }
        catch (DataAccessException ex) { pesan("Gagal menghapus: " + ex.getMostSpecificCause().getMessage()); }
    }

    public void refresh() {
        try {
            Option lama = (Option) cKaryawan.getSelectedItem();
            cKaryawan.removeAllItems();
            for (Option o : lookup.opsi()) cKaryawan.addItem(o);
            if (lama != null)
                for (int i = 0; i < cKaryawan.getItemCount(); i++)
                    if (cKaryawan.getItemAt(i).id() == lama.id()) cKaryawan.setSelectedIndex(i);
            muatTabel();
        } catch (DataAccessException ex) {
            pesan("Gagal memuat data: " + ex.getMostSpecificCause().getMessage());
        }
    }

    private void muatTabel() {
        data = dao.semua();
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"NIK", "Nama", "Tanggal", "Jam Masuk", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Absensi a : data)
            m.addRow(new Object[]{a.nik(), a.nama(), a.tanggal(), a.jamMasuk(), a.status()});
        table.setModel(m);
    }

    private void pesan(String s) { JOptionPane.showMessageDialog(this, s); }
}
//Spring