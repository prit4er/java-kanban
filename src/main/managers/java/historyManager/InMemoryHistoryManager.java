package main.managers.java.historyManager;

import main.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int HISTORY_LIMIT = 10; // Ограничение на 10 задач в истории

    // Узел двусвязного списка
    private static class Node {

        Task task;
        Node prev;
        Node next;

        public Node(Task task) {
            this.task = task;
        }
    }

    // Хеш-таблица для быстрого доступа к узлам по ID задачи
    private final Map<Integer, Node> nodeMap = new HashMap<>();
    // Головной и хвостовой узлы двусвязного списка
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        // Если задача уже есть в истории, удалим её, чтобы добавить заново
        if (nodeMap.containsKey(task.getId())) {
            remove(task.getId());
        }
        // Добавляем задачу в конец двусвязного списка
        linkLast(task);

        // Если количество задач превысило лимит, удаляем самую старую (первую) задачу
        if (nodeMap.size() > HISTORY_LIMIT) {
            removeFirst();
        }
    }

    @Override
    public void remove(int id) {
        Node nodeToRemove = nodeMap.get(id);
        if (nodeToRemove != null) {
            removeNode(nodeToRemove); // Удаляем узел из списка
            nodeMap.remove(id);       // Удаляем его из HashMap
        }
    }


    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }

    // Добавляем задачу в конец списка
    private void linkLast(Task task) {
        Node newNode = new Node(task);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        nodeMap.put(task.getId(), newNode); // Добавляем в HashMap
    }

    // Удаляем узел из двусвязного списка
    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next; // Если удаляемый узел был первым
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev; // Если удаляемый узел был последним
        }
    }

    // Удаляем первый узел (самую старую задачу)
    private void removeFirst() {
        if (head != null) {
            removeNode(head);
            nodeMap.remove(head.task.getId()); // Удаляем из HashMap
        }
    }
}