package main.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> subtaskIds;

    public Epic(String name, String description, int id) {
        super(name, description, id);
        this.subtaskIds = new ArrayList<>();
        this.type = TaskType.EPIC;  // Устанавливаем тип задачи как EPIC
    }

    // Геттер для поля type
    @Override
    public TaskType getType() {
        return TaskType.EPIC;
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
}