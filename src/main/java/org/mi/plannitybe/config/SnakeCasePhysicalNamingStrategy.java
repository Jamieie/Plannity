package org.mi.plannitybe.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;

public class SnakeCasePhysicalNamingStrategy implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return apply(identifier);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return apply(identifier);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return apply(identifier);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return apply(identifier);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return apply(identifier);
    }

    private Identifier apply(Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        
        String name = identifier.getText();
        String snakeCaseName = camelCaseToSnakeCase(name);
        return Identifier.toIdentifier(snakeCaseName);
    }

    private String camelCaseToSnakeCase(String name) {
        final StringBuilder result = new StringBuilder();
        result.append(name.substring(0, 1).toLowerCase(Locale.ROOT));
        
        for (int i = 1; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_');
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
}