package main.manager.task.file;

import main.manager.task.inMemory.InMemoryTaskManager;
import main.model.Epic;
import main.model.Status;
import main.model.Subtask;
import main.model.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    private FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Метод автосохранения данных
    private void save() {
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(CsvTaskConverter.getCsvHeader() + "\n");
            saveTasks(writer);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл: " + file.getName(), e);
        }
    }

    // Метод для сохранения задач в файл
    private void saveTasks(Writer writer) throws IOException {
        for (Task task : tasks.values()) {
            writer.write(CsvTaskConverter.taskToString(task) + "\n");
        }
        for (Epic epic : epics.values()) {
            writer.write(CsvTaskConverter.taskToString(epic) + "\n");
        }
        for (Subtask subtask : subtasks.values()) {
            writer.write(CsvTaskConverter.taskToString(subtask) + "\n");
        }
    }

    // Метод восстановления данных из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            if (lines.size() > 1) {
                for (String line : lines.subList(1, lines.size())) {
                    Task task = CsvTaskConverter.taskFromString(line.split(","));
                    switch (task.getType()) {
                        case TASK:
                            manager.tasks.put(task.getId(), task);
                            break;
                        case EPIC:
                            manager.epics.put(task.getId(), (Epic) task);
                            break;
                        case SUBTASK:
                            manager.subtasks.put(task.getId(), (Subtask) task);
                            break;
                    }
                }
            }
            // Обновление статусов эпиков после загрузки
            for (Epic epic : manager.getAllEpics()) {
                manager.updateEpicStatus(epic); // обновляем статусы эпиков
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении данных из файла: " + e.getMessage());
        }
        return manager;
    }

    // Переопределяем методы для добавления и обновления задач с автосохранением
    @Override
    public Task addTask(Task task) {
        super.addTask(task);
        save();
        return task;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        super.addEpic(epic);
        save();
        return epic;
    }

    @Override
    public void updateTask(Task task) {
        Task existingTask = tasks.get(task.getId());
        if (existingTask != null) {
            existingTask.setName(task.getName());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
            save();
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    // Переопределяем методы удаления задач с автосохранением
    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }

    public static void main(String[] args) {
        // Пример использования
        try {
            File tempFile = File.createTempFile("task_manager_example", ".csv");
            tempFile.deleteOnExit();

            FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
            Task task1 = new Task("Task 1", "Description for task 1", 1, Status.NEW, Duration.ofHours(1), LocalDateTime.now());
            Task task2 = new Task("Task 2", "Description for task 2", 2, Status.NEW, Duration.ofHours(1), LocalDateTime.now());
            Epic epic1 = new Epic("Epic 1", "Description for epic 1", 3, Status.NEW);
            Subtask subtask1 = new Subtask("Subtask 1", "Description for subtask 1", 4, epic1.getId(), Status.NEW, Duration.ofHours(1),
                                           LocalDateTime.now());
            Subtask subtask2 = new Subtask("Subtask 2", "Description for subtask 2", 5, epic1.getId(), Status.NEW, Duration.ofHours(1),
                                           LocalDateTime.now());

            manager.addTask(task1);
            manager.addTask(task2);
            manager.addEpic(epic1);
            manager.addSubtask(subtask1);
            manager.addSubtask(subtask2);

            // Загружаем из файла
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

            // Вывод загруженных задач
            System.out.println("Tasks in loaded manager:");
            for (Task task : loadedManager.getAllTasks()) {
                System.out.println(task);
            }
            for (Epic epic : loadedManager.getAllEpics()) {
                System.out.println(epic);
                for (Integer subtaskId : epic.getSubtask()) {
                    System.out.println("  " + loadedManager.getSubtask(subtaskId));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}