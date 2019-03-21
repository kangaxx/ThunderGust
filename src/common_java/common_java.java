package common_java;

import java.io.File;   
import javax.xml.parsers.DocumentBuilder;   
import javax.xml.parsers.DocumentBuilderFactory;   
import org.w3c.dom.Document;   
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList; 
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.IOException;

public class common_java
{
  public static String HDFS_PROCESS_PROPERTIES = "hdfs-start.properties";

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  //通过xml文件读取配置
  public static String getAttributeByElem(String fileName, String elemName, String attrName){  
    try {
      //创建解析工厂
      DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
      //指定DocumentBuilder
      DocumentBuilder builder = dbfactory.newDocumentBuilder();
      Document doc = builder.parse(new File(fileName));
      //得到Document的根
      Element root = doc.getDocumentElement();
      //获得一级子元素
      Element r = resFindElementByName(root, elemName);
      if (null != r) 
        return r.getAttribute(attrName);
      else
        return "";
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }

  }  
  
  private static Element resFindElementByName(Element elem, String name)
  {
    NodeList l = null; 
    Element result = null;
    try {
      l = elem.getChildNodes();
      if (elem.getTagName().equals(name))
        return elem;
      for (int i = 0; i < l.getLength(); i++){
        if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element)l.item(i);
          result = resFindElementByName(element, name);
          if (null != result){
            return result;
          }
        }
      }
      return result;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }
  ///////////////////////////////////////////////////////////////////////////////////////////////////////

  public static int StrToInt_safe(String input){
    return StrToInt_safe(input, -1);
  }

  public static int StrToInt_safe(String input, int execReturn){
    try{
      return Integer.parseInt(input);
    }catch(Exception e){
      return execReturn;
    }
  }
  
  //为debug println专用的参数类
  public class debugProperties{
    private int printNum = 0;
    private int jumpNum = 1; //算法上，用printNum 对jumpNum取余，这样可以实现每jumpNum次打印一次等loop功能
    private int _int_cache[] = {0, 0, 0};//记录一些cache 值，方便打印与记录

    public debugProperties(){
      printNum = 0;
      jumpNum = 1;
    }

    //记录cache，方便调试时的打印统计功能
    public void setCache(int num){
      setIntCache(num, 0);
    }

    public void setIntCache(int num, int idx){
      _int_cache[idx] = num;
    }

    public void incCache(int num){
      incIntCache(num, 0);
    }

    public void incIntCache(int num, int idx){
      setIntCache(num + _int_cache[idx], idx);
    }

    public int getIntCache(int idx){
      return _int_cache[idx];
    }

    public void printNumInc(){
      printNum ++;
    }
    
    //获取真实的打印次数,如果jumpNum > 1,真实打印次数会小于调用debugPrint的次数
    public int getPrintNum_truth(){
      return printNum / jumpNum;
    }

    public void setPrintNum(int num){
      printNum = num;
    }

    public void setJumpNum(int num){
      if (num > 0)
        jumpNum = num;
      else{
        System.out.println("System debug warning ,jump num must > 0 ,otherwise , it will be set to 1");
        jumpNum = 1; //maybe warning
      }
    }
    
    //针对printnum取余，为0时则表示打印，jumpNum设为1时默认每次打印
    public boolean isPrint(){
      if (printNum % jumpNum == 0)
        return true;
      else
        return false;
    }
    
    public void doPrint(String msg){
      if (isPrint()){
        System.out.println(msg);
        printNum ++;
      }
    }

    public void doPrintWithCount(String msg){
      if (isPrint()){
        System.out.println(msg + " ;Count cache[0]:" + getIntCache(0)); 
      }
    }
  }

  //调试代码用的println语句，为了大数据编程，特地增加跳跃显示（每n次只打印一次）功能参数
  public static void debugPrintln(String msg, debugProperties printProp){
    printProp.doPrint(msg);
  }

  //简单统计数据功能
  public static void debugPrintln(String msg, debugProperties printProp, int count){
    printProp.incCache(count);
    printProp.doPrintWithCount(msg);    
  }
}
