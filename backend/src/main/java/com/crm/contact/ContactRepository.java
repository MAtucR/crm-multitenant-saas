package com.crm.contact;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ContactRepository
        extends JpaRepository<Contact, UUID>,
                JpaSpecificationExecutor<Contact> {
}
