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

}
