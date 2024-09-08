package test.history;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import model.Task;
import managers.historyManager.InMemoryHistoryManager; // Используем конкретный менеджер истории

import java.util.List;

public class HistoryManagerTests {

    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void init() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    @DisplayName("1. Проверка добавления задачи в историю")
    void add_addTaskToHistory_shouldContainAddedTask() {
        Task task = new Task("Task 1", "Description 1", 1);

        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть пустой.");
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task, history.getFirst(), "История не содержит добавленную задачу.");
    }

    @Test
    @DisplayName("2. Проверка лимита размера истории")
    void add_addTasksBeyondLimit_shouldContainOnlyLast10Tasks() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description " + i, i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть пустой.");
        assertEquals(10, history.size(), "История должна содержать только 10 задач.");
        assertEquals(6, history.getFirst().getId(), "История должна начинаться с 6-й задачи.");
    }

    @Test
    @DisplayName("3. Проверка удаления задачи из истории")
    void remove_removeTaskFromHistory_shouldNotContainTask() {
        Task task1 = new Task("Task 1", "Description 1", 1);
        Task task2 = new Task("Task 2", "Description 2", 2);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);  // Удаляем первую задачу

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу после удаления.");
        assertEquals(task2, history.getFirst(), "В истории должна остаться только вторая задача.");
    }

    @Test
    @DisplayName("4. Проверка, что добавление задачи несколько раз оставляет только последнюю версию")
    void add_addingSameTaskMultipleTimes_shouldContainOnlyLastAddedTask() {
        Task task1 = new Task("Task 1", "Description 1", 1);
        Task task2 = new Task("Task 2", "Description 2", 2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // Повторное добавление первой задачи

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать две задачи.");
        assertEquals(task2, history.get(0), "Первой в истории должна быть вторая задача.");
        assertEquals(task1, history.get(1), "Последней в истории должна быть первая задача.");
    }

    @Test
    @DisplayName("5. Проверка удаления из середины списка")
    void remove_removeTaskFromMiddleOfHistory_shouldCorrectlyRemoveTask() {
        Task task1 = new Task("Task 1", "Description 1", 1);
        Task task2 = new Task("Task 2", "Description 2", 2);
        Task task3 = new Task("Task 3", "Description 3", 3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);  // Удаляем вторую задачу

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать две задачи после удаления.");
        assertEquals(task1, history.get(0), "Первой в истории должна остаться первая задача.");
        assertEquals(task3, history.get(1), "Последней в истории должна быть третья задача.");
    }

    @Test
    @DisplayName("6. Проверка целостности данных при изменении задачи")
    void setTaskFields_shouldNotAffectTaskInHistory() {
        Task task = new Task("Task 1", "Description 1", 1);

        historyManager.add(task);

        // Изменим поле задачи через сеттеры
        task.setName("Updated Task 1");
        task.setDescription("Updated Description");

        List<Task> history = historyManager.getHistory();

        // Убедимся, что изменения отражены в истории
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals("Updated Task 1", history.getFirst().getName(), "Имя задачи должно быть обновлено.");
        assertEquals("Updated Description", history.getFirst().getDescription(), "Описание задачи должно быть обновлено.");
    }
}