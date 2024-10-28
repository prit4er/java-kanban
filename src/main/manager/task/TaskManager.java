package main.manager.task;

import main.model.Task;
import main.model.Epic;
import main.model.Subtask;

import java.util.List;

public interface TaskManager {

    Task addTask(Task task);

    Epic addEpic(Epic epic);

    Subtask addSubtask(Subtask subtask);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    void removeTask(int id);

    void removeEpic(int id);

    void removeSubtask(int id);

    List<Subtask> getSubtasksByEpic(int epicId);

    void clearTasks();

    void clearEpics();

    void clearSubtasks();

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}