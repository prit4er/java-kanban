package main.manager.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import main.manager.Managers;
import main.manager.task.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private final TaskManager manager;
    private final HttpServer server;

    // Конструктор по умолчанию
    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    // Основной конструктор с TaskManager
    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        initContexts(); // инициализация контекстов
    }

    // Инициализация контекстов для эндпоинтов
    private void initContexts() {
        Gson gson = getGson(); // создаём экземпляр Gson
        server.createContext("/tasks", new TaskHandler(manager, gson));
        server.createContext("/subtasks", new SubtaskHandler(manager, gson));
        server.createContext("/epics", new EpicHandler(manager, gson));
        server.createContext("/history", new HistoryHandler(manager, gson));
        server.createContext("/prioritized", new PrioritizedTaskHandler(manager, gson));
    }

    // Точка входа для запуска сервера
    public static void main(String[] args) {
        try {
            HttpTaskServer taskServer = new HttpTaskServer();
            taskServer.start();
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
        }
    }

    // Запуск сервера
    public void start() {
        System.out.println("Сервер запущен на порту: " + PORT);
        server.start();
    }

    // Остановка сервера с проверкой на инициализацию
    public void stop() {
        if (server != null) {
            System.out.println("Сервер остановлен на порту: " + PORT);
            server.stop(0);
        }
    }

    // Метод для создания настроенного Gson
    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }
}