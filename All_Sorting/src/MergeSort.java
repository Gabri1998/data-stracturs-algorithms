public class MergeSort {


    public static void merging(int[] arrayA, int []arrayB, int []arrayC){

        int i=0;
        int j=0;
        int k=0;
        while (i < arrayA.length && j < arrayB.length){

            if(arrayA[i]<arrayB[j]){
                arrayC[k]=arrayA[i];
                i++;
                k++;

            } else {
                arrayC[k]=arrayB[j];
                j++;
                k++;

            }


        }

        if(i==arrayA.length){
            while (j<arrayB.length) {
                arrayC[k] = arrayB[j];
                j++;
                k++;
            }
        } else if (j==arrayB.length) {
            while (i<arrayA.length) {
                arrayC[k] = arrayA[i];
                i++;
                k++;
            }

        }



    }

    public static int[] sorting(int[] array){

        int mid=array.length/2;
        int end = array.length;
        if(array.length<=1){

            return array;
        }else{

            int []left= new int[mid];
            int[]right=new int[end-mid];
            System.arraycopy(array,0,left,0,mid);
            System.arraycopy(array,mid,right,0,(end-mid));
            left = sorting(left);
            right = sorting(right);
            sorting(left);
            sorting(right);
            merging(left,right,array);


        }

        return  array;
    }




    public static void main(String[] args) {

        int[] array = {12, 23, 4, 54, 6, 4, 9,6, 7, 5};

        sorting(array);
        for (int i = 0; i < array.length-1; i++) {

            System.out.print(array[i]+ " ");
        }


    }





}