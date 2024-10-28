package main.manager.task.file;

import main.model.Epic;
import main.model.Status;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvTaskConverter {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Приватный метод для базовой части конвертации задачи в строку
    private static String baseTaskToString(Task task) {
        return String.format("%d,%s,%s,%s,%s,%d,%s",
                             task.getId(),
                             task.getType(), // Тип задачи (TASK, EPIC или SUBTASK)
                             task.getName(),
                             task.getStatus(),
                             task.getDescription(),
                             task.getDuration().toMinutes(),  // Добавляем продолжительность
                             task.getStartTime() != null ? task.getStartTime().format(formatter) : "null"); // Добавляем время старта
    }

    // Метод для преобразования задачи (Task) в строку для сохранения в CSV файл
    public static String taskToString(Task task) {
        return baseTaskToString(task);
    }

    // Метод для преобразования подзадачи (Subtask) в строку для сохранения в CSV файл
    public static String taskToString(Subtask subtask) {
        return baseTaskToString(subtask) + "," + subtask.getEpicId(); // Добавляем epicId для подзадачи
    }

    // Метод для преобразования эпика (Epic) в строку для сохранения в CSV файл
    public static String taskToString(Epic epic) {
        return baseTaskToString(epic); // У эпика нет дополнительных полей, можно использовать базовую строку
    }

    // Метод, который возвращает заголовок CSV файла
    public static String getCsvHeader() {
        return "id,type,name,status,description,duration,startTime,epicId";
    }

    // Пример метода для создания задачи из CSV строки (универсальный для всех типов задач)
    public static Task taskFromString(String[] fields) {
        int id = Integer.parseInt(fields[0]);

        // Проверка на null или пустую строку перед преобразованием
        if (fields[1] == null || fields[1].isEmpty()) {
            throw new IllegalArgumentException("Task type cannot be null or empty");
        }

        TaskType type;
        try {
            type = TaskType.valueOf(fields[1]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown task type: " + fields[1]);
        }

        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        Duration duration = Duration.ofMinutes(Long.parseLong(fields[5]));  // Преобразуем продолжительность из минут
        LocalDateTime startTime = !fields[6].equals("null") ? LocalDateTime.parse(fields[6], formatter) : null;  // Преобразуем время старта

        // Используем switch для выбора типа задачи
        return switch (type) {
            case TASK -> new Task(name, description, id, status, duration, startTime);
            case EPIC -> new Epic(name, description, id, status);
            case SUBTASK -> {
                int epicId = Integer.parseInt(fields[7]);
                yield new Subtask(name, description, id, epicId, status, duration, startTime);
            }
            default -> throw new IllegalArgumentException("Unknown task type: " + type);
        };
    }
}