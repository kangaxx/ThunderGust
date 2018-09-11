import common_java.common_java;
import common_java.common_global_variant;

public class common_unit_test{
  public static void main(String [] args){
    System.out.println("input xml file name in args!");
    System.out.println(args[0]);
    String attribute = common_java.getAttributeByElem(args[0], common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT, common_global_variant.GLOB_STRING_MEMSHARE_ATTRIBUTE_BLOCKCOUNT);
    System.out.println("attribute : " + attribute);

  }

}
