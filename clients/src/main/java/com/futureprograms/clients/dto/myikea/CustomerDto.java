package com.futureprograms.clients.dto.myikea;

import com.futureprograms.clients.entity.myikea.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * DTO para Customer (MyIkea)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private Long customerId;
    private String firstName;
    private String lastName;
    private String telefono;
    private String email;
    private LocalDate fechaDeNacimiento;

    public static CustomerDto fromEntity(Customer customer) {
        return CustomerDto.builder()
                .customerId(customer.getCustomerId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .telefono(customer.getTelefono())
                .email(customer.getEmail())
                .fechaDeNacimiento(customer.getFechaDeNacimiento())
                .build();
    }
}
