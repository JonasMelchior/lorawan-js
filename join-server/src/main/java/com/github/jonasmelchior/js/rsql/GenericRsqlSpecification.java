package com.github.jonasmelchior.js.rsql;

import com.github.jonasmelchior.js.data.lrwan.MACVersion;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class GenericRsqlSpecification<T> implements Specification<T> {

    private String property;
    private ComparisonOperator operator;
    private List<String> arguments;

    public GenericRsqlSpecification(final String property, final ComparisonOperator operator, final List<String> arguments) {
        super();
        this.property = property;
        this.operator = operator;
        this.arguments = arguments;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        final List<Object> args = castArguments(root);
        final Object argument = args.get(0);
        switch (RsqlSearchOperation.getSimpleOperator(operator)) {
            case EQUAL: {
                if (argument instanceof String) {
                    return builder.like(root.get(property), argument.toString().replace('*', '%'));
                } else if (argument instanceof MACVersion){
                    return builder.equal(root.get(property).as(MACVersion.class), MACVersion.valueOf(argument.toString()));
                } else if (argument instanceof Boolean) {
                    return builder.equal(root.get(property).as(Boolean.class), Boolean.parseBoolean(argument.toString()));
                } else if (argument == null) {
                    return builder.isNull(root.get(property));
                } else {
                    return builder.equal(root.get(property), argument);
                }
            }
            case NOT_EQUAL: {
                if (argument instanceof String) {
                    return builder.notLike(root.get(property), argument.toString().replace('*', '%'));
                } else if (argument instanceof MACVersion) {
                    return builder.notEqual(root.get(property).as(MACVersion.class), MACVersion.valueOf(argument.toString()));
                } else if (argument instanceof Boolean) {
                    return builder.notEqual(root.get(property).as(Boolean.class), Boolean.parseBoolean(argument.toString()));
                } else if (argument == null) {
                    return builder.isNotNull(root.get(property));
                } else {
                    return builder.notEqual(root.get(property), argument);
                }
            }
            case GREATER_THAN: {
                if (argument instanceof LocalDateTime) {
                    return builder.greaterThan(root.get(property).as(LocalDateTime.class), (LocalDateTime) argument);
                } else {
                    return builder.greaterThan(root.get(property), argument.toString());
                }
            }
            case GREATER_THAN_OR_EQUAL: {
                if (argument instanceof LocalDateTime) {
                    return builder.greaterThanOrEqualTo(root.get(property).as(LocalDateTime.class), (LocalDateTime) argument);
                } else {
                    return builder.greaterThanOrEqualTo(root.get(property), argument.toString());
                }
            }
            case LESS_THAN: {
                if (argument instanceof LocalDateTime) {
                    return builder.lessThan(root.get(property).as(LocalDateTime.class), (LocalDateTime) argument);
                } else {
                    return builder.lessThan(root.get(property), argument.toString());
                }
            }
            case LESS_THAN_OR_EQUAL: {
                if (argument instanceof LocalDateTime) {
                    return builder.lessThanOrEqualTo(root.get(property).as(LocalDateTime.class), (LocalDateTime) argument);
                } else {
                    return builder.lessThanOrEqualTo(root.get(property), argument.toString());
                }
            }
            case IN:
                return root.get(property).in(args);
            case NOT_IN:
                return builder.not(root.get(property).in(args));
        }
        return null;
    }

    // === private

    private List<Object> castArguments(final Root<T> root) {
        final Class<?> type = root.get(property).getJavaType();

        final List<Object> args = arguments.stream().map(arg -> {
            if (type.equals(Integer.class)) {
                return Integer.parseInt(arg);
            } else if (type.equals(Long.class)) {
                return Long.parseLong(arg);
            } else if (type.equals(LocalDateTime.class)) {
                String[] possibleFormats = {
                        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                        "yyyy-MM-dd'T'HH:mm:ss",
                        "yyyy-MM-dd'T'HH:mm",
                        "yyyy-MM-dd'T'HH",
                        "yyyy-MM-dd",
                };

                LocalDateTime dateTime = null;

                // Try parsing with each format
                for (String format : possibleFormats) {
                    try {
                        dateTime = LocalDateTime.parse(arg, DateTimeFormatter.ofPattern(format));
                        System.out.println("Parsed LocalDateTime using format " + format + ": " + dateTime);
                        // If parsing succeeds, break the loop
                        break;
                    } catch (DateTimeParseException e) {
                        // If parsing fails, continue to the next format
                        System.out.println("Failed to parse using format " + format);
                    }
                }

                // Handle the case when none of the formats work
                return dateTime;
            } else if (type == MACVersion.class) {
                return MACVersion.valueOf(arg);
            } else if (type.equals(Boolean.class)) {
                return Boolean.parseBoolean(arg);
            } else {
                return arg;
            }
        }).collect(Collectors.toList());

        return args;
    }
}
