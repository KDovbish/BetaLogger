package dk.betalogger;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

/** Реализация интерфейса для управления входной очередью */
class AlphaInputQueueHandler extends InputQueueHandler {

    /** Список ссылок на логирующие очереди, в которые будут разбрасываться сообщения из входной очереди  */
    private Set<AddingToQueue> loggingQueueList = new CopyOnWriteArraySet<>();

    AlphaInputQueueHandler() {

        queue = new ConcurrentLinkedQueue<LogData>();;
        threadCode = () -> {

            if (loggingQueueList.size() > 0) {
                LogData logData;
                while ((logData = this.queue.poll()) != null) {
                    for(AddingToQueue q: loggingQueueList) {
                        q.offer(logData);
                    }
                }
            }
        };
    }


    @Override
    void registerLoggingQueue(AddingToQueue queue) {
        loggingQueueList.add(queue);
    }

    @Override
    void unregisterLoggingQueue(AddingToQueue queue) {
        loggingQueueList.remove(queue);
    }
}



