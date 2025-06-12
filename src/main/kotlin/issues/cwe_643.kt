package issues

import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import org.w3c.dom.Document
import org.xml.sax.InputSource

// Simula una función que obtiene datos no confiables de una fuente externa.
fun getUntrustedUserInput(): String {
    return "' or '1'='1" 
}

fun main() {
    val xmlData = "<users>" +
                  "  <user name='carlo' pass='123'></user>" +
                  "  <user name='juan' pass='456'></user>" +
                  "</users>"

    // CAMBIO CLAVE: El dato ahora viene de una "fuente" externa.
    val userInput = getUntrustedUserInput()

    // El "sink" vulnerable, donde se usa el dato no confiable.
    val vulnerableQuery = "/users/user[@name='$userInput']"
    
    println("Consulta maliciosa: $vulnerableQuery")

    val factory = DocumentBuilderFactory.newInstance()
    val doc: Document = factory.newDocumentBuilder().parse(InputSource(StringReader(xmlData)))
    val xpath = XPathFactory.newInstance().newXPath()
    
    val userExists = xpath.evaluate(vulnerableQuery, doc, XPathConstants.BOOLEAN) as Boolean
    
    println("¿El usuario existe? $userExists")
}