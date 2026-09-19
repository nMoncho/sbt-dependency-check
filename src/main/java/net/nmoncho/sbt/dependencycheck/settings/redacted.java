/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.settings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a settings field as secret so {@code redactedToString} replaces its value with a redacted
 * placeholder, keeping credentials out of {@code show <settingKey>} and any diagnostic logging.
 *
 * <p>This is a Java annotation on purpose: it needs {@link RetentionPolicy#RUNTIME} retention to be
 * readable through reflection on both the Scala 2.12 and Scala 3 cross-builds (Scala
 * {@code StaticAnnotation}s are not retained in bytecode). It targets {@code FIELD} only so that
 * scalac places it on the backing field of a case-class {@code val} parameter rather than on the
 * constructor parameter, where {@code getDeclaredFields} would not see it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface redacted {}
