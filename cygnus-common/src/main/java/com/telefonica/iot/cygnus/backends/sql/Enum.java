package com.telefonica.iot.cygnus.backends.sql;

public class Enum {
    public enum SQLInstance {
        MYSQL {
            @Override
            public String toString() {
                return "mysql";
            }
        },
        POSTGRESQL {
            @Override
            public String toString() {
                return "postgresql";
            }
        }
    }
}
