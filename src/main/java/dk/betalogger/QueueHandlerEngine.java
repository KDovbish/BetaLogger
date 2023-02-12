package dk.betalogger;

import lombok.AccessLevel;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Абстрактный класс, реализующий каркас механизма обработки очереди.
 * <br/><br/>
 * В данном классе полностью прописана логика создания, запуска, останова отдельного потока для обработки очереди, а также настройка периода опроса очереди.
 * В порожденном классе следует через конструктор или иным способом получить очередь для обработки и присвоить ссылку на эту очередь переменной queue.
 * В порожденном классе необходимо прописать логику обработки очереди в ламбда-переменной threadCode.
 * @version 3.0 (переписана логика старта/останова потока: останавливается и запускатся именно пользовательский поток, а не исполнитель)
 */
abstract class QueueHandlerEngine {

    /**
     * Очередь, которая будет обрабатываться потоком, код которого прописан в threadCode.
     * Тем или иным способом передается в порожденный класс.
     */
    Queue<LogData> queue;

    /** Код, который работает с очередью. Прописывается в порожденных классах. */
    Runnable threadCode = () -> {};

    @Setter(AccessLevel.PACKAGE) private long pollPeriod = 1000l;                            //  период опроса очереди
    @Setter(AccessLevel.PACKAGE) private TimeUnit pollPeriodUnit = TimeUnit.MILLISECONDS;    //  единицы времени используемые для опеределения периода опроса

    private ScheduledExecutorService executorService;           //  исполнитель, запускающий поток threadCode
    private ScheduledFuture scheduledFuture;                    //  объект, управляющий работающим потоком

    /**
     * Фабрика, которая унифицирует создание потоков с заданными свойствами. В данном случае будут создаваться потоки-демоны.
     * Фабрика передается в метод, который используется для создания исполнителя.
     * Созданный исполнитель, при запуске потока, принимает переменную, ссылающаяся на объект, который реализуют тип Runnable.
     * Созданный исполнитель вызывает CustomThreadFactory.newThread(Runnable), тем самым создавая поток с нужными свойствами.
     */
    private class CustomThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable threadCode) {
            Thread thread = new Thread(threadCode);
            thread.setDaemon(true);
            return thread;
        }
    }

    QueueHandlerEngine() {
        executorService = Executors.newScheduledThreadPool(1, new CustomThreadFactory());
    }

    /** Запустить поток */
    void start() {
        if (isThreadDone()) {
            scheduledFuture = executorService.scheduleWithFixedDelay(threadCode, 0, pollPeriod, pollPeriodUnit);
        }
    }

    /** Остановить поток */
    void stop() {
        if (isThreadDone() == false) {
            scheduledFuture.cancel(false);
        }
    }

    /** Работает ли поток? true(не работает)/false(работает) */
    boolean isThreadDone() {
        if (scheduledFuture != null) return scheduledFuture.isDone();
            else return true;
    }

}
