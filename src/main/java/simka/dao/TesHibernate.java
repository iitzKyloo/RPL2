package simka.dao;

import simka.model.Karyawan;

public class TesHibernate {
    public static void main(String[] args) {
        KaryawanDao dao = new KaryawanDao();
        dao.simpan(new Karyawan("001", "Budi", "Staff", "0812"));
        System.out.println("Jumlah data: " + dao.semua().size());
        System.exit(0);
    }
}