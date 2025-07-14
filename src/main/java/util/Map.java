package util;

import java.util.Objects;

public class Map<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private static final int MAXIMUM_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private Node<K, V>[] table; // the array of buckets
    private int size;
    private float loadFactor;
    private int threshold;

    static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    public Map() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public Map(int initialCapacity, float loadFactor) {
        if (initialCapacity <= 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }

    private static int tableSizeFor(int cap) {
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    public V put(K key, V value) {
        return putVal(hash(key), key, value);
    }

    private V putVal(int hash, K key, V value) {
        if (table == null || table.length == 0) {
            table = (Node<K,V>[])new Node[threshold];
            threshold = (int)(table.length * loadFactor);
        }

        int n = table.length;
        int i = (n - 1) & hash;

        for (Node<K,V> node = table[i]; node != null; node = node.next) {
            if (node.hash == hash &&
                    (Objects.equals(key, node.key))) {
                V oldValue = node.value;
                node.value = value;
                return oldValue;
            }
        }

        table[i] = new Node<>(hash, key, value, table[i]);
        if (++size > threshold)
            resize();
        return null;
    }

    public V get(Object key) {
        Node<K,V> node;
        return (node = getNode(hash(key), key)) == null ? null : node.value;
    }

    private Node<K,V> getNode(int hash, Object key) {
        if (table == null || table.length == 0)
            return null;

        int n = table.length;
        Node<K,V> node = table[(n - 1) & hash];

        while (node != null) {
            if (node.hash == hash &&
                    (Objects.equals(key, node.key)))
                return node;
            node = node.next;
        }
        return null;
    }

    private void resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int newCap = oldCap << 1;
        threshold = (int)(newCap * loadFactor);
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;

        if (oldTab != null) {
            for (int j = 0; j < oldCap; j++) {
                Node<K,V> node;
                if ((node = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (node.next == null) {
                        newTab[node.hash & (newCap - 1)] = node;
                    }
                    else {
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;

                        do {
                            next = node.next;
                            if ((node.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = node;
                                else
                                    loTail.next = node;
                                loTail = node;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = node;
                                else
                                    hiTail.next = node;
                                hiTail = node;
                            }
                        } while ((node = next) != null);

                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
    }

    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}