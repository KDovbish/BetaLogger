package dk.betalogger;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Класс, реализующий управляющий объект для логирования в таблицу БД MySQL.
 * <br/><br/>
 * Для соединения с базой данных используется JDBC. В URL подключения к базе должно обязательно присутствовать имя базы. Пример: <br/>
 * jdbc:mysql://127.0.0.1:3306/logging
 * Таблица в которую будут писаться сообщения, должна иметь следующие столбцы:
 * time TIMESTAMP(...)
 * message VARCHAR(...)
 * Точность дробной части секунд TIMESTAMP и длина VARCHAR выбирается по желанию.
 */
@Accessors(chain = true)
class MySQLDbQueueHandler extends DbQueueHandler {

    //  Параметры подключение к базе данных
    @Setter(AccessLevel.PACKAGE) private String dbUrl;
    @Setter(AccessLevel.PACKAGE) private String user;
    @Setter(AccessLevel.PACKAGE) private String password;
    //  Таблица, куда будет осуществляться логирование
    @Setter(AccessLevel.PACKAGE) private String table;


    /**
     * Признак ошибки при взаимодействии с БД.
     * Используется для реализации логики восстановления логирования после обнаружения ошибки в коммуникации с БД.
     */
    private boolean error = false;

    MySQLDbQueueHandler() {

        queue = new ConcurrentLinkedQueue<LogData>();
        threadCode = () -> {
            try {

                if (queue.peek() != null) {

                    Connection connection = DriverManager.getConnection(dbUrl, user, password);
                    LogData logData;

                    while ((logData = queue.poll()) != null) {
                        connection.createStatement().executeUpdate(
                                "INSERT INTO " + table +
                                        " (time,message)" +
                                        " VALUES " + "(" +
                                        "'" + Timestamp.valueOf(LocalDateTime.now()) + "'" +
                                        "," +
                                        "'" + logData.getStringForLogging() + "'"
                                        + ")"
                        );

                    }

                    connection.close();

                }

                if (error == true) {
                    error = false;
                    setAddToQueue(true);
                }

            } catch (/*SQLException*/ Exception e) {

                /*
                    Код в блоке try требует обработки только одного проверяемого исключения - SQLException.
                    Предполагается, что данный код будет выполняется по планировщику, который реалзизован в исполнителе.
                    Но суть в том, что если еще какое-то исключение(например, непроверяемое) возникнет в этом коде и не будет перехвачено,
                    то код на следующем запуске работать уже не будет. А если будет перехвачено, то будет. Поэтому, кодируем
                    перехват абсолютно всех исключений.
                 */

                //e.printStackTrace();
                System.out.println(e);
                if (error == false) {
                    error = true;
                    setAddToQueue(false);
                }

            }

        };
    }


}


/*
    @Getter(AccessLevel.PACKAGE)
    private boolean jdbcDriver = true;

    boolean isTableReady() {
        return true;
    }
*/





/*
    MySQLDbQueueHandler() {
        try {
            //Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            jdbcDriver = false;

            */
/*
            Передача управления на данный блок catch вовсе не означает, что объект не будет создан.
            Проблема в том, что конструктор для данного объекта, в подобной ситуации, отработает не полностью.
            Все что лежит ниже оператора Class.forName(...) выполнено не будет, т.е. не пройдет инициализация
            внутренней переменной, хранящей ссылку на очередь, не пройдет иницилизация кода, обслуживающего эту очередь.

            Вывод:
            Необходимо вводить некий признак внутри класса, который, сразу после создания объекта, будет вычитываться клиентом класса
            и будет делаться вывод о готовности только что созданного объекта к работе. Фабрика, например, будет проверять этот признак и отдавать
            null, если объект не удалось нормально инициализировать.

            Предположение:
            Так может быть стоит перехватывать все исключения в этом блоке?
            Мало ли какие ошибки могут быть после оператора Class.forName()... С другой сторны - какие?
            Какие могут быть ошибки при присвоении значений двух переменным? Думаю, что только непроверяемые.
             *//*

        }
    }
*/


