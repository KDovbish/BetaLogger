# BetaLogger
Реализация библиотеки логгера

Примерный принцип использования: 

<code>
//  Создали объект логгера
BetaLogger betaLogger = new BetaLogger();
//  Включили необходимы типы логирования
betaLogger.setConsoleLogging();				
betaLogger.setFileLogging("c:\\logs\\example.log");
betaLogger.setDbLogging(DbType.MySQL, "jdbc:mysql://127.0.0.1:3306/loggingDb", "user", "password", "logging_table");
//  Включили механизм логгера
betaLogger.start();             
...
betaLogger.add("... событие ...");	   // логирование в консоль, файл, БД
...
betaLogger.unsetLogging(LoggingType.DB);   // отключили логирование в БД
...
betaLogger.add("... событие ...");         // логирование в консоль, файл
...
betaLogger.stop();                         // остановили логирование
</code>
