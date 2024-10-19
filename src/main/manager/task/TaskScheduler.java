package main.manager.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TaskScheduler {

    // Интервал времени для задачи (в минутах)
    private static final int TIME_INTERVAL = 15;

    // Хранение занятости времени
    private final Map<LocalDateTime, Boolean> schedule;

    public TaskScheduler() {
        schedule = new HashMap<>();
        // Заполняем расписание на 1 год
        LocalDateTime start = LocalDateTime.now();
        for (int i = 0; i < 365 * 24 * 60 / TIME_INTERVAL; i++) {
            schedule.put(start.plusMinutes(i * TIME_INTERVAL), true); // Все интервалы свободны
        }
    }

    public boolean isTimeAvailable(LocalDateTime startTime, Duration duration) {
        long requiredIntervals = duration.toMinutes() / TIME_INTERVAL;
        for (long i = 0; i < requiredIntervals; i++) {
            LocalDateTime intervalTime = startTime.plusMinutes(i * TIME_INTERVAL);
            if (!schedule.getOrDefault(intervalTime, false)) {
                return false; // Если хотя бы один интервал занят
            }
        }
        return true; // Все интервалы свободны
    }

    public void scheduleTask(LocalDateTime startTime, Duration duration) {
        long requiredIntervals = duration.toMinutes() / TIME_INTERVAL;
        for (long i = 0; i < requiredIntervals; i++) {
            LocalDateTime intervalTime = startTime.plusMinutes(i * TIME_INTERVAL);
            schedule.put(intervalTime, false); // Отмечаем интервал как занятый
        }
    }

    public static void main(String[] args) {
        TaskScheduler scheduler = new TaskScheduler();

        LocalDateTime taskStartTime = LocalDateTime.now().plusMinutes(15);
        Duration taskDuration = Duration.ofMinutes(30);

        if (scheduler.isTimeAvailable(taskStartTime, taskDuration)) {
            System.out.println("Время доступно для планирования задачи.");
            scheduler.scheduleTask(taskStartTime, taskDuration);
        } else {
            System.out.println("Время занято. Невозможно запланировать задачу.");
        }
    }
}

