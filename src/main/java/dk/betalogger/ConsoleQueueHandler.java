package dk.betalogger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Реализация управляющего объекта для консольного логирования */
class ConsoleQueueHandler extends QueueHandler {

    ConsoleQueueHandler() {
        queue = new ConcurrentLinkedQueue<LogData>();
        threadCode = () -> {
            //System.out.println("DEBUG  console handler thread begin");
            LogData logData = queue.poll();
            while (logData != null) {
                System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")) + "  " + logData.getStringForLogging());
                logData = queue.poll();
            }
        };
    }

}
