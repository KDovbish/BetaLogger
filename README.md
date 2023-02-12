# BetaLogger
Реализация библиотеки логгера

Ключевые моменты реализации:<br/>
Есть одна входная очередь логгера. Есть очереди обслуживающие каждый вид логирования.
Сообщения, попавшие во входную очередь, распределяются по очередям логирования. Каждая из очередей, обслуживается отдельным потоком.
Включение/отключение любого из типов логирования возможно в любой момент времени, без необходиомсти каких-либо дополнительных действий, типа пересоздания/переинициализации логгера. Т.е. все изменения внутренних настроек логгера подхватывается на лету.

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
