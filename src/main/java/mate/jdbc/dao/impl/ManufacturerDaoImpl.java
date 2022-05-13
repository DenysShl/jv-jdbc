package mate.jdbc.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mate.jdbc.dao.ManufacturerDao;
import mate.jdbc.dao.mappers.impl.ManufacturerMapper;
import mate.jdbc.exception.DataProcessingException;
import mate.jdbc.lib.Dao;
import mate.jdbc.model.Manufacturer;
import mate.jdbc.util.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Dao
public class ManufacturerDaoImpl implements ManufacturerDao {
    private static final Logger log = LogManager.getLogger(ManufacturerDaoImpl.class);
    private final DataSource dataSource;
    private ManufacturerMapper mapper;

    public ManufacturerDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Manufacturer create(Manufacturer element) {
        String insertQuery
                = "INSERT INTO manufacturers (name, country, is_delete) VALUES (?, ?, ?);";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(insertQuery,
                        Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, element.getName());
            statement.setString(2, element.getCountry());
            statement.setBoolean(3, element.isStatus());
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                resultSet.next();
                element.setId(resultSet.getLong(1));
                log.info("{} was created", element);
                return element;
            }
        } catch (SQLException e) {
            log.error("Unable to create {}, DataProcessingException {}", element, e);
            throw new DataProcessingException("Unable to create " + element, e);
        }
    }

    @Override
    public Optional<Manufacturer> get(Long id) {
        String selectQuery = "SELECT * FROM manufacturers WHERE id = ?;";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Manufacturer manufacturer = mapper.apply(resultSet);
                    return Optional.of(manufacturer);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Unable to get manufacturer with ID " + id, e);
        }
    }

    @Override
    public List<Manufacturer> getAll() {
        String selectAllQuery = "SELECT * FROM manufacturers;";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(selectAllQuery);
                ResultSet resultSet = statement.executeQuery()) {
            List<Manufacturer> allManufacturers = new ArrayList<>();
            while (resultSet.next()) {
                Manufacturer manufacturer = mapper.apply(resultSet);
                allManufacturers.add(manufacturer);
            }
            return allManufacturers;
        } catch (SQLException e) {
            throw new DataProcessingException("Unable to retrieve all manufacturers", e);
        }
    }

    @Override
    public Manufacturer update(Manufacturer element) {
        String updateQuery = "UPDATE manufacturers SET name = ?, country = ?, is_delete = ? "
                + "WHERE id = ?;";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setString(1, element.getName());
            statement.setString(2, element.getName());
            statement.setString(3, element.getCountry());
            statement.setBoolean(4, element.isStatus());
            statement.executeUpdate();
            log.info("Element {}, was updated", element);
            return element;
        } catch (SQLException e) {
            log.error("Unable to update {}, DataProcessingException {}", element, e);
            throw new DataProcessingException("Unable to update " + element, e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String deleteQuery = "DELETE FROM manufacturers WHERE id = ?;";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setLong(1, id);
            int numberOfRowsDeleted = statement.executeUpdate();
            log.info("Element with id {}, was deleted", id);
            return numberOfRowsDeleted != 0;
        } catch (SQLException e) {
            log.error("Unable to delete manufacturer with ID {}, "
                    + "DataProcessingException {}", id, e);
            throw new DataProcessingException("Unable to delete manufacturer with ID " + id, e);
        }
    }
}
