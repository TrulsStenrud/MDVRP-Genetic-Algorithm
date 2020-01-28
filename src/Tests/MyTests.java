package Tests;
import Stuff.GA;
import Stuff.Reproduction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MyTests {


    @Test
    public void firstCrossoverTest(){
        int[] parent1 = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] parent2 = new int[]{9, 3, 7, 8, 2, 6, 5, 1, 4};

        int[] actual = Reproduction.innerMappedCrossover(parent1, parent2, 3, 6);

        int[] expected = new int[]{9, 3, 2, 4, 5, 6, 7, 1, 8};

        Assertions.assertArrayEquals(expected, actual);
    }

    @Test
    public void inverseSubstring(){
        int[] actual = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};

        GA.inverseSubstring(actual, 3, 6);

        int[] expected = new int[]{1, 2, 3, 7, 6, 5, 4, 8, 9};

        Assertions.assertArrayEquals(expected, actual);
    }

    @Test
    public void a(){
        var a = new int[]{1, 2, 3, 4, 5};
        var b = Arrays.asList(a);

        a[3] = 100;

        Assertions.assertNotEquals(a[3], b.get(3));
    }
}
