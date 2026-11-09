package simka.dao;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import simka.config.DbConfig;
import simka.model.Option;

public class KaryawanLookupDao {
    private final JdbcTemplate jdbc = DbConfig.jdbc();

    public List<Option> opsi() {
        return jdbc.query(
            "SELECT id_karyawan AS id, CONCAT(nik,' - ',nama) AS label FROM karyawan ORDER BY nama",
            (rs, i) -> new Option(rs.getInt("id"), rs.getString("label")));
    }
}
//Spring