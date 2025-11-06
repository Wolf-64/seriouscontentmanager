package com.wlf.app.main.data;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EntityManagerProvider {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("app");

    public static EntityManagerFactory get() {
        return emf;
    }
}