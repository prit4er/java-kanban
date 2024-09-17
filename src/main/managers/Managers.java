package main.managers;

import main.managers.history.HistoryManager;
import main.managers.history.InMemoryHistoryManager;
import main.managers.task.InMemoryTaskManager;
import main.managers.task.TaskManager;

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