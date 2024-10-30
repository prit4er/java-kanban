package test.httpManagerTest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.manager.server.HttpTaskServer;
import main.manager.task.TaskManager;
import main.manager.task.inMemory.InMemoryTaskManager;
import main.model.Status;
import main.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HttpTaskManagerTasksTest {

    private TaskManager manager = new InMemoryTaskManager();
    private HttpTaskServer taskServer = new HttpTaskServer(manager);
    private Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.clearTasks();
        manager.clearSubtasks();
        manager.clearEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    private HttpResponse<String> sendRequest(String method, URI uri, String body) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                                                        .uri(uri)
                                                        .version(HttpClient.Version.HTTP_1_1);

        switch (method.toUpperCase()) {
            case "POST":
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
                break;
            case "GET":
                requestBuilder.GET();
                break;
            case "DELETE":
                requestBuilder.DELETE();
                break;
            default:
                throw new IllegalArgumentException("Неподдерживаемый метод: " + method);
        }

        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Тест1", "Тест1", 0, Status.NEW,
                             Duration.ofMinutes(45),
                             LocalDateTime.of(2024, 10, 2, 12, 30, 0));

        String taskJson = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpResponse<String> response = sendRequest("POST", url, taskJson);

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Тест1", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldUpdateTaskById() throws IOException, InterruptedException {
        Task task = new Task("Тест1", "Тест1", 0, Status.NEW,
                             Duration.ofMinutes(45),
                             LocalDateTime.of(2024, 10, 2, 12, 30, 0));
        manager.addTask(task);

        Task taskForAddById = new Task("Тест1", "Тест1 - обновленная", 1, Status.IN_PROGRESS,
                                       Duration.ofMinutes(45),
                                       LocalDateTime.of(2024, 10, 2, 12, 30, 0));

        String taskJson = gson.toJson(taskForAddById);
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpResponse<String> response = sendRequest("POST", url, taskJson);

        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Тест1", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldGetTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Тест1", "Тест1", 1, Status.NEW,
                              Duration.ofMinutes(45),
                              LocalDateTime.of(2024, 10, 2, 12, 30, 0));
        manager.addTask(task1);

        Task task2 = new Task("Тест2", "Тест2", 2, Status.NEW,
                              Duration.ofMinutes(45),
                              LocalDateTime.of(2024, 10, 2, 14, 30, 0));
        manager.addTask(task2);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpResponse<String> response = sendRequest("GET", url, null);

        ArrayList<Task> tasks = gson.fromJson(response.body(), new TypeToken<ArrayList<Task>>() {
        }.getType());

        assertEquals(200, response.statusCode());
        assertEquals("Тест1", tasks.get(0).getName(), "Некорректное имя задачи");
        assertEquals("Тест2", tasks.get(1).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Тест1", "Тест1", 1, Status.NEW,
                             Duration.ofMinutes(45),
                             LocalDateTime.of(2024, 10, 2, 12, 30, 0));
        manager.addTask(task);

        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpResponse<String> response = sendRequest("GET", url, null);

        Task responseTask = gson.fromJson(response.body(), Task.class);

        assertEquals(200, response.statusCode());
        assertEquals(task, responseTask, "Задача не получена от сервера.");
    }

    @Test
    public void shouldDeleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Тест1", "Тест1", 0, Status.NEW,
                             Duration.ofMinutes(45),
                             LocalDateTime.of(2024, 10, 2, 12, 30, 0));
        manager.addTask(task);

        assertNotNull(manager.getTask(1), "Задача не добавлена.");

        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpResponse<String> response = sendRequest("DELETE", url, null);

        assertEquals(204, response.statusCode());
        assertNull(manager.getTask(1), "Задача не удалена.");
    }
}