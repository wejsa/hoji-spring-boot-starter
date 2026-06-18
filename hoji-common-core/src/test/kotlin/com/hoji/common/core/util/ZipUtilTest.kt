package com.hoji.common.core.util

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ZipUtilTest {

    private val password = "s3cr3t-pw".toCharArray()

    @Test
    fun `compressZipFile - 압축 후 정답 비밀번호로 해제 성공`(@TempDir tempDir: File) {
        val srcDir = File(tempDir, "src").apply { mkdirs() }
        File(srcDir, "data.txt").writeText("zip-payload-한글")

        val zip = compressZipFile(srcDir.path, "archive", password)
        assertTrue(zip.exists(), "zip 파일이 생성되어야 한다")

        val outDir = File(tempDir, "out").apply { mkdirs() }
        ZipFile(zip, password).extractAll(outDir.path)

        // zip4j는 압축 대상 폴더명(src)을 엔트리 경로에 포함한다.
        assertEquals("zip-payload-한글", File(outDir, "src/data.txt").readText())
    }

    @Test
    fun `compressZipFile - 오답 비밀번호로 해제 시 예외`(@TempDir tempDir: File) {
        val srcDir = File(tempDir, "src").apply { mkdirs() }
        File(srcDir, "data.txt").writeText("secret")

        val zip = compressZipFile(srcDir.path, "archive", password)
        val outDir = File(tempDir, "out").apply { mkdirs() }

        assertThrows(ZipException::class.java) {
            ZipFile(zip, "wrong-pw".toCharArray()).extractAll(outDir.path)
        }
    }
}
