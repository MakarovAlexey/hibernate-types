package com.vladmihalcea.hibernate.type.array;

import com.vladmihalcea.hibernate.type.util.AbstractPostgreSQLIntegrationTest;
import com.vladmihalcea.hibernate.type.util.providers.DataSourceProvider;
import com.vladmihalcea.hibernate.type.util.providers.PostgreSQLDataSourceProvider;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * @author Vlad Mihalcea
 */
public class Simple2DArrayTypeTest extends AbstractPostgreSQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Event.class,
        };
    }

    @Override
    public void init() {
        DataSource dataSource = newDataSource();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try {
                statement.executeUpdate(
                    "DROP TYPE sensor_state CASCADE"
                );
            } catch (SQLException ignore) {
            }
            statement.executeUpdate(
                "CREATE TYPE sensor_state AS ENUM ('ONLINE', 'OFFLINE', 'UNKNOWN')"
            );
        } catch (SQLException e) {
            fail(e.getMessage());
        }
        super.init();
    }

    @Override
    protected DataSourceProvider dataSourceProvider() {
        return new PostgreSQLDataSourceProvider() {
            @Override
            public String hibernateDialect() {
                return PostgreSQL95ArrayDialect.class.getName();
            }
        };
    }

    @Test
    public void test() {
        doInJPA(entityManager -> {
            Event nullEvent = new Event();
            nullEvent.setId(0L);
            entityManager.persist(nullEvent);

            entityManager.persist(
                new Event()
                .setId(1L)
                .setTest2DArray(new String[][] {})
            );
        });

        doInJPA(entityManager -> {
            Event event = entityManager.find(Event.class, 1L);
        });
    }

    @Entity(name = "Event")
    @Table(name = "event")
    @TypeDefs({
        @TypeDef(
            typeClass = String2DArrayType.class,
            defaultForType = String[][].class
        )
    })
    public static class Event {

        @Id
        private Long id;
        
        @Column(name = "test_2d_array", columnDefinition = "text[]")
        private String[][] test2dArray;

        public Long getId() {
            return id;
        }

        public Event setId(Long id) {
            this.id = id;
            return this;
        }

        public String[][] getTest2DArray() {
            return test2dArray;
        }

        public Event setTest2DArray(String[][] test2dArray) {
            this.test2dArray = test2dArray;
            return this;
        }
    }
}
