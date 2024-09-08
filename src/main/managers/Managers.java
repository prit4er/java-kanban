package main.managers;

import main.managers.java.historyManager.HistoryManager;
import main.managers.java.historyManager.InMemoryHistoryManager;
import main.managers.java.taskManager.InMemoryTaskManager;
import main.managers.java.taskManager.TaskManager;

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