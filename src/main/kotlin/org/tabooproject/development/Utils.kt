package org.tabooproject.development

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.kotlin.psi.KtAnnotated
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

fun createFileWithDirectories(baseDir: String, relativePath: String): Path? {
    val fullPath = Paths.get(baseDir, relativePath)
    val parentDir = fullPath.parent ?: return null

    if (Files.notExists(parentDir)) {
        Files.createDirectories(parentDir)
    }

    if (Files.notExists(fullPath)) {
        return Files.createFile(fullPath)
    }
    return null
}

fun createOkHttpClientWithSystemProxy(block: OkHttpClient.Builder.() -> Unit = {}): OkHttpClient {
    val proxyHost = System.getProperty("http.proxyHost")
    val proxyPort = System.getProperty("http.proxyPort")

    val clientBuilder = OkHttpClient.Builder().apply {
        block(this)
    }

    if (!proxyHost.isNullOrEmpty() && !proxyPort.isNullOrEmpty()) {
        val proxy = Proxy(
            Proxy.Type.HTTP,
            InetSocketAddress(proxyHost, proxyPort.toInt())
        )
        clientBuilder.proxy(proxy)
    }

    return clientBuilder.build()
}

fun getRequest(url: String): Request {
    return Request.Builder().url(url).build()
}

fun PsiElement.findContainingAnnotated(): KtAnnotated? = findParent(resolveReferences = false) { it is KtAnnotated }

private inline fun <reified T : PsiElement> PsiElement.findParent(
    resolveReferences: Boolean,
    stop: (PsiElement) -> Boolean,
): T? {
    var el: PsiElement = this

    while (true) {
        if (resolveReferences && el is PsiReference) {
            el = el.resolve() ?: return null
        }

        if (el is T) {
            return el
        }

        if (el is PsiFile || el is PsiDirectory || stop(el)) {
            return null
        }

        el = el.parent ?: return null
    }
}

fun readFromUrl(url: String): String? {
    val client = createOkHttpClientWithSystemProxy {
        connectTimeout(10, TimeUnit.SECONDS)
        readTimeout(10, TimeUnit.SECONDS)
    }

    val response = client
        .newCall(getRequest(url))
        .execute()
        .takeIf { it.isSuccessful } ?: throw IOException("Failed to get $url")

    return response.body?.string()
}