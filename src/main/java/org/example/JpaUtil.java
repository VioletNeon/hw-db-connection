package org.example;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JpaUtil {
    private JpaUtil() {}

    public static final String PU = "HW5";

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory(PU);

    public static EntityManagerFactory emf() {
        return emf;
    }

    public static void close() {
        emf.close();
    }
}

