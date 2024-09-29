package main.model;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String name, String description, int id, int epicId, Status status) {
        super(name, description, id, status);
        this.epicId = epicId;
        this.type = TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public static Subtask fromCsvString(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        int epicId = Integer.parseInt(fields[5]);
        return new Subtask(name, description, id, epicId, status);
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK; // Для подзадач возвращаем SUBTASK
    }

    @Override
    public String toCsvString() {
        return String.format("%s,%d", super.toCsvString(), epicId);
    }
}