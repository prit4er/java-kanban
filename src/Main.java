import main.manager.Managers;
import main.manager.task.TaskManager;
import main.model.Epic;
import main.model.Status;
import main.model.Subtask;
import main.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Задаем фиксированное время для начала задач
        LocalDateTime now = LocalDateTime.now();

        // 1. Создаём две задачи с разными временами
        Task task1 = new Task("Задача 1", "Описание задачи 1", 1, Status.NEW, Duration.ofHours(1), now);
        Task task2 = new Task("Задача 2", "Описание задачи 2", 2, Status.NEW, Duration.ofHours(1),
                              now.plusHours(1)); // Смещаем на 1 час

        // 2. Создаём эпик с двумя подзадачами с разными временами
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1", 3, Status.NEW);
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание подзадачи 1", 4, epic1.getId(), Status.NEW, Duration.ofHours(1),
                                       now.plusHours(2)); // Смещаем на 2 часа
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание подзадачи 2", 5, epic1.getId(), Status.IN_PROGRESS,
                                       Duration.ofHours(1), now.plusHours(3)); // Смещаем на 3 часа
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2", 6, Status.NEW);

        // 3. Добавляем задачи и эпики в менеджер
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.addEpic(epic2);

        // 4. Запрашиваем задачи в разном порядке и выводим историю
        System.out.println("Запрашиваем task1");
        manager.getTask(task1.getId());
        printHistory(manager);

        System.out.println("Запрашиваем epic1");
        manager.getEpic(epic1.getId());
        printHistory(manager);

        System.out.println("Запрашиваем subtask1");
        manager.getSubtask(subtask1.getId());
        printHistory(manager);

        System.out.println("Запрашиваем task2");
        manager.getTask(task2.getId());
        printHistory(manager);

        System.out.println("Запрашиваем subtask2");
        manager.getSubtask(subtask2.getId());
        printHistory(manager);

        // 5. Удаляем задачу task1 и проверяем, что она исчезла из истории
        System.out.println("Удаляем task1");
        manager.deleteTaskById(task1.getId());
        printHistory(manager);

        // 6. Удаляем эпик с подзадачами и проверяем, что он и подзадачи исчезли из истории
        System.out.println("Удаляем epic1");
        manager.deleteEpicById(epic1.getId());
        printHistory(manager);
    }

    // Метод для вывода истории задач
    private static void printHistory(TaskManager manager) {
        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
        System.out.println();
    }
}