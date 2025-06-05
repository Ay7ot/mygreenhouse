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
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PagerState
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DankBankScreen(
    onNavigateBack: () -> Unit,
    viewModel: DankBankViewModel = viewModel(factory = DankBankViewModel.Factory),
    onNavigateToAddHarvest: () -> Unit = {},
    onNavigateToAddSeed: () -> Unit = {},
    navController: NavController,
    darkTheme: Boolean
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
                            textStyle = TextStyle(
                                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface, 
                                fontSize = 16.sp
                            ),
                            cursorBrush = SolidColor(if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .background(
                                            if (darkTheme) DarkSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), 
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search Icon",
                                        tint = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Box(Modifier.weight(1f)) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "Search...", 
                                                color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), 
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            }
                        )
                    } else {
                        Text("Dank Bank", color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface)
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
                                tint = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
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
                                    tint = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = { isSearchMode = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (darkTheme) DarkBackground else MaterialTheme.colorScheme.surface,
                    titleContentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            GreenhouseBottomNavigation(
                currentRoute = NavDestination.DankBank.route,
                navController = navController,
                darkTheme = darkTheme
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
                containerColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                contentColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary,
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
                .background(if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = if (darkTheme) DarkBackground else MaterialTheme.colorScheme.background,
                contentColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { Text("Harvest Tracking") },
                    selectedContentColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                    unselectedContentColor = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { Text("Seed Bank") },
                    selectedContentColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                    unselectedContentColor = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                    onClear = { viewModel.setHarvestFilter(HarvestFilterType.ALL) },
                    darkTheme = darkTheme
                )
            }
            
            val currentSeedTypeFilter = seedTypeFilter
            if (currentSeedTypeFilter != null && selectedTab == 1) {
                FilterChip(
                    label = "Type: ${currentSeedTypeFilter.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    onClear = { viewModel.setSeedTypeFilter(null) },
                    darkTheme = darkTheme
                )
            }
            
            if (searchQuery.isNotEmpty() && !isSearchMode) {
                FilterChip(
                    label = "Search: $searchQuery",
                    onClear = { viewModel.setSearchQuery("") },
                    darkTheme = darkTheme
                )
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> HarvestTrackingContent(
                        viewModel = viewModel,
                        uiState = uiState,
                        onMarkDryClick = { harvest -> showDryWeightDialog = harvest },
                        onMarkCuredClick = { harvest -> showCuredWeightDialog = harvest },
                        onEditHarvestClick = { harvest -> navController.navigate("editHarvest/${harvest.id}") },
                        onHarvestClick = { harvest -> navController.navigate("harvestDetail/${harvest.id}") },
                        darkTheme = darkTheme,
                        passedSearchQuery = searchQuery,
                        passedHarvestFilter = harvestFilter
                    )
                    1 -> SeedBankContent(
                        viewModel = viewModel,
                        uiState = uiState,
                        onEditSeedClick = { seed -> navController.navigate("editSeed/${seed.id}") },
                        onSeedClick = { seed -> navController.navigate("seedDetail/${seed.id}") },
                        darkTheme = darkTheme,
                        passedSearchQuery = searchQuery,
                        passedSeedTypeFilter = seedTypeFilter
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
            },
            darkTheme = darkTheme
        )
    }

    showCuredWeightDialog?.let { harvest ->
        CuredWeightInputDialog(
            harvest = harvest,
            onDismiss = { showCuredWeightDialog = null },
            onConfirm = { finalCuredWeight, qualityRating ->
                viewModel.completeHarvest(harvest.id, finalCuredWeight, qualityRating = qualityRating)
                showCuredWeightDialog = null
            },
            darkTheme = darkTheme
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
            onDismiss = { showFilterDialog = false },
            darkTheme = darkTheme
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    darkTheme: Boolean
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(
                if (darkTheme) 
                    PrimaryGreen.copy(alpha = 0.1f) 
                else 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                RoundedCornerShape(16.dp)
            )
            .padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
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
                tint = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
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
    onDismiss: () -> Unit,
    darkTheme: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Filter", 
                color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
            ) 
        },
        text = {
            Column {
                if (selectedTab == 0) {
                    Text(
                        "Filter Harvests By Status", 
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    FilterOption(
                        text = "All Harvests",
                        isSelected = currentHarvestFilter == HarvestFilterType.ALL,
                        onClick = { onHarvestFilterSelected(HarvestFilterType.ALL) },
                        darkTheme = darkTheme
                    )
                    
                    FilterOption(
                        text = "Drying Only",
                        isSelected = currentHarvestFilter == HarvestFilterType.DRYING,
                        onClick = { onHarvestFilterSelected(HarvestFilterType.DRYING) },
                        darkTheme = darkTheme
                    )
                    
                    FilterOption(
                        text = "Curing Only",
                        isSelected = currentHarvestFilter == HarvestFilterType.CURING,
                        onClick = { onHarvestFilterSelected(HarvestFilterType.CURING) },
                        darkTheme = darkTheme
                    )
                    
                    FilterOption(
                        text = "Completed Only",
                        isSelected = currentHarvestFilter == HarvestFilterType.COMPLETED,
                        onClick = { onHarvestFilterSelected(HarvestFilterType.COMPLETED) },
                        darkTheme = darkTheme
                    )
                } else {
                    Text(
                        "Filter Seeds By Type", 
                        color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    FilterOption(
                        text = "All Types",
                        isSelected = currentSeedTypeFilter == null,
                        onClick = { onSeedTypeFilterSelected(null) },
                        darkTheme = darkTheme
                    )
                    
                    FilterOption(
                        text = "Autoflower Regular",
                        isSelected = currentSeedTypeFilter == SeedType.AUTOFLOWER_REGULAR,
                        onClick = { onSeedTypeFilterSelected(SeedType.AUTOFLOWER_REGULAR) },
                        darkTheme = darkTheme
                    )
                    
                    FilterOption(
                        text = "Autoflower Feminized",
                        isSelected = currentSeedTypeFilter == SeedType.AUTOFLOWER_FEMINIZED,
                        onClick = { onSeedTypeFilterSelected(SeedType.AUTOFLOWER_FEMINIZED) },
                        darkTheme = darkTheme
                    )
                    
                    FilterOption(
                        text = "Photoperiod Regular",
                        isSelected = currentSeedTypeFilter == SeedType.PHOTOPERIOD_REGULAR,
                        onClick = { onSeedTypeFilterSelected(SeedType.PHOTOPERIOD_REGULAR) },
                        darkTheme = darkTheme
                    )
                    
                    FilterOption(
                        text = "Photoperiod Feminized",
                        isSelected = currentSeedTypeFilter == SeedType.PHOTOPERIOD_FEMINIZED,
                        onClick = { onSeedTypeFilterSelected(SeedType.PHOTOPERIOD_FEMINIZED) },
                        darkTheme = darkTheme
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Close", 
                    color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface
    )
}

@Composable
fun FilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    darkTheme: Boolean
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
                checkedColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                uncheckedColor = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                checkmarkColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onPrimary
            )
        )
        Text(
            text = text,
            color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun HarvestTrackingContent(
    viewModel: DankBankViewModel,
    uiState: DankBankUiState,
    onMarkDryClick: (Harvest) -> Unit,
    onMarkCuredClick: (Harvest) -> Unit,
    onEditHarvestClick: (Harvest) -> Unit,
    onHarvestClick: (Harvest) -> Unit,
    darkTheme: Boolean,
    passedSearchQuery: String,
    passedHarvestFilter: HarvestFilterType
) {
    val filteredHarvests by viewModel.filteredHarvests.collectAsState()
    
    when {
        uiState.isLoading || filteredHarvests == null -> {
            HarvestTrackingSkeleton(darkTheme = darkTheme)
        }
        filteredHarvests?.isEmpty() == true && passedSearchQuery.isEmpty() && passedHarvestFilter == HarvestFilterType.ALL -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No harvests recorded yet.\nTap the + button to add your first harvest.",
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
        filteredHarvests?.isEmpty() == true -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No harvests found with current filters.",
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    HarvestStatsSection(uiState, darkTheme = darkTheme)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(filteredHarvests ?: emptyList()) { harvest ->
                    HarvestListItem(
                        harvest = harvest,
                        onHarvestClick = onHarvestClick,
                        onEdit = onEditHarvestClick,
                        onDelete = { viewModel.deleteHarvest(it) },
                        onMarkDryClick = onMarkDryClick,
                        onMarkCuredClick = onMarkCuredClick,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        darkTheme = darkTheme
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun SeedBankContent(
    viewModel: DankBankViewModel,
    uiState: DankBankUiState,
    onEditSeedClick: (com.example.mygreenhouse.data.model.Seed) -> Unit,
    onSeedClick: (com.example.mygreenhouse.data.model.Seed) -> Unit,
    darkTheme: Boolean,
    passedSearchQuery: String,
    passedSeedTypeFilter: SeedType?
) {
    val filteredSeeds by viewModel.filteredSeeds.collectAsState()
    
    when {
        uiState.isLoading || filteredSeeds == null -> {
            SeedBankSkeleton(darkTheme = darkTheme)
        }
        filteredSeeds?.isEmpty() == true && passedSearchQuery.isEmpty() && passedSeedTypeFilter == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your seed bank is empty.\nTap the + button to add seeds to your collection.",
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
        filteredSeeds?.isEmpty() == true -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No seeds found with current filters.",
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    SeedStatsSection(uiState, darkTheme = darkTheme)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(filteredSeeds ?: emptyList()) { seed ->
                    SeedListItem(
                        seed = seed,
                        onSeedClick = onSeedClick,
                        onEdit = onEditSeedClick,
                        onDelete = { viewModel.deleteSeed(it) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                        darkTheme = darkTheme
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DryWeightInputDialog(
    harvest: Harvest,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    darkTheme: Boolean
) {
    var dryWeightInput by remember { mutableStateOf(harvest.dryWeight?.toString() ?: "") }
    val isError = dryWeightInput.toDoubleOrNull() == null && dryWeightInput.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Dry Weight", color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                Text(
                    "Enter the dry weight in grams for harvest of ${harvest.strainName} (Batch #${harvest.batchNumber}).", 
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = dryWeightInput,
                    onValueChange = { dryWeightInput = it },
                    label = { Text("Dry Weight (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        disabledContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        cursorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        focusedLabelColor = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        unfocusedLabelColor = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
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
                Text("Confirm", color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuredWeightInputDialog(
    harvest: Harvest,
    onDismiss: () -> Unit,
    onConfirm: (Double, Int?) -> Unit,
    darkTheme: Boolean
) {
    var finalCuredWeightInput by remember { mutableStateOf(harvest.finalCuredWeight?.toString() ?: "") }
    var qualityRatingInput by remember { mutableStateOf(harvest.qualityRating?.toString() ?: "") }

    val isWeightError = finalCuredWeightInput.toDoubleOrNull() == null && finalCuredWeightInput.isNotEmpty()
    val isRatingError = qualityRatingInput.toIntOrNull() == null && qualityRatingInput.isNotEmpty() ||
            (qualityRatingInput.toIntOrNull() != null && (qualityRatingInput.toInt() < 1 || qualityRatingInput.toInt() > 5))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Cured Weight & Quality", color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                Text(
                    "Enter the final cured weight (grams) and optionally a quality rating (1-5) for ${harvest.strainName} (Batch #${harvest.batchNumber}).", 
                    color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = finalCuredWeightInput,
                    onValueChange = { finalCuredWeightInput = it },
                    label = { Text("Final Cured Weight (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isWeightError,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        disabledContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        cursorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        focusedLabelColor = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        unfocusedLabelColor = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
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
                        focusedContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        disabledContainerColor = if (darkTheme) DarkSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        cursorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                        focusedLabelColor = if (darkTheme) TextWhite.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        unfocusedLabelColor = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
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
                Text("Confirm", color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface
    )
}

@Composable
fun SimpleBarChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true
) {
    if (data.isEmpty() || data.values.sum() == 0) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No harvest data available yet",
                color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    // Define colors outside of Canvas scope
    val gridColor = if (darkTheme) TextWhite.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val completedColor = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
    
    Box(
        modifier = modifier.padding(
            start = 32.dp, // Space for y-axis labels
            end = 16.dp,
            top = 24.dp,
            bottom = 40.dp // Space for x-axis labels
        )
    ) {
        val values = data.values.toList()
        val maxValue = values.maxOrNull() ?: 0
        
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val barWidth = size.width / data.size * 0.6f
            val spacing = size.width / data.size * 0.4f
            val yScale = if (maxValue > 0) size.height / maxValue.toFloat() else 1f
            
            // Draw grid lines
            for (i in 0..4) {
                val y = size.height - (i * size.height / 4)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
            
            // Draw bars
            data.entries.toList().forEachIndexed { index, entry ->
                val barHeight = (entry.value * yScale).toFloat()
                val x = index * (barWidth + spacing) + spacing / 2
                
                if (entry.value > 0) {
                    // Use different colors for different categories
                    val barColor = when (entry.key) {
                        "Drying" -> Color(0xFFFFD54F) // Amber for drying
                        "Curing" -> Color(0xFFAED581) // Light green for curing
                        "Completed" -> completedColor
                        else -> completedColor
                    }
                    
                    drawRect(
                        color = barColor,
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidth, barHeight)
                    )
                }
            }
        }
        
        // Y-axis labels
        Column(
            modifier = Modifier
                .height(200.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-24).dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 5 downTo 0) {
                val value = if (maxValue > 0) ((maxValue * i) / 5) else i
                if (i < 5 || (i == 0 && maxValue > 0) ) {
                    Text(
                        text = value.toString(),
                        color = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
        
        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.keys.forEach { label ->
                Text(
                    text = label,
                    color = if (darkTheme) TextWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SimplePieChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true
) {
    if (data.isEmpty() || data.values.sum() == 0) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No seed data available yet",
                color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
        return
    }
    
    val total = data.values.sum().toFloat()
    val colors = listOf(
        Color(0xFF81C784),  // Green for Regular
        Color(0xFFE57373),  // Red for Feminized
        Color(0xFF64B5F6),  // Blue for Autoflower
        Color(0xFFFFD54F)   // Amber for additional types
    )
    
    // Define text color before Canvas scope
    val textColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pie Chart
        Canvas(
            modifier = Modifier.size(180.dp)
        ) {
            var startAngle = 0f
            
            data.entries.filter { it.value > 0 }.forEachIndexed { index, entry ->
                val sweepAngle = 360f * entry.value / total
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Legend - centered below the chart
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            data.entries.filter { it.value > 0 }.forEachIndexed { index, entry ->
                val percentage = (entry.value * 100f / total).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(colors[index % colors.size], shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${entry.key} ($percentage%)",
                        color = textColor,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HarvestStatsSection(uiState: DankBankUiState, darkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // Keep padding here for the section itself
    ) {
        Text(
            text = "Harvest Statistics",
            color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
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
                modifier = Modifier.weight(1f),
                darkTheme = darkTheme
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            StatCard(
                title = "Current Status",
                value = "${uiState.dryingCount + uiState.curingCount} Active",
                modifier = Modifier.weight(1f),
                darkTheme = darkTheme
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Harvest Status",
                    color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                SimpleBarChart(
                    data = mapOf(
                        "Drying" to uiState.dryingCount,
                        "Curing" to uiState.curingCount,
                        "Completed" to uiState.completedCount
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    darkTheme = darkTheme
                )
            }
        }
    }
}

@Composable
fun SeedStatsSection(uiState: DankBankUiState, darkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // Keep padding here for the section itself
    ) {
        Text(
            text = "Seed Bank Statistics",
            color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
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
                modifier = Modifier.weight(1f),
                darkTheme = darkTheme
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            StatCard(
                title = "Custom Strains",
                value = "${uiState.customStrainCount}",
                modifier = Modifier.weight(1f),
                darkTheme = darkTheme
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SeedBankChartsPager(uiState, darkTheme)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeedBankChartsPager(uiState: DankBankUiState, darkTheme: Boolean) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    // Chart titles
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Seed Type Distribution",
            color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .clickable { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
        )
        
        Text(
            text = "Seeds Per Strain",
            color = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .clickable { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) { page ->
                when (page) {
                    0 -> SimplePieChart(
                        data = mapOf(
                            "Autoflower Regular" to uiState.autoflowerRegularSeedCount,
                            "Autoflower Feminized" to uiState.autoflowerFeminizedSeedCount,
                            "Photoperiod Regular" to uiState.photoperiodRegularSeedCount,
                            "Photoperiod Feminized" to uiState.photoperiodFeminizedSeedCount
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        darkTheme = darkTheme
                    )
                    1 -> SeedsPerStrainPieChart(
                        data = uiState.seedsPerStrain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        darkTheme = darkTheme
                    )
                }
            }
            
            // Pager indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 10.dp else 8.dp)
                            .background(
                                color = if (isSelected) 
                                    if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary
                                else 
                                    if (darkTheme) TextGrey.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    )
                    if (index < 1) Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
fun SeedsPerStrainPieChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true
) {
    if (data.isEmpty() || data.values.sum() == 0) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No seed strain data available yet",
                color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
        return
    }
    
    // Take only top 5 strains by seed count to avoid chart getting too crowded
    val sortedData = data.entries
        .sortedByDescending { it.value }
        .take(5)
        .associate { it.key to it.value }
    
    val total = sortedData.values.sum().toFloat()
    
    // Use a fixed set of colors for consistency
    val colors = listOf(
        Color(0xFF81C784),  // Green
        Color(0xFFE57373),  // Red
        Color(0xFF64B5F6),  // Blue
        Color(0xFFFFD54F),  // Amber
        Color(0xFFBA68C8)   // Purple
    )
    
    // Define text color before Canvas scope
    val textColor = if (darkTheme) TextWhite else MaterialTheme.colorScheme.onSurface
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pie Chart
        Canvas(
            modifier = Modifier.size(180.dp)
        ) {
            var startAngle = 0f
            
            sortedData.entries.filter { it.value > 0 }.forEachIndexed { index, entry ->
                val sweepAngle = 360f * entry.value / total
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Legend - centered below the chart with full strain names
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            sortedData.entries.filter { it.value > 0 }.forEachIndexed { index, entry ->
                val percentage = (entry.value * 100f / total).toInt()
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(colors[index % colors.size], shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${entry.key} ($percentage%)",
                        color = textColor,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) DarkSurface else MaterialTheme.colorScheme.surface
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
                color = if (darkTheme) TextGrey else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                color = if (darkTheme) PrimaryGreen else MaterialTheme.colorScheme.primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}