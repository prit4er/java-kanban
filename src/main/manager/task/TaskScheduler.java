package main.manager.task;

import main.model.Status;
import main.model.Task;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskScheduler {
    // Список задач
    private final List<Task> tasks;
    private final Map<LocalDateTime, Boolean> timeSlots; // Хранит занятые интервалы

    public TaskScheduler() {
        tasks = new ArrayList<>();
        timeSlots = new HashMap<>();
    }

    // Метод для проверки доступности времени
    public boolean isTimeAvailable(LocalDateTime startTime, Duration duration) {
        long totalSlots = duration.toMinutes() / 15; // Количество 15-минутных интервалов
        LocalDateTime endTime = startTime.plus(duration);

        // Проверка каждого 15-минутного интервала
        for (long i = 0; i < totalSlots; i++) {
            LocalDateTime slot = startTime.plusMinutes(i * 15);
            Boolean isSlotFree = timeSlots.get(slot);
            if (isSlotFree == null || !isSlotFree) {
                System.out.println("Занятое время: " + slot);
                return false; // Найден занятый интервал
            }
        }
        return true; // Все интервалы свободны
    }

    // Метод для добавления задачи в расписание
    public void scheduleTask(String title, LocalDateTime startTime, Duration duration) {
        if (isTimeAvailable(startTime, duration)) {
            Task task = new Task(title, "Описание задачи", 0, Status.NEW, duration, startTime);
            tasks.add(task);

            // Обновляем занятые интервалы
            long totalSlots = duration.toMinutes() / 15;
            for (long i = 0; i < totalSlots; i++) {
                LocalDateTime slot = startTime.plusMinutes(i * 15);
                timeSlots.put(slot, true); // Помечаем как занятый
            }

            // Сортируем задачи по времени начала для упрощения будущих проверок
            tasks.sort(Comparator.comparing(Task::getStartTime)); // Удалены скобки
            System.out.println("Задача успешно добавлена в расписание.");
        } else {
            System.out.println("Время занято. Невозможно запланировать задачу.");
        }
    }

    // Вспомогательный метод для вывода списка задач
    public void printSchedule() {
        for (Task task : tasks) {
            System.out.println(task);
        }
    }

    public static void main(String[] args) {
        TaskScheduler scheduler = new TaskScheduler();

        LocalDateTime task1StartTime = LocalDateTime.now().plusMinutes(15);
        Duration task1Duration = Duration.ofMinutes(30);

        scheduler.scheduleTask("Task 1", task1StartTime, task1Duration);
        scheduler.printSchedule();

        LocalDateTime task2StartTime = task1StartTime.plusMinutes(45); // Попробуем запланировать задачу на 45 минут позже
        Duration task2Duration = Duration.ofMinutes(30);

        scheduler.scheduleTask("Task 2", task2StartTime, task2Duration);
        scheduler.printSchedule();
    }
}