package simka.dao;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import simka.config.DbConfig;
import simka.model.Cuti;

public class CutiDao {
    private final JdbcTemplate jdbc = DbConfig.jdbc();

    private static final RowMapper<Cuti> MAPPER = (rs, i) -> new Cuti(
            rs.getInt("id"), rs.getInt("id_karyawan"), rs.getString("nik"), rs.getString("nama"),
            rs.getDate("tgl_mulai").toLocalDate(), rs.getDate("tgl_selesai").toLocalDate(),
            rs.getString("alasan"), rs.getString("status"));

    public List<Cuti> semua() {
        return jdbc.query(
            "SELECT c.id, c.id_karyawan, k.nik, k.nama, c.tgl_mulai, c.tgl_selesai, c.alasan, c.status " +
            "FROM cuti c JOIN karyawan k ON c.id_karyawan=k.id_karyawan ORDER BY c.id DESC", MAPPER);
    }

    public int ajukan(int idKaryawan, LocalDate mulai, LocalDate selesai, String alasan) {
        return jdbc.update(
            "INSERT INTO cuti(id_karyawan,tgl_mulai,tgl_selesai,alasan) VALUES(?,?,?,?)",
            idKaryawan, Date.valueOf(mulai), Date.valueOf(selesai), alasan);
    }

    public int ubahStatus(int id, String status) {
        return jdbc.update("UPDATE cuti SET status=? WHERE id=?", status, id);
    }

    public int hapus(int id) {
        return jdbc.update("DELETE FROM cuti WHERE id=?", id);
    }
}