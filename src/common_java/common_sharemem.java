package common_java;

import java.lang.Thread;
import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import sun.nio.ch.FileChannelImpl;

import common_java.*;

public class common_sharemem{
  private static common_sharemem _instance = new common_sharemem();
  private static String configFile = "";
  private static RandomAccessFile[] lockFiles = null;
  private static RandomAccessFile[] memFiles = null;
  private static FileChannel[] lockChannels = null;
  private static FileChannel[] memChannels = null;
  private static boolean memshareInitialed = false; //共享内存成功创建则为true
  public static common_sharemem getInstance(String fileName){
    _instance.configFile = fileName;
    return _instance;  
  }

  private common_sharemem(){
    try {
      

    } catch (Exception e) {
      e.printStackTrace();      
    }


  }

  private static void abortInitial(){
    memshareInitialed = false;
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  //共享内存相关功能函数
  public static String getConfigAttributeString(String nodeName,String attributeName){
    try{
      String result = common_java.getAttributeByElem(_instance.configFile, nodeName, attributeName); 
      System.out.println(result);
      return result;
    }catch(Exception e){
      e.printStackTrace();
      return null;


    }

  }

  public static int blockCount(){
    try{
      return common_java.StrToInt_safe(common_java.getAttributeByElem(_instance.configFile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT,
                                       common_global_variant.GLOB_STRING_MEMSHARE_ATTRIBUTE_BLOCKCOUNT), 0);
    }catch(Exception e){
      e.printStackTrace();
      return -1;
    }

  }

  public static int fileSize(){
    try{
      return common_java.StrToInt_safe(common_java.getAttributeByElem(_instance.configFile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT, 
                                       common_global_variant.GLOB_STRING_MEMSHARE_FILE_CAPCITY), 0);
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
  }

  public static FileLock getFileLock(int index){
    try{
      int block_num = common_java.StrToInt_safe(common_java.getAttributeByElem(_instance.configFile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT,
                                                        common_global_variant.GLOB_STRING_MEMSHARE_ATTRIBUTE_BLOCKCOUNT), 0);
      String prefix = common_java.getAttributeByElem(_instance.configFile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT, 
                                                      common_global_variant.GLOB_STRING_MEMSHARE_FILE_PREFIX_ATTRIBUTE);


      if (lockFiles == null){
        lockFiles = new RandomAccessFile[block_num];
        lockChannels = new FileChannel[block_num];
        for(int i = 0; i< block_num; ++i){
          lockFiles[i] = new RandomAccessFile(prefix + i + ".lock","rw");
          lockChannels[i] = lockFiles[i].getChannel();
        }
      }


      FileLock flock = lockChannels[index].tryLock();
      return flock;
    } catch(Exception e){
      return null;
    }
  }

  //创建一组共享内存映射，组内映射文件的数量，名称及容量由配置文件决定,并且程序会返回可以使用的映射文件文件名
  //默认模式下（目前只有默认模式，文件组的特征是文件名最后一位是index序号，如果一组文件内有任意一份未能正确闯将将会导致报错并抛出异常）
  public MappedByteBuffer[] createWriteSharemem(){
    MappedByteBuffer [] result = null;
    try {
      int block_num = 0;
      //1. load share mem config
      block_num = common_java.StrToInt_safe(common_java.getAttributeByElem(_instance.configFile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT,
                                                        common_global_variant.GLOB_STRING_MEMSHARE_ATTRIBUTE_BLOCKCOUNT), 0);
      //2. create share mem
      
      if (block_num > 0 && block_num < common_global_variant.GLOB_INT_MEMSHARE_BLOCKCOUNT_MAX){
        result = new MappedByteBuffer[block_num];
      }
      else{
        abortInitial();
        return null;
      }
      
      
      //3. initial (if new cache file, if old file with data and lock,
      //   some complex data.
      String prefix = common_java.getAttributeByElem(_instance.configFile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT, 
                                                      common_global_variant.GLOB_STRING_MEMSHARE_FILE_PREFIX_ATTRIBUTE);

      int fileSize = common_java.StrToInt_safe(common_java.getAttributeByElem(_instance.configFile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT, 
                                                      common_global_variant.GLOB_STRING_MEMSHARE_FILE_CAPCITY), 0);
      
      if (memChannels == null) memChannels = new FileChannel[block_num];
      if (memFiles == null){
        memFiles = new RandomAccessFile[block_num];
        //这部分目前要求必须成功依次创建共享内存，今后会改进到更加易于使用
        for (int i = 0; i < block_num; ++i){
          memFiles[i] = new RandomAccessFile(prefix + i, "rw");
          memChannels[i] = memFiles[i].getChannel();
          result[i] = memChannels[i].map(MapMode.READ_WRITE, 0L, fileSize);
        }
      }
      return result;      
    } catch (Exception e) {
      e.printStackTrace();      
      return null;
    }
  }




  //hdfs端读取程序用的句柄池,替代之前的方案（之前的方案开关文件导致大量占用文件句柄）
  public static MappedByteBuffer [] getReadProcessBufferPool(String prefix, int count, int size){
    try{
      MappedByteBuffer[] result = new MappedByteBuffer[count];
      if (memFiles == null) memFiles = new RandomAccessFile[count];
      if (memChannels == null) memChannels = new FileChannel[count];
      for(MappedByteBuffer tmp : result){ tmp = null;} //返回结果先设置为null
      for(int i = 0; i < count ; ++i){
        //打开一组文件通道，如果可以的话，就让jvm去负责释放吧
        File tmp = new File(prefix + i);
        if (!tmp.exists()){
          result[i] = null;
        }
        else{
          memFiles[i] = new RandomAccessFile(prefix + i, "rw");
          memChannels[i]  = memFiles[i].getChannel();
          result[i] = memChannels[i].map(MapMode.READ_WRITE, 0L, size);
        }
      }
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
 
  
  //释放内存，关闭文件句柄
  public static void shareMemFree()
  {
    try{
      if (memFiles != null){
        for (RandomAccessFile memFile : memFiles){
          if (memFile != null){
            memFile.close();
            memFile = null;
          }
        }
      }
      memFiles = null;
      if (memChannels != null){
        for (FileChannel memChannel : memChannels){
          if (memChannel != null){
            memChannel.close();
            memChannel = null;
          }
        }
      }
      memChannels = null;

      if (lockFiles != null){
        for (RandomAccessFile lockFile : lockFiles){
          if (lockFile != null){
            lockFile.close();
            lockFile = null;
          }
        }
      }
      lockFiles = null;

      if (lockChannels != null){
        for (FileChannel lockChannel : lockChannels){
          if (lockChannel != null){
            lockChannel.close();
            lockChannel = null;
          }
        }
      }
      lockChannels = null;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  
  ///////////////////////////////////////////////////////////////////////////////////////////////////////




}
