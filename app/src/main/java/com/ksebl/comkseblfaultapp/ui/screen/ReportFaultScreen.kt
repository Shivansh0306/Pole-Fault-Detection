package com.ksebl.comkseblfaultapp.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ksebl.comkseblfaultapp.ui.components.MapComponent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ksebl.comkseblfaultapp.viewmodel.MainViewModel
import com.ksebl.comkseblfaultapp.model.request.ReportFaultRequest
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import org.koin.androidx.compose.koinViewModel

data class NodeStatus(val name: String) {
    companion object {
        val NORMAL = NodeStatus("NORMAL")
        val FAULTY = NodeStatus("FAULTY")
        val OFFLINE = NodeStatus("OFFLINE")
    }
}

data class Node(
    val id: String,
    val status: NodeStatus,
    val mlConfidence: Double
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFaultScreen(navController: NavController, mainViewModel: MainViewModel = koinViewModel()) {
    var selectedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var description by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var confidence by remember { mutableStateOf(90.0) }
    var isSubmitting by remember { mutableStateOf(false) }
    val error by mainViewModel.error.collectAsState()
    val nodes by mainViewModel.nodes.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImages = selectedImages + it
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFF),
                        Color(0xFFE8F2FF)
                    )
                )
            )
    ) {
    Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ModernTopAppBar(
                    title = "Report Fault",
                    onBackClick = { navController.popBackStack() }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Progress Indicator
                item {
                    StepProgressIndicator(
                        currentStep = when {
                            selectedLocation == null -> 1
                            description.isBlank() -> 2
                            selectedImages.isEmpty() -> 3
                            else -> 4
                        },
                        totalSteps = 4
                    )
                }

                // Step 1: Location Selection
                item {
                    ModernSectionCard(
                        title = "Select Location",
                        subtitle = "Tap on the map to mark the fault location",
                        stepNumber = 1,
                        isCompleted = selectedLocation != null
                    ) {
                        ModernMapCard(
                            selectedLocation = selectedLocation,
                            onLocationSelected = { lat, lng ->
                                selectedLocation = Pair(lat, lng)
                            }
                        )
                    }
                }


                // Step 3: Description
                item {
                    ModernSectionCard(
                        title = "Fault Description",
                        subtitle = "Describe what you observed",
                        stepNumber = 2,
                        isCompleted = description.isNotBlank()
                    ) {
                        ModernDescriptionField(
                            value = description,
                            onValueChange = { description = it }
                        )
                    }
                }

                // Step 4: Image Upload
                item {
                    ModernSectionCard(
                        title = "Add Photos",
                        subtitle = "Upload images to help identify the fault",
                        stepNumber = 3,
                        isCompleted = selectedImages.isNotEmpty()
                    ) {
                        ModernImageUploadSection(
                            selectedImages = selectedImages,
                            onCameraClick = { /* Camera functionality */ },
                            onGalleryClick = { galleryLauncher.launch("image/*") },
                            onRemoveImage = { uri ->
                                selectedImages = selectedImages - uri
                            }
                        )
                    }
                }

                // Confidence slider
                item {
                    ModernSectionCard(
                        title = "Confidence",
                        subtitle = "How confident are you about this fault?",
                        stepNumber = 3,
                        isCompleted = true
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Slider(value = confidence.toFloat(), onValueChange = { confidence = it.toDouble() }, valueRange = 0f..100f)
                            Text(text = "${confidence.toInt()}% confidence")
                        }
                    }
                }

                // Submit Section
                item {
                    ModernSubmitSection(
                        isEnabled = selectedLocation != null && description.isNotBlank(),
                        isSubmitting = isSubmitting,
                        onSubmit = {
                            if (selectedLocation != null && description.isNotBlank()) {
                                coroutineScope.launch {
                                    isSubmitting = true
                                    // Choose nearest node to selected location
                                    val nearest = nodes.minByOrNull { n ->
                                        val dx = n.location.latitude - selectedLocation!!.first
                                        val dy = n.location.longitude - selectedLocation!!.second
                                        dx*dx + dy*dy
                                    }
                                    // Optional upload (await)
                                    var imageUrl: String? = null
                                    selectedImages.firstOrNull()?.let { uri ->
                                        try {
                                            val file = copyUriToCache(context, uri)
                                            val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                                            val part = MultipartBody.Part.createFormData("file", file.name, body)
                                            imageUrl = mainViewModel.uploadImage(part)
                                        } catch (_: Exception) { }
                                    }
                                    mainViewModel.reportFault(
                                        ReportFaultRequest(
                                            nodeId = nearest?.id?.toLongOrNull() ?: 0L,
                                            description = description,
                                            confidence = confidence,
                                            imageUrl = imageUrl
                                        )
                                    )
                                    isSubmitting = false
                                    navController.popBackStack()
                                }
                            }
                        }
                    )
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
    if (error != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { mainViewModel.clearError() }) {
                    Text("Dismiss")
                }
            }
        ) { Text(error ?: "") }
    }
}

private fun copyUriToCache(context: android.content.Context, uri: Uri): File {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val tempFile = File.createTempFile("upload_", ".tmp", context.cacheDir)
    FileOutputStream(tempFile).use { out ->
        inputStream?.use { input ->
            input.copyTo(out)
        }
    }
    return tempFile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopAppBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1A237E)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color(0xFF1A237E)
        )
    )
}

@Composable
fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Progress",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalSteps) { step ->
                    val isCompleted = step < currentStep
                    val isCurrent = step == currentStep - 1

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = when {
                                    isCompleted -> Color(0xFF26DE81)
                                    isCurrent -> Color(0xFF667EEA)
                                    else -> Color.Gray.copy(alpha = 0.3f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = "${step + 1}",
                                color = if (isCurrent) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    if (step < totalSteps - 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    color = if (step < currentStep - 1)
                                        Color(0xFF26DE81)
                                    else Color.Gray.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernSectionCard(
    title: String,
    subtitle: String,
    stepNumber: Int,
    isCompleted: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (isCompleted) Color(0xFF26DE81) else Color(0xFF667EEA),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )

                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            content()
        }
    }
}

@Composable
fun ModernMapCard(
    selectedLocation: Pair<Double, Double>?,
    onLocationSelected: (Double, Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            MapComponent(
                selectedLocation = selectedLocation,
                onLocationSelected = onLocationSelected,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )

            if (selectedLocation != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    color = Color(0xFF26DE81),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Location Selected",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernNodeInfoCard(node: Node) {
    val statusColor = when (node.status.name) {
        "NORMAL" -> Color(0xFF26DE81)
        "FAULTY" -> Color(0xFFFF6B6B)
        "OFFLINE" -> Color(0xFF95A5A6)
        else -> Color(0xFF95A5A6)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Nearest Node Detected",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Node ${node.id}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3748)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = statusColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = node.status.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = statusColor,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "ML Confidence",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${(node.mlConfidence * 100).toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDescriptionField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Describe the fault") },
        placeholder = { Text("e.g., Sparking wires, damaged pole, unusual sounds...") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 4,
        maxLines = 6,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF667EEA),
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
            focusedLabelColor = Color(0xFF667EEA),
            cursorColor = Color(0xFF667EEA)
        )
    )
}

@Composable
fun ModernImageUploadSection(
    selectedImages: List<Uri>,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ModernUploadButton(
                text = "Camera",
                icon = Icons.Default.Add,
                onClick = onCameraClick,
                colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
                modifier = Modifier.weight(1f)
            )

            ModernUploadButton(
                text = "Gallery",
                icon = Icons.Default.Email,
                onClick = onGalleryClick,
                colors = listOf(Color(0xFF26DE81), Color(0xFF20BF6B)),
                modifier = Modifier.weight(1f)
            )
        }

        if (selectedImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${selectedImages.size} photo(s) selected",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(selectedImages) { uri ->
                    ModernImagePreview(
                        uri = uri,
                        onRemove = { onRemoveImage(uri) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernUploadButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(colors)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ModernImagePreview(
    uri: Uri,
    onRemove: () -> Unit
) {
    Box {
        Card(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .size(24.dp)
                .background(
                    Color(0xFFFF6B6B),
                    shape = CircleShape
                )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ModernSubmitSection(
    isEnabled: Boolean,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Ready to Submit?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your report will help us maintain the electrical infrastructure",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            ModernSubmitButton(
                isEnabled = isEnabled,
                isSubmitting = isSubmitting,
                onClick = onSubmit
            )
        }
    }
}

@Composable
fun ModernSubmitButton(
    isEnabled: Boolean,
    isSubmitting: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isEnabled && !isSubmitting) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button scale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        enabled = isEnabled && !isSubmitting,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isEnabled && !isSubmitting) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF26DE81),
                                Color(0xFF20BF6B)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Gray.copy(alpha = 0.5f),
                                Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSubmitting) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Submitting Report...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Report",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}