package main.manager.task;

import main.model.Task;
import main.model.Epic;
import main.model.Subtask;

import java.util.List;

public interface TaskManager {

    Task addTask(Task task);          // Возвращает добавленный Task

    Epic addEpic(Epic epic);          // Возвращает добавленный Epic

    Subtask addSubtask(Subtask subtask); // Возвращает добавленный Subtask

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    void updateTask(Task task);       // Возвращает обновленный Task

    void updateEpic(Epic epic);       // Возвращает обновленный Epic

    void updateSubtask(Subtask subtask); // Возвращает обновленный Subtask

    Task getTask(int id);             // Получение Task по ID

    Epic getEpic(int id);             // Получение Epic по ID

    Subtask getSubtask(int id);       // Получение Subtask по ID

    void removeTask(int id);

    void removeEpic(int id);

    void removeSubtask(int id);

    List<Subtask> getSubtasksByEpic(int epicId);

    void clearTasks();

    void clearEpics();

    void clearSubtasks();

    List<Task> getHistory();          // Получение истории задач

    List<Task> getPrioritizedTasks(); // Возвращает список приоритезированных задач
}