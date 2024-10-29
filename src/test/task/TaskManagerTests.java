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
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskManagerTests {

    private TaskManager taskManager;
    private Epic epic;

    @BeforeEach
    void init() {
        taskManager = new InMemoryTaskManager(); epic = new Epic("Epic 1", "Description 1", 1, Status.NEW); taskManager.addEpic(epic);
    }

    private Task createTask(String name, String description, int id, Status status, Duration duration, LocalDateTime startTime) {
        return new Task(name, description, id, status, duration, startTime);
    }

    private Subtask createSubtask(String name, String description, int id, int epicId, Status status, Duration duration,
                                  LocalDateTime startTime) {
        return new Subtask(name, description, id, epicId, status, duration, startTime);
    }

    private Epic createEpic(String name, String description, int id, Status status) {
        return new Epic(name, description, id, status);
    }

    @Test
    @DisplayName("1. Проверка равенства объектов Task по ID")
    void testTaskEqualityById() {
        Task task1 = createTask("Task 1", "Description 1", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        Task task2 = createTask("Task 2", "Description 2", 2, Status.NEW, Duration.ofHours(1), LocalDateTime.now());

        // Проверка, что ID разных задач не равны
        assertNotEquals(task1.getId(), task2.getId(), "Задачи с одинаковым ID не должны быть равны.");
    }

    @Test
    @DisplayName("2. Проверка равенства наследников класса Task по ID")
    void testSubtaskAndEpicEqualityById() {
        Epic epic = createEpic("Epic 1", "Description 1", 1, Status.NEW); // Добавлено значение статуса
        Subtask subtask = createSubtask("Subtask", "Description", 3, epic.getId(), Status.NEW, Duration.ofHours(1), LocalDateTime.now());

        // Проверка, что ID подзадачи и эпика не равны
        assertNotEquals(epic.getId(), subtask.getId(), "Подзадача и эпик с различными ID не должны быть равны.");
    }

    @Test
    @DisplayName("3. Проверка, что утилитарный класс всегда возвращает проинициализированные экземпляры менеджеров")
    void testManagersUtility() {
        TaskManager manager = Managers.getDefault(); assertNotNull(manager,
                                                                   "Утилитарный класс должен возвращать инициализированный экземпляр " +
                                                                           "менеджера.");
        assertInstanceOf(InMemoryTaskManager.class, manager, "Утилитарный класс должен возвращать экземпляр InMemoryTaskManager.");
    }

    @Test
    @DisplayName("4. Проверка добавления задач разного типа и их нахождение по ID")
    void testAddDifferentTypesOfTasks() {
        LocalDateTime now = LocalDateTime.now();

        // Создаем первую задачу с продолжительностью 1 час
        Task task = createTask("Task", "Description", 1, Status.NEW, Duration.ofHours(1), now); taskManager.addTask(task);

        // Создаем эпик
        Epic epic = createEpic("Epic", "Description", 2, Status.NEW); taskManager.addEpic(epic);

        // Создаем подзадачу, которая начинается после завершения первой задачи
        Subtask subtask = createSubtask("Subtask", "Description", 3, epic.getId(), Status.NEW, Duration.ofHours(1), now.plusHours(2));
        taskManager.addSubtask(subtask);

        // Проверяем, что задачи найдены
        assertNotNull(taskManager.getTask(task.getId()), "Задача не найдена."); assertNotNull(taskManager.getEpic(epic.getId()),
                                                                                              "Эпик не найден."); assertNotNull(
                taskManager.getSubtask(subtask.getId()), "Подзадача не найдена.");
    }

    @Test
    @DisplayName("5. Проверка, что задачи с заданным и сгенерированным ID не конфликтуют")
    void testUniqueIds() {
        Task task1 = createTask("Task 1", "Description 1", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        Task task2 = createTask("Task 2", "Description 2", 2, Status.NEW, Duration.ofHours(1),
                                LocalDateTime.now().plusHours(1)); // Сдвигаем на 1 час вперед
        taskManager.addTask(task1); taskManager.addTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "ID задач не должны конфликтовать.");
    }

    @Test
    @DisplayName("6. Проверка неизменности задачи")
    void testTaskImmutability() {
        Task task = createTask("Task", "Description", 0, Status.NEW, Duration.ofHours(1), LocalDateTime.now()); taskManager.addTask(task);

        Task retrievedTask = taskManager.getTask(task.getId());

        // Проверяем, что задача осталась неизменной после извлечения
        assertEquals(task.getName(), retrievedTask.getName(), "Имя задачи должно оставаться неизменным после извлечения."); assertEquals(
                task.getDescription(), retrievedTask.getDescription(), "Описание задачи должно оставаться неизменным после извлечения.");
        assertEquals(task.getStatus(), retrievedTask.getStatus(), "Статус задачи должен оставаться неизменным после извлечения.");
    }

    @Test
    @DisplayName("7. Обновление эпика должно обновить его детали")
    void updateEpic_shouldUpdateEpicDetails() {
        Epic originalEpic = createEpic("Original Epic", "Original Description", 1, Status.NEW); taskManager.addEpic(originalEpic);

        // Добавление подзадач с заданным временем начала
        Subtask subtask1 = createSubtask("Subtask 1", "Subtask Description 1", 1, epic.getId(), Status.NEW, Duration.ofHours(1),
                                         LocalDateTime.now()); Subtask subtask2 = createSubtask("Subtask 2", "Subtask Description 2", 1,
                                                                                                epic.getId(), Status.NEW,
                                                                                                Duration.ofHours(1),
                                                                                                LocalDateTime.now().plusHours(2));
        taskManager.addSubtask(subtask1); taskManager.addSubtask(subtask2);

        // Обновляем эпик с новыми данными
        Epic updatedEpic = createEpic("Updated Epic", "Updated Description", originalEpic.getId(), Status.NEW); taskManager.updateEpic(
                updatedEpic);

        Epic retrievedEpic = taskManager.getEpic(originalEpic.getId()); assertNotNull(retrievedEpic,
                                                                                      "Эпик должен быть найден в менеджере задач.");
        assertEquals("Updated Epic", retrievedEpic.getName(), "Название эпика должно быть обновлено."); assertEquals("Updated Description",
                                                                                                                     retrievedEpic.getDescription(),
                                                                                                                     "Описание эпика " +
                                                                                                                             "должно быть" +
                                                                                                                             " обновлено.");
    }

    @Test
    @DisplayName("8. Проверка удаления задачи")
    public void testDeleteTask() {
        int taskId = 1; // Или получить ID из вашего менеджера задач
        Task task = createTask("Task to Delete", "Description", taskId, Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        taskManager.addTask(task);

        taskManager.removeTask(task.getId()); // Удаляем задачу

        assertNull(taskManager.getTask(task.getId())); // Проверяем, что задача удалена
    }

    @Test
    @DisplayName("9. Проверка обновления задачи")
    void updateTask_shouldUpdateTaskDetails() {
        Task originalTask = createTask("Original Task", "Original Description", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        taskManager.addTask(originalTask);

        Task updatedTask = createTask("Updated Task", "Updated Description", originalTask.getId(), Status.IN_PROGRESS, Duration.ofHours(2),
                                      LocalDateTime.now().plusDays(1)); taskManager.updateTask(updatedTask);

        Task retrievedTask = taskManager.getTask(originalTask.getId()); assertNotNull(retrievedTask,
                                                                                      "Задача должна быть найдена в менеджере задач.");
        assertEquals("Updated Task", retrievedTask.getName(), "Имя задачи должно быть обновлено."); assertEquals("Updated Description",
                                                                                                                 retrievedTask.getDescription(),
                                                                                                                 "Описание задачи должно " +
                                                                                                                         "быть обновлено.");
        assertEquals(Status.IN_PROGRESS, retrievedTask.getStatus(), "Статус задачи должен быть обновлен.");
    }

    @Test
    @DisplayName("10. Проверка статусов эпиков")
    void testEpicStatusUpdates() {
        Epic epic = createEpic("Epic 1", "Description 1", 1, Status.NEW); taskManager.addEpic(epic);

        Subtask subtask1 = createSubtask("Subtask 1", "Description 1", 2, epic.getId(), Status.NEW, Duration.ofHours(1),
                                         LocalDateTime.now()); Subtask subtask2 = createSubtask("Subtask 2", "Description 2", 3,
                                                                                                epic.getId(), Status.DONE,
                                                                                                Duration.ofHours(1), LocalDateTime.now()
                                                                                                                                  .plusHours(
                                                                                                                                          1)); // Сдвигаем на 1 час вперед
        taskManager.addSubtask(subtask1); taskManager.addSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть IN_PROGRESS.");
    }

    @Test
    @DisplayName("Эпики 1. Все подзадачи со статусом NEW")
    void testAllSubtasksNew() {
        LocalDateTime now = LocalDateTime.now(); Subtask subtask1 = createSubtask("Subtask 1", "Description 1", 2, epic.getId(), Status.NEW,
                                                                                  Duration.ofHours(1), now);
        Subtask subtask2 = createSubtask("Subtask 2", "Description 2", 3, epic.getId(), Status.NEW, Duration.ofHours(1),
                                         now.plusHours(1)); // Смещение на 1 час
        taskManager.addSubtask(subtask1); taskManager.addSubtask(subtask2);

        assertEquals(2, epic.getSubtask().size(), "Эпик должен содержать 2 подзадачи."); assertTrue(
                epic.getSubtask().contains(subtask1.getId()), "Эпик должен содержать подзадачу subtask1."); assertTrue(
                epic.getSubtask().contains(subtask2.getId()), "Эпик должен содержать подзадачу subtask2.");
    }

    @Test
    @DisplayName("Эпики 2. Все подзадачи со статусом DONE")
    void testAllSubtasksDone() {
        LocalDateTime now = LocalDateTime.now(); Subtask subtask1 = createSubtask("Subtask 1", "Description 1", 2, epic.getId(),
                                                                                  Status.DONE, Duration.ofHours(1), now);
        Subtask subtask2 = createSubtask("Subtask 2", "Description 2", 3, epic.getId(), Status.DONE, Duration.ofHours(1),
                                         now.plusHours(1)); // Смещение на 1 час
        taskManager.addSubtask(subtask1); taskManager.addSubtask(subtask2);

        assertEquals(Status.DONE, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть DONE.");
    }

    @Test
    @DisplayName("Эпики 3. Подзадачи со статусами NEW и DONE")
    void testSubtasksNewAndDone() {
        LocalDateTime now = LocalDateTime.now(); Subtask subtask1 = createSubtask("Subtask 1", "Description 1", 2, epic.getId(), Status.NEW,
                                                                                  Duration.ofHours(1), now);
        Subtask subtask2 = createSubtask("Subtask 2", "Description 2", 3, epic.getId(), Status.DONE, Duration.ofHours(1),
                                         now.plusHours(1)); // Смещение на 1 час
        taskManager.addSubtask(subtask1); taskManager.addSubtask(subtask2);

        assertTrue(epic.getSubtask().contains(subtask1.getId()), "Эпик должен содержать подзадачу subtask1."); assertTrue(
                epic.getSubtask().contains(subtask2.getId()), "Эпик должен содержать подзадачу subtask2.");
    }

    @Test
    @DisplayName("Эпики 4. Подзадачи со статусом IN_PROGRESS")
    void testAllSubtasksInProgress() {
        LocalDateTime now = LocalDateTime.now(); Subtask subtask1 = createSubtask("Subtask 1", "Description 1", 2, epic.getId(),
                                                                                  Status.IN_PROGRESS, Duration.ofHours(1), now);
        Subtask subtask2 = createSubtask("Subtask 2", "Description 2", 3, epic.getId(), Status.IN_PROGRESS, Duration.ofHours(1),
                                         now.plusHours(1)); // Смещение на 1 час
        taskManager.addSubtask(subtask1); taskManager.addSubtask(subtask2);

        // Проверяем, что подзадачи добавлены в эпик
        assertTrue(epic.getSubtask().contains(subtask1.getId()), "Эпик должен содержать подзадачу subtask1."); assertTrue(
                epic.getSubtask().contains(subtask2.getId()), "Эпик должен содержать подзадачу subtask2.");

        // Проверяем статусы подзадач
        assertEquals(Status.IN_PROGRESS, taskManager.getSubtask(subtask1.getId()).getStatus(),
                     "Статус подзадачи subtask1 должен быть IN_PROGRESS."); assertEquals(Status.IN_PROGRESS,
                                                                                         taskManager.getSubtask(subtask2.getId())
                                                                                                    .getStatus(),
                                                                                         "Статус подзадачи subtask2 должен быть " +
                                                                                                 "IN_PROGRESS.");
    }

    @Test
    @DisplayName("Эпики 5. Эпик без подзадач должен иметь статус NEW")
    void testEpicWithoutSubtasks() {
        assertEquals(Status.NEW, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика без подзадач должен быть NEW.");
    }

    @Test
    @DisplayName("Эпики 6. Статус эпика должен обновляться при добавлении подзадач с разными статусами")
    void testEpicStatusUpdatesWithMixedSubtasks() {
        Epic epic = createEpic("Epic 1", "Description 1", 1, Status.NEW); taskManager.addEpic(epic);

        Subtask subtask1 = createSubtask("Subtask 1", "Description 1", 2, epic.getId(), Status.NEW, Duration.ofHours(1),
                                         LocalDateTime.now()); Subtask subtask2 = createSubtask("Subtask 2", "Description 2", 3,
                                                                                                epic.getId(), Status.DONE,
                                                                                                Duration.ofHours(1),
                                                                                                LocalDateTime.now().plusHours(1));

        taskManager.addSubtask(subtask1); taskManager.addSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть IN_PROGRESS.");
    }

    @Test
    @DisplayName("Пересечения 1. Проверка пересечения задач при добавлении новой задачи")
    void testTaskTimeOverlap() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = createTask("Task 1", "Description 1", 1, Status.NEW, Duration.ofHours(2), now);
        taskManager.addTask(task1);

        // Создаем вторую задачу с пересекающимся временем
        Task task2 = createTask("Task 2", "Description 2", 3, Status.NEW, Duration.ofHours(2), now.plusHours(1));

        // Проверяем, что вторая задача не добавляется из-за пересечения
        Exception exception = assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(task2),
                                           "Должно возникнуть исключение при добавлении пересекающейся задачи.");
        assertEquals("Задачи пересекаются по времени.", exception.getMessage(), "Сообщение об ошибке должно быть корректным.");
        assertNull(taskManager.getTask(task2.getId()), "Пересекающаяся задача не должна добавиться.");
    }

    @Test
    @DisplayName("Пересечения 2. Проверка пересечения подзадач в рамках одного эпика")
    void testSubtaskTimeOverlap() {
        LocalDateTime now = LocalDateTime.now();
        Epic epic = createEpic("Epic 1", "Description 1", 1, Status.NEW);
        taskManager.addEpic(epic);

        Subtask subtask1 = createSubtask("Subtask 1", "Description 1", 2, epic.getId(), Status.NEW, Duration.ofHours(2), now);
        taskManager.addSubtask(subtask1);

        // Создаем вторую подзадачу с пересекающимся временем
        Subtask subtask2 = createSubtask("Subtask 2", "Description 2", 4, epic.getId(), Status.NEW, Duration.ofHours(2), now.plusHours(1));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> taskManager.addSubtask(subtask2),
                                           "Должно возникнуть исключение при добавлении пересекающейся подзадачи.");
        assertEquals("Задачи пересекаются по времени.", exception.getMessage(), "Сообщение об ошибке должно быть корректным.");
        assertNull(taskManager.getSubtask(subtask2.getId()), "Пересекающаяся подзадача не должна добавиться.");
    }

    @Test
    @DisplayName("Пересечения 3. Проверка удаления задачи из истории и приоритетного списка")
    void testTaskRemovalFromHistoryAndPrioritizedTasks() {
        Task task = createTask("Task 1", "Description 1", 1, Status.NEW, Duration.ofHours(2), LocalDateTime.now());
        taskManager.addTask(task);
        taskManager.getTask(task.getId()); // Добавляем задачу в историю

        taskManager.removeTask(task.getId());

        assertNull(taskManager.getTask(task.getId()), "Задача должна быть удалена.");
        assertFalse(taskManager.getPrioritizedTasks().contains(task), "Задача не должна быть в приоритетном списке.");
        assertFalse(taskManager.getHistory().contains(task), "Задача не должна быть в истории.");
    }

    @Test
    @DisplayName("Пересечения 4. Проверка удаления эпика и связанных с ним подзадач")
    void testEpicAndSubtasksDeletion() {
        Epic epic = createEpic("Epic 1", "Description 1", 1, Status.NEW);
        taskManager.addEpic(epic);

        Subtask subtask1 = createSubtask("Subtask 1", "Description 1", 2, epic.getId(), Status.NEW, Duration.ofHours(2),
                                         LocalDateTime.now());
        Subtask subtask2 = createSubtask("Subtask 2", "Description 2", 3, epic.getId(), Status.NEW, Duration.ofHours(1),
                                         LocalDateTime.now().plusHours(3));
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.removeEpic(epic.getId());

        assertNull(taskManager.getEpic(epic.getId()), "Эпик должен быть удален.");
        assertNull(taskManager.getSubtask(subtask1.getId()), "Подзадача должна быть удалена.");
        assertNull(taskManager.getSubtask(subtask2.getId()), "Подзадача должна быть удалена.");
        assertFalse(taskManager.getPrioritizedTasks().contains(subtask1), "Подзадача не должна быть в приоритетном списке.");
        assertFalse(taskManager.getPrioritizedTasks().contains(subtask2), "Подзадача не должна быть в приоритетном списке.");
    }

    @Test
    @DisplayName("Пересечения 5. Проверка, что эпик не добавляется в приоритетный список")
    void testEpicNotInPrioritizedTasks() {
        Epic epic = createEpic("Epic 1", "Description 1", 1, Status.NEW);
        taskManager.addEpic(epic);

        assertFalse(taskManager.getPrioritizedTasks().contains(epic), "Эпик не должен быть в приоритетном списке.");
    }

    @Test
    @DisplayName("Пересечения 6. Проверка временного пересечения при обновлении задачи")
    void testUpdateTaskTimeOverlap() {
        LocalDateTime now = LocalDateTime.now();

        // Создаем две задачи без пересечений
        Task task1 = createTask("Task 1", "До обновления", 1, Status.NEW, Duration.ofHours(1), now);
        Task task2 = createTask("Task 2", "Description 2", 3, Status.NEW, Duration.ofHours(1), now.plusHours(2));

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        // Пытаемся обновить первую задачу так, чтобы она пересекалась со второй
        Task updatedTask1 = createTask("Task 1 Updated", "Description 1 Updated", task1.getId(), Status.IN_PROGRESS, Duration.ofHours(2),
                                       now.plusHours(1));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> taskManager.updateTask(updatedTask1));
        assertEquals("Задачи пересекаются по времени.", exception.getMessage());

        // Проверяем, что старая версия task1 осталась в менеджере и не была заменена
        Task currentTask1 = taskManager.getTask(task1.getId());
        assertEquals("Task 1", currentTask1.getName());
        assertEquals(now, currentTask1.getStartTime());
    }

    @Test
    @DisplayName("Пересечения 7. Проверка обновления подзадачи с пересечением времени")
    void testUpdateSubtaskWithTimeConflict() {
        LocalDateTime now = LocalDateTime.now();
        Epic epic = createEpic("Epic 1", "Description 1", 0, Status.NEW);
        taskManager.addEpic(epic);

        // Создаем первую подзадачу
        Subtask subtask1 = createSubtask("Subtask 1", "Description 1", 0, epic.getId(), Status.NEW, Duration.ofHours(2), now);
        taskManager.addSubtask(subtask1);

        // Создаем вторую подзадачу, которая будет пересекаться по времени с первой
        Subtask subtask2 = createSubtask("Subtask 2", "Description 2", 1, epic.getId(), Status.NEW, Duration.ofHours(2), now.plusHours(1));

        // Добавляем вторую подзадачу
        taskManager.addSubtask(subtask2);

        // Обновляем первую подзадачу, устанавливая время, пересекающееся со второй подзадачей
        subtask1.setStartTime(now.plusMinutes(30)); // Устанавливаем пересекающееся время
        subtask1.setDuration(Duration.ofHours(2)); // Устанавливаем продолжительность

        // Проверяем, что возникает исключение при обновлении
        Exception exception = assertThrows(IllegalArgumentException.class, () -> taskManager.updateSubtask(subtask1),
                                           "Должно возникнуть исключение при обновлении подзадачи с пересечением по времени.");
        assertEquals("Задачи пересекаются по времени.", exception.getMessage(), "Сообщение об ошибке должно быть корректным.");
    }
}