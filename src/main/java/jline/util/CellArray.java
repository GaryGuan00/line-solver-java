package jline.util;

import jline.api.UTIL;

import java.util.HashMap;
import java.util.Objects;

public class CellArray<T> {
    private int rows;
    private int cols;
    private final HashMap<MapKey, T> arr;

    public CellArray() {
        this.rows = 0;
        this.cols = 0;
        this.arr = new HashMap<>();
    }

    public void set(int i, T o){
        if(this.getRows() == 0 && this.getCols() == 0){
            // New cell array
            set(0, i, o);
        } else {
            set(i % this.getRows(), i / this.getRows(), o);
        }
    }

    /**
     * Sets an object in the cell array
     * @param i - the row index
     * @param j - the column index
     * @param o - the object to set
     */
    public void set(int i, int j, T o){
        if(i >= this.rows){
            this.rows = i + 1;
        }
        if(j >= this.cols){
            this.cols = j + 1;
        }
        this.arr.put(new MapKey(i, j), o);
    }

    public T get(int i){
        return this.get(i % this.getRows(), i / this.getRows());
    }

    /**
     * Returns the length of the cell array -- the maximum between the two dimensions
     * @return - the max between the number of rows and the number of columns
     */
    public int length(){
        return (int) UTIL.max(this.rows, this.cols);
    }

    /**
     * Retrieves an object from the cell array
     * @param i - the row index
     * @param j - the column index
     * @return - the object stored at the (i,j) position in the cell array
     */
    public T get(int i, int j){
        return arr.getOrDefault(new MapKey(i, j), null);
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public static class MapKey {

        private final int x;
        private final int y;

        public MapKey(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MapKey)) return false;
            MapKey key = (MapKey) o;
            return x == key.x && y == key.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}
