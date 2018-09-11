import common_java.*;
import common_java.common_sharemem;
public class sharemem_unit_creator{
  public sharemem_unit_creator(){
    //wait?
  }
  
  public static void main(String[] args){
    String configfile = "";
    if (args.length > 0) configfile = args[0];
    
    common_sharemem instance = common_sharemem.getInstance(configfile);
    
    String [] blocks = instance.createWriteSharemem();
    for(int i = 0; i < 100; ++i){
      for(String block: blocks){
        System.out.println("block name : " + block);
             
      }
    } 
  }
  
}
