package simka.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record Absensi(int id, int idKaryawan, String nik, String nama,
                      LocalDate tanggal, LocalTime jamMasuk, String status) { }