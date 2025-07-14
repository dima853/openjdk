package tree;

import java.util.NoSuchElementException;

public class RedBlackTree<T extends Comparable<T>> {
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private class Node {
        T key;          // Ключ (значение) узла
        Node left;      // Левый потомок
        Node right;     // Правый потомок
        boolean color;  // Цвет узла (RED/BLACK)
        int size;       // Количество узлов в поддереве

        Node(T key, boolean color, int size) {
            this.key = key;
            this.color = color;
            this.size = size;
        }
    }

    private Node root;  // Корень дерева

    // Проверка цвета узла
    private boolean isRed(Node x) {
        if (x == null) return false;  // Листья (NIL) считаются чёрными
        return x.color == RED;
    }

    // Размер поддерева
    private int size(Node x) {
        if (x == null) return 0;
        return x.size;
    }

    public int size() {
        return size(root);
    }

    public boolean isEmpty() {
        return root == null;
    }

    public boolean contains(T key) {
        return get(key) != null;
    }

    public T get(T key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        return get(root, key);
    }

    private T get(Node x, T key) {
        while (x != null) {
            int cmp = key.compareTo(x.key);
            if (cmp < 0) x = x.left;    // Ищем в левом поддереве
            else if (cmp > 0) x = x.right; // Ищем в правом поддереве
            else return x.key;           // Нашли
        }
        return null;  // Не нашли
    }

    public void insert(T key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        root = insert(root, key);
        root.color = BLACK;  // Корень всегда чёрный
    }

    private Node insert(Node h, T key) {
        if (h == null) return new Node(key, RED, 1);  // Новые узлы всегда красные

        // Обычная вставка в BST
        int cmp = key.compareTo(h.key);
        if (cmp < 0) h.left = insert(h.left, key);
        else if (cmp > 0) h.right = insert(h.right, key);
        else h.key = key;  // Если ключ уже есть, обновляем значение

        // Балансировка:
        // 1. Если правый ребёнок красный, а левый чёрный - левый поворот
        if (isRed(h.right) && !isRed(h.left)) h = rotateLeft(h);
        // 2. Если два красных узла подряд - правый поворот
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        // 3. Если оба ребёнка красные - смена цветов
        if (isRed(h.left) && isRed(h.right)) flipColors(h);

        h.size = size(h.left) + size(h.right) + 1;  // Обновляем размер
        return h;
    }

    private Node rotateLeft(Node h) {
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = x.left.color;  // Сохраняем цвет родителя
        x.left.color = RED;      // Делаем узел красным
        x.size = h.size;         // Переносим размер
        h.size = size(h.left) + size(h.right) + 1;  // Пересчитываем размер
        return x;
    }

    private Node rotateRight(Node h) {
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = x.right.color;  // Сохраняем цвет родителя
        x.right.color = RED;      // Делаем узел красным
        x.size = h.size;          // Переносим размер
        h.size = size(h.left) + size(h.right) + 1;  // Пересчитываем размер
        return x;
    }

    private void flipColors(Node h) {
        h.color = !h.color;      // Инвертируем цвет узла
        h.left.color = !h.left.color;  // И его детей
        h.right.color = !h.right.color;
    }

    public void delete(T key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (!contains(key)) return;

        // Если оба ребёнка чёрные, делаем корень красным
        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;

        root = delete(root, key);
        if (!isEmpty()) root.color = BLACK;  // Возвращаем корню чёрный цвет
    }

    private Node delete(Node h, T key) {
        if (key.compareTo(h.key) < 0) {  // Ищем ключ в левом поддереве
            if (!isRed(h.left) && !isRed(h.left.left))
                h = moveRedLeft(h);  // Подготовка к удалению
            h.left = delete(h.left, key);
        } else {  // Ищем ключ в правом поддереве или нашли
            if (isRed(h.left))
                h = rotateRight(h);
            if (key.compareTo(h.key) == 0 && (h.right == null))
                return null;  // Удаляем лист
            if (!isRed(h.right) && !isRed(h.right.left))
                h = moveRedRight(h);
            if (key.compareTo(h.key) == 0) {
                Node x = min(h.right);  // Находим минимум в правом поддереве
                h.key = x.key;          // Заменяем ключ
                h.right = deleteMin(h.right);  // Удаляем минимум
            } else {
                h.right = delete(h.right, key);
            }
        }
        return balance(h);  // Балансируем дерево
    }

    private Node moveRedLeft(Node h) {
        flipColors(h);  // Смена цветов
        if (isRed(h.right.left)) {  // Если у правого ребёнка есть красный левый потомок
            h.right = rotateRight(h.right);  // Двойной поворот
            h = rotateLeft(h);
            flipColors(h);
        }
        return h;
    }

    private Node moveRedRight(Node h) {
        flipColors(h);
        if (isRed(h.left.left)) {
            h = rotateRight(h);
            flipColors(h);
        }
        return h;
    }

    private Node balance(Node h) {
        if (isRed(h.right)) h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right)) flipColors(h);

        h.size = size(h.left) + size(h.right) + 1;
        return h;
    }

    public T min() {
        if (isEmpty()) throw new NoSuchElementException("Tree is empty");
        return min(root).key;
    }

    private Node min(Node x) {
        if (x.left == null) return x;  // Самый левый элемент
        else return min(x.left);
    }

    public void deleteMin() {
        if (isEmpty()) throw new NoSuchElementException("Tree is empty");

        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;

        root = deleteMin(root);
        if (!isEmpty()) root.color = BLACK;
    }

    private Node deleteMin(Node h) {
        if (h.left == null)  // Нашли минимум
            return null;

        if (!isRed(h.left) && !isRed(h.left.left))
            h = moveRedLeft(h);  // Подготовка к удалению

        h.left = deleteMin(h.left);
        return balance(h);  // Балансировка
    }
}
