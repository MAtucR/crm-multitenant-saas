package com.crm.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 200) String email,
        @Size(max = 30) String phone,
        Contact.ContactStatus status,
        @Size(max = 500) String notes
) {}
