package structures;

import entities.Order;

/* Custom linked list implementation
* */
public class LinkedList {
    private Node head;
    private Node tail;
    private int size;

    public LinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public int size() {return size;}
    public boolean isEmpty() {return size == 0;}
    public Node getHead() {return head;}
    public Node getTail() {return tail;}

    public void add(Order order) {
        Node node = new Node(order);
        if (isEmpty()) {
            head = node;
            tail = node;
        } else {
            tail.setNext(node);
            node.setPrev(tail);
            tail = node;
        }
        size++;
    }

    public void remove(Node node) {
        if (node == null) return;
        if (node == head) {
            head = head.getNext();
            if (head != null) head.setPrev(null);
        } else if (node == tail) {
            tail = tail.getPrev();
            if (tail != null) tail.setNext(null);
        } else {
            Node prev = node.getPrev();
            Node next = node.getNext();
            prev.setNext(next);
            next.setPrev(prev);
        }
        size--;
    }

    public Order head() {
        if (isEmpty()) return null;
        return head.getVal();
    }

    public Order tail() {
        if (isEmpty()) return null;
        return tail.getVal();
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

}
