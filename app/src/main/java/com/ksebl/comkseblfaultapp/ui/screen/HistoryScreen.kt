package com.ksebl.comkseblfaultapp.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import com.ksebl.comkseblfaultapp.viewmodel.MainViewModel
import com.ksebl.comkseblfaultapp.model.Fault

@Composable
fun HistoryScreen(navController: NavController, mainViewModel: MainViewModel = koinViewModel()) {
    val faults by mainViewModel.faults.collectAsState()
    val isEmpty = faults.isEmpty()
    LaunchedEffect(Unit) { mainViewModel.fetchFaults() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("History") }) }
    ) { padding ->
        if (isEmpty) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No history yet")
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(faults) { fault: Fault ->
                    ListItem(
                        headlineContent = { Text(fault.description) },
                        supportingContent = { Text("Node ${fault.nodeId} • ${fault.reportedAt}") },
                        overlineContent = { Text(fault.status) }
                    )
                    Divider(thickness = 0.5.dp)
                }
            }
        }
    }
}
