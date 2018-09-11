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
  public static FileLock getFileLock(String preflex){
    try{
      String fileName = preflex + ".lock";
      RandomAccessFile fis = new RandomAccessFile(fileName, "rw");
      FileChannel lockfc = fis.getChannel();
      FileLock flock = lockfc.tryLock();
      return flock;
    } catch(Exception e){
      return null;
    }
  }

  //创建一组共享内存映射，组内映射文件的数量，名称及容量由配置文件决定,并且程序会返回可以使用的映射文件文件名
  //默认模式下（目前只有默认模式，文件组的特征是文件名最后一位是index序号，如果一组文件内有任意一份未能正确闯将将会导致报错并抛出异常）
  public String[] createWriteSharemem(){
    try {
      String [] result = null;
      int block_num = 0;
      //1. load share mem config
      block_num = common_java.StrToInt_safe(common_java.getAttributeByElem(_instance.configFile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT,
                                                        common_global_variant.GLOB_STRING_MEMSHARE_ATTRIBUTE_BLOCKCOUNT), 0);
      System.out.println("what block num : " + block_num);
      //2. create share mem
      if (block_num > 0 && block_num < common_global_variant.GLOB_INT_MEMSHARE_BLOCKCOUNT_MAX){
        result = new String[block_num];
      }
      else{
        abortInitial();
        return null;
      }
      
      
      //3. initial (if new cache file, if old file with data and lock,
      //   should an do some complex data.
      String preflex = common_java.getAttributeByElem(_instance.configFile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT, 
                                                      common_global_variant.GLOB_STRING_MEMSHARE_FILE_PREFLEX_ATTRIBUTE);

      int fileSize = common_java.StrToInt_safe(common_java.getAttributeByElem(_instance.configFile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT, 
                                                      common_global_variant.GLOB_STRING_MEMSHARE_FILE_CAPCITY), 0);
      
      for(int i = 0; i < block_num; ++i){
        //这部分目前要求必须成功依次创建共享内存，今后会改进到更加易于使用
        if (createSharemem(preflex + i, fileSize) == true){
           result[i] = preflex + i;
        }
        else{
          abortInitial();
          result = null;
        }
      }
      return result;      
    } catch (Exception e) {
      e.printStackTrace();      
      return null;
    }


  }

  //创建单个共享内存映射文件
  private static boolean createSharemem(String name, int maxSize){
    RandomAccessFile aFile = null;
    FileChannel inChannel = null;
    FileLock _lock = null;
    boolean result = false;
    try {
      aFile = new RandomAccessFile(name, "rw");
      inChannel = aFile.getChannel();
      MappedByteBuffer buf = inChannel.map(MapMode.READ_WRITE, 0L, maxSize);
      byte mode = buf.get();
      _lock = getFileLock(name);
      if (mode != (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_READING || _lock != null){
        buf.put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WAIT);
        result = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        inChannel.close();
        aFile.close();
        if (_lock != null)
          _lock.release();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return result;
    }
  }
  
  //功能内存块单元
  public static class MemoryBlock{
    public MappedByteBuffer buf;
    public FileLock lock;
  }

  //获取单个可读数据的映射buff,如果未能获取目标区域或者区域被锁定，则返回null
  public static MemoryBlock getReadWriteBuff(String name, int maxSize){
    RandomAccessFile aFile = null;
    FileChannel inChannel = null;
    FileLock _lock = null;
    MemoryBlock result = new MemoryBlock();
    try {
      aFile = new RandomAccessFile(name, "rw");
      inChannel = aFile.getChannel();
      MappedByteBuffer buf = inChannel.map(MapMode.READ_WRITE, 0L, maxSize);
      byte mode = buf.get();
      _lock = getFileLock(name);
      if (mode == (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WAIT || _lock != null){
        buf.rewind();
        result.lock = _lock;
        result.buf = buf;
      }
      else
      {
        if (_lock != null)  _lock.release();
        result = null;
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
      } catch (Exception e) {
        e.printStackTrace();
      }
      return result;
    }   

  }

  

  ///////////////////////////////////////////////////////////////////////////////////////////////////////




}
