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

    // Метод для проверки пересечений задач по времени с использованием отсортированного списка
    private void checkTimeConflict(Task newTask) {
        for (Task existingTask : prioritizedTasks) {
            if (areTasksOverlapping(existingTask, newTask)) {
                throw new IllegalArgumentException("Задачи пересекаются по времени.");
            }
        }
    }

    private boolean areTasksOverlapping(Task existingTask, Task newTask) {
        // Если это одна и та же задача, пересечения быть не может
        if (Objects.equals(existingTask.getId(), newTask.getId())) {
            return false;
        }

        // Если время старта не задано, пересечения не будет
        if (existingTask.getStartTime() == null || newTask.getStartTime() == null) {
            return false;
        }

        // Используем метод из класса Task для получения времени окончания
        LocalDateTime existingEndTime = existingTask.getEndTime();
        LocalDateTime newEndTime = newTask.getEndTime();

        // Если время окончания не задано, считаем, что задача не длится
        if (existingEndTime == null || newEndTime == null) {
            return false;
        }

        return existingTask.getStartTime().isBefore(newEndTime) &&
                newTask.getStartTime().isBefore(existingEndTime);
    }

    public Task addTask(Task task) {
        checkTimeConflict(task);
        task.setId(generateId()); // Устанавливаем уникальный ID
        tasks.put(task.getId(), task); // Добавляем задачу в Map
        addToPrioritizedTasks(task); // Добавляем задачу в отсортированный список
        return task; // Если нужно возвращать добавленную задачу
    }

    @Override
    public Epic addEpic(Epic epic) {
        epic.setId(generateId()); // Устанавливаем уникальный ID
        epics.put(epic.getId(), epic);

        // Обновляем статус и временные поля после добавления эпика
        updateEpicStatus(epic);
        updateEpicPeriod(epic);

        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        checkTimeConflict(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с таким ID не существует."); // Исключение, если эпик не найден
        }

        subtask.setId(generateId()); // Устанавливаем уникальный ID
        subtasks.put(subtask.getId(), subtask); // Добавление подзадачи
        epic.addSubtaskId(subtask.getId()); // Добавление ID подзадачи в эпик

        updateEpicStatus(epic); // Обновление статуса эпика
        updateEpicPeriod(epic); // Обновление временных полей эпика
        prioritizedTasks.add(subtask); // Добавление подзадачи в приоритетный список

        return subtask;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id); // Получение задачи по ID
        addToHistory(task); // Добавление задачи в историю, если она найдена
        return task; // Возврат задачи или пустого значения
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id); // Получение эпика по ID
        addToHistory(epic); // Добавление эпика в историю, если он найден
        return epic; // Возврат эпика или пустого значения
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id); // Получение подзадачи по ID
        addToHistory(subtask); // Добавление подзадачи в историю, если она найдена
        return subtask; // Возврат подзадачи или пустого значения
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId); // Получение эпика по ID
        if (epic == null) {
            throw new NoSuchElementException("Эпик с ID " + epicId + " не найден."); // Кидаем исключение, если эпик не найден
        }

        List<Subtask> result = new ArrayList<>();
        for (int subtaskId : epic.getSubtask()) { // Получаем ID подзадач из эпика
            Subtask subtask = subtasks.get(subtaskId); // Получаем подзадачу по ID
            if (subtask != null) {
                result.add(subtask); // Добавляем подзадачу в результат
            }
        }
        return result; // Возвращаем список подзадач
    }

    @Override
    public void updateTask(Task task) {
        Task oldTask = tasks.get(task.getId()); // Получение старой версии задачи для корректного обновления
        if (oldTask == null) {
            throw new NoSuchElementException("Задача с ID " + task.getId() + " не найдена.");
        }

        prioritizedTasks.remove(oldTask); // Удаляем старую версию из приоритетного списка перед проверкой
        try {
            checkTimeConflict(task); // Проверка на пересечение с другими задачами
        } catch (IllegalArgumentException e) {
            prioritizedTasks.add(oldTask); // Восстанавливаем старую версию задачи в списке при ошибке
            throw e; // Пробрасываем исключение дальше
        }

        // Обновление полей задачи
        oldTask.setName(task.getName());
        oldTask.setDescription(task.getDescription());
        oldTask.setStatus(task.getStatus());
        tasks.put(task.getId(), task); // Обновление задачи в Map
        prioritizedTasks.add(task); // Добавление новой версии в приоритетный список
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic oldEpic = epics.get(epic.getId()); // Получение старой версии эпика
        if (oldEpic != null) {
            // Обновление полей эпика
            oldEpic.setName(epic.getName());
            oldEpic.setDescription(epic.getDescription());
            updateEpicStatus(oldEpic); // Обновляем статус эпика на основе подзадач
        } else {
            throw new NoSuchElementException("Эпик с ID " + epic.getId() + " не найден.");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        // Получение старой версии подзадачи
        Subtask oldSubtask = subtasks.get(subtask.getId());
        if (oldSubtask == null) {
            throw new IllegalArgumentException("Подзадача с таким ID не существует."); // Исключение, если подзадача не найдена
        }

        // Проверка совпадения ID эпиков
        if (oldSubtask.getEpicId() != subtask.getEpicId()) {
            throw new IllegalArgumentException("ID эпиков не совпадают."); // Исключение, если ID эпиков не совпадают
        }

        // Проверка на пересечение со всеми подзадачами, кроме самой обновляемой задачи
        checkTimeConflict(subtask);

        // Получаем эпик по ID
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с таким ID не существует."); // Исключение, если эпик не найден
        }

        // Удаление старой версии из приоритетного списка
        prioritizedTasks.remove(oldSubtask);

        // Обновление полей подзадачи
        oldSubtask.setName(subtask.getName());
        oldSubtask.setDescription(subtask.getDescription());
        oldSubtask.setStatus(subtask.getStatus());
        oldSubtask.setStartTime(subtask.getStartTime());
        oldSubtask.setDuration(subtask.getDuration());

        // Обновление подзадачи в Map
        subtasks.put(subtask.getId(), oldSubtask);
        prioritizedTasks.add(oldSubtask); // Добавление обновленной версии в приоритетный список

        // Обновление статуса эпика, так как изменились данные подзадачи
        updateEpicStatus(epic);
    }

    @Override
    public void removeTask(int id) {
        Task task = tasks.remove(id); // Удаление задачи по ID
        if (task != null) {
            removeFromPrioritizedTasks(task); // Удаление задачи из отсортированного списка
            historyManager.remove(task.getId()); // Удаление задачи из истории
        } else {
            throw new IllegalArgumentException("Задача с таким ID не найдена.");
        }
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id); // Удаление эпика по ID
        if (epic != null) {
            for (int subtaskId : epic.getSubtask()) {
                removeSubtask(subtaskId); // Удаление всех подзадач, связанных с эпиком
            }
        } else {
            throw new IllegalArgumentException("Эпик с таким ID не найден.");
        }
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id); // Удаление подзадачи по ID
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId()); // Получаем эпик
            if (epic != null) {
                epic.removeSubtaskId(id); // Удаление ID подзадачи из эпика
                updateEpicPeriod(epic); // Обновление полей эпика (время, продолжительность)
                updateEpicStatus(epic); // Обновление статуса эпика
            }

            removeFromPrioritizedTasks(subtask); // Удаление подзадачи из приоритетного списка
            historyManager.remove(subtask.getId()); // Удаление задачи из истории
        } else {
            throw new IllegalArgumentException("Подзадача с таким ID не найдена.");
        }
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
    public void clearTasks() {
        tasks.clear(); // Очистка задач
    }

    @Override
    public void clearEpics() {
        for (Epic epic : epics.values()) {
            List<Integer> relatedSubtasks = epic.getSubtask(); // Получаем связанные подзадачи
            for (Integer subtaskId : relatedSubtasks) {
                Subtask subtask = subtasks.remove(subtaskId); // Удаляем подзадачу из Map
                if (subtask != null) {
                    prioritizedTasks.remove(subtask); // Удаляем подзадачу из приоритетного списка
                    historyManager.remove(subtaskId); // Удаляем подзадачу из истории
                }
            }
            // Удаляем эпик из истории и приоритетного списка
            historyManager.remove(epic.getId());
            prioritizedTasks.remove(epic); // Удаление эпика из приоритетного списка
        }
        // Очищаем мапу эпиков
        epics.clear(); // Очистка эпиков
    }

    @Override
    public void clearSubtasks() {
        // Удаляем все подзадачи из списка приоритетных задач и истории
        for (Subtask subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
            historyManager.remove(subtask.getId());
        }

        // Очистка всех подзадач из эпиков
        for (Epic epic : epics.values()) {
            epic.getSubtask().clear();
            updateEpicStatus(epic); // Обновление статуса эпика
            updateEpicPeriod(epic); // Обновление временных полей эпика
        }

        // Очищаем подзадачи
        subtasks.clear();
    }

    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task); // Добавление задачи в отсортированный список
        }
    }

    private void removeFromPrioritizedTasks(Task task) {
        prioritizedTasks.remove(task); // Удаление задачи из отсортированного списка
    }


    protected void updateEpicStatus(Epic epic) {
        if (epic == null) {
            return; // Эпик не найден
        }

        List<Subtask> subtasks = getSubtasksByEpic(epic.getId());
        if (subtasks == null || subtasks.isEmpty()) {
            epic.setStatus(Status.NEW); // Устанавливаем статус NEW, если подзадач нет
            return;
        }

        // Проверка статусов подзадач
        boolean allNew = subtasks.stream().allMatch(subtask -> subtask.getStatus() == Status.NEW);
        boolean allDone = subtasks.stream().allMatch(subtask -> subtask.getStatus() == Status.DONE);

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private void updateEpicPeriod(Epic epic) {
        List<Subtask> subtasks = getSubtasksByEpic(epic.getId());

        if (subtasks.isEmpty()) {
            // Если нет подзадач, обнуляем поля эпика
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(Duration.ZERO);
            return;
        }

        // Получаем время начала и окончания
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

        // Суммируем продолжительность всех подзадач
        Duration totalDuration = subtasks.stream()
                                         .map(Subtask::getDuration)
                                         .reduce(Duration.ZERO, Duration::plus);

        // Устанавливаем новые значения в эпик
        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
        epic.setDuration(totalDuration);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory(); // Получение истории
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks); // Возвращаем копию списка приоритезированных задач
    }

    private void addToHistory(Task task) {
        if (task != null) {
            historyManager.add(task); // Добавление задачи (или эпика, или подзадачи) в историю
        }
    }
}