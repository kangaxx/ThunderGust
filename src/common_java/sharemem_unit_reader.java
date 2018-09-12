import common_java.*;
import common_java.common_java;
import common_java.common_sharemem;
import common_java.common_global_variant;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileLock;

public class sharemem_unit_reader{
  public sharemem_unit_reader(){
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
    int blockCount = common_java.StrToInt_safe(common_java.getAttributeByElem(configfile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT,
                   common_global_variant.GLOB_STRING_MEMSHARE_ATTRIBUTE_BLOCKCOUNT), 0);

    String prefix = common_java.getAttributeByElem(configfile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT,
                                                    common_global_variant.GLOB_STRING_MEMSHARE_FILE_PREFIX_ATTRIBUTE);
    MappedByteBuffer [] buffs = common_sharemem.getReadProcessBufferPool(prefix, blockCount, fileSize);
    if (buffs == null){
      System.out.println("System Error, cannot create share memory buffs!");
      return;
    }

    while(true){
      int index = 0;
      for(MappedByteBuffer buf : buffs){
        try{
          Thread.sleep(1);
        } catch(Exception e) {
          //nothing
        }
        if (buf == null) continue;
        //测试文件
        byte mode = buf.get();
        FileLock lock = common_sharemem.getFileLock(index);
        if (mode == (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WAIT || lock != null){
          buf.put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_READING); //读文件前先设置标志位，以免读到一半被修改
          for(int k = 0 ; k < fileSize; ++k){
            if (k < 10 || k > fileSize - 6){
              byte tmp = buf.get(k);
              System.out.print(tmp);
            }
          }
          System.out.println("");
          System.out.println("file [" + prefix + index + "] end");
          index++;
          buf.put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WAIT);
          try{
            lock.release();
          } catch (Exception e) {
            //do nothing

          }
        }
      }
    }
    
  }
  
}
