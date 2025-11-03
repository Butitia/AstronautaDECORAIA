package com.decoraia.app.ui.nav

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.decoraia.app.ui.screens.*

@Composable
fun AppNavGraph(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val start = if (auth.currentUser != null) "principal" else "carga"

    NavHost(navController = navController, startDestination = start) {

        composable("acercade") { PantallaAcercaDe(navController) }
        composable("ajustescuenta") { PantallaAjustesCuenta(navController) }
        composable("carga") { PantallaCarga(navController) }

        // Historial
        composable("chatguardados") { PantallaChatGuardados(navController) }

        // Chat
        composable("chatia") { PantallaChatIA(navController = navController, chatId = null) }
        composable(
            route = "chatia/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")!!
            PantallaChatIA(navController = navController, chatId = sessionId)
        }

        composable("configuracion") { PantallaConfiguracion(navController) }
        composable("descripcion") { PantallaDescripcion(navController) }
        composable("editarperfil") { PantallaEditarPerfil(navController) }
        composable("favoritos") { PantallaFavoritos(navController) }
        composable("inicio") { PantallaInicio(navController) }
        composable("login") { PantallaLogin(navController) }
        composable("mensajesalida") { PantallaMensajeSalida(navController) }
        composable("olvidocontrasena") { PantallaOlvidoContrasena(navController) }
        composable("perfil") { PantallaPerfil(navController) }
        composable("principal") { PantallaPrincipal(navController) }
        composable("registro") { PantallaRegistro(navController) }
        composable("salidaperfil") { PantallaSalidaPerfil(navController) }
        composable("soporte") { PantallaSoporte(navController) }

        // --- Visualización 3D ---
        composable("visualizacion") { PantallaVisualizacion() }

        // Ruta genérica con path (decodificar es clave)
        composable(
            route = "visualizacion/{modelPath}",
            arguments = listOf(
                navArgument("modelPath") {
                    type = NavType.StringType
                    defaultValue = "modelos/jarron1.glb"
                }
            )
        ) { backStackEntry ->
            val raw = backStackEntry.arguments?.getString("modelPath") ?: "modelos/jarron1.glb"
            val decoded = Uri.decode(raw)
            PantallaVisualizacion(modelPath = decoded)
        }

        // Rutas fijas para los 16 modelos
        val modelos3D = listOf(
            "jarron1","jarron2","jarron3","jarron4",
            "lampara1","lampara2","lampara3","lampara4",
            "cuadro1","cuadro2","cuadro3","cuadro4",
            "sofa1","sofa2","sofa3","sofa4"
        )
        modelos3D.forEach { nombre ->
            composable("visualizacion/$nombre") {
                PantallaVisualizacion(modelPath = "modelos/$nombre.glb")
            }
        }

        // --- RA ---
        composable("raestilos") { PantallaRAEstilos(navController) }
        composable(
            route = "raobjetos/{style}",
            arguments = listOf(navArgument("style") { type = NavType.StringType })
        ) { backStackEntry ->
            val style = Uri.decode(backStackEntry.arguments?.getString("style").orEmpty())
            PantallaRAObjetos(navController, style = style)
        }
        composable(
            route = "ramodelos/{style}/{categoryId}",
            arguments = listOf(
                navArgument("style") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val style = Uri.decode(backStackEntry.arguments?.getString("style").orEmpty())
            val categoryId = backStackEntry.arguments?.getString("categoryId").orEmpty()
            PantallaRAModelos(navController, style = style, categoryId = categoryId)
        }
    }
}
