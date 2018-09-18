import common_java.*;
import common_java.common_global_variant;
import common_java.common_java;
import common_java.common_sharemem;
import java.util.Scanner;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

public class shmMonitor{
  public static common_sharemem instance = null;
  private static MappedByteBuffer [] blocks = null;
  private static int blockCount = 0;
  private static int fileSize = 0;
 
  public static void main(String [] args){
    if (args.length > 0)
      instance = common_sharemem.getInstance(args[0]);
    else
      System.out.println("[gxx warning] pls input config file name ,command like : java sharemem_unit_creator /mydata/sharemem.conf");
    if (initialShm()){ //初始化共享内存空间
      System.out.println("pls input monitor command, only read cacha status by (status-all;sa) , edit status by (edit-status; es) and exit by (q) , press (help) for command list,cry");
      Scanner sc = new Scanner(System.in);
      String command = sc.nextLine();
      System.out.println(command + "...");
      while (command.equals("q") == false){
        if (command.equals("status-all")|| command.equals("sa")){
          //显示每个内存块的当前状态
          for(int i = 0 ; i < blockCount; ++i)
            showBlockStatus(i);
        }
        else if (command.equals("edit-status")|| command.equals("es")){
          //修改状态
          System.out.println("pls input block id :");
          int id = sc.nextInt();
          System.out.println("id : [" + id + "]");
          System.out.println("pls input status you want, 0 sleep , 1 writing , 2 reading , 3 after write , 5 write continue , 9 wait");
          int status = sc.nextInt();
          System.out.println("status : [" + status + "] ");
          editBlockStatus(id, status);
        }
        else if (command.equals("ea") || command.equals("edit-all")){ //重置全部block的状态
           System.out.println("pls input status you want, 0 sleep , 1 writing , 2 reading , 3 after write , 5 write continue , 9 wait");
           int status = sc.nextInt();
           for (int id = 0; id < blockCount; ++id){
             editBlockStatus(id, status);
           }
        }
        else if (command.equals("st") || command.equals("showText")){
          System.out.println("pls input block id :");
          int id = sc.nextInt();
          System.out.println("pls input start position : ");
          int begin = sc.nextInt();
          System.out.println("pls input text length , max 20000 : ");
          int length = sc.nextInt();
          if (length < 1) length = 1;
          if (length > 20000) length = 20000;
          System.out.println("show text (id =" + id + ", begin=" + begin + ", length = " + length + ")");
          showText(id , begin, length);
        }
        else if (command.equals("h") || command.equals("help")){
          showHelp();
        }
        System.out.println("pls input monitor command, only read cacha status by (status-all;sa) , edit status by (edit-status; es) and exit by (q) ,cry");
        command = sc.nextLine();
        System.out.println(command + "...");
      }
    }
  }

  private static void showHelp(){
    System.out.println(" \" status-all \" or \" sa \" : show all cache status");
    System.out.println(" \" edit-status \" or \" es \" : edit one cache block status");
    System.out.println(" \" edit-all \" or \"ea \" : edit all cache with same status");
    System.out.println(" \" showText \" or \"st \" : show cache text");
    System.out.println(" \" help \" or \"h \" : help");
  }

  private static void showText(int id , int begin, int length){
    try{
      if (blocks[id] != null){
        blocks[id].rewind();
        byte [] text = new byte[length];
        System.out.println("block[" + id + "], position:" + blocks[id].position() + ", limit:" + blocks[id].limit());
        blocks[id].get(text, begin, length - 1);
        System.out.println(text);
      }
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  private static void showBlockStatus(int num){
    try{
      if (blocks[num] != null){
        switch(blocks[num].get(0)){
        case 0:
          System.out.println("file [" + num + "] status is [STATUS_SLEEP]");break;
        case 1:
          System.out.println("file [" + num + "] status is [STATUS_WRITING]");break;
        case 2: 
          System.out.println("file [" + num + "] status is [READING]");break;
        case 3: 
          System.out.println("file [" + num + "] status is [AFTER_WRITE]");break;
        case 5: 
          System.out.println("file [" + num + "] status is [WRITE_CONTINUE]");break;
        case 9:
          System.out.println("file [" + num + "] status is [WAIT]");break;
        default :
          System.out.println("cache error");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private static void editBlockStatus(int id, int status){
    try{
      if (blocks[id] != null){
        blocks[id].put(0,(byte)status);
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
    System.out.println ("Create share memory  ... success!");
    return true;
  }
  
 

}

