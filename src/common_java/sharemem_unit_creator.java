import common_java.*;
import common_java.common_java;
import common_java.common_sharemem;
import common_java.common_global_variant;
public class sharemem_unit_creator{
  public sharemem_unit_creator(){
    //wait?
  }
  
  public static void main(String[] args){
    String configfile = "";
    if (args.length > 0)
      configfile = args[0];
    else
      System.out.println("[gxx warning] pls input config file name ,command like : java sharemem_unit_creator /mydata/sharemem.conf");
    common_sharemem instance = common_sharemem.getInstance(configfile);
    int fileSize = common_java.StrToInt_safe(common_java.getAttributeByElem(configfile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT,
                   common_global_variant.GLOB_STRING_MEMSHARE_FILE_CAPCITY), 0);

    String [] blocks = null;
    while( (blocks = instance.createWriteSharemem()) != null){}
    for(int i = 0; i < 1000; ++i){
      for(String block: blocks){
        System.out.println("block name : " + block);
        common_sharemem.MemoryBlock mb = common_sharemem.getReadWriteBuff(block, fileSize);
        if (mb != null){
          mb.buf.put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WRITING);
          for(int k = 0 ; k < fileSize; ++k){
            if (k < fileSize -5)
              mb.buf.put((byte)98);
            else
              mb.buf.put((byte)67);
          }
          mb.buf.put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WAIT);
 
        }
      }
    }

  }
  
}
