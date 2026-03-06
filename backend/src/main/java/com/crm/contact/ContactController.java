package com.crm.contact;

import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService service;
    private final Tracer tracer;

    @GetMapping
    @PreAuthorize("hasRole('crm-viewer') or hasRole('crm-admin')")
    public Page<Contact> list(@PageableDefault(size = 20) Pageable pageable) {
        log.info("[Contacts] list traceId={}",
                tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : "none");
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('crm-viewer') or hasRole('crm-admin')")
    public ResponseEntity<Contact> get(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('crm-admin')")
    public ResponseEntity<Contact> create(@Valid @RequestBody ContactRequest req) {
        Contact saved = service.create(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(uri).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('crm-admin')")
    public ResponseEntity<Contact> update(@PathVariable UUID id,
                                          @Valid @RequestBody ContactRequest req) {
        return service.update(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('crm-admin')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
