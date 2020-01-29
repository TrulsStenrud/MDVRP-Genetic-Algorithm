package Tests;
import Stuff.GA;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MyTests {


    @Test
    public void inverseSubstring(){
        var actual = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

        GA.inverseSubstring(actual, 3, 6);

        var expected = Arrays.asList(1, 2, 3, 7, 6, 5, 4, 8, 9);

        Assertions.assertEquals(expected, actual);
    }
}
