package test.task;

import static org.junit.jupiter.api.Assertions.*;

import main.manager.task.file.FileBackedTaskManager;
import main.model.Epic;
import main.model.Status;
import main.model.Subtask;
import main.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManagerTest extends TaskManagerTests {

    private FileBackedTaskManager manager;
    private File tempFile;

    @BeforeEach
    public void setUp() {
        try {
            // Создаем временный файл для тестов
            tempFile = File.createTempFile("task_manager_test", ".csv");
            tempFile.deleteOnExit(); // Удаляем файл после завершения теста

            // Используем фабричный метод вместо прямого вызова конструктора
            manager = FileBackedTaskManager.loadFromFile(tempFile);
        } catch (IOException e) {
            fail("Не удалось создать временный файл для тестирования: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        // Удаляем файл, если он не был удален в методе setUp
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    @DisplayName("1. Проверка загрузки из пустого файла")
    public void testSaveAndLoadEmptyFile() {
        // Проверяем, что в новом менеджере задач нет задач
        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());

        // Загружаем из пустого файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    @DisplayName("2. Проверяем, что файл не пустой")
    public void testSaveMultipleTasks() {
        // Устанавливаем фиксированное время для задач
        LocalDateTime startTime = LocalDateTime.of(2024, 10, 23, 10, 0);

        // Создаем несколько задач с различными временными параметрами
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW, Duration.ofHours(1), startTime);
        Task task2 = new Task("Task 2", "Description 2", 2, Status.NEW, Duration.ofHours(1), startTime.plusHours(1)); // Сдвигаем на 1 час
        Epic epic = new Epic("Epic 1", "Epic description", 1, Status.NEW);
        Subtask subtask = new Subtask("Subtask 1", "Subtask description", 1, epic.getId(), Status.NEW, Duration.ofHours(1),
                                      startTime.plusHours(2)); // Сдвигаем на 2 часа

        // Добавляем задачи в менеджер
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic);
        manager.addSubtask(subtask);

        // Проверяем, что файл не пустой
        assertTrue(tempFile.length() > 0);
    }
}