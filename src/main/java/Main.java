import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    final String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

    //  Задача 1 CSV - JSON парсер
    final List<Employee> listFromCSV = parseCSV(columnMapping, "data.csv");
    listToJson(listFromCSV, "dataFromCSV.json");

    // Задача 2 XML - JSON парсер
    final List<Employee> listFromXML = parseXML("data.xml");
    listToJson(listFromXML, "dataFromXML.json");

    // Задача 3 JSON парсер
    final String jsonFromCSV = readString("dataFromCSV.json");
    final String jsonFromXML = readString("dataFromXML.json");
    final List<Employee> listJsonFromCSV = jsonToList(jsonFromCSV);
    final List<Employee> listJsonFromXML = jsonToList(jsonFromXML);
    System.out.println(listJsonFromCSV.toString());
    System.out.println(listJsonFromXML.toString());
  }

  public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
    List<Employee> employees = null;

    try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
      ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();

      strategy.setType(Employee.class);
      strategy.setColumnMapping(columnMapping);

      CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader).withMappingStrategy(strategy).build();

      employees = csv.parse();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return employees;
  }

  public static List<Employee> parseXML(String fileName) throws IOException, SAXException, ParserConfigurationException {
    List<Employee> employees = new ArrayList<>();
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Document doc = builder.parse(new File(fileName));
    final Node root = doc.getDocumentElement();
    final NodeList employeeList = root.getChildNodes();

    for (int item = 0; item < employeeList.getLength(); item++) {
      final Node employeeNode = employeeList.item(item);
      Employee employee = new Employee();

      if (Node.ELEMENT_NODE == employeeNode.getNodeType()) {
        final NodeList propertyList = employeeNode.getChildNodes();

        for (int property = 0; property < propertyList.getLength(); property++) {
          final Node propertyNode = propertyList.item(property);

          if (Node.ELEMENT_NODE == propertyNode.getNodeType()) {
            final String propertyName = propertyList.item(property).getNodeName();
            final String propertyValue = propertyList.item(property).getTextContent();

            switch (propertyName) {
              case "id" -> employee.id = Long.parseLong(propertyValue);
              case "firstName" -> employee.firstName = propertyValue;
              case "lastName" -> employee.lastName = propertyValue;
              case "country" -> employee.country = propertyValue;
              case "age" -> employee.age = Integer.parseInt(propertyValue);
            }
          }
        }

        employees.add(employee);
      }
    }

    return employees;
  }

  public static void listToJson(List<Employee> list, String fileName) {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    Type listType = new TypeToken<List<Employee>>() {}.getType();
    String json = gson.toJson(list, listType);

    writeString(json, fileName);
  }

  public static void writeString(String json, String fileName) {
    try (FileWriter file = new FileWriter(fileName)) {
      file.write(json);
      file.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String readString(String fileName) {
    JSONParser parser = new JSONParser();
    JSONArray jsonArray = new JSONArray();
    StringBuilder jsonStringBuilder = new StringBuilder();

    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      String fileLine;

      while ((fileLine = br.readLine()) != null) {
        jsonStringBuilder.append(fileLine);
      }

      jsonArray = (JSONArray) parser.parse(jsonStringBuilder.toString());
    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }

    return jsonArray.toString();
  }

  public static List<Employee> jsonToList(String json) {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    Type listType = new TypeToken<List<Employee>>(){}.getType();

    return gson.fromJson(json, listType);
  }
}