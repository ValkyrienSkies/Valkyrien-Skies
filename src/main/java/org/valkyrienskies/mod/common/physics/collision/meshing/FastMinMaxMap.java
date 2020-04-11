package org.valkyrienskies.mod.common.physics.collision.meshing;

import lombok.Getter;

/**
 * This is effectively a Map<Integer, Integer> with all the entries stored as "Nodes" in a LinkedList.
 * To get O(1) runtime, these LinkedList nodes are stored in an array.
 *
 * Unlike a Map<Integer, Integer>, we are only allowed to increment/decrement the value of a given key. Values below 0
 * are not allowed.
 *
 * We also cannot directly view the value of a key, we can only get the minimum and maximum keys that have non-zero
 * values.
 */
public class FastMinMaxMap {

    /**
     * The "Node struct" is defined as follows:
     * struct Node {
     *     unsigned int value;
     *     Node* prev, next;
     * }
     *
     * However, since this is Java not C we emulate this behavior as 3 integers in an int[] array.
     */
    private final int[] backing;
    private final int capacity;
    @Getter
    private int front, back;
    @Getter
    private int size;

    /**
     * @param capacity The capacity of this map.
     */
    public FastMinMaxMap(int capacity) {
        this.backing = new int[capacity * 3];
        this.capacity = capacity;
        this.front = -1;
        this.back = -1;
        this.size = 0;
        clear();
    }

    public void increment(int key) throws IllegalArgumentException {
        ensureCapacity(key);
        int curValue = getValue(key);
        // Update the pointers
        if (size == 0) {
            front = back = key;
            setPrev(key, -1);
            setNext(key, -1);
        } else if (key < front) {
            if (curValue != 0) {
                throw new IllegalStateException("How did we get here?");
            }
            setPrev(front, key);
            setNext(key, front);
            front = key;
        } else if (key > back) {
            if (curValue != 0) {
                throw new IllegalStateException("How did we get here?");
            }
            setNext(back, key);
            setPrev(key, back);
            back = key;
        }
        // Update the value
        setValue(key, curValue + 1);
        size++;
    }

    public void decrement(int key) throws IllegalArgumentException {
        ensureCapacity(key);
        if (size <= 0) {
            throw new IllegalArgumentException("Cannot decrement when list is empty");
        }
        int curValue = getValue(key);
        if (curValue <= 0) {
            throw new IllegalArgumentException("Cannot store negative values");
        } else if (curValue == 1) {
            // Update pointers
            if (size == 1) {
                // This is now empty, make the pointers correct
                setNext(key, -1);
                setPrev(key, -1);
                front = -1;
                back = -1;
            } else if (key == front) {
                int frontNext = getNext(front);
                setNext(front, -1);
                setPrev(frontNext, -1);
                front = frontNext;
            } else if (key == back) {
                int backPrev = getPrev(back);
                setPrev(back, -1);
                setNext(backPrev, -1);
                back = backPrev;
            } else {
                // Generic case
                int prevPtr = getPrev(key);
                int nextPtr = getNext(key);
                setNext(prevPtr, nextPtr);
                setPrev(nextPtr, prevPtr);
            }
            setValue(key, curValue - 1);
        } else {
            // Only need to update the value
            setValue(key, curValue - 1);
        }
        size--;
    }

    private void setValue(int key, int value) {
        backing[key * 3] = value;
    }

    private void setPrev(int key, int prev) {
        backing[key * 3 + 1] = prev;
    }

    private void setNext(int key, int next) {
        backing[key * 3 + 2] = next;
    }

    private int getValue(int key) {
        return backing[key * 3];
    }

    private int getPrev(int key) {
        return backing[key * 3 + 1];
    }

    private int getNext(int key) {
        return backing[key * 3 + 2];
    }

    private void ensureCapacity(int key) {
        if (key < 0 || key > capacity) {
            throw new IllegalArgumentException("Cannot store key of value " + key);
        }
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            setValue(i, 0);
            setPrev(i, -1);
            setNext(i, -1);
        }
    }
}
