package com.crm.contact;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactService {

    private final ContactRepository repo;

    public Page<Contact> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Optional<Contact> findById(UUID id) {
        return repo.findById(id);
    }

    @Transactional
    public Contact create(ContactRequest req) {
        var c = Contact.builder()
                .name(req.name())
                .email(req.email())
                .phone(req.phone())
                .status(req.status() != null ? req.status() : Contact.ContactStatus.LEAD)
                .notes(req.notes())
                .build();
        return repo.save(c);
    }

    @Transactional
    public Optional<Contact> update(UUID id, ContactRequest req) {
        return repo.findById(id).map(c -> {
            c.setName(req.name());
            c.setEmail(req.email());
            c.setPhone(req.phone());
            if (req.status() != null) c.setStatus(req.status());
            c.setNotes(req.notes());
            return repo.save(c);
        });
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }
}
