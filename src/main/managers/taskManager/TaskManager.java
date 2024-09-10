package main.managers.taskManager;

import main.model.Task;
import main.model.Epic;
import main.model.Subtask;

import java.util.List;

public interface TaskManager {

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    Task addTask(Task task);          // Возвращает добавленный Task

    Epic addEpic(Epic epic);          // Возвращает добавленный Epic

    Subtask addSubtask(Subtask subtask); // Возвращает добавленный Subtask

    List<Subtask> getSubtasksByEpic(int epicId);

    Task updateTask(Task task);       // Возвращает обновленный Task

    Epic updateEpic(Epic epic);       // Возвращает обновленный Epic

    Subtask updateSubtask(Subtask subtask); // Возвращает обновленный Subtask

    void deleteTaskById(int id);

    void deleteEpicById(int id);

    void deleteSubtaskById(int subtaskId);

    Task getTask(int id);             // Получение Task по ID

    Epic getEpic(int id);             // Получение Epic по ID

    Subtask getSubtask(int id);       // Получение Subtask по ID

    List<Task> getHistory();          // Получение истории задач
}