package com.hoji.common.core.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileOutputStream

class FileUtilTest {

    @Test
    fun `move - 원본 삭제 후 대상에 생성`(@TempDir tempDir: File) {
        val src = File(tempDir, "src.txt").apply { writeText("payload") }
        val destDir = File(tempDir, "dest").apply { mkdirs() }

        src.move(destDir.path)

        assertFalse(src.exists(), "원본은 삭제되어야 한다")
        val moved = File(destDir, "src.txt")
        assertTrue(moved.exists())
        assertEquals("payload", moved.readText())
    }

    @Test
    fun `writeLine - UTF-8로 기록하고 자신을 반환`(@TempDir tempDir: File) {
        val target = File(tempDir, "out.txt")
        FileOutputStream(target).use { fos ->
            val returned = fos.writeLine("line-한글")
            assertEquals(fos, returned, "체이닝용으로 스트림 자신을 반환해야 한다")
        }
        assertEquals("line-한글", target.readText())
    }

    @Test
    fun `getFilesUnder - 디렉토리 하위 파일 목록`(@TempDir tempDir: File) {
        File(tempDir, "a.txt").writeText("a")
        File(tempDir, "b.txt").writeText("b")
        val files = File(".").getFilesUnder(tempDir.path)
        assertEquals(2, files?.size)
    }
}
