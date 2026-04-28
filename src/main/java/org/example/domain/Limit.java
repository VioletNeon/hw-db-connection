package org.example.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "limits")
public class Limit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "limit_gen")
    @SequenceGenerator(name = "limit_gen", sequenceName = "limit_seq", allocationSize = 50)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "remaining", nullable = false, precision = 19, scale = 2)
    private BigDecimal remaining;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }
    public BigDecimal getRemaining() { return remaining; }
    public void setRemaining(BigDecimal remaining) { this.remaining = remaining; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

