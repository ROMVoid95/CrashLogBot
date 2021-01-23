/*
 * Copyright (C) 2016-2021 David Rubio Escares / Kodehawa
 *
 *  Mantaro is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  Mantaro is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mantaro. If not, see http://www.gnu.org/licenses/
 */

package net.romvoid.crashbot.db;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.romvoid.crashbot.data.CrashBotData;

public interface ManagedObject {
    @Nonnull
    String getId();

    @JsonIgnore
    @Nonnull
    String getTableName();

    @JsonIgnore
    @Nonnull
    default String getDatabaseId() {
        return getId();
    }

    default void delete() {
        CrashBotData.db().delete(this);
    }

    /**
     * Saves an object to the database.
     * This will save the object by REPLACING it, instead of updating.
     * Useful sometimes.
     */
    default void save() {
    	CrashBotData.db().save(this);
    }

    /**
     * Saves an object to the database.
     * This will save the object by updating it.
     * Useful sometimes.
     */
    default void saveUpdating() {
    	CrashBotData.db().saveUpdating(this);
    }

    default void deleteAsync() {
    	CrashBotData.queue(this::delete);
    }

    default void saveAsync() {
    	CrashBotData.queue(this::save);
    }
}
