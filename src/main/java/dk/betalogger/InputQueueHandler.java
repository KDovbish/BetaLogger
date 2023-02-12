package dk.betalogger;

/**
 * Абстрактный класс, на базе которого должны реализовываться любые
 * варианты логики для управления входной очередью.
 */
abstract  class InputQueueHandler extends QueueHandler {

    /**
     * Регистрация логирующей очереди в управляющем объекте входной очереди
     * @param queue Объект, реализующий интерфейс {@link AddingToQueue}. Все управляющие объекты логирующих очередей реализуют данный интерфейс.
     */
    abstract void registerLoggingQueue(AddingToQueue queue);

    /**
     * Снятие регистрации логирующей очереди в управляющем объекте входной очереди
     * @param queue Объект, реализующий интерфейс {@link AddingToQueue}. Все управляющие объекты логирующих очередей реализуют данный интерфейс.
     */
    abstract void unregisterLoggingQueue(AddingToQueue queue);
}

