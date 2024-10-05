package main.model;

import java.util.Objects;

public class Task {

    private String name;
    private String description;
    private Integer id;
    private Status status;
    protected TaskType type;  // Новое поле для типа задачи

    // Основной конструктор
    public Task(String name, String description, int id, Status status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.type = TaskType.TASK;  // По умолчанию, тип задачи — TASK
    }

    // Конструктор с установкой статуса по умолчанию
    public Task(String name, String description, int id) {
        this(name, description, id, Status.NEW);
    }

    // Геттеры и сеттеры
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

    // Геттер для поля type
    public TaskType getType() {
        return type;
    }

    // Переопределение методов equals и hashCode
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

    // Метод для удобного отображения задачи
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