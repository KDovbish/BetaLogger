package dk.betalogger;

/** Простая фабрика по созданию управляющих объктов очередей */
class QueueHandlerFactory {

    /** Создать управляющий объект входной очереди */
    static InputQueueHandler createInputQueueHandler() {
        return new AlphaInputQueueHandler();
    }

    /** Создать управляющий объект логирующей очереди в консоль */
    static QueueHandler createConsoleQueueHandler() {
        return new ConsoleQueueHandler();
    }

    /** Создать управляющий объект логирующей очереди в файл */
    static FileQueueHandler createFileQueueHandler() {
        return new AlphaFileQueueHandler();
    }

    /** Создать управляющий объект логирующей очереди в базу данных */
    static DbQueueHandler createDbQueueHandler(DbType dbType) {
        switch (dbType) {
            case MYSQL:
                return new MySQLDbQueueHandler();
            case POSTGRESQL:
                return new PostgreDbQueueHandler();
            default:
                return null;
        }
    }

}
