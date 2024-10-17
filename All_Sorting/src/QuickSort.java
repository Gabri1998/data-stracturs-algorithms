public class QuickSort {

    public static <V extends Comparable<? super V>> int partitioning(V[] array, int low, int high) {
        V pivot = array[low];
        int keeper = low;


        while (low < high) {
            while (array[high].compareTo(pivot) > 0)
                high--;

            while (low < high && array[low].compareTo(pivot) <= 0)
                low++;

            if (low < high) {
                V temp = array[low];
                array[low] = array[high];
                array[high] = temp;
            }
        }

        array[keeper] = array[high];
        array[high] = pivot;

        return high;
    }


    public static <V extends Comparable<? super V>>void sort(V[]array,int low,int high ){
        if (low < high) {
            int pos = partitioning(array, low, high);
            sort(array, low, pos - 1);
            sort(array, pos + 1, high);
        }
    }

    public static void main(String[] args) {
        Integer[] array = {4,2,6,3,9,5,10};
        sort(array, 0, array.length - 1);

        System.out.println("Sorted array:");
        for (int value : array) {
            System.out.print(value + " ");
        }
    }
}