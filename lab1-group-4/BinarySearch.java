/**
 * Different versions of binary search.
 * All search functions share the following description.

 * Arguments:
 * - The first argument `array` is a sorted array of comparable items.
 * - The second argument `value` is the value to search for.

 * All arguments and array values are not null (no need to check).

 * Complexity requirement:
 * The function makes O(log(n)) comparisons where n is the length of `array`.
 */
public class BinarySearch {

    /**
     * Check if the array contains the given value.
     * Iterative version.
     *
     * @return `true` if `value` is in `array`, otherwise `false` (duh!).
     */
    public static<V extends Comparable<? super V>> boolean containsIterative(V[] array, V searchValue) {

            int start=0 ;
            int end=array.length-1;
            int mid;
            V target= searchValue;

            while (start<=end){
                mid= (start+end)/2;

                int compare = array[mid].compareTo(target);

                if (compare>0){
                    end=mid-1;


                } else if (compare<0) {
                    start=mid+1;


                }else{

                    return true;

                }

            }


            return false;
    }

    /**
     * Check if the array contains the given value.
     * Recursive version.
     */
    public static<V extends Comparable<? super V>> boolean containsRecursive(V[] array, V value) {

            return  helper(0, array, array.length - 1, value);


    }



    /**
     *
     * The helper function
     **/

    public static  <V extends Comparable<? super V>> boolean helper(int start,V[] array,int end, V value){


        if(end<start||array.length==0){

            return false;
        }
        else{

            int mid= (start+end)/2;
            V target=value;

            int compare = array[mid].compareTo(target);
            if(end==start&&compare!=0){
                return false;
            }else if (compare>0){
                end=mid-1;

                return helper(start,array,end,target);
            } else if (compare<0) {
                start=mid+1;

                return helper(start,array,end,target);
            }else{

                return true;

            }
        }

    }




    /**
     * Search for the *first* position in the array that matches the given value.
     *
     * @return the smallest index whose array element matches `value`, or -1 if no such index exists.
     */

    public static <V extends Comparable<? super V>> int firstIndexOf(V[] array, V value) {
        int start = 0;
        int end = array.length - 1;

        while (start <end) {
            int mid = (start + end) / 2;
            int compare = array[mid].compareTo(value);

            if (compare >= 0) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }

            if (compare == 0) {
                end = mid;
            }
        }

        if (end >= 0 && array[end].compareTo(value)==0) {
            return end;
        }

        return -1;
    }

    public static <V extends Comparable<? super V>> int indexHelper(int start, V[] array, int mid, V value) {
        while (start < mid) {
            int midOfMid = (start + mid) / 2;
            int compare = array[midOfMid].compareTo(value);

            if (compare == 0) {
                mid = midOfMid;  // Continue searching in the left half
            } else {
                start = midOfMid + 1;
            }
        }

        return mid;
    }


    // Put your own tests here.
    public static void main(String[] args) {
        Integer[] integerTestArray = {1, 3, 5, 7, 9};
        String[] stringTestArray = {"cat", "cat", "cat", "dog", "turtle", "turtle"};

        // Testing using assertions.
        // Remember to run with assertion checks turned on: `java -ea BinarySearch`
        assert containsIterative(integerTestArray, 4) == false;
        assert containsIterative(integerTestArray, 7) == true;

        assert containsRecursive(integerTestArray, 0) == false;
        assert containsRecursive(integerTestArray, 9) == true;

        assert firstIndexOf(stringTestArray, "cat") == 0;
        assert firstIndexOf(stringTestArray, "dog") == 3;
        assert firstIndexOf(stringTestArray, "turtle") == 4;
        assert firstIndexOf(stringTestArray, "zebra") == -1;

        // Or you can use printing.
        //System.out.println("Is 5 in the array? " + containsIterative(integerTestArray, 5));
        //System.out.println("Is 2 in the array? " + containsRecursive(integerTestArray, 2));
        System.out.println("First index of 'turtle': " + firstIndexOf(stringTestArray, "turtle"));
    }

}

