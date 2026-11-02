package simka.model;

import jakarta.persistence.*;

@Entity
@Table(name = "karyawan")
public class Karyawan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nik;
    private String nama;
    private String jabatan;
    private String telepon;

    public Karyawan() { }
    public Karyawan(String nik, String nama, String jabatan, String telepon) {
        this.nik = nik; this.nama = nama; this.jabatan = jabatan; this.telepon = telepon;
    }

    public Long getId() { return id; }
    public String getNik() { return nik; }
    public void setNik(String nik) { this.nik = nik; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getJabatan() { return jabatan; }
    public void setJabatan(String jabatan) { this.jabatan = jabatan; }
    public String getTelepon() { return telepon; }
    public void setTelepon(String telepon) { this.telepon = telepon; }
}