package api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GmlToWktConverter {
    public static void convert(String gmlFileName) throws IOException, ParserConfigurationException, SAXException {
        File fXmlFile = new File(gmlFileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();

        NodeList featureNodes = doc.getElementsByTagName("gml:featureMember");
        Map<String, String> wkts = new HashMap<>();

        for (int i = 0; i < featureNodes.getLength(); i++) {
            Element featureElement = (Element) featureNodes.item(i);
            String name = featureElement.getElementsByTagName("fme:PRENAME").item(0).getTextContent();

            NodeList polygonNodes = featureElement.getElementsByTagName("gml:posList");

            StringBuilder finalWkt = polygonNodes.getLength() > 1 ? new StringBuilder("MULTIPOLYGON(") : new StringBuilder("POLYGON");

            for (int j = 0; j < polygonNodes.getLength(); j++) {
                String rawPoints = polygonNodes.item(j).getFirstChild().getTextContent();
                List<String> pointsArray = Arrays.asList(rawPoints.split(" "));

                StringBuilder pointsForWkt = new StringBuilder("((");
                for (int h = 0; h < pointsArray.size(); h += 2) {
                    pointsForWkt.append(" ").append(pointsArray.get(h + 1)).append(" ").append(pointsArray.get(h)).append(",");
                }

                pointsForWkt.deleteCharAt(pointsForWkt.lastIndexOf(","));
                pointsForWkt.append(")), ");
                finalWkt.append(pointsForWkt);
            }

            finalWkt.deleteCharAt(finalWkt.lastIndexOf(","));

            if (polygonNodes.getLength() > 1) {
                finalWkt.append(")");
            }

            wkts.put(name, finalWkt.toString());
        }

        for (Map.Entry<String, String> eachWkt : wkts.entrySet()) {
            PrintWriter writer = new PrintWriter(eachWkt.getKey() + ".wkt", "UTF-8");
            writer.println(eachWkt.getValue());
            writer.close();
        }
    }
}
