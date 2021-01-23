package net.romvoid.crashbot.utilities.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface ConfigName {
	String value() default "unknown";
}
