package main.manager.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.manager.exceptions.ManagerIOException;
import main.manager.exceptions.NotFoundException;
import main.manager.exceptions.ValidationException;
import main.model.ErrorResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

abstract class BaseHttpHandler implements HttpHandler {

    protected final Gson gson;

    public BaseHttpHandler(Gson gson) {
        this.gson = gson;
    }

    protected void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        // Set Content-Type if not already set
        if (!exchange.getResponseHeaders().containsKey("Content-Type")) {
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        }

        // Set content length for the response
        long contentLength = (statusCode == 204 || responseBytes.length == 0) ? -1 : responseBytes.length;

        exchange.sendResponseHeaders(statusCode, contentLength);

        // Write response if there's content
        if (contentLength > 0) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }

        exchange.close();
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        try (var inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "{\"error\":\"Not Found\"}", 404);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "{\"error\":\"Conflict\"}", 406);
    }

    protected void handleException(HttpExchange exchange, Exception e) throws IOException {
        e.printStackTrace();
        try {
            // Check and send appropriate error response based on exception type
            if (!exchange.getResponseHeaders().containsKey("Content-Type")) {
                if (e instanceof ManagerIOException) {
                    sendError(exchange, 500, "Внутренняя ошибка сервера: " + e.getMessage());
                } else if (e instanceof NotFoundException) {
                    sendNotFound(exchange);
                } else if (e instanceof ValidationException) {
                    sendHasInteractions(exchange);
                } else if (e instanceof JsonSyntaxException) {
                    sendError(exchange, 400, "Получен некорректный JSON: " + e.getMessage());
                } else {
                    sendError(exchange, 500, "Произошла непредвиденная ошибка: " + e.getMessage());
                }
            } else {
                System.err.println("Ошибка обработки запроса после отправки заголовков: " + e.getMessage());
            }
        } catch (Exception innerException) {
            innerException.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    protected void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        if (exchange.getResponseHeaders().containsKey("Content-Type")) {
            return;
        }
        ErrorResponse errorResponse = new ErrorResponse(message);
        String responseJson = gson.toJson(errorResponse);
        sendResponse(exchange, responseJson, statusCode);
    }
}