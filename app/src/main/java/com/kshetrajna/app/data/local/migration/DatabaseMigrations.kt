package com.kshetrajna.app.data.local.migration

import androidx.room.migration.Migration

/**
 * Migration definitions for Kshetrajna Room Database schema evolution.
 * Initial schema is Version 1.
 */
object DatabaseMigrations {
    const val LATEST_VERSION = 1

    /**
     * List of database migrations.
     * New schema migrations (e.g. MIGRATION_1_2) should be added here when database schema evolves.
     */
    val ALL_MIGRATIONS: Array<Migration> = emptyArray()
}
