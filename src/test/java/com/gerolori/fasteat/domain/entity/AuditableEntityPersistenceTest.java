package com.gerolori.fasteat.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.gerolori.fasteat.domain.config.JpaAuditingConfig;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class AuditableEntityPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistsUuidIdentityAndAuditTimestamps() {
        PersistenceProbeEntity entity = new PersistenceProbeEntity();
        entity.setName("created");

        PersistenceProbeEntity persisted = entityManager.persistFlushFind(entity);

        assertThat(persisted.getId()).isNotNull();
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getUpdatedAt()).isNotNull();

        Instant createdAt = persisted.getCreatedAt();
        Instant firstUpdatedAt = persisted.getUpdatedAt();

        persisted.setName("updated");
        entityManager.persistAndFlush(persisted);
        entityManager.clear();

        PersistenceProbeEntity updated = entityManager.find(PersistenceProbeEntity.class, persisted.getId());

        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(firstUpdatedAt);
    }

    @Entity(name = "PersistenceProbeEntity")
    @Table(name = "persistence_probe")
    static class PersistenceProbeEntity extends AuditableEntity {

        @Column(name = "name", nullable = false)
        private String name;

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }
    }
}
