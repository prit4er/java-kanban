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

import static org.junit.jupiter.api.Assertions.*;

public class EpicStatusTests {

    private InMemoryTaskManager taskManager;
    private Epic epic;

    @BeforeEach
    void init() {
        taskManager = new InMemoryTaskManager();
        epic = new Epic("Epic 1", "Epic description", 1);
        taskManager.addEpic(epic);
    }

    @Test
    @DisplayName("1. Все подзадачи со статусом NEW")
    void testAllSubtasksNew() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 2, epic.getId(), Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 3, epic.getId(), Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(Status.NEW, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть NEW.");
    }

    @Test
    @DisplayName("2. Все подзадачи со статусом DONE")
    void testAllSubtasksDone() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 2, epic.getId(), Status.DONE, Duration.ofHours(1),
                                       LocalDateTime.now());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 3, epic.getId(), Status.DONE, Duration.ofHours(1),
                                       LocalDateTime.now());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(Status.DONE, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть DONE.");
    }

    @Test
    @DisplayName("3. Подзадачи со статусами NEW и DONE")
    void testSubtasksNewAndDone() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 2, epic.getId(), Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 3, epic.getId(), Status.DONE, Duration.ofHours(1),
                                       LocalDateTime.now());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть IN_PROGRESS.");
    }

    @Test
    @DisplayName("4. Подзадачи со статусом IN_PROGRESS")
    void testAllSubtasksInProgress() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 2, epic.getId(), Status.IN_PROGRESS, Duration.ofHours(1),
                                       LocalDateTime.now());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 3, epic.getId(), Status.IN_PROGRESS, Duration.ofHours(1),
                                       LocalDateTime.now());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть IN_PROGRESS.");
    }
}