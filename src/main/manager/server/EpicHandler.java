package main.manager.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import main.manager.exceptions.NotFoundException;
import main.manager.task.TaskManager;
import main.model.Epic;
import main.model.Subtask;
import main.model.Task;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EpicHandler extends BaseHttpHandler {

    private final TaskManager manager;

    public EpicHandler(TaskManager manager, Gson gson) {
        super(gson);
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (method) {
                case "GET":
                    handleGetEpic(exchange, path);
                    break;
                case "POST":
                    handlePostEpic(exchange, path);
                    break;
                case "DELETE":
                    handleDeleteEpic(exchange, path);
                    break;
                default:
                    sendError(exchange, 405, "Метод не поддерживается");
            }
        } catch (Exception e) {
            handleException(exchange, e);
        } finally {
            exchange.close();
        }
    }

    private void handleGetEpic(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            List<Epic> epics = manager.getAllEpics();
            sendResponse(exchange, gson.toJson(epics), 200);
        } else {
            int id = parseIdFromPath(path);
            if (id == -1) {
                sendError(exchange, 400, "Некорректный ID эпика");
                return;
            }

            if (path.endsWith("/subtasks")) {
                handleGetSubtasks(exchange, id);
            } else {
                handleGetEpicById(exchange, id);
            }
        }
    }

    private void handleGetSubtasks(HttpExchange exchange, int epicId) throws IOException {
        List<Subtask> subtasks = manager.getSubtasksByEpic(epicId);
        sendResponse(exchange, gson.toJson(subtasks), subtasks.isEmpty() ? 404 : 200);
    }

    private void handleGetEpicById(HttpExchange exchange, int epicId) throws IOException {
        Optional<Epic> epic = Optional.ofNullable(manager.getEpic(epicId));
        if (epic.isPresent()) {
            sendResponse(exchange, gson.toJson(epic.get()), 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostEpic(HttpExchange exchange, String path) throws IOException {
        if (!path.matches("/epics(/\\d+)?")) {
            sendError(exchange, 404, "Эндпоинт не найден");
            return;
        }

        String body = readRequestBody(exchange);
        try {
            Epic epic = gson.fromJson(body, Epic.class);
            if (epic == null) {
                sendError(exchange, 400, "Некорректный JSON формат");
                return;
            }

            if (path.matches("/epics/\\d+")) {
                updateEpic(exchange, epic);
            } else {
                createEpic(exchange, epic);
            }
        } catch (JsonSyntaxException e) {
            sendError(exchange, 400, "Некорректный JSON формат: " + e.getMessage());
        }
    }

    private void createEpic(HttpExchange exchange, Epic epic) throws IOException {
        if (epic.getId() != 0) {
            sendError(exchange, 400, "ID должен быть равен 0 при создании нового эпика");
            return;
        }
        manager.addEpic(epic);
        sendResponse(exchange, "Эпик создан", 201);
    }

    private void updateEpic(HttpExchange exchange, Epic epic) throws IOException {
        int epicId = parseIdFromPath(exchange.getRequestURI().getPath());
        if (epic.getId() == 0 || epic.getId() != epicId) {
            sendError(exchange, 400, "Неверный ID");
            return;
        }

        try {
            manager.updateTask(epic);
            sendResponse(exchange, "Эпик обновлен", 200);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        }
    }

    private void handleDeleteEpic(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/epics/\\d+")) {
            int id = parseIdFromPath(path);
            if (id == -1) {
                sendError(exchange, 400, "Некорректный ID эпика");
                return;
            }
            if (manager.getEpic(id) != null) {
                manager.removeTask(id);
                sendResponse(exchange, "Эпик удален", 200);
            } else {
                sendNotFound(exchange);
            }
        } else if (path.equals("/epics")) {
            manager.clearEpics();
            sendResponse(exchange, "Все эпики удалены", 200);
        } else {
            sendError(exchange, 404, "Эндпоинт не найден");
        }
    }

    private int parseIdFromPath(String path) {
        String[] segments = path.split("/");
        for (int i = segments.length - 1; i >= 0; i--) {
            try {
                return Integer.parseInt(segments[i]);
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }
}