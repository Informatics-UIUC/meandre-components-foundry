package org.seasr.meandre.support.components.discovery.ruleassociation;

import java.util.HashMap;



public class ItemSetTool {

   public static void print(ItemSetInterface itemSet)
   {

      String[] targetNames = itemSet.getTargetNames();
      int size = targetNames.length;
      System.out.println("targetNames (columns) " + size);
      for (int i = 0;i < size; i++ ) {
         System.out.println(i + " " + targetNames[i]);
      }

      System.out.println("");
      HashMap<String, int[]> unique = itemSet.getUnique();
      String[] names = itemSet.getItemsOrderedByFrequency();
      for (int i = 0;i < names.length; i++ ) {
         String key = names[i];
         int[] cnt_and_id = unique.get(key);
         int count = cnt_and_id[0];
         System.out.println(i + " " + key +  "(" + cnt_and_id[1] + ")" + " has freq: " + count);
      }

      assert unique.size() == names.length;
      assert unique.size() == itemSet.getItemsOrderedByFrequency().length;

      int rows = itemSet.getNumExamples();
      int cols = itemSet.getItemsOrderedByFrequency().length;
      for (int i = 0;i < rows; i++) {
         System.out.println("row " + i);
         for (int j = 0; j < cols; j++) {
            if (itemSet.getItemFlag(i,j)) {
               //String colName = itemSet.getItemsInColumnOrder()[j];
               String colName = itemSet.getItemsOrderedByFrequency()[j];
               System.out.print(colName + ",");
            }
         }
         System.out.println("");
      }

   }


}



// DEBUG
//
// Assert numAttributes == targetNames.length
/*
System.out.println("num attributes " + numAttributes);
System.out.println(unique.size() + " vs " + names.length);
for (int i = 0 ; i < targetNames.length; i++) {
   System.out.println(targetNames[i]);
}
System.out.println("NAmes");
for (int i = 0 ; i < names.length; i++) {
   int[] out = (int[]) unique.get(names[i]);
   System.out.println(names[i] + " " + out[0] + " " + out[1]);
}
*/