package main.manager.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import main.manager.exceptions.NotFoundException;
import main.manager.task.TaskManager;
import main.model.Subtask;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler {

    private final TaskManager manager;

    public SubtaskHandler(TaskManager manager, Gson gson) {
        super(gson);
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try {
            switch (method) { // Используем switch для лучшей читаемости
                case "GET":
                    handleGetSubtask(exchange, path);
                    break;
                case "POST":
                    handlePostSubtask(exchange, path);
                    break;
                case "DELETE":
                    handleDeleteSubtask(exchange, path);
                    break;
                default:
                    System.out.println("Неправильный метод");
                    exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleException(exchange, e);
        }
    }

    private void handleGetSubtask(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            List<Subtask> subtasks = manager.getAllSubtasks();
            sendResponse(exchange, gson.toJson(subtasks), 200);
        } else if (path.matches("/subtasks/\\d+")) {
            int id = parseIdFromPath(path);
            Subtask subtask = manager.getSubtask(id);
            if (subtask == null) {
                sendNotFound(exchange);
            } else {
                sendResponse(exchange, gson.toJson(subtask), 200);
            }
        } else {
            sendResponse(exchange, "Эндпоинт не найден", 404);
        }
    }

    private void handlePostSubtask(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/subtasks(/\\d+)?")) {
            try {
                String body = readRequestBody(exchange);
                Subtask subtask = gson.fromJson(body, Subtask.class);

                if (subtask == null) {
                    sendResponse(exchange, "Некорректный JSON формат", 400);
                    return;
                }

                // Установка значений по умолчанию
                if (subtask.getStartTime() == null) {
                    subtask.setStartTime(LocalDateTime.now());
                }
                if (subtask.getDuration() == null) {
                    subtask.setDuration(Duration.ZERO);
                }

                if (path.matches("/subtasks/\\d+")) { // Обработка пути /subtasks/{id}
                    int subtaskId = parseIdFromPath(path);

                    if (subtask.getId() != subtaskId) {
                        sendResponse(exchange, "Неверный id подзадачи", 400);
                        return;
                    }

                    try {
                        manager.updateSubtask(subtask);
                        sendResponse(exchange, "Подзадача обновлена", 201);
                    } catch (NotFoundException e) {
                        sendResponse(exchange, "Подзадача с таким id не найдена", 404);
                    }
                } else {
                    if (subtask.getId() != 0) {
                        sendResponse(exchange, "Неверный id (для новой подзадачи id должен быть 0)", 400);
                        return;
                    }

                    manager.addSubtask(subtask);
                    sendResponse(exchange, "Подзадача создана", 201);
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

    private void handleDeleteSubtask(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/subtasks/\\d+")) {
            int id = parseIdFromPath(path);
            manager.removeSubtask(id);
            sendResponse(exchange, "Подзадача удалена", 204);
        } else if (path.equals("/subtasks")) {
            manager.clearSubtasks();
            sendResponse(exchange, "Все подзадачи удалены", 200);
        } else {
            sendResponse(exchange, "Эндпоинт не найден", 404);
        }
    }

    private int parseIdFromPath(String path) {
        String[] parts = path.split("/");
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return -1; // Если не удалось разобрать id, возвращаем -1
        }
    }
}