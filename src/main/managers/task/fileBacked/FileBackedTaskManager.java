package main.managers.task.fileBacked;

import main.managers.task.inMemory.InMemoryTaskManager;
import main.model.Epic;
import main.model.Status;
import main.model.Subtask;
import main.model.Task;
import main.model.TaskType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    private FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Метод автосохранения данных
    private void save() {
        try (Writer writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic\n");  // Заголовок CSV
            // Сохраняем все задачи, эпики и подзадачи
            for (Task task : tasks.values()) {
                writer.write(CsvTaskConverter.taskToString(task) + "\n");
            }
            for (Epic epic : epics.values()) {
                writer.write(CsvTaskConverter.taskToString(epic) + "\n");
            }
            for (Subtask subtask : subtasks.values()) {
                writer.write(CsvTaskConverter.taskToString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл: " + file.getName(), e);
        }
    }

    // Метод восстановления данных из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            // Проверяем, есть ли строки для обработки (проверка на пустоту файла)
            if (lines.size() > 1) { // Если есть строки, кроме заголовка
                for (String line : lines.subList(1, lines.size())) { // Пропускаем заголовок
                    Task task = CsvTaskConverter.taskFromString(line);
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
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении данных из файла");
        }
        return manager;
    }

    public static class CsvTaskConverter {

        // Преобразование задачи в строку для сохранения в файл
        private static String taskToString(Task task) {
            StringBuilder sb = new StringBuilder();
            sb.append(task.getId()).append(",");
            sb.append(task.getType()).append(",");
            sb.append(task.getName()).append(",");
            sb.append(task.getStatus()).append(",");
            sb.append(task.getDescription()).append(",");
            if (task instanceof Subtask) {
                sb.append(((Subtask) task).getEpicId());
            }
            return sb.toString();
        }

        public static Task taskFromString(String value) {
            String[] fields = value.split(",");
            int id = Integer.parseInt(fields[0]);
            TaskType type = TaskType.valueOf(fields[1]);
            String name = fields[2];
            Status status = Status.valueOf(fields[3]);
            String description = fields[4];

            return switch (type) {
                case TASK -> new Task(name, description, id, status);
                case EPIC -> new Epic(name, description, id);
                case SUBTASK -> {
                    int epicId = Integer.parseInt(fields[5]);
                    yield new Subtask(name, description, id, epicId, status);
                }
            };
        }
    }

    // Пример переопределения метода для добавления задачи
    @Override
    public Task addTask(Task task) {
        super.addTask(task);
        save(); // После добавления задачи сохраняем данные
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
    public Task updateTask(Task task) {
        Task existingTask = tasks.get(task.getId());
        if (existingTask != null) {
            existingTask.setName(task.getName());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
            save();
        }
        return existingTask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
        return epic;
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }


    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    public static void main(String[] args) {
        // Создаем временный файл для менеджера
        try {
            File tempFile = File.createTempFile("task_manager_example", ".csv");
            tempFile.deleteOnExit(); // Удаляем файл после завершения программы

            // Создаем менеджер задач
            FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

            // Создаем и добавляем задачи
            Task task1 = new Task("Task 1", "Description for task 1", 1);
            Task task2 = new Task("Task 2", "Description for task 2", 2);
            Epic epic1 = new Epic("Epic 1", "Description for epic 1", 3);
            Subtask subtask1 = new Subtask("Subtask 1", "Description for subtask 1", 4, epic1.getId(), Status.NEW);
            Subtask subtask2 = new Subtask("Subtask 2", "Description for subtask 2", 5, epic1.getId(), Status.NEW);

            // Добавляем задачи в менеджер
            manager.addTask(task1);
            manager.addTask(task2);
            manager.addEpic(epic1);
            manager.addSubtask(subtask1);
            manager.addSubtask(subtask2);

            // Сохраняем состояние в файл
            manager.save();

            // Создаем новый менеджер и загружаем из файла
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

            // Проверяем, что загруженные задачи совпадают с добавленными
            System.out.println("Tasks in loaded manager:");
            System.out.println("Total Tasks: " + loadedManager.getAllTasks().size());
            System.out.println("Total Epics: " + loadedManager.getAllEpics().size());
            System.out.println("Total Subtasks: " + loadedManager.getAllSubtasks().size());

            // Проверяем каждую задачу
            for (Task task : loadedManager.getAllTasks()) {
                System.out.println(task);
            }
            for (Epic epic : loadedManager.getAllEpics()) {
                System.out.println(epic);
                // Проверяем подзадачи эпика
                for (Integer subtaskId : epic.getSubtaskIds()) {
                    System.out.println("  " + loadedManager.getSubtask(subtaskId));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
