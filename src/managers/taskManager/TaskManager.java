package managers.taskManager;

import model.Task;
import model.Epic;
import model.Subtask;

import java.util.List;

public interface TaskManager {
    List<Task> getAllTasks();
    List<Epic> getAllEpics();
    List<Subtask> getAllSubtasks();

    Task addTask(Task task);      // Измененный метод, возвращает добавленный Task
    Epic addEpic(Epic epic);      // Измененный метод, возвращает добавленный Epic
    Subtask addSubtask(Subtask subtask); // Измененный метод, возвращает добавленный Subtask

    List<Subtask> getSubtasksByEpic(int epicId);

    Task updateTask(Task task);     // Измененный метод, возвращает обновленный Task
    Subtask updateSubtask(Subtask subtask); // Измененный метод, возвращает обновленный Subtask

    void deleteTaskById(int id);
    void deleteEpicById(int id);
    void deleteSubtaskById(int subtaskId);

    Task getTask(int id);      // Новый метод
    Subtask getSubtask(int id); // Новый метод
    Epic getEpic(int id);      // Новый метод
    List<Task> getHistory();   // Новый метод для получения истории
}