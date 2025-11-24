package com.ksebl.comkseblfaultapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import com.ksebl.comkseblfaultapp.viewmodel.MainViewModel

@Composable
fun FaultDetailScreen(navController: NavController, faultId: String?, mainViewModel: MainViewModel = koinViewModel()) {
    val faults by mainViewModel.faults.collectAsState()
    LaunchedEffect(Unit) { mainViewModel.fetchFaults() }
    val fault = remember(faultId, faults) { faults.firstOrNull { it.id == faultId } }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Fault Details") })
        }
    ) { padding ->
        if (fault == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Fault not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Fault #${fault.id}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(fault.description)
                Text("Status: ${fault.status}")
                Text("Node: ${fault.nodeId}")
                Text("Reported at: ${fault.reportedAt}")
                fault.imageUrl?.let { url ->
                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp))
                }
                Button(onClick = { navController.popBackStack() }) { Text("Back") }
            }
        }
    }
}
