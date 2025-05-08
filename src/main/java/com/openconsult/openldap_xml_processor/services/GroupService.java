package com.openconsult.openldap_xml_processor.services;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.directory.*;
import java.util.List;
import java.util.Map;

@Service
public class GroupService {

    final LdapTemplate ldapTemplate;

    public GroupService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void createGroup(Map<String, List<String>> attr){
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

    public void addUserToGroup(String userId, String groupName) {
        var groupDn = LdapNameBuilder.newInstance()
                .add("ou", "system")
                .add("ou", "groups")
                .add("cn", groupName)
                .build();

        var userDn = LdapNameBuilder.newInstance()
                .add("ou", "system")
                .add("ou", "users")
                .add("uid", userId)
                .build();

        ModificationItem[] modifications = new ModificationItem[1];
        Attribute memberAttr = new BasicAttribute("member", userDn.toString());
        modifications[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, memberAttr);

        ldapTemplate.modifyAttributes(groupDn, modifications);
    }

    public void removeUserFromGroup(String userId, String groupName) {
        var groupDn = LdapNameBuilder.newInstance()
                .add("ou", "system")
                .add("ou", "groups")
                .add("cn", groupName)
                .build();

        var userDn = LdapNameBuilder.newInstance()
                .add("ou", "system")
                .add("ou", "users")
                .add("uid", userId)
                .build();

        ModificationItem[] modifications = new ModificationItem[1];
        Attribute memberAttr = new BasicAttribute("member", userDn.toString());
        modifications[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, memberAttr);

        ldapTemplate.modifyAttributes(groupDn, modifications);
    }
}
