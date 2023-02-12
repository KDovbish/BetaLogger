package dk.betalogger;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;


/** Реализация интерфейса для обработки файловой очереди */
@Accessors(chain = true)
class AlphaFileQueueHandler extends FileQueueHandler {

    @Setter(AccessLevel.PACKAGE) private String fileName;

    private boolean error = false;

    AlphaFileQueueHandler() {
        queue = new ConcurrentLinkedQueue<LogData>();
        threadCode = () -> {

            try {

                if (queue.peek() != null) {
                    FileWriter file = new FileWriter(fileName, true);

                    LogData logData;
                    while ((logData = queue.poll()) != null) {
                        //file.write(logData.getStringForLogging() + "\n");
                        file.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")) + "  " + logData.getStringForLogging() + "\n");
                    }
                    file.close();
                }

                if (error == true) {
                    //  Сбрасываем флаг ошибки
                    error = false;
                    //  Разрешаем прием сообщений в очередь
                    setAddToQueue(true);
                    //inputQueueHandler.registerLoggingQueue(queue);
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (error == false) {
                    //  Выставляем флаг ошибки
                    error = true;
                    //  Блокируем прием сообщений в очередь
                    setAddToQueue(false);
                }
            }

        };
    }


}
