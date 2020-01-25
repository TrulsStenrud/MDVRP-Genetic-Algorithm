package Stuff;

import java.util.Arrays;

public class Reproduction {

    public static int[][] PartiallyMappedCrossover(int[] parentA, int[] parentB){

        if(parentA.length != parentB.length)
        {
            System.out.println("Parents different length");
            return null;
        }

        int temp1 = (int) (Math.random()*parentA.length),
            temp2 = (int) (Math.random()*parentA.length);

        int start = Math.min(temp1, temp2), end = Math.max(temp1, temp2);

        int[] child1 = innerMappedCrossover(parentA, parentB, start, end);
        int[] child2 = innerMappedCrossover(parentB, parentA, start, end);

        return new int[][]{child1, child2};
    }

    // this is only public to allow testing

    public static int[] innerMappedCrossover(int[] parentA, int[] parentB, int start, int end) {

        int[] child = new int[parentA.length];

        Arrays.fill(child, -1); //to not mixup 0 for not being filled

        // copy segment
        for(int i = start; i <= end; i++){
            child[i] = parentA[i];
        }

        for (int i = start; i <= end; i++){

            var value = parentB[i];
            if(!contains(child, value)){

                var valueJ = child[i];
                int indexJ = indexOf(parentB, valueJ);

                while(child[indexJ] != -1){
                    valueJ = child[indexJ];
                    indexJ = indexOf(parentB, valueJ);
                }

                child[indexJ] = value;
            }
        }

        // copy remainder
        for (int i = 0; i < child.length; i++) {
            if(child[i] == -1)
                child[i] = parentB[i];
        }

        return child;
    }

    private static int indexOf(int[] arr, int value){
        for(int i = 0; i < arr.length; i++){
            if(arr[i] == value){
                return i;
            }
        }
        return -1;
    }

    private static boolean contains(int[] child, int value) {
        for (int item : child) {
            if (item == value)
                return true;
        }
        return false;
    }
}
