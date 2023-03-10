package dk.betalogger;

/**
 * Интерфейс добавления в очередь.
 * <br/><br/>
 * Данный интерфейс введен для стандартизации добавления в любую очередь бета-логера.<br/>
 * Данный интерфейс должен реализовываться любыми управляющими объектами очередей.<br/>
 * При любых добавлениях сообщений в очереди должен использоваться только данный интерфейс.<br/>
 * <b>Ссылка на данный интерфейс используется в управляющем объекте входной очереди, для стандартизации добавления сообщений в логирующие очереди.</b>
 */
interface AddingToQueue {
    /**
     * Добавить в очередь.
     * @param logData Данные для добавления. Должны быть представлены любым подклассом абстрактного класса {@link LogData}
     */
    void offer(LogData logData);
}
