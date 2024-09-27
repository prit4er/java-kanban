package main.managers.task;

import main.managers.Managers;
import main.managers.history.HistoryManager;
import main.model.Epic;
import main.model.Status;
import main.model.Subtask;
import main.model.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private int currentId = 0;  // Счётчик для генерации уникальных идентификаторов
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public InMemoryTaskManager() {
        this(Managers.getDefaultHistory());
    }

    // Генерация уникального идентификатора
    private int generateId() {
        return ++currentId;
    }

    // Общий метод добавления задач, эпиков и подзадач
    private <T extends Task> T addTaskToMap(Map<Integer, T> map, T task) {
        task.setId(generateId());
        map.put(task.getId(), task);
        return task;
    }

    // Общий метод обновления задач, эпиков и подзадач
    private <T extends Task> T updateTaskInMap(Map<Integer, T> map, T updatedTask) {
        T existingTask = map.get(updatedTask.getId());
        if (existingTask != null) {
            existingTask.updateFrom(updatedTask);
        }
        return existingTask;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Task addTask(Task task) {
        return addTaskToMap(tasks, task);
    }

    @Override
    public Epic addEpic(Epic epic) {
        if (tasks.containsKey(epic.getId()) || subtasks.containsKey(epic.getId())) {
            throw new IllegalArgumentException("ID эпика не может совпадать с ID существующих задач или подзадач.");
        }
        return addTaskToMap(epics, epic);
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с таким ID не существует.");
        }
        Subtask addedSubtask = addTaskToMap(subtasks, subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
        return addedSubtask;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        addToHistory(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        addToHistory(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        addToHistory(subtask);
        return subtask;
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }

        List<Subtask> result = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    @Override
    public Task updateTask(Task task) {
        return updateTaskInMap(tasks, task);
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updatedEpic = updateTaskInMap(epics, epic);
        updateEpicStatus(updatedEpic);
        return updatedEpic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updatedSubtask = updateTaskInMap(subtasks, subtask);
        Epic epic = epics.get(updatedSubtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
        return updatedSubtask;
    }

    private void updateEpicStatus(Epic epic) {
        List<Subtask> subtasks = getSubtasksByEpic(epic.getId());
        if (subtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        deleteFromMapById(tasks, id);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void deleteFromMapById(Map<Integer, ? extends Task> map, int id) {
        map.remove(id);
        historyManager.remove(id);
    }

    private void addToHistory(Task task) {
        if (task != null) {
            historyManager.add(task);
        }
    }
}