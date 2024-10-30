package main.manager.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import main.manager.task.TaskManager;
import main.model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedTaskHandler extends BaseHttpHandler {

    private final TaskManager manager;

    public PrioritizedTaskHandler(TaskManager manager, Gson gson) {
        super(gson);
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        try {
            switch (method) { // Используем switch для лучшей читаемости
                case "GET":
                    handleGet(exchange);
                    break;
                default:
                    System.out.println("Неправильный метод");
                    sendResponse(exchange, "Метод не поддерживается", 405); // Отправляем ответ клиенту
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleException(exchange, e);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        List<Task> prioritized = manager.getPrioritizedTasks();
        sendResponse(exchange, gson.toJson(prioritized), 200);
    }
}