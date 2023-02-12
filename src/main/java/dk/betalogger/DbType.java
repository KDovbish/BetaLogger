package dk.betalogger;

/** Типы баз данных, для которых существуют реализации логики логирования */
public enum DbType {
    MYSQL("com.mysql.cj.jdbc.Driver"),
    POSTGRESQL("org.postgresql.Driver");

    private String className;

    DbType(String className) {
        this.className = className;
    }

    boolean registerDriver() {
        try {
            Class.forName(className);
            return true;
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
