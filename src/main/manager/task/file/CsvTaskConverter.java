package main.manager.task.file;

import main.model.Epic;
import main.model.Status;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskType;

public class CsvTaskConverter {

    // Приватный метод для базовой части конвертации задачи в строку
    private static String baseTaskToString(Task task) {
        return String.format("%d,%s,%s,%s,%s",
                             task.getId(),
                             task.getType(), // Тип задачи (TASK, EPIC или SUBTASK)
                             task.getName(),
                             task.getStatus(),
                             task.getDescription());
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
        return "id,type,name,status,description,epicId";
    }

    // Пример метода для создания задачи из CSV строки (универсальный для всех типов задач)
    public static Task taskFromString(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        // Используем switch для выбора типа задачи
        return switch (type) {
            case TASK -> new Task(name, description, id, status);
            case EPIC -> new Epic(name, description, id);
            case SUBTASK -> {
                int epicId = Integer.parseInt(fields[5]);
                yield new Subtask(name, description, id, epicId, status);
            }
            default -> throw new IllegalArgumentException("Unknown task type: " + type);
        };
    }
}