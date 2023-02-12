package dk.betalogger;

import java.time.LocalDateTime;

/**
 * Логер "Бета".
 * <br/><br/>
 * Алгоритм запуска:<br/>
 * <pre>
 * BetaLogger betaLogger = new BetaLogger();
 * betaLogger.setConsoleLogging();
 * betaLogger.setFileLogging("c:\\logs\\example.log");
 * betaLogger.setDbLogging(DbType.MySQL, "jdbc:mysql://127.0.0.1:3306/loggingDb", "user", "password", "logging_table");
 * betaLogger.start();
 * </pre>
 * @version 3.0
 */
public class BetaLogger {

    /** Объект, управляющий входной очередью и реализующий логику распределения сообщений по логирующим очередям */
    private InputQueueHandler inputQueueHandler;

    /** Список ссылок на все управляющие объекты логирующих очередей */
    private QueueHandler[] loggingQueueHandlerList;

    /** На приборе логера кнопка Старт */
    private boolean start = false;


    /** Конструктор */
    public BetaLogger() {
        //  Создается объект, управляющий входной очередью и реализующий логику распределения сообщений по логирующим очередям
        inputQueueHandler = QueueHandlerFactory.createInputQueueHandler();
        //  Создается список, в котором будут храниться ссылки на все управляющие объекты логирующих очередей.
        //  Нет необходимости использовать потокобезопастную реализацию(например, Vector), поскольку к данному списку
        //  имеет доступ только поток Бета-Логера.
        loggingQueueHandlerList = new QueueHandler[LoggingType.values().length];
        //  Обновление состояния логера
        updateState();
    }


    /**
     * Обновления состояния бета-логера в целом, в зависимости от состояния внутренних флагов и объектов.
     * <br/><br/>
     * Есть кнопка "Старт". Она может быть нажата(start = true), она может быть не нажата(start = false)<br/>
     * <br/>
     * В каждом из управляющих объектов есть флаг который разрешает или блокирует данный объект к использованию.<br/>
     * QueueHandler.enable = true: объект работает, т.е. является частью фунционала бета-логера.<br/>
     * QueueHandler.enable = false: объект блокирован, т.е. в очередь этого объекта уже не должны передаваться сообщения и поток этого объекта должен быть отключен.<br/>
     * <br/>
     * В управляющих объектах есть флаг необходимости перезагрузки внутреннего потока<br/>
     * <br/>
     * Метод анализирует различные флаги и, в зависимости от них...<br/>
     * - запускает или останавливает потоки в управляющих объектах<br/>
     * - регистрирует или снимает с регистрации управляющие логирующие объекты в управляющем объекте входной очереди<br/>
     * - перегружает потоки внутри управляющих объектов<br/>
     */
    private void updateState() {

        //  В зависимости от состояния кнопки Старт, запускаем или останавливаем функционал
        //  управляющего объекта входной очереди
        if (start) {
            if (inputQueueHandler.isThreadRestartNeed()) {
                inputQueueHandler.stop();
                inputQueueHandler.setThreadRestartNeed(false);
            }
            inputQueueHandler.start();
            inputQueueHandler.setEnable(true);
        } else {
            inputQueueHandler.stop();
            inputQueueHandler.setEnable(false);
        }

        //  В зависимости от состояния кнопки Старт, в зависимости от разрешенности или запрещенности конкретного
        //  управляюго объекта логирующей очереди, запускаем или останавливаем функционал управляющих объектов логирующих очередей,
        //  производим регистрацию или снятие с регистрации управляющих объектов логирующих очередей в управляющем объекте
        //  входной очереди.
        for (QueueHandler queueHandler: loggingQueueHandlerList) {
            if (queueHandler != null) {
                if (start) {
                    if (queueHandler.isEnable()) {
                        //  Регистрация логирующей очереди в управляющем объекте входной очереди
                        inputQueueHandler.registerLoggingQueue(queueHandler);

                        //  В случае, если выставлен флаг необходимости перезагрузки потока, то останавлиавем поток,
                        //  с тем, чтобы на следующем шаге запустить его. Т.е. сделать перезагрузку.
                        if (queueHandler.isThreadRestartNeed()) {
                            queueHandler.stop();
                            queueHandler.setThreadRestartNeed(false);
                        }

                        //  Запуск потока, обслуживающего логирующую очердеь
                        queueHandler.start();
                    } else {
                        //  Снятие регистрации логирующей очереди в управляющем объекте входной очереди
                        inputQueueHandler.unregisterLoggingQueue(queueHandler);
                        //  Остнаов потока, обслуживающего логирующую очередь
                        queueHandler.stop();
                    }
                } else {
                    inputQueueHandler.unregisterLoggingQueue(queueHandler);
                    queueHandler.stop();
                }
            }
        }


    }



    /** Включить логирование на консоль */
    public void setConsoleLogging() {

        QueueHandler consoleQueueHandler;

        //  Если логирование в консоль до сих пор еще не включалось, то создаем ссответствующий объект
        //  и сохраняем ссылку на него во внутреннем массиве бета-логера
        if (loggingQueueHandlerList[LoggingType.CONSOLE.ordinal()] == null) {
            //  Создается управляющий объект логирующей очереди на консоль
            consoleQueueHandler = QueueHandlerFactory.createConsoleQueueHandler();
            //  Созданный управляющий объект логирующей очереди сохраняется в объекте Бета-логера
            loggingQueueHandlerList[LoggingType.CONSOLE.ordinal()] = consoleQueueHandler;
        } else {
            //  Получаем управляющий объект уже когда то созданный
            consoleQueueHandler = loggingQueueHandlerList[LoggingType.CONSOLE.ordinal()];
        }

        //  "Включить" управляющий объект
        //  Если управляющий логирующий объект однажды был создан, то он уже не удаляется. Просто отключается,
        //  если того требует логика алгоритма
        consoleQueueHandler.setEnable(true);

        //  Обновляется состояние бета-логера
        updateState();

    }


    /**
     * Включить логирование в файл.
     * @param fileName Имя файла лога
     */
    public void setFileLogging(String fileName) {

        FileQueueHandler fileQueueHandler;

        if (loggingQueueHandlerList[LoggingType.FILE.ordinal()] == null) {
            //  Файловое логирование еще не использовалось.
            //  Создается управляющий объект, содержащий в себе логирующую очередь для логирования в файл и код, обслуживающий эту очередь.
            fileQueueHandler = QueueHandlerFactory.createFileQueueHandler();
            //  Регистрация управляющего объекта для логирования в файл во внутреннем массиве бета-логера
            loggingQueueHandlerList[LoggingType.FILE.ordinal()] = fileQueueHandler;
        } else {
            //  Файловое логирование уже используется, т.к. соответствующий управляющий объект существует во внутреннем массиве бета-логера
            //  Нисходящее преобразование
            //  Преобразование будет абсолютно безопасным, поскольку точно известно, что по индексу LoggingType.FILE.ordinal()
            //  может находиться только ссылка на объект, реализующий интерфейс FileQueueHandler и больше ничего.
            fileQueueHandler = (FileQueueHandler) loggingQueueHandlerList[LoggingType.FILE.ordinal()];
        }

        //  Инициализацция имени файла, куда будут записываться сообщения из логирующей очереди
        fileQueueHandler.setFileName(fileName);

        //  Переводим управляющий объект в рабочее состояние
        fileQueueHandler.setEnable(true);

        //  Была нажата одна из "кнопок" на панели управления бета-логера. Обновляем состояние бета-логера
        updateState();
    }


    /**
     * Включить логирование в таблицу БД.
     * <br/><br/>
     * Для успешного логирования в таблицу БД, данная таблица должна содержать следующие столбы:<br/>
     * time TIMESTAMP(...)<br/>
     * message VARCHAR(...)<br/>
     * Размерность типов задается по желанию.
     * @param dbType Тип БД. Допустимые типы описаны в перечислении {@link DbType}
     * @param dbUrl URL подключения к БД. В обязательном порядке необходимо наличие имени БД
     * @param user Имя пользователя
     * @param password Пароль
     * @param table Таблица, в которую будет осуществляться логирование
     */
    public void setDbLogging(DbType dbType, String dbUrl, String user, String password, String table) {

        DbQueueHandler dbQueueHandler = null;

        if (loggingQueueHandlerList[LoggingType.DB.ordinal()] == null) {
            if (dbType.registerDriver()) {
                //  Создается управляющий объект логирующей очереди БД в зависимости от типа БД
                dbQueueHandler = QueueHandlerFactory.createDbQueueHandler(dbType);

                //  Созданный управляющий объект логирующей очереди БД сохраняется в объекте Бета-логера
                loggingQueueHandlerList[LoggingType.DB.ordinal()] = dbQueueHandler;
            }
        } else {
            dbQueueHandler = (DbQueueHandler) loggingQueueHandlerList[LoggingType.DB.ordinal()];
        }

        if (dbQueueHandler != null) {
            //  В созданном управляющем объекте прописываются параметры подключения к БД
            dbQueueHandler.setDbUrl(dbUrl).setUser(user).setPassword(password);

            //  В созданном управляющем объекте прописвается таблица в которую будет производиться логирование
            dbQueueHandler.setTable(table);

            dbQueueHandler.setEnable(true);

            updateState();
        }

    }


    /**
     * Отключение логирования заданного типа
     * @param loggingType Тип логирования {@link LoggingType}
     */
    public void unsetLogging(LoggingType loggingType) {

        if (loggingQueueHandlerList[loggingType.ordinal()] != null) {
            loggingQueueHandlerList[loggingType.ordinal()].setEnable(false);
            updateState();
        }
    }

    /** Запустить логер */
    public void start() {
        start = true;
        updateState();
    }

    /** Остановить логер */
    public void stop() {
        start = false;
        updateState();
    }

    /**
     * Добавить во входную очередь данные для логирования
     * @param data Данные для логирования построенные на базе абстрактного класса {@link LogData}
     */
    public void add(LogData data) {
        inputQueueHandler.offer(data);
    }

    /**
     * Добавить во входную очередь данные для логирования
     * @param data Строка для логирования
     */
    public void add(String data) {

        LogData logData = new LogData() {
            private String s = data;
            @Override
            public String getStringForLogging() {
                return s;
            }
        };

        inputQueueHandler.offer(logData);
    }


    /**
     * Установить период опроса логирующей очереди
     * @param loggingType Тип логирования из перечисления {@link LoggingType}
     * @param pollPeriod Период опроса очереди в милисекундах
     */
    public void setLoggingQueuePollPeriod(LoggingType loggingType, long pollPeriod) {

        QueueHandler queueHandler = loggingQueueHandlerList[loggingType.ordinal()];
        if (queueHandler != null) {
            queueHandler.setPollPeriod(pollPeriod);
            queueHandler.setThreadRestartNeed(true);
            updateState();
        }
    }


    /**
     * Установить период опроса входной очереди
     * @param pollPeriod Период опроса в милисекундах
     */
    public void setInputQueuePollPeriod(long pollPeriod) {

        inputQueueHandler.setPollPeriod(pollPeriod);
        inputQueueHandler.setThreadRestartNeed(true);
        updateState();

    }


    /** Находиться ли управляющий поток логирующей очереди в неработающем состоянии?
     * (неважно по какой причине - принудительное прерываение, нормальное завершение или сработка исключения) */
/*
    public boolean isLoggingQueueHandlerThreadDone(LoggingType loggingType) {
        QueueHandler queueHandler = loggingQueueHandlerList[loggingType.ordinal()];
        if (queueHandler != null) return queueHandler.isThreadDone();
            else return true;
    }
*/

    /** Возвратить длину списка со ссылками на логирующие очереди, который храниться в
     *  управляющем объекте входной очереди и который используется управляющим объектом входной очереди
     *  для доставки сообщений в логирующие очереди */
/*
    public int getLoggingQueueListSize() {
        return inputQueueHandler.getLoggingQueueCollection().size();
    }
*/


}
