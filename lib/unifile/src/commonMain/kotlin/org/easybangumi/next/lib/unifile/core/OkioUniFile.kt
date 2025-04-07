package org.easybangumi.next.lib.unifile.core

import okio.*
import org.easybangumi.next.lib.unifile.UniFile

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
class OkioUniFile(
    private val path: Path,
    private val system: FileSystem = FileSystem.SYSTEM,
): UniFile {

    override fun getType(): String {
        return UniFile.TYPE_OKIO
    }

    override fun getUri(): String {
        return "${UniFile.TYPE_OKIO}://${path}"
    }

    override fun getName(): String {
        return path.name
    }

    override fun getFilePath(): String {
        return path.toString()
    }

    override fun getParentFile(): UniFile? {
        return path.parent?.let {
            OkioUniFile(it, system)
        }
    }

    override fun isDirectory(): Boolean {
        return kotlin.runCatching {
            system.metadataOrNull(path)?.isDirectory
        }.getOrNull() ?: false
    }

    override fun isFile(): Boolean {
        return kotlin.runCatching {
            system.metadataOrNull(path)?.isRegularFile
        }.getOrNull() ?: false
    }

    override fun lastModified(): Long {
        return kotlin.runCatching {
            system.metadataOrNull(path)?.lastModifiedAtMillis
        }.getOrNull() ?: 0L
    }

    override fun lenght(): Long {
        return kotlin.runCatching {
            system.metadataOrNull(path)?.size
        }.getOrNull() ?: 0L
    }

    override fun exists(): Boolean {
        return kotlin.runCatching {
            system.metadataOrNull(path)?.isDirectory?:false ||
                    system.metadataOrNull(path)?.isRegularFile ?: false
        }.getOrNull() ?: false
    }

    override fun child(displayName: String): UniFile? {
        val childPath = path.resolve(displayName)
        return OkioUniFile(childPath, system)
    }

    override fun childIfExist(displayName: String): UniFile? {
        val child = child(displayName)
        if (child?.exists() == true) {
            return child
        }
        return null
    }

    override fun listFiles(filter: ((UniFile, String) -> Boolean)?): Array<UniFile?> {
        return system.listOrNull(path)?.map {
            val child = OkioUniFile(it, system)
            if (filter == null || filter(child, it.name)) {
                child
            } else {
                null
            }
        }?.filterNotNull()?.toTypedArray()?: emptyArray()
    }

    override fun canRead(): Boolean {
        return isFile() && exists()
    }

    override fun canWrite(): Boolean {
        return isFile() && exists()
    }

    override fun delete(): Boolean {
        return kotlin.runCatching {
            system.deleteRecursively(path, true)
        }.isSuccess

    }

    override fun createDirectory(displayName: String): UniFile? {
        val childPath = path.resolve(displayName)
        kotlin.runCatching {
            system.createDirectory(childPath)
        }.onSuccess {
            return OkioUniFile(childPath, system)
        }
        return null
    }

    override fun renameTo(displayName: String): Boolean {
        val targetPath = path.parent?.resolve(displayName) ?: return false
        return kotlin.runCatching {
            system.copy(path, targetPath)
            system.deleteRecursively(path, false)
        }.isSuccess
    }

    override fun openSink(append: Boolean): Sink {
        return kotlin.run {
            if (append) {
                system.appendingSink(path)
            } else {
                system.sink(path)
            }
        }
    }

    override fun openSource(): Source {
        return kotlin.run {
            system.source(path)
        }
    }
}