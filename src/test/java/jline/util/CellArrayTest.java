package jline.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellArrayTest {

    @Test
    void testGetSet1(){
        CellArray<String> a = new CellArray<>();
        assertEquals(0, a.getRows());
        assertEquals(0, a.getCols());
        a.set(5, "a");
        assertEquals(1, a.getRows());
        assertEquals(6, a.getCols());
        assertEquals("a", a.get(0, 5));
        assertNull(a.get(0));
    }

    @Test
    void testGetSet2(){
        CellArray<String> a = new CellArray<>();
        assertEquals(0, a.getRows());
        assertEquals(0, a.getCols());
        a.set(5, "a");
        a.set(1, 0, "b");
        assertEquals(2, a.getRows());
        assertEquals(6, a.getCols());
        assertEquals("b", a.get(1));
        assertNull(a.get(0));
    }

    @Test
    void testGetSet3(){
        CellArray<String> a = new CellArray<>();
        assertEquals(0, a.getRows());
        assertEquals(0, a.getCols());
        a.set(5, "a");
        a.set(1, 0, "b");
        a.set(0, 1, "c");
        assertEquals(2, a.getRows());
        assertEquals(6, a.getCols());
        assertEquals("c", a.get(2));
    }

    @Test
    void testZeroLength(){
        CellArray<String> a = new CellArray<>();
        assertEquals(0, a.length());
    }

    @Test
    void testPositiveLength(){
        CellArray<String> a = new CellArray<>();
        assertEquals(0, a.length());
        a.set(5, "a");
        assertEquals(6, a.length());
    }

    @Test
    void testPositiveLength2(){
        CellArray<String> a = new CellArray<>();
        assertEquals(0, a.length());
        a.set(5, "a");
        a.set(7, 0, "b");
        assertEquals(8, a.length());
    }

}