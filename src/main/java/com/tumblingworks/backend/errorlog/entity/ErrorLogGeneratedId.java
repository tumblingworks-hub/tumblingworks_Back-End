package com.tumblingworks.backend.errorlog.entity;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@IdGeneratorType(ErrorLogIdGenerator.class)
@Retention(RUNTIME)
@Target(FIELD)
public @interface ErrorLogGeneratedId {
}
