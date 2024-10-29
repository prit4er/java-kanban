package main.manager.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import main.manager.task.TaskManager;
import main.model.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public HistoryHandler(TaskManager manager, Gson gson) {
        super(gson);
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                handleGet(exchange);
            } else {
                sendError(exchange, 405, "Метод не поддерживается");
            }
        } catch (Exception e) {
            handleException(exchange, e);
        } finally {
            exchange.close(); // закрываем exchange в блоке finally
        }
    }

    //Обработка GET-запроса для получения истории задач
    private void handleGet(HttpExchange exchange) throws IOException {
        List<Task> history = manager.getHistory();
        sendResponse(exchange, gson.toJson(history), 200);
    }
}