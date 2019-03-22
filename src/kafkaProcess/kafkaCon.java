/**
 * kafka process , copy kafka data to hadoop hdfs 
 * because of two system has some conflict (in log class), I had to build these program.
 * I will combine two system in other method later(gxx, 2018-10-11)
 *
 **/
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

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import common_java.common_global_variant;
import common_java.common_java;
import common_java.common_sharemem;
import java.io.*;

public class kafkaCon {
  public static common_sharemem instance = null;
  private static MappedByteBuffer [] blocks = null;
  private static int blockCount = 0;
  private static int fileSize = 0;
  public static void main(String[] args)throws IOException{
    if (args.length > 0)
      instance = common_sharemem.getInstance(args[0]);
    else
      System.out.println("[gxx warning] pls input config file name ,command like : java sharemem_unit_creator /mydata/sharemem.conf"); 
    initialShm(); //初始化共享内存空间
    
    Properties props = new Properties();
    props.put("bootstrap.servers", instance.getConfigAttributeString("kafka_property","bootstrap.servers") );
    props.put("group.id", instance.getConfigAttributeString("kafka_property","group.id"));
    props.put("enable.auto.commit", instance.getConfigAttributeString("kafka_property","enable.auto.commit"));
    //props.put("enable.auto.commit", "false");
    props.put("auto.commit.interval.ms", instance.getConfigAttributeString("kafka_property","auto.commit.interval.ms"));
    props.put("session.timeout.ms", instance.getConfigAttributeString("kafka_property","session.timeout.ms"));
    props.put("key.deserializer",instance.getConfigAttributeString("kafka_property","key.deserializer"));
    props.put("value.deserializer",instance.getConfigAttributeString("kafka_property","value.deserializer"));
    Consumer<String,String> consu = new KafkaConsumer<String,String>(props);
    Collection<String> topics = Arrays.asList("MetisTest");
    consu.subscribe(topics);
    ConsumerRecords<String,String>consumerRecords = null;
    common_java cj = new common_java();
    common_java.debugProperties dp = cj.new debugProperties();
    dp.setJumpNum(30);
    
    while(true){
      consumerRecords = consu.poll(100);
      for(ConsumerRecord<String, String> consumerRecord : consumerRecords){
        String value = consumerRecord.value();
        //System.out.println("get word : " + value);        
        int result = writeToShmHead(value);
        common_java.debugPrintln("first print, after write to shmhead , result is : " + result, dp, value.length());
        while ( result != common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_SUCCESS){
          try{
            if (result == common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_WAIT_READING){
              Thread.sleep(1); //等待读,这里的确需要等
            }
            else if (result == common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_WAIT_FLUSH)//等待强制写
              forceWriteOver();
            else if (result == common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_FATAL_ERROR ||
                    result == common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_INPUT_ERROR){
              System.out.println("Write to shm error , fatal error or input error");
              break;
            }
            result = writeToShmHead(value);
          } catch (Exception e){
            e.printStackTrace();
          }
        }
      }
    }
  }

  private static void forceWriteOver(){
    try{
      for (int i = 0; i < blockCount; ++i){
        byte mode = blocks[i].get(0);
        if (mode == common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WRITE_CONTINUE){
          blocks[i].put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_AFTER_WRITE);
          return;
        }
      }
    } catch(Exception e){
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
    System.out.println ("Create share memory  ... success! fileSize[" + fileSize + "]");
    return true;
  }


  //打开文件开始写入，与追加写入的程序略微不同
  private static int writeToShmHead(String value){
    if (value.length() >fileSize - 2) {
      System.out.print("String save to share mem fail, Too many words or Too little memory capcity");
      System.out.println(value); //一次性传入了一大堆数据，超过了共享内存块的极限，为什么这样？后续添加到日志管理内      
      return common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_INPUT_ERROR; 
    }
    boolean [] results = new boolean[common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_FATAL_ERROR];
    for (int i = 0; i < results.length; ++i)
      results[i] = false;
    for(int j = 0; j < blockCount; ++j){
      if (blocks[j] != null){
        byte mode = blocks[j].get(0);
        if (mode == (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_AFTER_WRITE
              || mode == (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_READING){
              //该内存块目前刚刚写完，正在或尚未开始读，暂时不能写
          results[common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_WAIT_READING] = true; //至少有一个内存块在等待读进程处理
        } 
        else if (mode == (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WRITING){
          results[common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_FATAL_ERROR] = true; //至少有一个内存块出现了未知的严重损坏情况
        }
       
        else if (mode == (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WRITE_CONTINUE && blocks[j].position() + value.length() > blocks[j].limit()) {
          results[common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_WAIT_FLUSH] = true; //至少有一个内存块可以强制写完（可以被设置为AFTER WRITE）
        }

        else if (mode == (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_SLEEP ||
                 mode == (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WAIT){
          blocks[j].rewind();
        }

        if (mode == (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_SLEEP ||
            mode == (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WAIT ||
            (mode == (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WRITE_CONTINUE && blocks[j].position() + value.length() <= blocks[j].limit())){
          blocks[j].put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WRITING);
          blocks[j].put(value.getBytes());
          if (blocks[j].position() == blocks[j].limit()) //文件正好写到末尾，概率不高吧        
            blocks[j].put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_AFTER_WRITE);
          else 
            blocks[j].put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WRITE_CONTINUE); //buf未写到末尾，可以继续写
          //System.out.println("write success : " + value + " in file : " + j);
 
          return common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_SUCCESS;
        }
      }
      else 
        results[common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_FATAL_ERROR] = true;
    }


    //如果代码走到这里，说明输入文字未能写入任何一个内存块，具体原因可能是内存块都满了，或者内存设置的有问题，或者传入语句过大,需要综合之前for语句得到的结果数组来分析
    //规则是，1 只要成功写入，就不会到这里
    //2 只要有一个block等待读，那最优先是返回等待读， 
    //3 没有等待读，但是至少有一个block目前写过，剩余空间不足够装下当前string，可以强制其写完（after write)
    //4 没有等待读也没有等待强制写，但是有输入错误，则返回输入错误，实际上一开始就比较了输入参数长度，input error初始阶段就return了
    //5 都没有，那就是严重系统错误fatal error， 返回系统错误
    if (results[common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_WAIT_READING])
      return common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_WAIT_READING;
    else if (results[common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_WAIT_FLUSH])
      return common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_WAIT_FLUSH;
    else if (results[common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_FATAL_ERROR])
      return common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_FATAL_ERROR;
    else
      return common_global_variant.GLOB_INT_MEMSHARE_WRITE_STATUS_FATAL_ERROR;
  }

  
}


