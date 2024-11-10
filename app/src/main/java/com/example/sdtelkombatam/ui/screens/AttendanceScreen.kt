package com.example.sdtelkombatam.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sdtelkombatam.Screen
import java.time.*
import java.time.format.DateTimeFormatter

// Custom Colors
val PrimaryBlue = Color(0xFF1976D2)
val SuccessGreen = Color(0xFF4CAF50)
val WarningRed = Color(0xFFF44336)
val BackgroundGray = Color(0xFFF5F5F5)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1F1F1F)
val TextSecondary = Color(0xFF757575)

// Data Classes
data class Student(
    val id: String,
    val name: String,
    val nisn: String,
    val isPresent: Boolean,
    val checkInTime: String? = null,
    val className: String,
    val attendance: Map<String, Boolean> = emptyMap()
)

private enum class SortOption {
    NAME, PRESENT_FIRST, ABSENT_FIRST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("5B") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var showClassDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(SortOption.NAME) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var filterStatus by remember { mutableStateOf<Boolean?>(null) }

    val canSelectNextDay = remember(selectedDate) {
        selectedDate.isBefore(LocalDate.now()) || selectedDate.isEqual(LocalDate.now())
    }

    // Dummy students data
    val students = remember {
        (0..19).map { index ->
            val isPresent = index % 3 != 0
            Student(
                id = index.toString(),
                name = listOf(
                    "Ahmad Dahlan",
                    "Budi Santoso",
                    "Citra Dewi",
                    "Dian Sastro",
                    "Eko Widodo"
                )[index % 5],
                nisn = "2024${index.toString().padStart(4, '0')}",
                isPresent = isPresent,
                checkInTime = if (isPresent)
                    LocalTime.of(7, 15 + index % 45)
                        .format(DateTimeFormatter.ofPattern("HH:mm"))
                else null,
                className = selectedClass,
                attendance = (0..4).associate { day ->
                    LocalDate.now().minusDays(day.toLong())
                        .format(DateTimeFormatter.ofPattern("d MMM yyyy")) to
                            (day % 2 == 0)
                }
            )
        }.distinctBy { it.nisn }
    }

    // Stats calculations
    val totalStudents = remember(selectedClass) {
        students.count { it.className == selectedClass }
    }
    val totalPresent = remember(selectedClass) {
        students.count { it.className == selectedClass && it.isPresent }
    }
    val totalAbsent = remember(selectedClass) {
        totalStudents - totalPresent
    }

    // Filtered students
    val filteredStudents by remember(searchQuery, selectedClass, sortOption, filterStatus, students) {
        derivedStateOf {
            students.filter { student ->
                val matchesSearch = student.name.contains(searchQuery, ignoreCase = true) ||
                        student.nisn.contains(searchQuery, ignoreCase = true)
                val matchesClass = student.className == selectedClass
                val matchesStatus = filterStatus?.let { student.isPresent == it } ?: true

                matchesSearch && matchesClass && matchesStatus
            }.let { filtered ->
                when (sortOption) {
                    SortOption.NAME -> filtered.sortedBy { it.name }
                    SortOption.PRESENT_FIRST -> filtered.sortedByDescending { it.isPresent }
                    SortOption.ABSENT_FIRST -> filtered.sortedBy { it.isPresent }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Daftar Kehadiran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Kelas $selectedClass",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showClassDialog = true }) {
                        Icon(Icons.Default.Class, "Pilih Kelas")
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export Data") },
                                leadingIcon = { Icon(Icons.Default.FileDownload, null) },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Pengaturan") },
                                leadingIcon = { Icon(Icons.Default.Settings, null) },
                                onClick = { showMenu = false }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == Screen.Home.route,
                    onClick = {
                        if (currentRoute != Screen.Home.route) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.People, contentDescription = "Absensi") },
                    label = { Text("Absensi") },
                    selected = currentRoute == Screen.Attendance.route,
                    onClick = {
                        if (currentRoute != Screen.Attendance.route) {
                            navController.navigate(Screen.Attendance.route) {
                                popUpTo(Screen.Attendance.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
        ) {
            DateSelectionRow(
                selectedDate = selectedDate,
                onDateSelected = { newDate ->
                    if (!newDate.isAfter(LocalDate.now())) {
                        selectedDate = newDate
                    }
                },
                onDatePickerRequested = { showDatePicker = true },
                canSelectNextDay = canSelectNextDay
            )

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AttendanceStatsRow(
                presentCount = totalPresent,
                absentCount = totalAbsent,
                totalStudents = totalStudents,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SortAndFilterRow(
                sortOption = sortOption,
                onSortOptionChange = { sortOption = it },
                filterStatus = filterStatus,
                onFilterClick = { showFilterSheet = true },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            StudentList(
                students = filteredStudents,
                onStudentClick = { selectedStudent = it }
            )
        }

        // Dialogs and Sheets
        if (showClassDialog) {
            ClassSelectionDialog(
                currentClass = selectedClass,
                onClassSelect = {
                    selectedClass = it
                    showClassDialog = false
                },
                onDismiss = { showClassDialog = false }
            )
        }

        if (showFilterSheet) {
            FilterBottomSheet(
                currentStatus = filterStatus,
                onDismiss = { showFilterSheet = false },
                onApplyFilters = { status ->
                    filterStatus = status
                    showFilterSheet = false
                }
            )
        }

        if (selectedStudent != null) {
            StudentDetailsSheet(
                student = selectedStudent!!,
                onDismiss = { selectedStudent = null }
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                onDateSelected = { newDate ->
                    if (!newDate.isAfter(LocalDate.now())) {
                        selectedDate = newDate
                    }
                    showDatePicker = false
                },
                initialDate = selectedDate
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Cari nama atau NISN siswa") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, "Clear search")
                }
            }
        } else null,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = SurfaceWhite,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = PrimaryBlue
        ),
        singleLine = true
    )
}

@Composable
private fun DateSelectionRow(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDatePickerRequested: () -> Unit,
    canSelectNextDay: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onDateSelected(selectedDate.minusDays(1)) }) {
            Icon(Icons.Default.ChevronLeft, "Previous Day")
        }

        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
            modifier = Modifier.clickable { onDatePickerRequested() },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        IconButton(
            onClick = { onDateSelected(selectedDate.plusDays(1)) },
            enabled = canSelectNextDay
        ) {
            Icon(
                Icons.Default.ChevronRight,
                "Next Day",
                tint = if (canSelectNextDay) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
            )
        }
    }
}

@Composable
private fun AttendanceStatsRow(
    presentCount: Int,
    absentCount: Int,
    totalStudents: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatsCard(
            count = presentCount,
            total = totalStudents,
            label = "Hadir",
            color = SuccessGreen,
            icon = Icons.Rounded.CheckCircle,
            modifier = Modifier.weight(1f)
        )

        StatsCard(
            count = absentCount,
            total = totalStudents,
            label = "Tidak Hadir",
            color = WarningRed,
            icon = Icons.Rounded.Cancel,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SortAndFilterRow(
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    filterStatus: Boolean?,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = {
                onSortOptionChange(when (sortOption) {
                    SortOption.NAME -> SortOption.PRESENT_FIRST
                    SortOption.PRESENT_FIRST -> SortOption.ABSENT_FIRST
                    SortOption.ABSENT_FIRST -> SortOption.NAME
                })
            },
            label = {
                Text(
                    when (sortOption) {
                        SortOption.NAME -> "Urut Nama"
                        SortOption.PRESENT_FIRST -> "Hadir Dulu"
                        SortOption.ABSENT_FIRST -> "Tidak Hadir Dulu"
                    }
                )
            },
            leadingIcon = { Icon(Icons.Default.Sort, null) }
        )

        FilterChip(
            selected = filterStatus != null,
            onClick = onFilterClick,
            label = { Text("Filter") },
            leadingIcon = { Icon(Icons.Default.FilterList, null) }
        )
    }
}

@Composable
private fun StudentList(
    students: List<Student>,
    onStudentClick: (Student) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = students,
            key = { it.id }
        ) { student ->
            StudentCard(
                student = student,
                onClick = { onStudentClick(student) }
            )
        }
    }
}

@Composable
private fun StatsCard(
    count: Int,
    total: Int,
    label: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val progress by remember(count, total) {
        derivedStateOf { if (total > 0) count.toFloat() / total else 0f }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun StudentCard(
    student: Student,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "NISN: ${student.nisn}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                if (student.isPresent && student.checkInTime != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = student.checkInTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen
                        )
                    }
                }
            }

            AttendanceStatusBadge(isPresent = student.isPresent)
        }
    }
}

@Composable
private fun AttendanceStatusBadge(isPresent: Boolean) {
    Surface(
        color = if (isPresent) SuccessGreen.copy(alpha = 0.1f)
        else WarningRed.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPresent) Icons.Rounded.CheckCircle
                else Icons.Rounded.Cancel,
                contentDescription = null,
                tint = if (isPresent) SuccessGreen else WarningRed,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isPresent) "Hadir" else "Tidak Hadir",
                color = if (isPresent) SuccessGreen else WarningRed,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    currentStatus: Boolean?,
    onDismiss: () -> Unit,
    onApplyFilters: (Boolean?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Filter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Status Kehadiran",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == true,
                    onClick = {
                        selectedStatus = if (selectedStatus == true) null else true
                    },
                    label = { Text("Hadir") },
                    leadingIcon = {
                        Icon(Icons.Rounded.CheckCircle, null)
                    }
                )

                FilterChip(
                    selected = selectedStatus == false,
                    onClick = {
                        selectedStatus = if (selectedStatus == false) null else false
                    },
                    label = { Text("Tidak Hadir") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Cancel, null)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Batal")
                }

                Button(
                    onClick = { onApplyFilters(selectedStatus) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Terapkan")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentDetailsSheet(
    student: Student,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        windowInsets = WindowInsets(0, 0, 0, 0),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detail Siswa",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    DetailRow("NISN", student.nisn)
                    DetailRow("Kelas", student.className)
                    DetailRow(
                        "Status",
                        if (student.isPresent) "Hadir" else "Tidak Hadir",
                        if (student.isPresent) SuccessGreen else WarningRed
                    )
                    if (student.isPresent && student.checkInTime != null) {
                        DetailRow("Check-in", student.checkInTime)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Riwayat Kehadiran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(
                    items = student.attendance.entries.toList(),
                    key = { it.key }
                ) { (date, isPresent) ->
                    AttendanceHistoryCard(
                        date = date,
                        isPresent = isPresent
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassSelectionDialog(
    currentClass: String,
    onClassSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val classes = remember { listOf("5A", "5B", "5C", "6A", "6B", "6C") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Pilih Kelas",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                classes.forEach { kelas ->
                    ListItem(
                        headlineContent = { Text(kelas) },
                        leadingContent = {
                            RadioButton(
                                selected = kelas == currentClass,
                                onClick = null
                            )
                        },
                        modifier = Modifier.clickable { onClassSelect(kelas) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                "Pilih Tanggal",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = null,
                headline = null
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AttendanceHistoryCard(
    date: String,
    isPresent: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPresent)
                SuccessGreen.copy(alpha = 0.1f)
            else
                WarningRed.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                imageVector = if (isPresent)
                    Icons.Rounded.CheckCircle
                else
                    Icons.Rounded.Cancel,
                contentDescription = null,
                tint = if (isPresent) SuccessGreen else WarningRed,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

