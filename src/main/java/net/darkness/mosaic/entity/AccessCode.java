package net.darkness.mosaic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Entity
@Table(name = "access_code")
public class AccessCode {

    public enum State {
        ACTIVE,
        CREATED,
        USED,
    }

    public AccessCode() {
        this.state = State.ACTIVE;
    }

    public AccessCode(String code) {
        this.code = code;
        this.state = State.ACTIVE;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(unique = true)
    private String code;

    @Setter
    private String originalImageName;
    @Setter
    private String visualImageName;
    @Setter
    private String generatedImageName;
    @Setter
    private String palette;
    @Setter
    private Integer verticalBlocks;
    @Setter
    private Integer horizontalBlocks;


    @CreationTimestamp
    private LocalDateTime created;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "access_code_id")
    private List<ImageBlock> blocks = new ArrayList<>();
}
