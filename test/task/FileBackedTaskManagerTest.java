package test.task;

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

import static org.junit.jupiter.api.Assertions.*;

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
        // Создаем несколько задач
        Task task1 = new Task("Task 1", "Description 1", 1);
        Task task2 = new Task("Task 2", "Description 2", 2);
        Epic epic = new Epic("Epic 1", "Epic description", 3);
        Subtask subtask = new Subtask("Subtask 1", "Subtask description", 4, epic.getId(), Status.NEW);

        // Добавляем задачи в менеджер
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic);
        manager.addSubtask(subtask);

        // Проверяем, что файл не пустой
        assertTrue(tempFile.length() > 0);
    }

    @Test
    @DisplayName("3. Проверяем содержание задач")
    public void testLoadMultipleTasks() {
        // Сначала сохраняем несколько задач
        Task task1 = new Task("Task 1", "Description 1", 1);
        Task task2 = new Task("Task 2", "Description 2", 2);
        Epic epic = new Epic("Epic 1", "Epic description", 3);
        Subtask subtask = new Subtask("Subtask 1", "Subtask description", 4, epic.getId(), Status.NEW);

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic);
        manager.addSubtask(subtask);

        // Теперь создаем новый менеджер и загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем, что загруженные задачи совпадают с добавленными
        assertEquals(2, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        // Проверяем содержание задач
        assertEquals(task1, loadedManager.getTask(task1.getId()));
        assertEquals(task2, loadedManager.getTask(task2.getId()));
        assertEquals(epic, loadedManager.getEpic(epic.getId()));
        assertEquals(subtask, loadedManager.getSubtask(subtask.getId()));
    }

}