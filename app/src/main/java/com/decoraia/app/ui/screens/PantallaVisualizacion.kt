@file:Suppress("UnusedMaterial3ScaffoldPaddingParameter")

package com.decoraia.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

@Composable
fun PantallaVisualizacion(modelPath: String = "modelos/jarron1.glb") {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val childNodes = rememberNodes()
    var model by remember { mutableStateOf<ModelInstance?>(null) }

    LaunchedEffect(modelPath) {
        model = modelLoader.createModelInstance(modelPath)
    }

    LaunchedEffect(model) {
        val instance = model ?: return@LaunchedEffect
        childNodes.clear()
        childNodes += ModelNode(
            modelInstance = instance,
            scaleToUnits = 0.2f,                 // reduce tamaño global
            centerOrigin = Position(0f, -0.5f, 0f), // asienta el modelo
            autoAnimate = true
        )
    }

    Scene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        childNodes = childNodes
    )
}
