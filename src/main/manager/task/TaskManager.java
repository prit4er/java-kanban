package main.manager.task;

import main.model.Task;
import main.model.Epic;
import main.model.Subtask;

import java.util.List;
import java.util.Optional;

public interface TaskManager {

    Optional<Task> getTaskById(int id);

    Optional<Subtask> getSubtaskById(int id);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    void addTask(Task task);          // Возвращает добавленный Task

    Epic addEpic(Epic epic);          // Возвращает добавленный Epic

    Subtask addSubtask(Subtask subtask); // Возвращает добавленный Subtask

    List<Subtask> getSubtasksByEpic(int epicId);

    void updateTask(Task task);       // Возвращает обновленный Task

    void updateEpic(Epic epic);       // Возвращает обновленный Epic

    void updateSubtask(Subtask subtask); // Возвращает обновленный Subtask

    void deleteTaskById(int id);

    void deleteEpicById(int id);

    void deleteSubtaskById(int subtaskId);

    Task getTask(int id);             // Получение Task по ID

    Epic getEpic(int id);             // Получение Epic по ID

    Subtask getSubtask(int id);       // Получение Subtask по ID

    void removeTask(int id);

    void removeEpic(int id);

    void removeSubtask(int id);

    void clearTasks();

    void clearEpics();

    void clearSubtasks();

    List<Task> getHistory();          // Получение истории задач
}