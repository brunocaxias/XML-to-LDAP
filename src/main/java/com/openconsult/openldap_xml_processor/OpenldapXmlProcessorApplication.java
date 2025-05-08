package com.openconsult.openldap_xml_processor;

import com.openconsult.openldap_xml_processor.services.LdapOrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpenldapXmlProcessorApplication implements CommandLineRunner {

	private final LdapOrchestratorService ldapOrchestratorService;

	public OpenldapXmlProcessorApplication(LdapOrchestratorService ldapOrchestratorService) {
		this.ldapOrchestratorService = ldapOrchestratorService;
	}

	public static void main(String[] args) {
		SpringApplication.run(OpenldapXmlProcessorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ldapOrchestratorService.parseXMLFile("src/main/resources/files/AddUsuario1.xml");
	}
}
