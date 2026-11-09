package simka.dao;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import simka.config.DbConfig;
import simka.model.Absensi;

public class AbsensiDao {
    private final JdbcTemplate jdbc = DbConfig.jdbc();

    private static final RowMapper<Absensi> MAPPER = (rs, i) -> new Absensi(
            rs.getInt("id"), rs.getInt("id_karyawan"), rs.getString("nik"), rs.getString("nama"),
            rs.getDate("tanggal").toLocalDate(), rs.getTime("jam_masuk").toLocalTime(),
            rs.getString("status"));

    public List<Absensi> semua() {
        return jdbc.query(
            "SELECT a.id, a.id_karyawan, k.nik, k.nama, a.tanggal, a.jam_masuk, a.status " +
            "FROM absensi a JOIN karyawan k ON a.id_karyawan=k.id_karyawan " +
            "ORDER BY a.tanggal DESC, a.jam_masuk DESC", MAPPER);
    }

    public int simpan(int idKaryawan, LocalDate tgl, LocalTime jam, String status) {
        return jdbc.update(
            "INSERT INTO absensi(id_karyawan,tanggal,jam_masuk,status) VALUES(?,?,?,?)",
            idKaryawan, Date.valueOf(tgl), Time.valueOf(jam), status);
    }

    public int hapus(int id) {
        return jdbc.update("DELETE FROM absensi WHERE id=?", id);
    }
}