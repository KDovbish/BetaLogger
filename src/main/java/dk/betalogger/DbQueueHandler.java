package dk.betalogger;

/** Интерфейс для объекта, который должен обслуживать очередь сообщений для логирования в таблицу БД */
abstract class DbQueueHandler extends QueueHandler {
    /** Установить адрес базы данных */
    abstract DbQueueHandler setDbUrl(String dbUrl);

    /** Имя пользователя для подключения к базе данных*/
    abstract DbQueueHandler setUser(String user);
    /** Пароль пользователя для подключения к базе данных */
    abstract DbQueueHandler setPassword(String password);

    /** Имя таблицы, в которую будет осуществляться логирование */
    abstract DbQueueHandler setTable(String password);
}
