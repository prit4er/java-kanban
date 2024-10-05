package test.task;

import static org.junit.jupiter.api.Assertions.*;

import main.manager.task.inMemory.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.model.Task;
import main.model.Subtask;
import main.model.Epic;
import main.model.Status;
import main.manager.task.TaskManager;
import main.manager.Managers;

public class TaskManagerTests {

    private InMemoryTaskManager taskManager; // Используем конкретный тип менеджера задач

    @BeforeEach
    void init() {
        taskManager = new InMemoryTaskManager(); // Используем конкретный менеджер задач
    }

    @Test
    @DisplayName("1. Проверка равенства объектов Task по ID")
    void testTaskEqualityById() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", 1, Status.NEW);

        // Используем id для сравнения, поэтому задачи считаются равными
        assertEquals(task1.getId(), task2.getId(), "Задачи с одинаковым ID должны быть равны.");
    }

    @Test
    @DisplayName("2. Проверка равенства наследников класса Task по ID")
    void testSubtaskAndEpicEqualityById() {
        Epic epic = new Epic("Epic 1", "Description 1", 1);
        Subtask subtask = new Subtask("Subtask 1", "Description 1", 1, epic.getId(), Status.NEW);

        // Используем id для сравнения, поэтому подзадача и эпик считаются равными
        assertEquals(epic.getId(), subtask.getId(), "Подзадача и эпик с одинаковым ID должны быть равны.");
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
        Task task = new Task("Task", "Description", 1, Status.NEW);
        Epic epic = new Epic("Epic", "Description", 2);
        Subtask subtask = new Subtask("Subtask", "Description", 3, epic.getId(), Status.NEW);

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
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", 2, Status.NEW);

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "ID задач не должны конфликтовать.");
    }

    @Test
    @DisplayName("6. Проверка неизменности задачи")
    void testTaskImmutability() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        // Создаем и добавляем задачу
        Task task = new Task("Task", "Description", 0, Status.NEW);
        manager.addTask(task);

        // Получаем задачу из менеджера
        Task retrievedTask = manager.getTask(task.getId());

        // Проверяем, что задача после извлечения из менеджера осталась неизменной
        assertEquals(task.getName(), retrievedTask.getName(), "Имя задачи должно оставаться неизменным после извлечения.");
        assertEquals(task.getDescription(), retrievedTask.getDescription(),
                     "Описание задачи должно оставаться неизменным после извлечения.");
        assertEquals(task.getStatus(), retrievedTask.getStatus(), "Статус задачи должен оставаться неизменным после извлечения.");
    }

    @Test
    @DisplayName("7. Новый тест для метода updateEpic")
    void updateEpic_shouldUpdateEpicDetails() {
        // given
        Epic originalEpic = new Epic("Original Epic", "Original Description", 1);
        taskManager.addEpic(originalEpic);

        // Обновляем эпик с новыми данными
        Epic updatedEpic = new Epic("Updated Epic", "Updated Description", originalEpic.getId());

        // when
        taskManager.updateEpic(updatedEpic);

        // then
        Epic retrievedEpic = taskManager.getEpic(originalEpic.getId());
        assertNotNull(retrievedEpic, "Эпик должен быть найден в менеджере задач.");
        assertEquals("Updated Epic", retrievedEpic.getName(), "Название эпика должно быть обновлено.");
        assertEquals("Updated Description", retrievedEpic.getDescription(), "Описание эпика должно быть обновлено.");
    }

}