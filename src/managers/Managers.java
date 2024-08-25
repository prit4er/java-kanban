package managers;

import managers.historyManager.HistoryManager;
import managers.historyManager.InMemoryHistoryManager;
import managers.taskManager.InMemoryTaskManager;
import managers.taskManager.TaskManager;

public class Managers {

    // Метод для получения менеджера задач по умолчанию
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    // Метод для получения менеджера истории по умолчанию
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}