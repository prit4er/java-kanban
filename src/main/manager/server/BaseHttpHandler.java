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

        if (!exchange.getResponseHeaders().containsKey("Content-Type")) {
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        }

        long contentLength = (statusCode == 204 || responseBytes.length == 0) ? -1 : responseBytes.length;

        exchange.sendResponseHeaders(statusCode, contentLength);

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

    protected void handleException(HttpExchange exchange, Exception e) throws IOException {
        e.printStackTrace();
        try (exchange) {
            if (!exchange.getResponseHeaders().containsKey("Content-Type")) {
                try {
                    throw e;
                } catch (ManagerIOException managerException) {
                    sendError(exchange, "Внутренняя ошибка сервера: " + managerException.getMessage(), 500);
                } catch (NotFoundException notFoundException) {
                    sendNotFound(exchange);
                } catch (ValidationException validationException) {
                    sendError(exchange, "Ошибка валидации: " + validationException.getMessage(), 422);
                } catch (JsonSyntaxException jsonSyntaxException) {
                    sendError(exchange, "Некорректный JSON: " + jsonSyntaxException.getMessage(), 400);
                } catch (Exception generalException) {
                    sendError(exchange, "Произошла непредвиденная ошибка: " + generalException.getMessage(), 500);
                }
            } else {
                System.err.println("Ошибка обработки запроса после отправки заголовков: " + e.getMessage());
            }
        }
    }

    protected void sendError(HttpExchange exchange, String message, int statusCode) throws IOException {
        if (exchange.getResponseHeaders().containsKey("Content-Type")) {
            return;
        }
        ErrorResponse errorResponse = new ErrorResponse(message);
        String responseJson = gson.toJson(errorResponse);
        sendResponse(exchange, responseJson, statusCode);
    }
}