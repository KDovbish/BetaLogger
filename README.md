# BetaLogger
Реализация библиотеки логгера

Примерный принцип использования: 

<code>
BetaLogger betaLogger = new BetaLogger();
betaLogger.setConsoleLogging();				
betaLogger.setFileLogging("c:\\logs\\example.log");
betaLogger.setDbLogging(DbType.MySQL, "jdbc:mysql://127.0.0.1:3306/loggingDb", "user", "password", "logging_table");
</code>
