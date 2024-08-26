package test.history;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.Task;
import managers.historyManager.InMemoryHistoryManager; // Используем конкретный менеджер истории
import java.util.List;

public class HistoryManagerTests {

    private InMemoryHistoryManager historyManager; // Используем конкретный тип менеджера

    @BeforeEach
    void init() {
        // Используем конкретный менеджер истории для изоляции тестов
        historyManager = new InMemoryHistoryManager();
    }

    // 1. Проверка добавления задачи в историю
    @Test
    void add_addTaskToHistory_shouldContainAddedTask() {
        // given
        Task task = new Task("Task 1", "Description 1", 1);

        // when
        historyManager.add(task);

        // then
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть пустой.");
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task, history.get(0), "История не содержит добавленную задачу.");
    }

    // 2. Проверка лимита размера истории
    @Test
    void add_addTasksBeyondLimit_shouldContainOnlyLast10Tasks() {
        // given
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description " + i, i);
            historyManager.add(task);
        }

        // when
        List<Task> history = historyManager.getHistory();

        // then
        assertNotNull(history, "История не должна быть пустой.");
        assertEquals(10, history.size(), "История должна содержать только 10 задач.");
        assertEquals(6, history.get(0).getId(), "История должна начинаться с 6-й задачи.");
    }
}