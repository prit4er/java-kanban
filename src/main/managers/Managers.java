package main.managers;

import main.managers.historyManager.HistoryManager;
import main.managers.historyManager.InMemoryHistoryManager;
import main.managers.taskManager.InMemoryTaskManager;
import main.managers.taskManager.TaskManager;

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