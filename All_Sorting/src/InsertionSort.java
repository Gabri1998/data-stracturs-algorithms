public class InsertionSort {


    public static <V extends Comparable<? super V>>void sort(V[]array){


        for (int i=1;i< array.length;i++){
            V temp=array[i];
           int j=i-1;
            int compare= array[j].compareTo(temp);
            while ( j>=0&&compare>0){
                    array[j+1]=array[j];
                   j--;
                    if (j>=0){
                   compare= array[j].compareTo(temp);
                    }
                }
                  array[j+1]=temp;

            }



        }




    public static void main(String[] args) {


        Integer[] integerTestArray = {4,2,6,3,9,5,10};
        String[] stringTestArray = {"cat", "cat", "cat", "dog", "turtle", "turtle"};

         InsertionSort.sort(integerTestArray);
        for (int i=0;i< integerTestArray.length;i++) {
            System.out.print(integerTestArray[i]+" ");
        }
    }

}
