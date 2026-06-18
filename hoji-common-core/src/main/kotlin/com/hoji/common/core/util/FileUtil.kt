package com.hoji.common.core.util

import java.io.File
import java.io.FileOutputStream

/**
 * 파일 입출력 보조 유틸. 도메인 비결합 — 순수 [java.io] 기반.
 */

/** [location] 디렉토리 바로 아래의 파일 목록을 반환한다(없으면 `null`). */
fun File.getFilesUnder(location: String): Array<File>? = File(location).listFiles()

/** 현재 파일을 [toLocation] 디렉토리로 옮긴다. [overwrite]가 `true`면 대상 파일을 덮어쓴다. */
fun File.move(toLocation: String, overwrite: Boolean = false) {
    val fileName = this.name
    this.copyTo(File("$toLocation${File.separator}$fileName"), overwrite)
    this.delete()
}

/** UTF-8로 [content]를 기록하고 스트림 자신을 반환한다(체이닝용). */
fun FileOutputStream.writeLine(content: String): FileOutputStream {
    this.write(content.toByteArray(Charsets.UTF_8))
    return this
}
