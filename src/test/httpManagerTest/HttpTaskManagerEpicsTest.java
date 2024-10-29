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

public class HttpTaskManagerEpicsTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    public HttpTaskManagerEpicsTest() throws IOException {
        this.manager = new InMemoryTaskManager();
        this.taskServer = new HttpTaskServer(manager);
        this.gson = HttpTaskServer.getGson();
        this.client = HttpClient.newHttpClient();
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

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            System.err.println("Ошибка при выполнении запроса: " + response.body());
        }
        return response;
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Организовать путешествие", 0, Status.NEW);

        String epicJson = gson.toJson(epic);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .build();

        HttpResponse<String> response = sendRequest(request);
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getAllEpics();
        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Эпик 1", epicsFromManager.get(0).getName(), "Некорректное имя эпика");
    }

    @Test
    public void shouldAddEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Организовать путешествие", 0, Status.NEW);
        manager.addEpic(epic); // Получаем добавленный эпик

        Epic epicForUpdate = new Epic("Эпик обновленный", "Организовать путешествие", epic.getId(), Status.IN_PROGRESS);
        epicForUpdate.setStartTime(LocalDateTime.now());
        epicForUpdate.setEndTime(LocalDateTime.now());
        epicForUpdate.setDuration(Duration.ofMinutes(0));

        String epicJson = gson.toJson(epicForUpdate);
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .build();

        HttpResponse<String> response = sendRequest(request);

        System.out.println(manager.getAllEpics());
        System.out.println(epic.getId());

        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getAllEpics();
        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals("Эпик обновленный", epicsFromManager.get(0).getName(),
                     "Некорректное имя обновленного эпика");
    }

    @Test
    public void shouldGetEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Организовать путешествие", 1, Status.NEW);
        manager.addEpic(epic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .GET()
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .build();

        HttpResponse<String> response = sendRequest(request);
        List<Epic> epics = gson.fromJson(response.body(), new TypeToken<ArrayList<Epic>>() {
        }.getType());

        assertEquals(200, response.statusCode());
        assertEquals("Эпик 1", epics.get(0).getName(), "Некорректное имя эпика");
    }

    @Test
    public void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Организовать путешествие", 1, Status.NEW);
        manager.addEpic(epic);

        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .GET()
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .build();

        HttpResponse<String> response = sendRequest(request);
        Epic responseEpic = gson.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode());
        assertEquals(epic, responseEpic, "Эпик не получен от сервера.");
    }

    @Test
    public void shouldDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Организовать путешествие", 0, Status.NEW);
        manager.addEpic(epic);
        System.out.println("id эпика после добавления: " + epic.getId());

        System.out.println("id эпика " + epic.getId());

        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .DELETE()
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .build();

        System.out.println("Эпики перед удалением: " + manager.getAllEpics());
        System.out.println("id эпика после добавления: " + epic.getId());
        HttpResponse<String> response = sendRequest(request);

        assertEquals(200, response.statusCode());
        assertNull(manager.getEpic(1), "Эпик не удален от сервера.");
    }

    @Test
    public void shouldGetSubtasksIds() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Организовать путешествие", 0, Status.NEW);
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Купить шпатель",
                                       "Выбрать в магазине шпатель и купить",
                                       0, epic.getId(), Status.DONE, Duration.ofMinutes(45),
                                       LocalDateTime.of(2024, 10, 1, 12, 30, 0));
        Subtask subtask2 = new Subtask("Купить краску", "Выбрать краску и купить",
                                       1, epic.getId(), Status.DONE, Duration.ofMinutes(45),
                                       LocalDateTime.of(2024, 10, 1, 13, 30, 0));

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        URI url = URI.create("http://localhost:8080/epics/1/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(url)
                                         .GET()
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .build();

        HttpResponse<String> response = sendRequest(request);
        ArrayList<Subtask> responseSubtasks = gson.fromJson(response.body(), new TypeToken<ArrayList<Subtask>>() {}.getType());
        ArrayList<Integer> responseSubtasksIds = new ArrayList<>();
        for (Subtask subtask : responseSubtasks) {
            responseSubtasksIds.add(subtask.getId());
        }

        assertEquals(200, response.statusCode());
        assertNotNull(responseSubtasksIds, "Id не получены от сервера.");
        assertEquals(1, responseSubtasksIds.get(0), "Id первой подзадачи не совпадает с ожидаемым.");
        assertEquals(2, responseSubtasksIds.get(1), "Id второй подзадачи не совпадает с ожидаемым.");
    }
}