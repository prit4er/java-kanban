package managers.taskManager;

import managers.Managers;
import managers.historyManager.HistoryManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private int currentId = 0;  // Счётчик для генерации уникальных идентификаторов
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;

    // Конструктор, принимающий HistoryManager
    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    // Конструктор по умолчанию, использующий стандартный HistoryManager
    public InMemoryTaskManager() {
        this(Managers.getDefaultHistory());
    }

    // Метод для генерации уникальных идентификаторов
    private int generateId() {
        return ++currentId;
    }

    // Методы для работы с задачами
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
        task.setId(generateId());
        tasks.put(task.getId(), task);
        return task;  // Возвращаем добавленный task
    }

    @Override
    public Epic addEpic(Epic epic) {
        if (tasks.containsKey(epic.getId()) || subtasks.containsKey(epic.getId())) {
            throw new IllegalArgumentException("ID эпика не может совпадать с ID существующих задач или подзадач.");
        }
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic;  // Возвращаем добавленный epic
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        if (epics.get(subtask.getEpicId()) == null) {
            throw new IllegalArgumentException("Эпик с таким ID не существует.");
        }
        if (subtask.getId() <= 0) { // Ensure ID is valid
            subtask.setId(generateId());
        }
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
        return subtask;  // Возвращаем добавленный subtask
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task); // Добавляем в историю
        }
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic); // Добавляем в историю
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask); // Добавляем в историю
        }
        return subtask;
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
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
    public Task updateTask(Task updatedTask) {
        Task existingTask = tasks.get(updatedTask.getId());
        if (existingTask != null) {
            // Обновляем только те поля, которые действительно изменились
            if (updatedTask.getName() != null) {
                existingTask.setName(updatedTask.getName());
            }
            if (updatedTask.getDescription() != null) {
                existingTask.setDescription(updatedTask.getDescription());
            }
            if (updatedTask.getStatus() != null) {
                existingTask.setStatus(updatedTask.getStatus());
            }
        }
        return existingTask;  // Возвращаем обновленный task
    }

    @Override
    public Epic updateEpic(Epic updatedEpic) {
        Epic existingEpic = epics.get(updatedEpic.getId());
        if (existingEpic != null) {
            // Обновляем только те поля, которые действительно изменились
            if (updatedEpic.getName() != null) {
                existingEpic.setName(updatedEpic.getName());
            }
            if (updatedEpic.getDescription() != null) {
                existingEpic.setDescription(updatedEpic.getDescription());
            }
            // Статус эпика может зависеть от подзадач, поэтому его не обновляем напрямую
            updateEpicStatus(existingEpic);
        }
        return existingEpic;  // Возвращаем обновленный epic
    }

    @Override
    public Subtask updateSubtask(Subtask updatedSubtask) {
        Subtask existingSubtask = subtasks.get(updatedSubtask.getId());
        if (existingSubtask != null) {
            existingSubtask.setName(updatedSubtask.getName());
            existingSubtask.setDescription(updatedSubtask.getDescription());
            existingSubtask.setStatus(updatedSubtask.getStatus());
            existingSubtask.setEpicId(updatedSubtask.getEpicId());
            // Обновляем статус эпика
            Epic epic = epics.get(existingSubtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
            }
        }
        return existingSubtask;  // Возвращаем обновленный subtask
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
        tasks.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    @Override
    public void deleteSubtaskById(int subtaskId) {
        Subtask subtask = subtasks.remove(subtaskId);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(subtaskId);
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}