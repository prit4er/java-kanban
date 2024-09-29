package main.model;

import java.util.Objects;

public class Task {

    private String name;
    private String description;
    private Integer id;
    private Status status;
    protected TaskType type;

    // Основной конструктор
    public Task(String name, String description, int id, Status status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.type = TaskType.TASK; // Устанавливаем тип задачи как TASK
    }

    // Конструктор с установкой статуса по умолчанию
    public Task(String name, String description, int id) {
        this(name, description, id, Status.NEW); // Статус по умолчанию - NEW
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

    // Метод для получения типа задачи
    public TaskType getType() {
        return type;
    }

    // Метод для преобразования задачи в CSV-строку
    public String toCsvString() {
        return String.format("%d,%s,%s,%s,%s",
                             id,
                             getType(), // Используем getType() для получения типа
                             name,
                             status,
                             description);
    }

    // Метод для создания задачи из CSV-строки
    public static Task fromCsvString(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        return new Task(name, description, id, status);
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