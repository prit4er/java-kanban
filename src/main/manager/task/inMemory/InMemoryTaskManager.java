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
    private boolean checkTimeConflict(Task newTask) {
        // Проверка на пересечение с задачами в приоритетном списке
        for (Task existingTask : prioritizedTasks) {
            if (existingTask.getId() != newTask.getId() && areTasksOverlapping(existingTask, newTask)) {
                return true; // Возвращаем true, если обнаружены пересечения
            }
        }
        return false;
    }

    // Вспомогательный метод, который непосредственно проверяет, пересекаются ли две конкретные задачи
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
        if (checkTimeConflict(task)) {
            throw new IllegalArgumentException("Задачи пересекаются по времени."); // Проверяем на пересечения
        }
        task.setId(generateId()); // Устанавливаем уникальный ID
        tasks.put(task.getId(), task); // Добавляем задачу в Map
        addToPrioritizedTasks(task); // Добавляем задачу в отсортированный список
        return task; // Если нужно возвращать добавленную задачу
    }

    @Override
    public Epic addEpic(Epic epic) {
        // Убираем проверку на совпадение ID с задачами и подзадачами
        epic.setId(generateId()); // Устанавливаем уникальный ID
        epics.put(epic.getId(), epic);
        addToPrioritizedTasks(epic); // Добавляем задачу в отсортированный список
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {

        if (checkTimeConflict(subtask)) {
            throw new IllegalArgumentException("Подзадачи пересекаются по времени."); // Проверяем на пересечения
        }
        Epic epic = epics.get(subtask.getEpicId());

        subtask.setId(generateId()); // Устанавливаем уникальный ID
        subtasks.put(subtask.getId(), subtask); // Добавление подзадачи
        epic.addSubtaskId(subtask.getId()); // Добавление ID подзадачи в эпик

        updateEpicStatus(epic.getId()); // Обновление статуса эпика
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
            return Collections.emptyList(); // Если эпик не найден, возвращаем пустой список
        }

        List<Subtask> result = new ArrayList<>();
        for (int subtaskId : epic.getSubtask()) {
            Subtask subtask = subtasks.get(subtaskId); // Получаем подзадачу по ID
            if (subtask != null) {
                result.add(subtask); // Добавляем подзадачу в результат
            }
        }
        return result; // Возвращаем список подзадач
    }

    public void updateTask(Task task) {
        // Проверка на пересечение со всеми другими задачами, кроме самой обновляемой задачи
        checkTimeConflict(task);

        Task oldTask = tasks.get(task.getId()); // Получение старой версии задачи для корректного обновления

        if (oldTask != null) {
            prioritizedTasks.remove(oldTask); // Удаление старой версии из приоритетного списка
            // Обновление полей задачи
            oldTask.setName(task.getName());
            oldTask.setDescription(task.getDescription());
            oldTask.setStatus(task.getStatus());
        }

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
            updateEpicPeriod(oldEpic); // Обновление полей эпика при его обновлении
        }
        epics.put(epic.getId(), epic); // Обновление эпика в Map
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask oldSubtask = subtasks.get(subtask.getId()); // Получение старой версии подзадачи
        if (oldSubtask == null) {
            throw new IllegalArgumentException("Подзадача с таким ID не существует."); // Исключение, если подзадача не найдена
        }

        Epic epic = epics.get(subtask.getEpicId()); // Получаем эпик по ID
        if (epic != null) {
            // Проверка на пересечение с остальными подзадачами эпика
            checkTimeConflict(subtask);
        }

        prioritizedTasks.remove(oldSubtask); // Удаление старой версии из приоритетного списка
        // Обновление полей подзадачи
        oldSubtask.setName(subtask.getName());
        oldSubtask.setDescription(subtask.getDescription());
        oldSubtask.setStatus(subtask.getStatus());

        subtasks.put(subtask.getId(), subtask); // Обновление подзадачи в Map
        prioritizedTasks.add(subtask); // Добавление новой версии в приоритетный список

        // Обновление статуса эпика, так как изменились данные подзадачи
        updateEpicStatus(epic.getId());
    }

    @Override
    public void removeTask(int id) {
        Task task = tasks.remove(id); // Удаление задачи по ID
        if (task != null) {
            removeFromPrioritizedTasks(task); // Удаление задачи из отсортированного списка
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
                updateEpicStatus(epic.getId()); // Обновление статуса эпика
            }
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
        subtasks.clear(); // Очистка подзадач
        epics.clear(); // Очистка эпиков
        prioritizedTasks.clear(); // Очистка списка приоритезированных задач
        historyManager.clear(); // Очистка истории задач
    }

    @Override
    public void clearEpics() {
        // Сначала удаляем все подзадачи
        List<Integer> subtaskIds = new ArrayList<>();
        for (Epic epic : epics.values()) {
            subtaskIds.addAll(epic.getSubtask()); // Собираем все ID подзадач
        }

        for (int id : subtaskIds) {
            removeSubtask(id); // Удаляем каждую подзадачу
        }

        // Затем очищаем все эпики
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
        if (subtasks == null || subtasks.isEmpty()) {
            epic.setStatus(Status.NEW); // Устанавливаем статус NEW, если подзадач нет
            return;
        }

        boolean hasNew = false;
        boolean hasDone = false;

        for (Subtask subtask : subtasks) {
            switch (subtask.getStatus()) {
                case NEW:
                    hasNew = true;
                    break;
                case DONE:
                    hasDone = true;
                    break;
            }
        }

        // Установите статус эпика в зависимости от наличия подзадач
        if (hasDone && hasNew) {
            epic.setStatus(Status.IN_PROGRESS);
        } else if (hasDone) {
            epic.setStatus(Status.DONE);
        } else if (hasNew) {
            epic.setStatus(Status.IN_PROGRESS);
        } else {
            epic.setStatus(Status.NEW);
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