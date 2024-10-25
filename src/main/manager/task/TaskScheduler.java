package main.manager.task;

import main.model.Status;
import main.model.Task;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class TaskScheduler {
    // Константа для длительности слота
    private static final int SLOT_DURATION_MINUTES = 15;
    private final Map<LocalDateTime, Boolean> timeSlots; // Хранит занятые интервалы

    public TaskScheduler() {
        timeSlots = new HashMap<>();
    }

    // Метод для проверки доступности времени
    public boolean isTimeAvailable(LocalDateTime startTime, Duration duration) {
        LocalDateTime endTime = startTime.plus(duration);
        while (startTime.isBefore(endTime)) {
            Boolean isSlotFree = timeSlots.get(startTime);
            if (isSlotFree != null && !isSlotFree) {
                System.out.println("Занятое время: " + startTime);
                return false; // Найден занятый интервал
            }
            startTime = startTime.plusMinutes(SLOT_DURATION_MINUTES);
        }
        return true; // Все интервалы свободны
    }

    // Метод для добавления задачи в расписание
    public void scheduleTask(String title, LocalDateTime startTime, Duration duration) {
        if (isTimeAvailable(startTime, duration)) {
            Task task = new Task(title, "Описание задачи", 0, Status.NEW, duration, startTime);
            LocalDateTime endTime = startTime.plus(duration);

            // Обновляем занятые интервалы
            while (startTime.isBefore(endTime)) {
                timeSlots.put(startTime, true); // Помечаем как занятый
                startTime = startTime.plusMinutes(SLOT_DURATION_MINUTES);
            }
            System.out.println("Задача успешно добавлена в расписание: " + task.getName()
                                       + " Начало: " + task.getStartTime()
                                       + " Длительность: " + task.getDuration().toMinutes() + " минут.");
        } else {
            System.out.println("Время занято. Невозможно запланировать задачу.");
        }
    }

    public static void main(String[] args) {
        TaskScheduler scheduler = new TaskScheduler();

        // Время начала первой задачи: через 15 минут
        LocalDateTime task1StartTime = LocalDateTime.now().plusMinutes(SLOT_DURATION_MINUTES);
        Duration task1Duration = Duration.ofMinutes(30); // Длительность задачи: 30 минут

        // Планируем первую задачу
        scheduler.scheduleTask("Task 1", task1StartTime, task1Duration);

        // Время начала второй задачи: через 45 минут после начала первой задачи
        LocalDateTime task2StartTime = task1StartTime.plusMinutes(45); // Смещение использует константу
        Duration task2Duration = Duration.ofMinutes(30); // Длительность второй задачи: 30 минут

        // Планируем вторую задачу
        scheduler.scheduleTask("Task 2", task2StartTime, task2Duration);
    }
}