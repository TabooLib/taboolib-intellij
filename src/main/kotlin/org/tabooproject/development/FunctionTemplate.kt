package org.tabooproject.development

import org.tabooproject.development.step.ConfigurationPropertiesStep
import org.tabooproject.development.step.OptionalPropertiesStep
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

object FunctionTemplate {

    fun getBuildProperty(): Map<String, Any> {
        return mutableMapOf<String, Any>().apply {
            val configProperty = ConfigurationPropertiesStep.property
            // 插件名
            put("name", configProperty.name!!)
            // 主类
            put("group", configProperty.mainClass
                // 截去插件名
                .substringBeforeLast(".")
            )
            // 版本
            put("version", configProperty.version)
            put("tabooVersion", tabooLatestVersion)
            // 模块列表
            put("modules", configProperty.modules.map { it.id })
            // 从模块构建额外 imports
            put("extraPackages", configProperty.modules.map { "import io.izzel.taboolib.gradle.${it.id}" })
            val optionalProperty = OptionalPropertiesStep.property
            // 插件描述
            put("description", optionalProperty.description)
            // 作者列表
            if (optionalProperty.authors.isNotEmpty()) {
                put("authors", optionalProperty.authors)
            }
            // 网站
            if (optionalProperty.website.isNotEmpty()) {
                put("website", optionalProperty.website)
            }
            // 依赖列表
            if (optionalProperty.depends.isNotEmpty()) {
                put("dependencies", optionalProperty.depends)
            }
            // 软依赖列表
            if (optionalProperty.softDepends.isNotEmpty()) {
                put("softDependencies", optionalProperty.softDepends)
            }
        }
    }

    private const val TABOO_GRADLE_PROPERTIES_FILE_URL = "https://raw.githubusercontent.com/TabooLib/taboolib/master/gradle.properties"

    private val tabooLatestVersion: String
        get() {
            val client = createOkHttpClientWithSystemProxy {
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
            }

            val response = client
                .newCall(getRequest(TABOO_GRADLE_PROPERTIES_FILE_URL))
                .execute()
                .takeIf { it.isSuccessful } ?: throw IOException("Failed to download file")

            val reader = response.body?.byteStream()?.bufferedReader(StandardCharsets.UTF_8) ?: throw IOException("Response body is null")

            var version = "UNKNOWN"
            reader.use { buffer ->
                while (true) {
                    val line = buffer.readLine() ?: throw IllegalStateException("Can not read TabooLib version")
                    val split = line.split("=")
                    if (split[0] == "version") {
                        version = split[1]
                        break
                    }
                }
            }
            return version
        }
}