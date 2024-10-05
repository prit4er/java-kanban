package main.manager;

import main.manager.history.HistoryManager;
import main.manager.history.InMemoryHistoryManager;
import main.manager.task.inMemory.InMemoryTaskManager;
import main.manager.task.TaskManager;

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