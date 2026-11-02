package simka.dao;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import simka.config.HibernateUtil;
import simka.model.Karyawan;

public class KaryawanDao {

    public void simpan(Karyawan k) {
        try (Session s = HibernateUtil.get().openSession()) {
            Transaction t = s.beginTransaction();
            s.persist(k);
            t.commit();
        }
    }

    public void ubah(Karyawan k) {
        try (Session s = HibernateUtil.get().openSession()) {
            Transaction t = s.beginTransaction();
            s.merge(k);
            t.commit();
        }
    }

    public void hapus(Long id) {
        try (Session s = HibernateUtil.get().openSession()) {
            Transaction t = s.beginTransaction();
            Karyawan k = s.find(Karyawan.class, id);
            if (k != null) s.remove(k);
            t.commit();
        }
    }

    public List<Karyawan> semua() {
        try (Session s = HibernateUtil.get().openSession()) {
            return s.createQuery("from Karyawan", Karyawan.class).list();
        }
    }
}