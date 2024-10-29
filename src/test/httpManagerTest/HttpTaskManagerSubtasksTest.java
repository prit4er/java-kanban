package test.httpManagerTest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.manager.server.HttpTaskServer;
import main.manager.task.TaskManager;
import main.manager.task.inMemory.InMemoryTaskManager;
import main.model.Epic;
import main.model.Status;
import main.model.Subtask;
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

public class HttpTaskManagerSubtasksTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;

    public HttpTaskManagerSubtasksTest() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager.clearTasks();
        manager.clearEpics();
        manager.clearSubtasks();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Организовать путешествие", 1, Status.NEW);
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Купить шпатель",
                                      "Выбрать в магазине шпатель и купить",
                                      1, epic.getId(), Status.NEW, Duration.ofMinutes(45),
                                      LocalDateTime.of(2024, 10, 1, 12, 30, 0));

        String taskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getAllSubtasks();

        assertNotNull(subtasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Купить шпатель", subtasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldAddSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Организовать путешествие", 1, Status.NEW);
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Купить шпатель",
                                      "Выбрать в магазине шпатель и купить",
                                      1, epic.getId(), Status.NEW, Duration.ofMinutes(45),
                                      LocalDateTime.of(2024, 10, 1, 12, 30, 0));
        manager.addSubtask(subtask);

        Subtask updatedSubtask = new Subtask("Обновленная подзадача",
                                             "Выбрать в магазине шпатель и купить",
                                             1, epic.getId(), Status.NEW, Duration.ofMinutes(45),
                                             LocalDateTime.of(2024, 10, 1, 12, 30, 0));

        String subtaskJson = gson.toJson(updatedSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getAllSubtasks();

        assertNotNull(subtasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Обновленная подзадача", subtasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldGetSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Сделать ремонт", "Покрасить стены на балконе", 1, Status.NEW);
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("подзадача 1",
                                       "Выбрать в магазине шпатель и купить",
                                       1, epic.getId(), Status.NEW, Duration.ofMinutes(45),
                                       LocalDateTime.of(2024, 10, 1, 12, 30, 0));
        manager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask("подзадача 2",
                                       "Выбрать в магазине шпатель и купить",
                                       1, epic.getId(), Status.NEW, Duration.ofMinutes(45),
                                       LocalDateTime.of(2024, 10, 1, 13, 30, 0));
        manager.addSubtask(subtask2);



        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .GET()
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .build();

        class SubtaskListTypeToken extends TypeToken<ArrayList<Subtask>> {

        }

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ArrayList<Subtask> subtasks = gson.fromJson(response.body(), new SubtaskListTypeToken().getType());

        assertEquals(200, response.statusCode());
        assertEquals("подзадача 1", subtasks.get(0).getName(), "Некорректное имя задачи");
        assertEquals("подзадача 2", subtasks.get(1).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Сделать ремонт", "Покрасить стены на балконе", 1, Status.NEW);
        manager.addEpic(epic);

        Subtask subtask = new Subtask("подзадача 1",
                                      "Выбрать в магазине шпатель и купить",
                                      1, epic.getId(), Status.DONE, Duration.ofMinutes(45),
                                      LocalDateTime.of(2024, 10, 1, 12, 30, 0));
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .GET()
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Subtask responseSubtask = gson.fromJson(response.body(), Subtask.class);

        assertEquals(200, response.statusCode());
        assertEquals(subtask, responseSubtask, "Подзадача не получена от сервера.");
    }

    @Test
    public void shouldDeleteSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Сделать ремонт", "Покрасить стены на балконе", 1, Status.NEW);

        manager.addEpic(epic);

        Subtask subtask = new Subtask("подзадача 1",
                                      "Выбрать в магазине шпатель и купить",
                                      1, epic.getId(), Status.DONE, Duration.ofMinutes(45),
                                      LocalDateTime.of(2024, 10, 1, 12, 30, 0));
        manager.addSubtask(subtask);

        assertNotNull(manager.getSubtask(1), "Подзадача не добавлена.");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .DELETE()
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(manager.getSubtask(1), "Подзадача не удалена.");
    }
}