package test;

import main.manager.task.inMemory.InMemoryTaskManager;
import main.model.Epic;
import main.model.Status;
import main.model.Subtask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EpicStatusTests {

    private InMemoryTaskManager taskManager;
    private Epic epic;

    @BeforeEach
    void init() {
        taskManager = new InMemoryTaskManager();
        epic = new Epic("Epic 1", "Description 1", 1, Status.NEW);
        taskManager.addEpic(epic);
    }

    @Test
    @DisplayName("1. Все подзадачи со статусом NEW")
    void testAllSubtasksNew() {
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 2, epic.getId(), Status.NEW, Duration.ofHours(1), now);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 3, epic.getId(), Status.NEW, Duration.ofHours(1),
                                       now.plusHours(1)); // Смещение на 1 час
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(2, epic.getSubtaskIds().size(), "Эпик должен содержать 2 подзадачи.");
        assertTrue(epic.getSubtaskIds().contains(subtask1.getId()), "Эпик должен содержать подзадачу subtask1.");
        assertTrue(epic.getSubtaskIds().contains(subtask2.getId()), "Эпик должен содержать подзадачу subtask2.");
    }

    @Test
    @DisplayName("2. Все подзадачи со статусом DONE")
    void testAllSubtasksDone() {
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 2, epic.getId(), Status.DONE, Duration.ofHours(1), now);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 3, epic.getId(), Status.DONE, Duration.ofHours(1),
                                       now.plusHours(1)); // Смещение на 1 час
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(Status.DONE, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть DONE.");
    }

    @Test
    @DisplayName("3. Подзадачи со статусами NEW и DONE")
    void testSubtasksNewAndDone() {
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 2, epic.getId(), Status.NEW, Duration.ofHours(1), now);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 3, epic.getId(), Status.DONE, Duration.ofHours(1),
                                       now.plusHours(1)); // Смещение на 1 час
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertTrue(epic.getSubtaskIds().contains(subtask1.getId()), "Эпик должен содержать подзадачу subtask1.");
        assertTrue(epic.getSubtaskIds().contains(subtask2.getId()), "Эпик должен содержать подзадачу subtask2.");
    }

    @Test
    @DisplayName("4. Подзадачи со статусом IN_PROGRESS")
    void testAllSubtasksInProgress() {
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 2, epic.getId(), Status.IN_PROGRESS, Duration.ofHours(1), now);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 3, epic.getId(), Status.IN_PROGRESS, Duration.ofHours(1),
                                       now.plusHours(1)); // Смещение на 1 час
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        // Проверяем, что подзадачи добавлены в эпик
        assertTrue(epic.getSubtaskIds().contains(subtask1.getId()), "Эпик должен содержать подзадачу subtask1.");
        assertTrue(epic.getSubtaskIds().contains(subtask2.getId()), "Эпик должен содержать подзадачу subtask2.");

        // Проверяем статусы подзадач
        assertEquals(Status.IN_PROGRESS, taskManager.getSubtask(subtask1.getId()).getStatus(),
                     "Статус подзадачи subtask1 должен быть IN_PROGRESS.");
        assertEquals(Status.IN_PROGRESS, taskManager.getSubtask(subtask2.getId()).getStatus(),
                     "Статус подзадачи subtask2 должен быть IN_PROGRESS.");
    }

    @Test
    @DisplayName("5. Эпик без подзадач должен иметь статус NEW")
    void testEpicWithoutSubtasks() {
        assertEquals(Status.NEW, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика без подзадач должен быть NEW.");
    }
}