package main.manager.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import main.manager.exceptions.NotFoundException;
import main.manager.task.TaskManager;
import main.model.Task;

import java.io.IOException;
import java.util.List;

class TaskHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public TaskHandler(TaskManager manager, Gson gson) {
        super(gson);
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        System.out.println("Received request: " + method + " " + path);

        try {
            switch (method) { // Используем switch для лучшей читаемости
                case "GET":
                    handleGetTask(exchange, path);
                    break;
                case "POST":
                    handlePostTask(exchange, path);
                    break;
                case "DELETE":
                    handleDeleteTask(exchange, path);
                    break;
                default:
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleException(exchange, e);
        }
    }

    private void handleGetTask(HttpExchange exchange, String path) throws IOException {
        if (isTasksEndpoint(path)) {
            List<Task> tasks = manager.getAllTasks();
            sendResponse(exchange, gson.toJson(tasks), 200);
        } else if (isTaskWithIdEndpoint(path)) {
            int id = parseIdFromPath(path);
            Task task = manager.getTask(id);
            if (task == null) {
                sendNotFound(exchange);
            } else {
                sendResponse(exchange, gson.toJson(task), 200);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostTask(HttpExchange exchange, String path) throws IOException {
        if (isTasksEndpoint(path) || isTaskWithIdEndpoint(path)) {
            try {
                String body = readRequestBody(exchange);
                Task task = gson.fromJson(body, Task.class);

                if (task == null) {
                    sendResponse(exchange, "Некорректный JSON формат", 400);
                    return;
                }

                if (isTaskWithIdEndpoint(path)) {
                    int taskId = parseIdFromPath(path);
                    if (task.getId() != taskId) {
                        sendResponse(exchange, "Неверный id", 400);
                        return;
                    }
                    try {
                        manager.updateTask(task);
                        sendResponse(exchange, "Задача обновлена", 200);
                    } catch (NotFoundException e) {
                        sendResponse(exchange, "Задача с таким id не найдена", 404);
                    }
                } else {
                    manager.addTask(task);
                    sendResponse(exchange, "Задача создана", 201);
                }
            } catch (JsonSyntaxException e) {
                sendResponse(exchange, "Некорректный JSON формат", 400);
            } catch (Exception e) {
                handleException(exchange, e);
            }
        } else {
            sendResponse(exchange, "Эндпоинт не найден", 404);
        }
    }

    private void handleDeleteTask(HttpExchange exchange, String path) throws IOException {
        if (isTaskWithIdEndpoint(path)) {
            int id = parseIdFromPath(path);
            if (id != -1) {
                manager.removeTask(id);
                sendResponse(exchange, "Задача удалена", 204);
            } else {
                sendResponse(exchange, "Неверный id", 400);
            }
        } else if (isTasksEndpoint(path)) {
            manager.clearTasks();
            sendResponse(exchange, "Все задачи удалены", 200);
        } else {
            sendResponse(exchange, "Эндпоинт не найден", 404);
        }
    }

    private boolean isTasksEndpoint(String path) {
        return path.equals("/tasks");
    }

    private boolean isTaskWithIdEndpoint(String path) {
        return path.matches("/tasks/\\d+");
    }

    private int parseIdFromPath(String path) {
        try {
            String[] parts = path.split("/");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return -1; // Возвращаем -1, если не удалось разобрать id
        }
    }
}