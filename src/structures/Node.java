/**
 * A custom linked list node that allows for
 * It contains the node's name and the node's weight.
 */
package structures;

import entities.Order;

// Used in conjunction with LinkedList class to create a doubly linked list
public class Node {
    private final Order val;
    private Node next;
    private Node prev;

    public Node(Order val) {
        this.val = val;
        this.next = null;
        this.prev = null;
    }

    public Order getVal() {return val;}
    public Node getNext() {return next;}
    public Node getPrev() {return prev;}

    public void setNext(Node next) {this.next = next;}
    public void setPrev(Node prev) {this.prev = prev;}
}
