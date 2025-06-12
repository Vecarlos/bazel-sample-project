package issue
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import org.w3c.dom.Document
import org.xml.sax.InputSource

fun main() {
    val xmlData = "<users>" +
                  "  <user name='carlo' pass='123'></user>" +
                  "  <user name='juan' pass='456'></user>" +
                  "</users>"

    val userInput = "' or '1'='1"
    val vulnerableQuery = "/users/user[@name='$userInput']"
    
    println(vulnerableQuery)

    val factory = DocumentBuilderFactory.newInstance()
    val doc: Document = factory.newDocumentBuilder().parse(InputSource(StringReader(xmlData)))
    val xpath = XPathFactory.newInstance().newXPath()
    
    val userExists = xpath.evaluate(vulnerableQuery, doc, XPathConstants.BOOLEAN) as Boolean
    
    println(userExists)
}