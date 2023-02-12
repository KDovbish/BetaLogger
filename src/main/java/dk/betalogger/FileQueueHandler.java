package dk.betalogger;

/** Интерфейс для объекта, который должен обслуживать очередь сообщений для логирования в файл */
abstract class FileQueueHandler extends QueueHandler {
    /** Определение файла, куда будет идти логирование */
    abstract FileQueueHandler setFileName(String file);
}
