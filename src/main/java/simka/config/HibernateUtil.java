package simka.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory FACTORY = new Configuration().configure().buildSessionFactory();

    public static SessionFactory get() { return FACTORY; }
}