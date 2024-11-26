package com.heyanle.buildsrc

import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.process.CommandLineArgumentProvider
import java.io.File

class RoomSchemaArgProvider(
  @get:InputDirectory
  @get:PathSensitive(PathSensitivity.RELATIVE)
  val schemaDir: File
) : CommandLineArgumentProvider {

  override fun asArguments(): Iterable<String> {
    // Note: If you're using KSP, change the line below to return
    // listOf("room.schemaLocation=${schemaDir.path}").
    return listOf("room.schemaLocation=${schemaDir.path}")
  }
}