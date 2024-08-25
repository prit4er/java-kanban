package model;

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
}