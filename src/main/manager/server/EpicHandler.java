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

public class EpicHandler extends BaseHttpHandler {

    private final TaskManager manager;

    public EpicHandler(TaskManager manager, Gson gson) {
        super(gson);
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            try {
                switch (method) {
                    case "GET" -> handleGetEpic(exchange, path);
                    case "POST" -> handlePostEpic(exchange, path);
                    case "DELETE" -> handleDeleteEpic(exchange, path);
                    default -> sendError(exchange, 405, "Метод не поддерживается");
                }
            } catch (Exception e) {
                handleException(exchange, e);
            }
        } finally {
            exchange.close();
        }
    }

    //Обработка GET-запросов для получения списка эпиков, эпика по ID или подзадач эпика.
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

            if (path.matches("/epics/\\d+")) {
                Task epic = manager.getEpic(id);
                if (epic != null) {
                    sendResponse(exchange, gson.toJson(epic), 200);
                } else {
                    sendNotFound(exchange);
                }
            } else if (path.matches("/epics/\\d+/subtasks")) {
                List<Subtask> subtasks = manager.getSubtasksByEpic(id);
                if (subtasks != null) {
                    sendResponse(exchange, gson.toJson(subtasks), 200);
                } else {
                    sendNotFound(exchange);
                }
            } else {
                sendError(exchange, 404, "Эндпоинт не найден");
            }
        }
    }

    //Обработка POST-запросов для создания или обновления эпика.
    private void handlePostEpic(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/epics(/\\d+)?")) {
            String body = readRequestBody(exchange);
            try {
                Epic epic = gson.fromJson(body, Epic.class);
                if (epic == null) {
                    sendError(exchange, 400, "Некорректный JSON формат");
                    return;
                }

                if (path.matches("/epics/\\d+")) {
                    // Обновление существующего эпика
                    int epicId = parseIdFromPath(path);
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
                } else {
                    // Создание нового эпика
                    if (epic.getId() != 0) {
                        sendError(exchange, 400, "ID должен быть равен 0 при создании нового эпика");
                        return;
                    }
                    manager.addEpic(epic);
                    sendResponse(exchange, "Эпик создан", 201);
                }
            } catch (JsonSyntaxException e) {
                sendError(exchange, 400, "Некорректный JSON формат: " + e.getMessage());
            }
        } else {
            sendError(exchange, 404, "Эндпоинт не найден");
        }
    }

    //Обработка DELETE-запросов для удаления эпика по ID или всех эпиков.
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

    //Извлекает последний числовой ID из пути запроса.
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