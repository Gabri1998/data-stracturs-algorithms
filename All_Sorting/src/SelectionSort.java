public class SelectionSort {

    public SelectionSort() {
    }

    public static <V extends Comparable<? super V>>void sort(V[]array){
V minValue;
int minIndex;
        for (int i=0;i< array.length-1;i++) {
            minValue = array[i];
            minIndex = i;

            for (int j=i+1;j< array.length;j++){

               int compare= minValue.compareTo(array[j]);
                if (compare>0){
                    minValue=array[j];
                    minIndex=j;

                }



            }
            if (minIndex!=i){
                V temp=array[i];
                array[i]= minValue;
                array[minIndex]=temp;
            }





        }




    }

    public static void main(String[] args) {


        Integer[] integerTestArray = {4,2,6,3,9,5,10};
        String[] stringTestArray = {"cat", "cat", "cat", "dog", "turtle", "turtle"};

        SelectionSort.sort(integerTestArray);
        for (int i=0;i< integerTestArray.length;i++) {
            System.out.print(integerTestArray[i]+" ");
        }
    }

}
