package com.example.mygreenhouse.ui.screens.dankbank

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygreenhouse.ui.components.GreenhouseBottomNavigation
import com.example.mygreenhouse.ui.components.HarvestTrackingSkeleton
import com.example.mygreenhouse.ui.components.SeedBankSkeleton
import com.example.mygreenhouse.ui.navigation.NavDestination
import com.example.mygreenhouse.ui.theme.DarkBackground
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextGrey
import com.example.mygreenhouse.ui.theme.TextWhite
import androidx.navigation.NavController
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.KeyboardType
import com.example.mygreenhouse.data.model.Harvest
import com.example.mygreenhouse.data.model.SeedType
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DankBankScreen(
    onNavigateBack: () -> Unit,
    viewModel: DankBankViewModel = viewModel(factory = DankBankViewModel.Factory),
    onNavigateToAddHarvest: () -> Unit = {},
    onNavigateToAddSeed: () -> Unit = {},
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState(initial = DankBankUiState())
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val harvestFilter by viewModel.harvestFilter.collectAsState()
    val seedTypeFilter by viewModel.seedTypeFilter.collectAsState()
    
    var showDryWeightDialog by remember { mutableStateOf<Harvest?>(null) }
    var showCuredWeightDialog by remember { mutableStateOf<Harvest?>(null) }
    var isSearchMode by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchMode) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                            textStyle = TextStyle(color = TextWhite, fontSize = 16.sp),
                            cursorBrush = SolidColor(PrimaryGreen),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .background(DarkSurface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search Icon",
                                        tint = TextGrey,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Box(Modifier.weight(1f)) {
                                        if (searchQuery.isEmpty()) {
                                            Text("Search...", color = TextGrey, fontSize = 16.sp)
                                        }
                                        innerTextField()
                                    }
                                }
                            }
                        )
                    } else {
                        Text("Dank Bank", color = TextWhite)
                    }
                },
                navigationIcon = {
                    if (isSearchMode) {
                        IconButton(onClick = {
                            isSearchMode = false
                            viewModel.setSearchQuery("")
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit Search",
                                tint = TextWhite
                            )
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextWhite
                            )
                        }
                    }
                },
                actions = {
                    if (isSearchMode) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = TextWhite
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = { isSearchMode = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextWhite
                            )
                        }
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = TextWhite
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextWhite
                )
            )
        },
        bottomBar = {
            GreenhouseBottomNavigation(
                currentRoute = NavDestination.DankBank.route,
                navController = navController
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (selectedTab == 0) {
                        onNavigateToAddHarvest()
                    } else {
                        onNavigateToAddSeed()
                    }
                },
                containerColor = PrimaryGreen,
                contentColor = TextWhite,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground)
        ) {
            if (uiState.isLoading && !isSearchMode) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            } else {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkBackground,
                    contentColor = PrimaryGreen,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        text = { Text("Harvest Tracking") },
                        selectedContentColor = PrimaryGreen,
                        unselectedContentColor = TextGrey
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        text = { Text("Seed Bank") },
                        selectedContentColor = PrimaryGreen,
                        unselectedContentColor = TextGrey
                    )
                }
                
                if (harvestFilter != HarvestFilterType.ALL && selectedTab == 0) {
                    FilterChip(
                        label = when(harvestFilter) {
                            HarvestFilterType.DRYING -> "Drying Only"
                            HarvestFilterType.CURING -> "Curing Only"
                            HarvestFilterType.COMPLETED -> "Completed Only"
                            else -> ""
                        },
                        onClear = { viewModel.setHarvestFilter(HarvestFilterType.ALL) }
                    )
                }
                
                val currentSeedTypeFilter = seedTypeFilter
                if (currentSeedTypeFilter != null && selectedTab == 1) {
                    FilterChip(
                        label = "Type: ${currentSeedTypeFilter.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        onClear = { viewModel.setSeedTypeFilter(null) }
                    )
                }
                
                if (searchQuery.isNotEmpty() && !isSearchMode) {
                    FilterChip(
                        label = "Search: $searchQuery",
                        onClear = { viewModel.setSearchQuery("") }
                    )
                }
                
                if (selectedTab == 0) {
                    HarvestStatsSection(uiState)
                } else {
                    SeedStatsSection(uiState)
                }
                
                when (selectedTab) {
                    0 -> HarvestTrackingContent(
                        viewModel = viewModel,
                        onMarkDryClick = { harvest -> showDryWeightDialog = harvest },
                        onMarkCuredClick = { harvest -> showCuredWeightDialog = harvest },
                        onEditHarvestClick = { harvest -> navController.navigate("editHarvest/${harvest.id}") },
                        onHarvestClick = { harvest -> navController.navigate("harvestDetail/${harvest.id}") }
                    )
                    1 -> SeedBankContent(
                        viewModel = viewModel,
                        onEditSeedClick = { seed -> navController.navigate("editSeed/${seed.id}") },
                        onSeedClick = { seed -> navController.navigate("seedDetail/${seed.id}") }
                    )
                }
            }
        }
    }

    showDryWeightDialog?.let { harvest ->
        DryWeightInputDialog(
            harvest = harvest,
            onDismiss = { showDryWeightDialog = null },
            onConfirm = { dryWeight ->
                viewModel.updateHarvestWithDryWeight(harvest.id, dryWeight)
                showDryWeightDialog = null
            }
        )
    }

    showCuredWeightDialog?.let { harvest ->
        CuredWeightInputDialog(
            harvest = harvest,
            onDismiss = { showCuredWeightDialog = null },
            onConfirm = { finalCuredWeight, qualityRating ->
                viewModel.completeHarvest(harvest.id, finalCuredWeight, qualityRating = qualityRating)
                showCuredWeightDialog = null
            }
        )
    }
    
    if (showFilterDialog) {
        FilterDialog(
            selectedTab = selectedTab,
            currentHarvestFilter = harvestFilter,
            currentSeedTypeFilter = seedTypeFilter,
            onHarvestFilterSelected = { filter -> 
                viewModel.setHarvestFilter(filter)
            },
            onSeedTypeFilterSelected = { type -> 
                viewModel.setSeedTypeFilter(type)
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = PrimaryGreen,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(
            onClick = onClear,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear filter",
                tint = PrimaryGreen,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun FilterDialog(
    selectedTab: Int,
    currentHarvestFilter: HarvestFilterType,
    currentSeedTypeFilter: SeedType?,
    onHarvestFilterSelected: (HarvestFilterType) -> Unit,
    onSeedTypeFilterSelected: (SeedType?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter", color = TextWhite) },
        text = {
            Column {
                if (selectedTab == 0) {
                    Text("Filter Harvests By Status", 
                        color = TextWhite, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    FilterOption(
                        text = "All Harvests",
                        isSelected = currentHarvestFilter == HarvestFilterType.ALL,
                        onClick = { onHarvestFilterSelected(HarvestFilterType.ALL) }
                    )
                    
                    FilterOption(
                        text = "Drying Only",
                        isSelected = currentHarvestFilter == HarvestFilterType.DRYING,
                        onClick = { onHarvestFilterSelected(HarvestFilterType.DRYING) }
                    )
                    
                    FilterOption(
                        text = "Curing Only",
                        isSelected = currentHarvestFilter == HarvestFilterType.CURING,
                        onClick = { onHarvestFilterSelected(HarvestFilterType.CURING) }
                    )
                    
                    FilterOption(
                        text = "Completed Only",
                        isSelected = currentHarvestFilter == HarvestFilterType.COMPLETED,
                        onClick = { onHarvestFilterSelected(HarvestFilterType.COMPLETED) }
                    )
                } else {
                    Text("Filter Seeds By Type", 
                        color = TextWhite, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    FilterOption(
                        text = "All Types",
                        isSelected = currentSeedTypeFilter == null,
                        onClick = { onSeedTypeFilterSelected(null) }
                    )
                    
                    FilterOption(
                        text = "Regular Seeds",
                        isSelected = currentSeedTypeFilter == SeedType.REGULAR,
                        onClick = { onSeedTypeFilterSelected(SeedType.REGULAR) }
                    )
                    
                    FilterOption(
                        text = "Feminized Seeds",
                        isSelected = currentSeedTypeFilter == SeedType.FEMINIZED,
                        onClick = { onSeedTypeFilterSelected(SeedType.FEMINIZED) }
                    )
                    
                    FilterOption(
                        text = "Autoflower Seeds",
                        isSelected = currentSeedTypeFilter == SeedType.AUTOFLOWER,
                        onClick = { onSeedTypeFilterSelected(SeedType.AUTOFLOWER) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PrimaryGreen)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun FilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = PrimaryGreen,
                uncheckedColor = TextGrey,
                checkmarkColor = TextWhite
            )
        )
        Text(
            text = text,
            color = TextWhite,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun HarvestStatsSection(uiState: DankBankUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Harvest Statistics",
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(
                title = "Total Harvested",
                value = "${String.format("%.1f", uiState.totalHarvestedWeight)}g",
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            StatCard(
                title = "Drying",
                value = "${uiState.dryingCount}",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(
                title = "Curing",
                value = "${uiState.curingCount}",
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            StatCard(
                title = "Completed",
                value = "${uiState.completedCount}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SeedStatsSection(uiState: DankBankUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Seed Bank Statistics",
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(
                title = "Total Seeds",
                value = "${uiState.totalSeedCount}",
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            StatCard(
                title = "Unique Strains",
                value = "${uiState.uniqueStrainCount}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = TextGrey,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                color = PrimaryGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HarvestTrackingContent(
    viewModel: DankBankViewModel,
    onMarkDryClick: (Harvest) -> Unit,
    onMarkCuredClick: (Harvest) -> Unit,
    onEditHarvestClick: (Harvest) -> Unit,
    onHarvestClick: (Harvest) -> Unit
) {
    val filteredHarvests by viewModel.filteredHarvests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val showSkeleton = isLoading && filteredHarvests.isEmpty() && searchQuery.isEmpty()

    if (showSkeleton) {
        HarvestTrackingSkeleton()
    } else if (filteredHarvests.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (searchQuery.isNotEmpty() || viewModel.harvestFilter.value != HarvestFilterType.ALL) 
                        "No harvests found with current filters."
                       else 
                        "No harvests recorded yet.\nTap the + button to add your first harvest.",
                color = TextGrey,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(filteredHarvests) { harvest ->
                HarvestListItem(
                    harvest = harvest,
                    onHarvestClick = onHarvestClick,
                    onEdit = onEditHarvestClick,
                    onDelete = { viewModel.deleteHarvest(it) },
                    onMarkDryClick = onMarkDryClick,
                    onMarkCuredClick = onMarkCuredClick
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SeedBankContent(
    viewModel: DankBankViewModel, 
    onEditSeedClick: (com.example.mygreenhouse.data.model.Seed) -> Unit,
    onSeedClick: (com.example.mygreenhouse.data.model.Seed) -> Unit
) {
    val filteredSeeds by viewModel.filteredSeeds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val showSkeleton = isLoading && filteredSeeds.isEmpty() && searchQuery.isEmpty()

    if (showSkeleton) {
        SeedBankSkeleton()
    } else if (filteredSeeds.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (searchQuery.isNotEmpty() || viewModel.seedTypeFilter.value != null)
                        "No seeds found with current filters."
                       else
                        "Your seed bank is empty.\nTap the + button to add seeds to your collection.",
                color = TextGrey,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(filteredSeeds) { seed ->
                SeedListItem(
                    seed = seed,
                    onSeedClick = onSeedClick,
                    onEdit = onEditSeedClick,
                    onDelete = { viewModel.deleteSeed(it) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DryWeightInputDialog(
    harvest: Harvest,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var dryWeightInput by remember { mutableStateOf(harvest.dryWeight?.toString() ?: "") }
    val isError = dryWeightInput.toDoubleOrNull() == null && dryWeightInput.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Dry Weight", color = TextWhite) },
        text = {
            Column {
                Text("Enter the dry weight in grams for harvest of ${harvest.strainName} (Batch #${harvest.batchNumber}).", color = TextGrey)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = dryWeightInput,
                    onValueChange = { dryWeightInput = it },
                    label = { Text("Dry Weight (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface.copy(alpha = 0.5f),
                        unfocusedContainerColor = DarkSurface.copy(alpha = 0.3f),
                        disabledContainerColor = DarkSurface.copy(alpha = 0.3f),
                        cursorColor = PrimaryGreen,
                        focusedIndicatorColor = PrimaryGreen,
                        focusedLabelColor = TextWhite.copy(alpha = 0.8f),
                        unfocusedLabelColor = TextGrey,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        errorCursorColor = MaterialTheme.colorScheme.error,
                        errorIndicatorColor = MaterialTheme.colorScheme.error
                    )
                )
                if (isError) {
                    Text("Please enter a valid number", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    dryWeightInput.toDoubleOrNull()?.let {
                        onConfirm(it)
                    }
                },
                enabled = dryWeightInput.toDoubleOrNull() != null
            ) {
                Text("Confirm", color = PrimaryGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGrey)
            }
        },
        containerColor = DarkSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuredWeightInputDialog(
    harvest: Harvest,
    onDismiss: () -> Unit,
    onConfirm: (Double, Int?) -> Unit
) {
    var finalCuredWeightInput by remember { mutableStateOf(harvest.finalCuredWeight?.toString() ?: "") }
    var qualityRatingInput by remember { mutableStateOf(harvest.qualityRating?.toString() ?: "") }

    val isWeightError = finalCuredWeightInput.toDoubleOrNull() == null && finalCuredWeightInput.isNotEmpty()
    val isRatingError = qualityRatingInput.toIntOrNull() == null && qualityRatingInput.isNotEmpty() ||
            (qualityRatingInput.toIntOrNull() != null && (qualityRatingInput.toInt() < 1 || qualityRatingInput.toInt() > 5))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Cured Weight & Quality", color = TextWhite) },
        text = {
            Column {
                Text("Enter the final cured weight (grams) and optionally a quality rating (1-5) for ${harvest.strainName} (Batch #${harvest.batchNumber}).", color = TextGrey)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = finalCuredWeightInput,
                    onValueChange = { finalCuredWeightInput = it },
                    label = { Text("Final Cured Weight (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isWeightError,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface.copy(alpha = 0.5f),
                        unfocusedContainerColor = DarkSurface.copy(alpha = 0.3f),
                        disabledContainerColor = DarkSurface.copy(alpha = 0.3f),
                        cursorColor = PrimaryGreen,
                        focusedIndicatorColor = PrimaryGreen,
                        focusedLabelColor = TextWhite.copy(alpha = 0.8f),
                        unfocusedLabelColor = TextGrey,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        errorCursorColor = MaterialTheme.colorScheme.error,
                        errorIndicatorColor = MaterialTheme.colorScheme.error
                    )
                )
                if (isWeightError) {
                    Text("Please enter a valid weight", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = qualityRatingInput,
                    onValueChange = { qualityRatingInput = it },
                    label = { Text("Quality Rating (1-5, Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isRatingError,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface.copy(alpha = 0.5f),
                        unfocusedContainerColor = DarkSurface.copy(alpha = 0.3f),
                        disabledContainerColor = DarkSurface.copy(alpha = 0.3f),
                        cursorColor = PrimaryGreen,
                        focusedIndicatorColor = PrimaryGreen,
                        focusedLabelColor = TextWhite.copy(alpha = 0.8f),
                        unfocusedLabelColor = TextGrey,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        errorCursorColor = MaterialTheme.colorScheme.error,
                        errorIndicatorColor = MaterialTheme.colorScheme.error
                    )
                )
                if (isRatingError) {
                    Text("Rating must be a number between 1 and 5", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    finalCuredWeightInput.toDoubleOrNull()?.let { weight ->
                        onConfirm(weight, qualityRatingInput.toIntOrNull())
                    }
                },
                enabled = finalCuredWeightInput.toDoubleOrNull() != null && !isRatingError
            ) {
                Text("Confirm", color = PrimaryGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGrey)
            }
        },
        containerColor = DarkSurface
    )
} 