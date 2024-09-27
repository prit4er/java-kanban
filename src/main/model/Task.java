package main.model;

import java.util.Objects;

public class Task {

    private String name;
    private String description;
    private Integer id;
    private Status status;

    public Task(String name, String description, int id, Status status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
    }

    public Task(String name, String description, int id) {
        this(name, description, id, Status.NEW); // Устанавливаем статус по умолчанию
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // Новый метод для получения типа задачи
    public TaskType getType() {
        return TaskType.TASK; // Для обычных задач возвращаем TASK
    }

    // Метод для обновления полей задачи
    public void updateFrom(Task updatedTask) {
        if (updatedTask.getName() != null) {
            this.name = updatedTask.getName();
        }
        if (updatedTask.getDescription() != null) {
            this.description = updatedTask.getDescription();
        }
        if (updatedTask.getStatus() != null) {
            this.status = updatedTask.getStatus();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}