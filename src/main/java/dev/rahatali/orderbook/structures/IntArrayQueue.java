package dev.rahatali.orderbook.structures;

import java.util.NoSuchElementException;

public class IntArrayQueue {
    private int[] items;
    private int head;
    private int tail;
    private int count;

    public IntArrayQueue() {
        this(16);
    }

    public IntArrayQueue(int initialCapacity) {
        this.items = new int[initialCapacity];
        this.head = 0;
        this.tail = 0;
        this.count = 0;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public int add(int item) {
        if (count == items.length) {
            resize(items.length * 2);
        }
        items[tail] = item;
        int indexSetIn = tail;
        tail = getIncrementedTail();
        count++;
        return indexSetIn;
    }

    public int poll() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }

        int item = items[head];
        head = (head + 1) % items.length;
        count--;
        return item;
    }

    public int peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return items[head];
    }

    // if queue is full, tail will be set to 0 creating a circular queue
    private int getIncrementedTail() {
        return (tail + 1) % items.length;
    }

    private void resize(int newCapacity) {
        int[] newArray = new int[newCapacity];
        for (int i = 0; i < count; i++) {
            newArray[i] = items[(head + i) % items.length];
        }
        items = newArray;
        head = 0;
        tail = count;
    }

    public int size() {
        return count;
    }
}