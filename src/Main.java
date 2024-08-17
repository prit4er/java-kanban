public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создание задач
        Task task1 = new Task("Переезд", "Подготовить вещи к переезду", manager.generateId(), Status.NEW);
        Task task2 = new Task("Уборка", "Сделать уборку в квартире", manager.generateId(), Status.NEW);

        manager.addTask(task1);
        manager.addTask(task2);

        // Создание эпиков с подзадачами
        Epic epic1 = new Epic("Ремонт", "Сделать ремонт в квартире", manager.generateId());
        Subtask subtask1_1 = new Subtask("Покраска стен", "Покрасить стены в комнате", manager.generateId(), epic1.getId(), Status.NEW);
        Subtask subtask1_2 = new Subtask("Замена пола", "Положить новый пол", manager.generateId(), epic1.getId(), Status.NEW);

        manager.addEpic(epic1);
        manager.addSubtask(subtask1_1);
        manager.addSubtask(subtask1_2);

        Epic epic2 = new Epic("Подготовка к экзамену", "Подготовиться к экзамену по Java", manager.generateId());
        Subtask subtask2_1 = new Subtask("Изучение материала", "Прочитать учебные материалы", manager.generateId(), epic2.getId(), Status.NEW);

        manager.addEpic(epic2);
        manager.addSubtask(subtask2_1);

        // Вывод списка всех задач, эпиков и подзадач
        System.out.println("Все задачи:");
        System.out.println(manager.getAllTasks());

        System.out.println("Все эпики:");
        System.out.println(manager.getAllEpics());

        System.out.println("Все подзадачи:");
        System.out.println(manager.getAllSubtasks());

        // Изменение статусов
        task1.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task1);

        subtask1_1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1_1);

        subtask1_2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1_2);

        subtask2_1.setStatus(Status.DONE);
        manager.updateSubtask(subtask2_1);

        // Повторный вывод для проверки обновлений
        System.out.println("Все задачи после изменения статусов:");
        System.out.println(manager.getAllTasks());

        System.out.println("Все эпики после изменения статусов подзадач:");
        System.out.println(manager.getAllEpics());

        System.out.println("Все подзадачи после изменения статусов:");
        System.out.println(manager.getAllSubtasks());

        // Удаление задачи и эпика
        manager.deleteTaskById(task2.getId());
        manager.deleteEpicById(epic1.getId());

        // Вывод списка после удаления
        System.out.println("Все задачи после удаления одной из задач:");
        System.out.println(manager.getAllTasks());

        System.out.println("Все эпики после удаления одного из эпиков:");
        System.out.println(manager.getAllEpics());

        System.out.println("Все подзадачи после удаления эпика:");
        System.out.println(manager.getAllSubtasks());
    }
}
