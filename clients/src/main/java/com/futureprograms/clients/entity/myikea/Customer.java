package com.futureprograms.clients.entity.myikea;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Entidad Customer para la base de datos myikea
 * Mapea la tabla customer
 */
@Entity
@Table(name = "customer")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "first_name", nullable = false, length = 45)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 45)
    private String lastName;

    @Column(name = "telefono", nullable = true, length = 9)
    private String telefono;

    @Column(name = "email", unique = true, nullable = true, length = 50)
    private String email;

    @Column(name = "fecha_de_nacimiento", nullable = true)
    private LocalDate fechaDeNacimiento;
}
