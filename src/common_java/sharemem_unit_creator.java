import common_java.*;
import common_java.common_java;
import common_java.common_sharemem;
import common_java.common_global_variant;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import sun.nio.ch.FileChannelImpl;

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
    int blockCount = common_java.StrToInt_safe(common_java.getAttributeByElem(configfile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT,
                   common_global_variant.GLOB_STRING_MEMSHARE_ATTRIBUTE_BLOCKCOUNT), 0);
    MappedByteBuffer [] blocks = null;
    while( (blocks = instance.createWriteSharemem()) == null){System.out.print("Try to initial share memory ");}
    System.out.println ("Create share memory  ... success!");
    for(int i = 0; i < 1000; ++i){
      for(int j = 0; j < blockCount; ++j){
        if (blocks[j] != null){
          blocks[j].rewind();
          byte mode = blocks[j].get(0);
          System.out.println("mode is : " + mode);
          FileLock lock = common_sharemem.getFileLock(j);
          if (mode != (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_AFTER_WRITE && 
              mode != (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_READING &&
              mode != (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WRITING){
            blocks[j].put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WRITING);
            for(int k = 0 ; k < fileSize; ++k){
              if (k < fileSize -5)
                blocks[j].put((byte)98);
              else
                blocks[j].put((byte)67);
            }
            blocks[j].put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_AFTER_WRITE);
            System.out.print("write file [" + j + "] finished at: ");
            System.out.print(System.currentTimeMillis());
            System.out.println("");
            try{
              Thread.sleep(1);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
    instance.shareMemFree();
    System.out.println(System.currentTimeMillis());
  }
  
}
