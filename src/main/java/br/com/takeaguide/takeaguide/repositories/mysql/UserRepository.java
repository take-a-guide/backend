package br.com.takeaguide.takeaguide.repositories.mysql;

import javax.sql.DataSource;

import static br.com.takeaguide.takeaguide.utils.StatementFormatter.format;

import java.math.BigInteger;
import java.util.List;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import br.com.takeaguide.takeaguide.dtos.account.ChangeUserRequest;
import br.com.takeaguide.takeaguide.dtos.account.CreateUserRequest;
import br.com.takeaguide.takeaguide.dtos.account.UserDto;
import br.com.takeaguide.takeaguide.repositories.mysql.rowmappers.UserRowMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Repository
public class UserRepository {

    @Autowired
    private DataSource dataSource;

    private NamedParameterJdbcTemplate jdbcTemplate;

    public UserDto login(String email, String password) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        
        String sql = String.format("""
            SELECT 
                cpf,
                name,
                email,
                password,
                user_type_id,
                phone,
                deleted_at
            FROM 
                account
            WHERE 
                email = '%s'
                AND password = '%s'
        """, email, password);

        MapSqlParameterSource map = new MapSqlParameterSource();

        try {
            return jdbcTemplate.queryForObject(sql, map, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void removeUser(String String) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        String sql = """
            UPDATE 
                account
            SET
                deleted_at = UTC_TIMESTAMP()
            WHERE 
                cpf = :cpf
        """;

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("cpf", String);

        jdbcTemplate.update(sql, map);
    }

    public Integer checkIfUserIsAllowed(String email, String name) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        String sql = String.format("""
            SELECT
                COUNT(cpf)
            FROM 
                account
            WHERE
                (
                    email LIKE '%s'
                    OR 
                    name LIKE '%s'
                )
                AND deleted_at IS NULL
        """, email, name);

        MapSqlParameterSource map = new MapSqlParameterSource();

        try {
            return jdbcTemplate.queryForObject(sql, map, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<UserDto> retrieveUserByName(String name) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        String sql = String.format("""
            SELECT 
                cpf,
                name,
                email,
                password,
                user_type_id,
                phone,
                deleted_at
            FROM
                account
            WHERE 
                name LIKE '%s'
                AND deleted_at IS NULL 
        """, ("%" + name + "%"));

        try {
            return jdbcTemplate.query(sql, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<UserDto> retrieveUserByEmail(String email) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        String sql = String.format("""
            SELECT 
                cpf,
                name,
                email,
                password,
                user_type_id,
                phone,
                deleted_at
            FROM
                account
            WHERE 
                email LIKE '%s'
                AND deleted_at IS NULL
        """, ("%" + email + "%"));

        try {
            return jdbcTemplate.query(sql, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<UserDto> retrieveUserByCpf(String cpf) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        String sql = """
            SELECT 
                cpf,
                name,
                email,
                password,
                user_type_id,
                phone,
                deleted_at
            FROM
                account
            WHERE 
                cpf = :cpf
                AND deleted_at IS NULL
        """;

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("cpf", cpf);

        try {
            return jdbcTemplate.query(sql, map, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public String updateUser(ChangeUserRequest request) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        String sql = String.format("""
            UPDATE 
                account
            SET 
                %s
            WHERE 
                cpf = :cpf;
        """, format(request));

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("cpf", request.cpf());

        KeyHolder keyholder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, map, keyholder);

        return keyholder.getKeyAs(String.class);
    } 

    public BigInteger insertUser(CreateUserRequest request) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        String sql = String.format("""
            INSERT INTO account(cpf, name, email, password, user_type_id, phone)
            VALUES('%s', '%s', '%s', '%s', :type, '%s');
        """, request.cpf(), request.name(), request.email(), request.password(), request.phone());

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("type", request.type());

        KeyHolder keyholder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, map, keyholder);

        return keyholder.getKeyAs(BigInteger.class);
    }

    
  
}
