package cmu.s3d.fortis.cli

import java.io.File
import java.nio.file.Path

fun copyResourceToDirectory(resourcePath: String, destination: Path) {
    val resource = ClassLoader.getSystemResource(resourcePath)
    val resourceFile = File(resource?.toURI()?: error("Resource not found: $resourcePath"))
    resourceFile.copyTo(destination.resolve(resourcePath.substringAfterLast("/")).toFile(), overwrite = true)
}