import managers.Managers;
import managers.taskManager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task("Задача 1", "Описание задачи 1", 1);
        Task task2 = new Task("Задача 2", "Описание задачи 2", 2);
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1", 3);
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание подзадачи 1", 4, epic1.getId(), Status.NEW);
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание подзадачи 2", 5, epic1.getId(), Status.IN_PROGRESS);

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        manager.getTask(task1.getId());
        printHistory(manager);

        manager.getEpic(epic1.getId());
        printHistory(manager);

        manager.getSubtask(subtask1.getId());
        printHistory(manager);

        manager.getTask(task2.getId());
        printHistory(manager);

        manager.getSubtask(subtask2.getId());
        printHistory(manager);
    }

    private static void printHistory(TaskManager manager) {
        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
        System.out.println();
    }
}