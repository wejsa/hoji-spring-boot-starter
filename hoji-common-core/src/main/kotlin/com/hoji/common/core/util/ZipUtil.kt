package com.hoji.common.core.util

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File

/**
 * 디렉토리를 AES-256으로 암호화한 zip으로 압축하는 유틸. 도메인 비결합.
 *
 * zip4j 2.x API 기반이며 비밀번호는 보안상 [CharArray]로 받는다.
 */

/**
 * [dirLocation] 디렉토리 전체를 `{dirLocation}/{zipName}.zip` 로 압축한다.
 *
 * @param password AES 암호화 비밀번호([CharArray]).
 * @return 생성된 zip 파일.
 */
fun compressZipFile(dirLocation: String, zipName: String, password: CharArray): File {
    val zipFile = ZipFile("$dirLocation${File.separator}$zipName.zip", password)
    val params = ZipParameters().apply {
        compressionMethod = CompressionMethod.DEFLATE
        compressionLevel = CompressionLevel.NORMAL
        isEncryptFiles = true
        encryptionMethod = EncryptionMethod.AES
        aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
    }
    zipFile.addFolder(File(dirLocation), params)
    return zipFile.file
}
