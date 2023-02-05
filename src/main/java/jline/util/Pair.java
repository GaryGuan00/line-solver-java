package jline.util;

import java.io.Serializable;

public class Pair<T, U> implements Comparable<Pair<T,U>>, Serializable {
    private T left;
    private U right;

    public Pair(T left, U right) {
        this.left = left;
        this.right = right;
    }

    public T getLeft() {
        return this.left;
    }

    public U getRight() {
        return this.right;
    }

    public void setLeft(T left) {
        this.left = left;
    }

    public void setRight(U right) {
        this.right = right;
    }

    @Override
    public int compareTo(Pair<T,U> other) {
        if (this.left instanceof Comparable) {
            return ((Comparable)this.left).compareTo(other.getLeft());
        } else if (this.right instanceof Comparable) {
            return ((Comparable)this.right).compareTo(other.getRight());
        }

        return 0;
    }
}
