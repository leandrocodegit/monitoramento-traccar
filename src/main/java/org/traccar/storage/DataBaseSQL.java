package org.traccar.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.traccar.config.Config;
import org.traccar.model.Bipe;
import org.traccar.model.Rotina;
import org.traccar.model.User;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class DataBaseSQL extends DatabaseStorage{

    private final Config config;
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private final String databaseType;

    @Inject
    public DataBaseSQL(Config config, DataSource dataSource, ObjectMapper objectMapper) {

        super(config, dataSource, objectMapper);
        this.config = config;
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;

        try {
            databaseType = dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> getNotificationsUsuario(String tipoNotificacao, long userId, long deviceId) throws StorageException {
        StringBuilder query = new StringBuilder("CALL USERS_NOTIFICATIONS('" + tipoNotificacao + "')");

        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());

            return builder.executeQuery(User.class);
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }


    public List<Rotina> getRotinas() throws StorageException {
        StringBuilder query = new StringBuilder("CALL BUSCA_ROTINAS()");

        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());

            return builder.executeQuery(Rotina.class);
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    public List<User> getNotificacoes(String tipoNotificacao, Long deviceId) throws StorageException {
        StringBuilder query = new StringBuilder("CALL USERS_NOTIFICATIONS('"+ tipoNotificacao +"', '" + deviceId + "')");

        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());

            return builder.executeQuery(User.class);
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }
    public List<Bipe> getBipes() throws StorageException {
        StringBuilder query = new StringBuilder("CALL BUSCA_BIPES()");

        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());

            return builder.executeQuery(Bipe.class);
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }
}
