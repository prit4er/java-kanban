package test.httpManagerTest;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.manager.server.HttpTaskServer;
import main.manager.task.TaskManager;
import main.manager.task.inMemory.InMemoryTaskManager;
import main.model.Status;
import main.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerHistoryTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;

    public HttpTaskManagerHistoryTest() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager.clearTasks();
        manager.clearSubtasks();
        manager.clearEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void shouldGetHistory() throws IOException, InterruptedException {
        Task task1 = new Task("задача 1", "Собрать и вынести мусор", 1, Status.NEW,
                              Duration.ofMinutes(45),
                              LocalDateTime.of(2024, 10, 2, 12, 30, 0));
        Task task2 = new Task("задача 2", "Приготовить еду", 2, Status.NEW,
                              Duration.ofMinutes(45),
                              LocalDateTime.of(2024, 10, 2, 13, 30, 0));
        Task task3 = new Task("задача 3", "Постирать вещи", 3, Status.NEW,
                              Duration.ofMinutes(45),
                              LocalDateTime.of(2024, 10, 2, 14, 30, 0));

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        // Получаем задачи по ID для создания истории
        manager.getTask(1);
        manager.getTask(2);
        manager.getTask(3);

        // Отправка GET-запроса на получение истории
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .GET()
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Десериализация ответа
        List<Task> responseHistory = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());

        // Проверка кода статуса и истории
        assertEquals(200, response.statusCode());

        assertEquals("задача 1", responseHistory.get(0).getName(), "Некорректное имя задачи");
        assertEquals("задача 2", responseHistory.get(1).getName(), "Некорректное имя задачи");
        assertEquals("задача 3", responseHistory.get(2).getName(), "Некорректное имя задачи");
    }
}
