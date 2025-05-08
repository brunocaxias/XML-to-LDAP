package com.openconsult.openldap_xml_processor.services;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.directory.BasicAttributes;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    final LdapTemplate ldapTemplate;
    GroupService groupService;

    public UserService(LdapTemplate ldapTemplate, GroupService groupService) {
        this.ldapTemplate = ldapTemplate;
        this.groupService = groupService;
    }


    public void createUsuario(Map<String, List<String>> attr) {
        var login = attr.get("Login").get(0);
        var nomeCompleto = attr.get("Nome Completo").get(0);
        var telefone = attr.get("Telefone").get(0).replaceAll("[()\\-\\s]", "");

        var userDn = LdapNameBuilder.newInstance()
                .add("ou", "system")
                .add("ou", "users")
                .add("uid", login)
                .build();

        var attrs = new BasicAttributes();
        attrs.put("objectClass", "inetOrgPerson");
        attrs.put("uid", login);
        attrs.put("cn", nomeCompleto);
        attrs.put("sn", nomeCompleto.split(" ")[nomeCompleto.split(" ").length - 1]);
        attrs.put("telephoneNumber", telefone);

        ldapTemplate.bind(userDn, null, attrs);

        // Adiciona usuario ao respectivo grupo
        if (attr.containsKey("Grupo")) {
            List<String> grupos = attr.get("Grupo");
            for (String grupoName : grupos) {
                groupService.addUserToGroup(login, grupoName);
            }
        }
    }
}
