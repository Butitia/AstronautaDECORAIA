package com.decoraia.app.ui.screens

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.decoraia.app.data.ProductoAR
import com.decoraia.app.data.repo.FavoritosRepository
import com.decoraia.app.data.repo.ProductRepository
import com.decoraia.app.data.repo.ProductRepositoryImpl
import com.decoraia.app.data.repo.RAProductsRepo
import com.decoraia.app.ui.components.RAModelosScreenUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun PantallaRAModelos(
    nav: NavHostController,
    style: String,
    categoryId: String,
    productRepo: ProductRepository = ProductRepositoryImpl(),
    favRepo: FavoritosRepository = FavoritosRepository()
) {
    val categoria = remember(categoryId) {
        RAProductsRepo.categoriasFijas.firstOrNull { it.id == categoryId }
            ?: RAProductsRepo.categoriasFijas.first()
    }

    var loading by remember { mutableStateOf(true) }
    var modelos by remember { mutableStateOf(emptyList<ProductoAR>()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val uid = remember { FirebaseAuth.getInstance().currentUser?.uid }
    var favoriteIds by remember { mutableStateOf(setOf<String>()) }

    // ---- Carga desde el repositorio: conserva imágenes y limita a 4 ----
    LaunchedEffect(style, categoryId) {
        loading = true
        errorMsg = null
        try {
            val todos = productRepo.loadBy(style = style, typeValue = categoria.typeValue)
            modelos = todos.take(4)   // <= muestra solo 4, manteniendo thumbnails del repo
        } catch (e: Exception) {
            errorMsg = e.message
            modelos = emptyList()
        } finally {
            loading = false
        }
    }

    LaunchedEffect(uid) {
        if (uid == null) {
            errorMsg = "Debes iniciar sesión para gestionar favoritos."
            return@LaunchedEffect
        }
        favRepo.listenFavoritosIds(uid).collectLatest { ids -> favoriteIds = ids }
    }

    fun toggleFavorite(item: ProductoAR) {
        val currentUid = uid ?: return
        scope.launch {
            runCatching {
                if (favoriteIds.contains(item.id)) favRepo.removeFavorito(currentUid, item.id)
                else favRepo.addFavorito(currentUid, item)
            }
        }
    }

    // Mapea índice a nombre de ruta (para tus 16 modelos locales)
    fun routeNameFor(categoryId: String, index: Int): String = when (categoryId.lowercase()) {
        "jarrones" -> "jarron${index + 1}"
        "lamparas" -> "lampara${index + 1}"
        "cuadros"  -> "cuadro${index + 1}"
        "sofas"    -> "sofa${index + 1}"
        else       -> "jarron1" // fallback
    }

    RAModelosScreenUI(
        categoriaTitulo = categoria.label,
        modelos = modelos,
        loading = loading,
        favoriteIds = favoriteIds,
        errorMsg = errorMsg,
        onBack = { nav.popBackStack() },

        onSelectModelo = { modelo ->
            // Calcula la ruta por posición (0..3) para abrir tu glb local correspondiente
            val idx = modelos.indexOf(modelo).coerceIn(0, 3)
            val route = routeNameFor(categoria.id, idx)
            nav.navigate("visualizacion/$route")
        },

        onToggleFavorite = { producto -> toggleFavorite(producto) },
        onHome = { nav.navigate("principal") { popUpTo("principal") { inclusive = true } } },
        onProfile = { nav.navigate("perfil") }
    )
}
