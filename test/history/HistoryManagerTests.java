package test.history;

import static org.junit.jupiter.api.Assertions.*;

import main.manager.history.InMemoryHistoryManager;
import main.model.Status;
import main.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class HistoryManagerTests {

    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void init() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    @DisplayName("1. Проверка добавления задачи в пустую историю")
    void addTaskToEmptyHistory() {
        Task task = new Task("Task 1", "Description 1", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now()); // Исправлено
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task, history.get(0), "Добавленная задача не найдена в истории.");
    }

    @Test
    @DisplayName("2. Проверка дублирования задач в истории")
    void addingDuplicateTasks() {
        Task task = new Task("Task 1", "Description 1", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now()); // Исправлено
        historyManager.add(task);
        historyManager.add(task); // Добавляем ту же задачу повторно

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дублированную задачу.");
    }

    @Test
    @DisplayName("3. Проверка удаления задачи из начала истории")
    void removeTaskFromBeginning() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now()); // Исправлено
        Task task2 = new Task("Task 2", "Description 2", 2, Status.NEW, Duration.ofHours(1), LocalDateTime.now()); // Исправлено
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу после удаления.");
        assertEquals(task2, history.get(0), "История не содержит ожидаемую задачу.");
    }

    @Test
    @DisplayName("4. Проверка удаления задачи из середины истории")
    void removeTaskFromMiddle() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now()); // Исправлено
        Task task2 = new Task("Task 2", "Description 2", 2, Status.NEW, Duration.ofHours(1), LocalDateTime.now()); // Исправлено
        Task task3 = new Task("Task 3", "Description 3", 3, Status.NEW, Duration.ofHours(1), LocalDateTime.now()); // Исправлено
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать две задачи после удаления.");
        assertEquals(task1, history.get(0), "Первой в истории должна остаться первая задача.");
        assertEquals(task3, history.get(1), "Последней в истории должна быть третья задача.");
    }

    @Test
    @DisplayName("5. Проверка удаления задачи из конца истории")
    void removeTaskFromEnd() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now()); // Исправлено
        Task task2 = new Task("Task 2", "Description 2", 2, Status.NEW, Duration.ofHours(1), LocalDateTime.now()); // Исправлено
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу после удаления.");
        assertEquals(task1, history.get(0), "История не содержит ожидаемую задачу.");
    }
}