package main.manager.task.inMemory;

import main.manager.Managers;
import main.manager.history.HistoryManager;
import main.manager.task.TaskManager;
import main.model.Epic;
import main.model.Status;
import main.model.Subtask;
import main.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private int currentId = 0;  // Счётчик для генерации уникальных идентификаторов
    protected final Map<Integer, Task> tasks = new HashMap<>(); // Хранение всех задач
    protected final Map<Integer, Epic> epics = new HashMap<>(); // Хранение всех эпиков
    protected final Map<Integer, Subtask> subtasks = new HashMap<>(); // Хранение всех подзадач
    private final HistoryManager historyManager; // Менеджер истории

    // TreeSet для хранения задач, отсортированных по времени начала
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));

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

    // Общий метод добавления задач, эпиков и подзадач в Map
    private <T extends Task> T addTaskToMap(Map<Integer, T> map, T task) {
        task.setId(generateId()); // Устанавливаем уникальный ID
        map.put(task.getId(), task); // Добавляем задачу в Map
        addToPrioritizedTasks(task); // Добавляем задачу в отсортированный список
        return task;
    }

    // Общий метод обновления задач, эпиков и подзадач в Map
    private <T extends Task> T updateTaskInMap(Map<Integer, T> map, T task) {
        removeFromPrioritizedTasks(task); // Удаляем старую версию задачи из отсортированного списка
        map.put(task.getId(), task); // Обновляем задачу в Map
        addToPrioritizedTasks(task); // Добавляем обновленную версию задачи
        return task;
    }

    // Проверка, пересекаются ли два временных интервала
    private boolean areTimeIntervalsOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1); // Возвращает true, если интервалы пересекаются
    }

    @Override
    public Optional<Task> getTaskById(int id) {
        return Optional.ofNullable(tasks.get(id)); // Возвращает задачу по ID или пустое значение
    }

    @Override
    public Optional<Subtask> getSubtaskById(int id) {
        return Optional.ofNullable(subtasks.get(id)); // Возвращает подзадачу по ID или пустое значение
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values()); // Возвращает список всех задач
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values()); // Возвращает список всех эпиков
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values()); // Возвращает список всех подзадач
    }

    @Override
    public void addTask(Task task) {
        validateTaskTime(task); // Валидация временных рамок задачи
        for (Task existingTask : tasks.values()) {
            if (areTimeIntervalsOverlapping(existingTask.getStartTime(), existingTask.getEndTime(),
                                            task.getStartTime(), task.getEndTime())) {
                throw new IllegalArgumentException("Задачи пересекаются по времени.");
            }
        }
        addTaskToMap(tasks, task);
    }

    private void validateTaskTime(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            throw new IllegalArgumentException("Временные рамки задачи не могут быть пустыми.");
        }
    }

    @Override
    public Epic addEpic(Epic epic) {
        if (tasks.containsKey(epic.getId()) || subtasks.containsKey(epic.getId())) {
            throw new IllegalArgumentException("ID эпика не может совпадать с ID существующих задач или подзадач.");
        }
        return addTaskToMap(epics, epic); // Добавление эпика
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId()); // Получаем эпик по ID
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с таким ID не существует."); // Исключение, если эпик не найден
        }

        // Проверка на пересечение для новой подзадачи
        for (Subtask existingSubtask : getSubtasksByEpic(epic.getId())) {
            if (areTimeIntervalsOverlapping(existingSubtask.getStartTime(), existingSubtask.getEndTime(), subtask.getStartTime(),
                                            subtask.getEndTime())) {
                throw new IllegalArgumentException("Подзадачи пересекаются по времени."); // Исключение при пересечении
            }
        }

        Subtask addedSubtask = addTaskToMap(subtasks, subtask); // Добавление подзадачи
        epic.addSubtaskId(subtask.getId()); // Добавление ID подзадачи в эпик
        updateEpicStatus(epic.getId()); // Передаём ID эпика для обновления статуса
        return addedSubtask;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id); // Получение задачи по ID
        addToHistory(task); // Добавление задачи в историю
        return task; // Возврат задачи
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id); // Получение эпика по ID
        addToHistory(epic); // Добавление эпика в историю
        return epic; // Возврат эпика
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id); // Получение подзадачи по ID
        addToHistory(subtask); // Добавление подзадачи в историю
        return subtask; // Возврат подзадачи
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId); // Получение эпика по ID
        if (epic == null) {
            return Collections.emptyList(); // Если эпик не найден, возвращаем пустой список
        }

        List<Subtask> result = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId); // Получаем подзадачу по ID
            if (subtask != null) {
                result.add(subtask); // Добавляем подзадачу в результат
            }
        }
        return result; // Возвращаем список подзадач
    }

    @Override
    public void updateTask(Task task) {
        validateTaskTime(task); // Валидация временных рамок задачи
        for (Task existingTask : tasks.values()) {
            // Пропустить проверку, если это та же задача (по ID)
            if (existingTask.getId() != task.getId() &&
                    areTimeIntervalsOverlapping(existingTask.getStartTime(), existingTask.getEndTime(), task.getStartTime(),
                                                task.getEndTime())) {
                throw new IllegalArgumentException("Задачи пересекаются по времени при обновлении."); // Исключение при пересечении
            }
        }
        updateTaskInMap(tasks, task);
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic updatedEpic = updateTaskInMap(epics, epic); // Обновление эпика
        updateEpicFields(updatedEpic); // Обновление полей эпика при его обновлении
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId()); // Получаем эпик по ID
        if (epic != null) {
            for (Subtask existingSubtask : getSubtasksByEpic(epic.getId())) {
                // Пропустить проверку, если это та же подзадача (по ID)
                if (existingSubtask.getId() != subtask.getId() &&
                        areTimeIntervalsOverlapping(existingSubtask.getStartTime(), existingSubtask.getEndTime(), subtask.getStartTime(),
                                                    subtask.getEndTime())) {
                    throw new IllegalArgumentException("Подзадачи пересекаются по времени при обновлении."); // Исключение при пересечении
                }
            }
        }
        updateTaskInMap(subtasks, subtask);
    }

    @Override
    public void deleteTaskById(int id) {

    }

    @Override
    public void deleteEpicById(int id) {

    }

    @Override
    public void deleteSubtaskById(int subtaskId) {

    }

    @Override
    public void removeTask(int id) {
        Task task = tasks.remove(id); // Удаление задачи по ID
        removeFromPrioritizedTasks(task); // Удаление задачи из отсортированного списка
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id); // Удаление эпика по ID
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId); // Удаление подзадач из Map
            }
        }
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id); // Удаление подзадачи по ID
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId()); // Получаем эпик
            if (epic != null) {
                epic.removeSubtaskId(id); // Удаление ID подзадачи из эпика
                updateEpicStatus(epic.getId()); // Обновление статуса эпика
            }
        }
    }

    @Override
    public void clearTasks() {
        tasks.clear(); // Очистка задач
    }

    @Override
    public void clearEpics() {
        epics.clear(); // Очистка эпиков
    }

    @Override
    public void clearSubtasks() {
        subtasks.clear(); // Очистка подзадач
    }

    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task); // Добавление задачи в отсортированный список
        }
    }

    private void removeFromPrioritizedTasks(Task task) {
        prioritizedTasks.remove(task); // Удаление задачи из отсортированного списка
    }


    protected void updateEpicStatus(int epicId) {
        Epic epic = getEpic(epicId);
        if (epic == null) {
            return; // Эпик не найден
        }

        List<Subtask> subtasks = getSubtasksByEpic(epicId);
        boolean hasNew = false;
        boolean hasDone = false;
        boolean hasInProgress = false;

        for (Subtask subtask : subtasks) {
            switch (subtask.getStatus()) {
                case NEW:
                    hasNew = true;
                    break;
                case DONE:
                    hasDone = true;
                    break;
                case IN_PROGRESS:
                    hasInProgress = true;
                    break;
            }
        }

        // Установите статус эпика в зависимости от наличия подзадач
        if (hasDone && hasNew) {
            epic.setStatus(Status.IN_PROGRESS);
        } else if (hasDone) {
            epic.setStatus(Status.DONE);
        } else if (hasNew) {
            epic.setStatus(Status.IN_PROGRESS); // Изменено, чтобы учесть статус IN_PROGRESS
        } else if (subtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.NEW); // Если ничего другого не подходит
        }
    }

    private void updateEpicFields(Epic epic) {
        List<Subtask> subtasks = getSubtasksByEpic(epic.getId());
        LocalDateTime startTime = subtasks.stream()
                                          .filter(subtask -> subtask.getStartTime() != null)
                                          .map(Subtask::getStartTime)
                                          .min(LocalDateTime::compareTo)
                                          .orElse(null);

        LocalDateTime endTime = subtasks.stream()
                                        .filter(subtask -> subtask.getEndTime() != null)
                                        .map(Subtask::getEndTime)
                                        .max(LocalDateTime::compareTo)
                                        .orElse(null);

        Duration duration = subtasks.stream()
                                    .filter(subtask -> subtask.getDuration() != null)
                                    .map(Subtask::getDuration)
                                    .reduce(Duration.ZERO, Duration::plus);

        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
        epic.setDuration(duration);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory(); // Получение истории
    }

    private void addToHistory(Task task) {
        if (task != null) {
            historyManager.add(task); // Добавление задачи в историю
        }
    }

    private void addToHistory(Epic epic) {
        if (epic != null) {
            historyManager.add(epic); // Добавление эпика в историю
        }
    }

    private void addToHistory(Subtask subtask) {
        if (subtask != null) {
            historyManager.add(subtask); // Добавление подзадачи в историю
        }
    }
}