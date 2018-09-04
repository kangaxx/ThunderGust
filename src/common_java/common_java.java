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

  public static void main(String [] args){
    getDocument();
  }



  private static void getDocument(){  
    try {
      //创建解析工厂
      DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
      //指定DocumentBuilder
      DocumentBuilder builder = dbfactory.newDocumentBuilder();
      Document doc = builder.parse(new File("/mydata/text.xml"));
      //得到Document的根
      Element root = doc.getDocumentElement();
      System.out.println("根节点标记名：" + root.getTagName());
      System.out.println("*****下面遍历XML元素*****");
      //获得一级子元素
      
      NodeList list = root.getElementsByTagName("学生");
      //遍历一级子元素
      for (int i=0; i < list.getLength() ; i++) {
        //获得一级子元素
        Element element = (Element)list.item(i);
        System.out.println("get tag name : " + element.getTagName()); 
        //获得性别属性
        String sex = element.getAttribute("性别");
        //获得元素的值
        String name = element.getFirstChild().getNodeValue();
        System.out.println("性别：" + sex + "  " +"名称：" + name);
        int age = StrToInt_safe(element.getAttribute("年龄"));
        System.out.println("年龄: " + age);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }  
  
  private static int StrToInt_safe(String input){
    return StrToInt_safe(input, -1);
  }

  private static int StrToInt_safe(String input, int execReturn){
    try{
      return Integer.parseInt(input);
    }catch(Exception e){
      return execReturn;
    }
  }

}
