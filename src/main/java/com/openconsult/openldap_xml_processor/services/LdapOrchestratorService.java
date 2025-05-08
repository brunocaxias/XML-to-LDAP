package com.openconsult.openldap_xml_processor.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.Attributes;

@Service
public class LdapOrchestratorService {

    final LdapTemplate ldapTemplate;

    public LdapOrchestratorService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
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
            createGroup(attributes);
        }else if (className.equals("Usuario")) {
            createUsuario(attributes);
        }

    }

    private void parseModify(String className, Element root) {
        Map<String, List<String>> attributes = new LinkedHashMap<>();
        NodeList modifyAttrNodes = root.getElementsByTagName("modify-attr");
    }

    private void createGroup(Map<String, List<String>> attr){
        String identificador = attr.get("Identificador").get(0);
        String descricao = attr.get("Descricao").get(0);

        var dn = LdapNameBuilder.newInstance()
                .add("ou", "system")
                .add("ou", "groups")
                .add("cn", identificador)
                .build();

        var attrs = new BasicAttributes();
        attrs.put("objectClass", "groupOfNames");
        attrs.put("cn", identificador);
        attrs.put("description", descricao);
        attrs.put("member", "uid=admin,ou=system");

        ldapTemplate.bind(dn, null, attrs);

    }

    private void createUsuario(Map<String, List<String>> attr) {
        var login = attr.get("Login").get(0); // UID do usuário
        var nomeCompleto = attr.get("Nome Completo").get(0);
        var telefone = attr.get("Telefone").get(0).replaceAll("[()\\-\\s]", "");

        var dn = LdapNameBuilder.newInstance()
                .add("ou", "system")
                .add("ou", "users")
                .add("uid", login)
                .build();

        var attrs = new BasicAttributes();

        attrs.put("objectClass", "inetOrgPerson");
        attrs.put("uid", login);
        attrs.put("cn", nomeCompleto);
        attrs.put("sn", nomeCompleto.split(" ")[nomeCompleto.split(" ").length - 1]); // Último sobrenome como 'sn'
        attrs.put("telephoneNumber", telefone);

        ldapTemplate.bind(dn, null, attrs);
    }

}
