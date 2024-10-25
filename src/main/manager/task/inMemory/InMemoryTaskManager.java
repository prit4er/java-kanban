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
    protected <T extends Task> T updateTaskInMap(Map<Integer, T> taskMap, T task) {
        T existingTask = taskMap.get(task.getId());
        if (existingTask != null) {
            // Проверка на пересечение с другими задачами
            for (T otherTask : taskMap.values()) {
                if (otherTask.getId() != task.getId() &&
                        areTimeIntervalsOverlapping(otherTask, task)) {
                    throw new IllegalArgumentException("Задачи пересекаются по времени при обновлении.");
                }
            }

            // Обновление полей задачи
            existingTask.setName(task.getName());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());

            // Если обновляемый объект - эпик, то обновляем его поля
            if (existingTask instanceof Epic) {
                updateEpicPeriod((Epic) existingTask); // Обновление полей эпика
            }
        }
        return existingTask;
    }

    // Метод для проверки пересечений задач по времени
    private boolean hasTimeConflict(Task newTask) {
        for (Task existingTask : tasks.values()) {
            if (taskTimesOverlap(existingTask, newTask)) {
                return true;
            }
        }
        for (Subtask existingSubtask : subtasks.values()) {
            if (taskTimesOverlap(existingSubtask, newTask)) {
                return true;
            }
        }
        return false;
    }

    private void checkForTaskAndSubtaskConflicts(Task newTask) {
        // Проверка на пересечение со всеми задачами
        for (Task existingTask : tasks.values()) {
            if (existingTask.getId() != newTask.getId() &&
                    areTimeIntervalsOverlapping(existingTask, newTask)) {
                throw new IllegalArgumentException("Задачи пересекаются по времени.");
            }
        }

        // Проверка на пересечение с подзадачами, если это подзадача
        if (newTask instanceof Subtask) {
            Epic epic = epics.get(((Subtask) newTask).getEpicId());
            if (epic != null) {
                for (Subtask existingSubtask : getSubtasksByEpic(epic.getId())) {
                    if (existingSubtask.getId() != newTask.getId() &&
                            areTimeIntervalsOverlapping(existingSubtask, (Subtask) newTask)) {
                        throw new IllegalArgumentException("Подзадачи пересекаются по времени.");
                    }
                }
            }
        }
    }

    // Вспомогательный метод, который непосредственно проверяет, пересекаются ли две конкретные задачи
    private boolean taskTimesOverlap(Task existingTask, Task newTask) {
        if (existingTask.getStartTime() == null || newTask.getStartTime() == null ||
                existingTask.getDuration() == null || newTask.getDuration() == null) {
            return false; // Если время старта или длительность не заданы, пересечения не будет
        }

        LocalDateTime existingEndTime = existingTask.getStartTime().plus(existingTask.getDuration());
        LocalDateTime newEndTime = newTask.getStartTime().plus(newTask.getDuration());

        return existingTask.getStartTime().isBefore(newEndTime) &&
                newTask.getStartTime().isBefore(existingEndTime);
    }

    // Проверка, пересекаются ли два временных интервала
    private boolean areTimeIntervalsOverlapping(Task task1, Task task2) {
        // Проверяем, что время начала и длительность обеих задач не равны null
        if (task1.getStartTime() == null || task2.getStartTime() == null ||
                task1.getDuration() == null || task2.getDuration() == null) {
            return false; // Если время или длительность не заданы, пересечения не будет
        }

        LocalDateTime endTime1 = task1.getStartTime().plus(task1.getDuration());
        LocalDateTime endTime2 = task2.getStartTime().plus(task2.getDuration());

        return task1.getStartTime().isBefore(endTime2) && task2.getStartTime().isBefore(endTime1);
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

    public void addTask(Task task) {
        if (hasTimeConflict(task)) {
            throw new IllegalArgumentException("Задачи пересекаются по времени.");
        }
        addTaskToMap(tasks, task);
    }

    @Override
    public Epic addEpic(Epic epic) {
        // Убираем проверку на совпадение ID с задачами и подзадачами
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId()); // Получаем эпик по ID
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с таким ID не существует."); // Исключение, если эпик не найден
        }

        // Проверка на пересечение для новой подзадачи
        checkForTaskAndSubtaskConflicts(subtask);

        Subtask addedSubtask = addTaskToMap(subtasks, subtask); // Добавление подзадачи
        epic.addSubtaskId(addedSubtask.getId()); // Добавление ID подзадачи в эпик

        updateEpicStatus(epic.getId()); // Обновление статуса эпика
        prioritizedTasks.add(addedSubtask); // Добавление подзадачи в приоритезированный список

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
        for (int subtaskId : epic.getSubtask()) {
            Subtask subtask = subtasks.get(subtaskId); // Получаем подзадачу по ID
            if (subtask != null) {
                result.add(subtask); // Добавляем подзадачу в результат
            }
        }
        return result; // Возвращаем список подзадач
    }

    @Override
    public void updateTask(Task task) {
        // Проверка на пересечение со всеми другими задачами, кроме самой обновляемой задачи
        checkForTaskAndSubtaskConflicts(task);

        Task oldTask = tasks.get(task.getId()); // Получение старой версии задачи для корректного обновления

        if (oldTask != null) {
            prioritizedTasks.remove(oldTask); // Удаление старой версии из приоритетного списка
        }

        updateTaskInMap(tasks, task); // Обновление задачи в Map
        prioritizedTasks.add(task); // Добавление новой версии в приоритетный список
    }

    @Override
    public void updateEpic(Epic epic) {
        // Обновление эпика выполняется без проверки пересечений, так как это не требуется.
        Epic updatedEpic = updateTaskInMap(epics, epic); // Обновление эпика
        updateEpicPeriod(updatedEpic); // Обновление полей эпика при его обновлении
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId()); // Получаем эпик по ID
        if (epic != null) {
            // Проверка на пересечение для всех подзадач эпика, кроме обновляемой
            for (Subtask existingSubtask : getSubtasksByEpic(epic.getId())) {
                if (existingSubtask.getId() != subtask.getId() &&
                        areTimeIntervalsOverlapping(existingSubtask, subtask)) {
                    throw new IllegalArgumentException("Подзадачи пересекаются по времени при обновлении.");
                }
            }
        }

        Subtask oldSubtask = subtasks.get(subtask.getId()); // Получение старой версии подзадачи

        if (oldSubtask != null) {
            prioritizedTasks.remove(oldSubtask); // Удаление старой версии из приоритетного списка
        }

        updateTaskInMap(subtasks, subtask); // Обновление подзадачи в Map
        prioritizedTasks.add(subtask); // Добавление новой версии в приоритетный список

        // Обновление полей и статуса эпика, так как изменились данные подзадачи
        updateEpicStatus(epic.getId());
    }

    @Override
    public void deleteTaskById(int id) {
        removeTask(id);
    }

    @Override
    public void deleteEpicById(int id) {
        removeEpic(id);
    }

    @Override
    public void deleteSubtaskById(int id) {
        removeSubtask(id);
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

    private void updateEpicPeriod(Epic epic) {
        List<Subtask> subtasks = getSubtasksByEpic(epic.getId());

        if (subtasks.isEmpty()) {
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

        // Суммируем продолжительность
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
        return List.of();
    }

    private void addToHistory(Task task) {
        if (task != null) {
            historyManager.add(task); // Добавление задачи (или эпика, или подзадачи) в историю
        }
    }
}