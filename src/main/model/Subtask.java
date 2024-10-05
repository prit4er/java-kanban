package main.model;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String name, String description, int id, int epicId, Status status) {
        super(name, description, id, status);
        this.epicId = epicId;
        this.type = TaskType.SUBTASK;  // Устанавливаем тип задачи как SUBTASK
    }

    // Геттер для поля type
    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }
}