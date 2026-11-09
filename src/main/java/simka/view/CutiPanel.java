package simka.view;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.springframework.dao.DataAccessException;
import simka.dao.CutiDao;
import simka.dao.KaryawanLookupDao;
import simka.model.Cuti;
import simka.model.Option;

public class CutiPanel extends JPanel {
    private final CutiDao dao = new CutiDao();
    private final KaryawanLookupDao lookup = new KaryawanLookupDao();

    private final JComboBox<Option> cKaryawan = new JComboBox<>();
    private final JTextField tAlasan = new JTextField();
    private final JTextField tMulai = new JTextField(LocalDate.now().toString());
    private final JTextField tSelesai = new JTextField(LocalDate.now().toString());
    private final JTable table = new JTable();
    private List<Cuti> data = List.of();

    public CutiPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel title = new JLabel("Pengajuan Cuti");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));

        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));
        form.add(new JLabel("Karyawan")); form.add(cKaryawan);
        form.add(new JLabel("Alasan")); form.add(tAlasan);
        form.add(new JLabel("Tgl Mulai (yyyy-MM-dd)")); form.add(tMulai);
        form.add(new JLabel("Tgl Selesai (yyyy-MM-dd)")); form.add(tSelesai);

        JButton btnAjukan = new JButton("Ajukan Cuti");
        JButton btnSetuju = new JButton("Setujui");
        JButton btnTolak = new JButton("Tolak");
        JButton btnHapus = new JButton("Hapus");
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btn.add(btnAjukan); btn.add(btnSetuju); btn.add(btnTolak); btn.add(btnHapus);

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.add(title, BorderLayout.NORTH);
        north.add(form, BorderLayout.CENTER);
        north.add(btn, BorderLayout.SOUTH);

        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnAjukan.addActionListener(e -> ajukan());
        btnSetuju.addActionListener(e -> ubahStatus("Disetujui"));
        btnTolak.addActionListener(e -> ubahStatus("Ditolak"));
        btnHapus.addActionListener(e -> hapus());
        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { refresh(); }
        });
        refresh();
    }

    private void ajukan() {
        Option k = (Option) cKaryawan.getSelectedItem();
        if (k == null) { pesan("Belum ada data karyawan."); return; }
        if (tAlasan.getText().trim().isEmpty()) { pesan("Alasan cuti wajib diisi."); return; }
        try {
            LocalDate m = LocalDate.parse(tMulai.getText().trim());
            LocalDate s = LocalDate.parse(tSelesai.getText().trim());
            if (s.isBefore(m)) { pesan("Tanggal selesai tidak boleh sebelum tanggal mulai."); return; }
            dao.ajukan(k.id(), m, s, tAlasan.getText().trim());
            tAlasan.setText("");
            muatTabel();
        } catch (DateTimeParseException ex) {
            pesan("Format tanggal harus yyyy-MM-dd.");
        } catch (DataAccessException ex) {
            pesan("Gagal menyimpan: " + ex.getMostSpecificCause().getMessage());
        }
    }

    private Cuti terpilih() {
        int r = table.getSelectedRow();
        if (r < 0) { pesan("Pilih pengajuan di tabel terlebih dahulu."); return null; }
        return data.get(table.convertRowIndexToModel(r));
    }

    private void ubahStatus(String status) {
        Cuti c = terpilih();
        if (c == null) return;
        try { dao.ubahStatus(c.id(), status); muatTabel(); }
        catch (DataAccessException ex) { pesan("Gagal mengubah status: " + ex.getMostSpecificCause().getMessage()); }
    }

    private void hapus() {
        Cuti c = terpilih();
        if (c == null) return;
        if (JOptionPane.showConfirmDialog(this, "Hapus pengajuan " + c.nama() + "?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { dao.hapus(c.id()); muatTabel(); }
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
                new String[]{"NIK", "Nama", "Mulai", "Selesai", "Alasan", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Cuti c : data)
            m.addRow(new Object[]{c.nik(), c.nama(), c.mulai(), c.selesai(), c.alasan(), c.status()});
        table.setModel(m);
    }

    private void pesan(String s) { JOptionPane.showMessageDialog(this, s); }
}