@file:Suppress("UnusedMaterial3ScaffoldPaddingParameter")

package com.decoraia.app.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.sceneview.Scene
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

/**
 * Visualiza un .glb desde assets/. Por defecto: assets/modelos/sillon.glb
 */
@Composable
fun PantallaVisualizacion(modelPath: String = "modelos/Astronaut.glb") {
    val TAG = "PantallaVisualizacion"
    val context = LocalContext.current
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val childNodes = rememberNodes()

    var model by remember { mutableStateOf<ModelInstance?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // 0) Debug: listar assets de la carpeta para verificar que el archivo exista
    LaunchedEffect(modelPath) {
        runCatching {
            val folder = modelPath.substringBeforeLast('/', missingDelimiterValue = "")
            val files = if (folder.isEmpty()) context.assets.list("") else context.assets.list(folder)
            Log.i(TAG, "Assets en '$folder': ${files?.joinToString() ?: "(sin archivos)"}")
        }.onFailure {
            Log.e(TAG, "No se pudo listar assets: ${it.message}", it)
        }
    }

    // 1) Carga segura del modelo (si falla, no crashea)
    LaunchedEffect(modelPath) {
        runCatching {
            require(modelPath.isNotBlank()) { "Ruta vacía de modelo" }
            require(modelPath.endsWith(".glb", true) || modelPath.endsWith(".gltf", true)) {
                "El modelo debe ser .glb o .gltf en assets/"
            }
            modelLoader.createModelInstance(modelPath)
        }.onSuccess {
            model = it
            errorMsg = null
        }.onFailure { e ->
            errorMsg = "No se pudo cargar '$modelPath': ${e.message}"
            Log.e(TAG, "Error creando ModelInstance para '$modelPath'", e)
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    // 2) Cuando el modelo esté listo, agrégalo a la escena
    LaunchedEffect(model) {
        val instance = model ?: return@LaunchedEffect
        childNodes.clear()
        childNodes += ModelNode(
            modelInstance = instance,
            scaleToUnits = 1.0f,     // normaliza ~1 m
            autoAnimate = true
        )
        Log.i(TAG, "Modelo agregado a la escena: $modelPath")
    }

    // 3) Escena 3D simple (sin onCreate, sin cámara ni luces extra)
    Box(Modifier.fillMaxSize()) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            childNodes = childNodes
        )

        // Si hubo error, muéstralo en overlay para diagnosticar rápido
        if (errorMsg != null) {
            Text(
                text = errorMsg ?: "",
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
