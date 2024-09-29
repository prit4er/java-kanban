package main.manager.task.file;

import main.model.Task;
import main.model.Epic;
import main.model.Subtask;
import main.model.TaskType;

public class CsvTaskConverter {

    // Преобразование задачи в строку для сохранения в файл (CSV формат)
    public static String taskToString(Task task) {
        return task.toCsvString();
    }

    // Преобразование строки из файла обратно в задачу
    public static Task taskFromString(String value) {
        String[] fields = value.split(",");
        TaskType type = TaskType.valueOf(fields[1]);

        return switch (type) {
            case TASK -> Task.fromCsvString(fields);
            case EPIC -> Epic.fromCsvString(fields);
            case SUBTASK -> Subtask.fromCsvString(fields);
        };
    }
}