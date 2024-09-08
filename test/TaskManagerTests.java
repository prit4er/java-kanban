package test.tasks;

import static org.junit.jupiter.api.Assertions.*;

import main.managers.java.taskManager.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import main.model.Task;
import main.model.Subtask;
import main.model.Epic;
import main.model.Status;
import main.managers.java.taskManager.TaskManager;
import main.managers.Managers;

public class TaskManagerTests {

    private InMemoryTaskManager taskManager; // Используем конкретный тип менеджера задач

    @BeforeEach
    void init() {
        taskManager = new InMemoryTaskManager(); // Используем конкретный менеджер задач
    }

    // 	1.	Проверка равенства объектов Task по ID
    @Test
    void testTaskEqualityById() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", 1, Status.NEW);

        // Используем id для сравнения, поэтому задачи считаются равными
        assertEquals(task1.getId(), task2.getId(), "Задачи с одинаковым ID должны быть равны.");
    }

    // 	2.	Проверка равенства наследников класса Task по ID
    @Test
    void testSubtaskAndEpicEqualityById() {
        Epic epic = new Epic("Epic 1", "Description 1", 1);
        Subtask subtask = new Subtask("Subtask 1", "Description 1", 1, epic.getId(), Status.NEW);

        // Используем id для сравнения, поэтому подзадача и эпик считаются равными
        assertEquals(epic.getId(), subtask.getId(), "Подзадача и эпик с одинаковым ID должны быть равны.");
    }

    //	3.	Проверка, что объект Epic нельзя добавить в самого себя в виде подзадачи
    // Вообще по чату ходит мненение, что написать такой тест невозможно!!!
    @Test
    void testEpicCannotAddSelfAsSubtask() {
        // Создаем и добавляем эпик
        Epic epic = new Epic("Epic 1", "Epic description", 1);
        taskManager.addEpic(epic);

        // Создаем подзадачу с тем же ID, что и у эпика
        Subtask subtask = new Subtask("Subtask 1", "Subtask description", 2, epic.getId(), Status.NEW);

        // Проверяем, что выбрасывается исключение при попытке добавления подзадачи
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.addSubtask(subtask);
        });

        assertEquals("Эпик не может быть добавлен как своя собственная подзадача.", thrown.getMessage());
    }

    // 	4.	Проверка, что объект Subtask нельзя сделать своим эпиком
    // Вообще по чату ходит мненение, что написать такой тест невозможно!!!
    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        // Создаем и добавляем подзадачу
        Subtask subtask = new Subtask("Subtask 1", "Description 1", 1, 0, Status.NEW);
        taskManager.addSubtask(subtask);

        // Попытка добавить эпик с тем же ID, что и у подзадачи
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.addEpic(new Epic("Epic 1", "Epic description", subtask.getId()));
        });

        // Проверяем, что исключение содержит правильное сообщение
        assertEquals("ID эпика не может совпадать с ID существующих задач или подзадач.", thrown.getMessage());
    }

    //	5.	Проверка, что утилитарный класс всегда возвращает проинициализированные экземпляры менеджеров
    @Test
    void testManagersUtility() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Утилитарный класс должен возвращать инициализированный экземпляр менеджера.");
        assertTrue(manager instanceof InMemoryTaskManager, "Утилитарный класс должен возвращать экземпляр InMemoryTaskManager.");
    }

    //	6.	Проверка добавления задач разного типа и их нахождение по ID
    @Test
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

    // 7.	Проверка, что задачи с заданным и сгенерированным ID не конфликтуют
    @Test
    void testUniqueIds() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", 2, Status.NEW);

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "ID задач не должны конфликтовать.");
    }

    // 	8.	Проверка неизменности задачи
    @Test
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

    // 9. Новый тест для метода updateEpic
    @Test
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