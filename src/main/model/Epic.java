package main.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {

    private final List<Integer> subtaskIds;

    // Новые поля
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Epic(String name, String description, int id) {
        super(name, description, id, Duration.ZERO, null);  // Продолжительность и время начала зададим позже
        this.subtaskIds = new ArrayList<>();
        this.type = TaskType.EPIC;  // Устанавливаем тип задачи как EPIC
        this.duration = Duration.ZERO;  // Изначально продолжительность равна нулю
    }

    // Переопределяем метод getType()
    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
        recalculateEpicFields();  // Пересчитываем поля при добавлении подзадачи
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
        recalculateEpicFields();  // Пересчитываем поля при удалении подзадачи
    }

    // Метод для пересчета полей эпика (duration, startTime и endTime)
    public void recalculateEpicFields() {
        List<Subtask> subtasks = getSubtasksFromManager();  // Метод для получения всех подзадач из менеджера

        if (subtasks.isEmpty()) {
            this.duration = Duration.ZERO;
            this.startTime = null;
            this.endTime = null;
        } else {
            this.duration = subtasks.stream()
                                    .map(Subtask::getDuration)
                                    .reduce(Duration.ZERO, Duration::plus);  // Суммируем продолжительность всех подзадач

            this.startTime = subtasks.stream()
                                     .map(Subtask::getStartTime)
                                     .filter(Objects::nonNull)
                                     .min(LocalDateTime::compareTo)  // Находим самую раннюю дату старта
                                     .orElse(null);

            this.endTime = subtasks.stream()
                                   .map(Subtask::getEndTime)
                                   .filter(Objects::nonNull)
                                   .max(LocalDateTime::compareTo)  // Находим самую позднюю дату окончания
                                   .orElse(null);
        }
    }

    // Метод для получения времени окончания эпика (конец самой поздней подзадачи)
    @Override
    public LocalDateTime getEndTime() {
        return this.endTime;
    }

    // Геттеры для новых полей
    @Override
    public Duration getDuration() {
        return this.duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    // Метод для получения подзадач эпика (предположительно из менеджера задач)
    private List<Subtask> getSubtasksFromManager() {
        // Здесь вы должны реализовать получение списка подзадач эпика из менеджера
        // Например, с помощью вызова метода в TaskManager, который будет возвращать все подзадачи по ID эпика
        return new ArrayList<>();
    }

    // Переопределение метода toString для отображения полей эпика
    @Override
    public String toString() {
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}