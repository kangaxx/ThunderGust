import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import sun.nio.ch.FileChannelImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import common_java.*;
import common_java.common_global_variant;
import common_java.common_java;
import common_java.common_sharemem;

public class hdfsProcess{
  public static common_sharemem instance = null;
  private static MappedByteBuffer [] blocks = null;
  private static int blockCount = 0;
  private static int fileSize = 0;
 
  public static void main(String [] args){
    try { 
      if (args.length > 0)
        instance = common_sharemem.getInstance(args[0]);
      else
        System.out.println("[gxx warning] pls input config file name ,command like : java sharemem_unit_creator /mydata/sharemem.conf");
      initialShm(); //初始化共享内存空间
      while(true){
        readShm();

      }

    } catch (Exception e) {  
      e.printStackTrace();  
    }  

  }

  public static void writeHdfs(byte [] text){
    Configuration conf = new Configuration();
    String hdfs_path = instance.getConfigAttributeString("hadoop_property","hadoop_path");
    String fileName = hdfs_path + System.currentTimeMillis();
    try{
      FileSystem fs = FileSystem.get(URI.create(fileName), conf); 
      Path path = new Path(fileName);  
      FSDataOutputStream outStream = fs.create(path);
    	outStream.write(text);
      outStream.close();
      fs.close();
    }catch(Exception e){
	    e.printStackTrace();
    }

  }
  
  private static boolean initialShm(){
    if (instance == null){
      System.out.println("Share memory failed !");
      return false;
    }
    while( (blocks = instance.createWriteSharemem()) == null){System.out.print("Try to initial share memory ");}
    blockCount = instance.blockCount();
    fileSize = instance.fileSize();
    System.out.println ("Create share memory  ... success!");
    return true;
  }

  //读数据
  private static void  readShm(){
    try{
      for(int i=0; i<blockCount; i++){
        if (blocks[i] != null){
          blocks[i].rewind(); 
          byte mode = blocks[i].get(0);
          if (mode == (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_AFTER_WRITE){
            blocks[i].put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_READING);
            System.out.println("buffer limit is : " + blocks[i].limit() + " and filesize is : " + fileSize);
            byte [] text = new byte[blocks[i].limit()];
            blocks[i].get(text , 1, blocks[i].limit() - 1);
            writeHdfs(text);
            blocks[i].put(0, (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WAIT);
          }
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }



}
