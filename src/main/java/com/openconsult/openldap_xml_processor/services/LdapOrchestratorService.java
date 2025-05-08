package com.openconsult.openldap_xml_processor.services;

import org.apache.catalina.User;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.naming.directory.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.*;

@Service
public class LdapOrchestratorService {

    final LdapTemplate ldapTemplate;
    UserService userService;
    GroupService groupService;

    public LdapOrchestratorService(LdapTemplate ldapTemplate, UserService userService, GroupService groupService) {
        this.ldapTemplate = ldapTemplate;
        this.userService = userService;
        this.groupService = groupService;
    }

    public void parseXMLFile(String xmlFile) throws Exception {
        File file = new File(xmlFile);

        // Criando o document builder
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(file);

        // Criando a instancia do Xpath
        XPath xPath = XPathFactory.newInstance().newXPath();

        // Vendo se o arquivo é de add
        var root = document.getDocumentElement();
        boolean isAddOperation = "add".equals(root.getNodeName());


        // Pegando o class name
        String className = document.getDocumentElement().getAttribute("class-name");

        // Verificando se a operacao e de adicionar
        if (isAddOperation) {
            parseAdd(className, root);
        }else{
            parseModify(root);
        }

    }

    private void parseAdd(String className, Element root) {
        // Hash para atributos e lista de valores
        Map<String, List<String>> attributes = new LinkedHashMap<>();

        // Pegando todos os atributos de add-attr
        NodeList addAttrNodes = root.getElementsByTagName("add-attr");

        // Iterando pelos nodes pegando cada nome de atributo e salvando seu valor na lista
        for (int i = 0; i < addAttrNodes.getLength(); i++) {
            var addAttr = (Element) addAttrNodes.item(i);
            String attrName = addAttr.getAttribute("attr-name");

            List<String> values = new ArrayList<>();
            NodeList valueNodes = addAttr.getElementsByTagName("value");
            for (int j = 0; j < valueNodes.getLength(); j++) {
                values.add(((Element) valueNodes.item(j)).getTextContent().trim());
            }

            attributes.put(attrName, values);
        }

        if(className.equals("Grupo")) {
            groupService.createGroup(attributes);
        }else if (className.equals("Usuario")) {
            userService.createUsuario(attributes);
        }

    }

    private void parseModify(Element root) {
        // Pega as associacoes
        NodeList associationNodes = root.getElementsByTagName("association");
        Element association = (Element) associationNodes.item(0);
        // Valor da associacao
        String associationValue = association.getTextContent().trim();

        // Pega os atributos que serao modificados
        NodeList modifyAttrNodes = root.getElementsByTagName("modify-attr");
        for (int i = 0; i < modifyAttrNodes.getLength(); i++) {
            Element modifyAttr = (Element) modifyAttrNodes.item(i);
            String attrName = modifyAttr.getAttribute("attr-name");

            // Processas os atributos de remocao
            NodeList removeValueNodes = modifyAttr.getElementsByTagName("remove-value");
            for (int j = 0; j < removeValueNodes.getLength(); j++) {
                Element removeValue = (Element) removeValueNodes.item(j);
                NodeList valueNodes = removeValue.getElementsByTagName("value");
                for (int k = 0; k < valueNodes.getLength(); k++) {
                    String value = ((Element) valueNodes.item(k)).getTextContent().trim();
                    groupService.removeUserFromGroup(associationValue, value);
                }
            }

            // Processa os valores a adicionar
            NodeList addValueNodes = modifyAttr.getElementsByTagName("add-value");
            for (int j = 0; j < addValueNodes.getLength(); j++) {
                Element addValue = (Element) addValueNodes.item(j);
                NodeList valueNodes = addValue.getElementsByTagName("value");
                for (int k = 0; k < valueNodes.getLength(); k++) {
                    String value = ((Element) valueNodes.item(k)).getTextContent().trim();
                    groupService.addUserToGroup(associationValue, value);
                }
            }
        }

    }

}
