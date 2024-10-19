package test.task;

import main.manager.task.inMemory.InMemoryTaskManager;
import main.manager.task.TaskManager;
import main.manager.Managers;
import main.model.Task;
import main.model.Subtask;
import main.model.Epic;
import main.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTests {

    private TaskManager taskManager;

    @BeforeEach
    void init() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    @DisplayName("1. Проверка равенства объектов Task по ID")
    void testTaskEqualityById() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Description 2", 2, Status.NEW, Duration.ofHours(1), LocalDateTime.now());

        // Проверка, что ID разных задач не равны
        assertNotEquals(task1.getId(), task2.getId(), "Задачи с одинаковым ID не должны быть равны.");
    }

    @Test
    @DisplayName("2. Проверка равенства наследников класса Task по ID")
    void testSubtaskAndEpicEqualityById() {
        Epic epic = new Epic("Epic 1", "Description 1", 1);
        Subtask subtask = new Subtask("Subtask", "Description", 3, epic.getId(), Status.NEW, Duration.ofHours(1), LocalDateTime.now());

        // Проверка, что ID подзадачи и эпика не равны
        assertNotEquals(epic.getId(), subtask.getId(), "Подзадача и эпик с различными ID не должны быть равны.");
    }

    @Test
    @DisplayName("3. Проверка, что утилитарный класс всегда возвращает проинициализированные экземпляры менеджеров")
    void testManagersUtility() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Утилитарный класс должен возвращать инициализированный экземпляр менеджера.");
        assertInstanceOf(InMemoryTaskManager.class, manager, "Утилитарный класс должен возвращать экземпляр InMemoryTaskManager.");
    }

    @Test
    @DisplayName("4. Проверка добавления задач разного типа и их нахождение по ID")
    void testAddDifferentTypesOfTasks() {
        Task task = new Task("Task", "Description", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        Epic epic = new Epic("Epic", "Description", 2);
        Subtask subtask = new Subtask("Subtask", "Description", 3, epic.getId(), Status.NEW, Duration.ofHours(1), LocalDateTime.now());

        taskManager.addTask(task);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);

        assertNotNull(taskManager.getTask(task.getId()), "Задача не найдена.");
        assertNotNull(taskManager.getEpic(epic.getId()), "Эпик не найден.");
        assertNotNull(taskManager.getSubtask(subtask.getId()), "Подзадача не найдена.");
    }

    @Test
    @DisplayName("5. Проверка, что задачи с заданным и сгенерированным ID не конфликтуют")
    void testUniqueIds() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Description 2", 2, Status.NEW, Duration.ofHours(1), LocalDateTime.now());

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "ID задач не должны конфликтовать.");
    }

    @Test
    @DisplayName("6. Проверка неизменности задачи")
    void testTaskImmutability() {
        Task task = new Task("Task", "Description", 0, Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        taskManager.addTask(task);

        Task retrievedTask = taskManager.getTask(task.getId());

        // Проверяем, что задача осталась неизменной после извлечения
        assertEquals(task.getName(), retrievedTask.getName(), "Имя задачи должно оставаться неизменным после извлечения.");
        assertEquals(task.getDescription(), retrievedTask.getDescription(),
                     "Описание задачи должно оставаться неизменным после извлечения.");
        assertEquals(task.getStatus(), retrievedTask.getStatus(), "Статус задачи должен оставаться неизменным после извлечения.");
    }

    @Test
    @DisplayName("7. Обновление эпика должно обновить его детали")
    void updateEpic_shouldUpdateEpicDetails() {
        Epic originalEpic = new Epic("Original Epic", "Original Description", 1);
        taskManager.addEpic(originalEpic);

        // Обновляем эпик с новыми данными
        Epic updatedEpic = new Epic("Updated Epic", "Updated Description", originalEpic.getId());

        taskManager.updateEpic(updatedEpic);

        Epic retrievedEpic = taskManager.getEpic(originalEpic.getId());
        assertNotNull(retrievedEpic, "Эпик должен быть найден в менеджере задач.");
        assertEquals("Updated Epic", retrievedEpic.getName(), "Название эпика должно быть обновлено.");
        assertEquals("Updated Description", retrievedEpic.getDescription(), "Описание эпика должно быть обновлено.");
    }

    @Test
    @DisplayName("8. Проверка удаления задачи")
    void testDeleteTask() {
        Task task = new Task("Task to Delete", "Description", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        taskManager.addTask(task);

        taskManager.deleteTaskById(task.getId());
        assertNull(taskManager.getTask(task.getId()), "Задача должна быть удалена.");
    }

    @Test
    @DisplayName("9. Проверка обновления задачи")
    void updateTask_shouldUpdateTaskDetails() {
        Task originalTask = new Task("Original Task", "Original Description", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        taskManager.addTask(originalTask);

        Task updatedTask = new Task("Updated Task", "Updated Description", originalTask.getId(), Status.IN_PROGRESS, Duration.ofHours(2),
                                    LocalDateTime.now().plusDays(1));
        taskManager.updateTask(updatedTask);

        Task retrievedTask = taskManager.getTask(originalTask.getId());
        assertNotNull(retrievedTask, "Задача должна быть найдена в менеджере задач.");
        assertEquals("Updated Task", retrievedTask.getName(), "Имя задачи должно быть обновлено.");
        assertEquals("Updated Description", retrievedTask.getDescription(), "Описание задачи должно быть обновлено.");
        assertEquals(Status.IN_PROGRESS, retrievedTask.getStatus(), "Статус задачи должен быть обновлен.");
    }

    @Test
    @DisplayName("10. Проверка статусов эпиков")
    void testEpicStatusUpdates() {
        Epic epic = new Epic("Epic 1", "Description 1", 1);
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 2, epic.getId(), Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 3, epic.getId(), Status.DONE, Duration.ofHours(1),
                                       LocalDateTime.now());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть IN_PROGRESS.");
    }
}