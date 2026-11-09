package simka.model;

import java.time.LocalDate;

public record Cuti(int id, int idKaryawan, String nik, String nama,
                   LocalDate mulai, LocalDate selesai, String alasan, String status) { }
//Spring