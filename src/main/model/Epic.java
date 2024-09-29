package main.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> subtaskIds;  // Список идентификаторов подзадач

    public Epic(String name, String description, int id) {
        super(name, description, id);
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public static Epic fromCsvString(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String name = fields[2];
        String description = fields[4];
        return new Epic(name, description, id);
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC; // Для эпиков возвращаем EPIC
    }

    @Override
    public String toCsvString() {
        return super.toCsvString();  // Для Epic дополнительные поля не нужны
    }
}