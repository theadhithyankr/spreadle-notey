package com.mininotes.app.ui.editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt

// --- Data Models ---

sealed class DrawingElement {
    abstract val color: Color
    abstract val strokeWidth: Float
    abstract val alpha: Float
}

data class StrokeElement(
    val path: Path,
    override val color: Color,
    override val strokeWidth: Float,
    override val alpha: Float
) : DrawingElement()

data class ShapeElement(
    val type: ShapeType,
    val start: Offset,
    val end: Offset,
    override val color: Color,
    override val strokeWidth: Float,
    override val alpha: Float
) : DrawingElement()

enum class ShapeType {
    RECTANGLE, OVAL, LINE
}

enum class DrawingTool {
    PEN, MARKER, ERASER, RECTANGLE, OVAL, LINE
}

// --- Main Composable ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingEditor(
    initialFilePath: String,
    onSave: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // State
    val elements = remember { mutableStateListOf<DrawingElement>() }
    val undoneElements = remember { mutableStateListOf<DrawingElement>() }
    
    // Tool Configuration
    var currentTool by remember { mutableStateOf(DrawingTool.PEN) }
    var currentColor by remember { mutableStateOf(Color.Black) }
    var currentStrokeWidth by remember { mutableStateOf(10f) }
    var currentAlpha by remember { mutableStateOf(1f) }
    
    // Interaction State
    var currentPath by remember { mutableStateOf<Path?>(null) } // For freehand
    var currentShapeStart by remember { mutableStateOf<Offset?>(null) } // For shapes
    var currentShapeEnd by remember { mutableStateOf<Offset?>(null) }
    var renderingTrigger by remember { mutableLongStateOf(0L) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    
    // Dialogs
    var showColorPicker by remember { mutableStateOf(false) }

    // Init Logic
    val backgroundBitmap = remember(initialFilePath) {
        if (initialFilePath.isNotEmpty()) {
            val file = File(context.filesDir, initialFilePath)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        } else null
    }

    // Save Logic
    fun saveAndExit() {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            val filename = saveDrawingToStorage(context, elements, backgroundBitmap, canvasSize.width, canvasSize.height, initialFilePath)
            onSave(filename)
        }
        onBack()
    }

    BackHandler { saveAndExit() }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        
        // 1. Canvas Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp) // Space for bottom sheet
                .onSizeChanged { canvasSize = it }
                .pointerInput(currentTool, currentColor, currentStrokeWidth, currentAlpha) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (currentTool == DrawingTool.PEN || currentTool == DrawingTool.MARKER || currentTool == DrawingTool.ERASER) {
                                val p = Path().apply { moveTo(offset.x, offset.y) }
                                currentPath = p
                            } else {
                                currentShapeStart = offset
                                currentShapeEnd = offset
                            }
                        },
                        onDrag = { change, _ ->
                            if (currentPath != null) {
                                currentPath!!.lineTo(change.position.x, change.position.y)
                                renderingTrigger++
                            } else if (currentShapeStart != null) {
                                currentShapeEnd = change.position
                                renderingTrigger++
                            }
                        },
                        onDragEnd = {
                            // Calculate effective properties at the moment of drag end
                            val paintColor = if (currentTool == DrawingTool.ERASER) Color.White else currentColor
                            val paintAlpha = if (currentTool == DrawingTool.MARKER) 0.5f else currentAlpha

                            if (currentPath != null) {
                                elements.add(StrokeElement(
                                    currentPath!!, 
                                    paintColor,
                                    currentStrokeWidth, 
                                    paintAlpha
                                ))
                                currentPath = null
                            } else if (currentShapeStart != null && currentShapeEnd != null) {
                                val type = when(currentTool) {
                                    DrawingTool.RECTANGLE -> ShapeType.RECTANGLE
                                    DrawingTool.OVAL -> ShapeType.OVAL
                                    else -> ShapeType.LINE
                                }
                                elements.add(ShapeElement(type, currentShapeStart!!, currentShapeEnd!!, currentColor, currentStrokeWidth, paintAlpha))
                                currentShapeStart = null
                                currentShapeEnd = null
                            }
                            renderingTrigger++
                            undoneElements.clear()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trigger = renderingTrigger
                val effectiveAlpha = if (currentTool == DrawingTool.MARKER) 0.5f else currentAlpha

                // Draw Background
                backgroundBitmap?.let { drawImage(it.asImageBitmap()) } ?: drawRect(Color.White) // Default white bg

                // Draw Elements
                elements.forEach { element ->
                    drawElement(this, element)
                }

                // Draw Active Interaction
                if (currentPath != null) {
                    drawPath(
                        path = currentPath!!,
                        color = if(currentTool == DrawingTool.ERASER) Color.White else currentColor,
                        alpha = effectiveAlpha,
                        style = Stroke(currentStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                if (currentShapeStart != null && currentShapeEnd != null) {
                    drawShapePreview(this, currentTool, currentShapeStart!!, currentShapeEnd!!, currentColor, currentStrokeWidth, effectiveAlpha)
                }
            }
        }

        // 2. Toolbar (Top)
        Row(
            Modifier.fillMaxWidth().padding(8.dp).align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { saveAndExit() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Row {
                IconButton(onClick = { if (elements.isNotEmpty()) { undoneElements.add(elements.removeLast()); renderingTrigger++ } }) {
                    Icon(Icons.Default.Undo, "Undo", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = {
                    elements.clear()
                    undoneElements.clear()
                    renderingTrigger++ 
                }) {
                    Icon(Icons.Default.Delete, "Clear", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // 3. Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(16.dp)
        ) {
            // Sliders (Size & Opacity)
            if (currentTool != DrawingTool.ERASER) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Text("Size", style = MaterialTheme.typography.labelSmall)
                     Slider(
                         value = currentStrokeWidth,
                         onValueChange = { currentStrokeWidth = it },
                         valueRange = 1f..100f,
                         modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                     )
                     Spacer(Modifier.width(16.dp))
                     Text("Opacity", style = MaterialTheme.typography.labelSmall)
                     Slider(
                         value = currentAlpha,
                         onValueChange = { currentAlpha = it },
                         valueRange = 0.1f..1f,
                         modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                     )
                 }
            } else {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Text("Eraser Size", style = MaterialTheme.typography.labelSmall)
                     Slider(
                         value = currentStrokeWidth,
                         onValueChange = { currentStrokeWidth = it },
                         valueRange = 10f..200f,
                         modifier = Modifier.weight(1f)
                     )
                 }
            }

            // Tool Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ToolButton(DrawingTool.PEN, Icons.Default.Edit, currentTool) { currentTool = it }
                ToolButton(DrawingTool.MARKER, Icons.Default.Brush, currentTool) { currentTool = it }
                ToolButton(DrawingTool.RECTANGLE, Icons.Default.CropSquare, currentTool) { currentTool = it }
                ToolButton(DrawingTool.OVAL, Icons.Default.Circle, currentTool) { currentTool = it }
                ToolButton(DrawingTool.LINE, Icons.Default.ShowChart, currentTool) { currentTool = it }
                ToolButton(DrawingTool.ERASER, Icons.Default.AutoFixHigh, currentTool) { currentTool = it } // Using AutoFixHigh as 'Rubber' proxy
            }

            // Palette
            if (currentTool != DrawingTool.ERASER) {
                 Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val quickColors = listOf(Color.Black, Color.Red, Color.Blue, Color(0xFF4CAF50), Color.Yellow, Color.Magenta)
                    quickColors.forEach { c ->
                        ColorButton(c, currentColor) { currentColor = it }
                    }
                    // Custom Color Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                                    listOf(Color.Red, Color.Green, Color.Blue, Color.Red)
                                )
                            )
                            .clickable { showColorPicker = true }
                            .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    )
                }
            }
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = currentColor,
            onDismiss = { showColorPicker = false },
            onColorSelected = { currentColor = it; showColorPicker = false }
        )
    }
}

// --- Helpers & Dialogs ---

@Composable
fun ToolButton(tool: DrawingTool, icon: androidx.compose.ui.graphics.vector.ImageVector, current: DrawingTool, onClick: (DrawingTool) -> Unit) {
    IconButton(
        onClick = { onClick(tool) },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (tool == current) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (tool == current) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(icon, contentDescription = tool.name)
    }
}

@Composable
fun ColorButton(color: Color, current: Color, onClick: (Color) -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .padding(2.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (color == current) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
            .clickable { onClick(color) }
    )
}

@Composable
fun ColorPickerDialog(initialColor: Color, onDismiss: () -> Unit, onColorSelected: (Color) -> Unit) {
    // A simple, pure-compose optional color picker dialog with Hex input
    var hexCode by remember { mutableStateOf(toHex(initialColor)) }
    var red by remember { mutableStateOf(initialColor.red) }
    var green by remember { mutableStateOf(initialColor.green) }
    var blue by remember { mutableStateOf(initialColor.blue) }

    // Sync Hex -> RGB effect not implemented for brevity, but RGB -> Hex is displayed
    val selectedColor = Color(red, green, blue)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Select Color", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                
                Box(Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)).background(selectedColor))
                Spacer(Modifier.height(16.dp))

                Text("Red: ${(red * 255).toInt()}")
                Slider(value = red, onValueChange = { red = it })
                Text("Green: ${(green * 255).toInt()}")
                Slider(value = green, onValueChange = { green = it })
                Text("Blue: ${(blue * 255).toInt()}")
                Slider(value = blue, onValueChange = { blue = it })

                OutlinedTextField(
                    value = hexCode,
                    onValueChange = { 
                        hexCode = it
                        // Parse hex logic (simplified)
                        if (it.length == 9 && it.startsWith("#")) {
                            try {
                                val c = android.graphics.Color.parseColor(it)
                                val cObj = Color(c)
                                red = cObj.red
                                green = cObj.green
                                blue = cObj.blue
                            } catch(e: Exception) {}
                        }
                    },
                    label = { Text("Hex Code (#AARRGGBB)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onColorSelected(selectedColor) }) { Text("Select") }
                }
            }
        }
    }
}

fun toHex(c: Color): String {
    return String.format("#%02X%02X%02X%02X", (c.alpha * 255).toInt(), (c.red * 255).toInt(), (c.green * 255).toInt(), (c.blue * 255).toInt())
}

// --- Drawing Logic ---

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawElement(scope: androidx.compose.ui.graphics.drawscope.DrawScope, element: DrawingElement) {
    when(element) {
        is StrokeElement -> {
            scope.drawPath(
                path = element.path,
                color = element.color,
                alpha = element.alpha,
                style = Stroke(element.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        is ShapeElement -> {
            drawShapePreview(scope, fdroidToolProxy(element.type), element.start, element.end, element.color, element.strokeWidth, element.alpha)
        }
    }
}

fun drawShapePreview(scope: androidx.compose.ui.graphics.drawscope.DrawScope, tool: DrawingTool, start: Offset, end: Offset, color: Color, width: Float, alpha: Float) {
    val topLeft = Offset(
        x = kotlin.math.min(start.x, end.x),
        y = kotlin.math.min(start.y, end.y)
    )
    val size = Size(
        width = kotlin.math.abs(end.x - start.x),
        height = kotlin.math.abs(end.y - start.y)
    )
    
    when(tool) {
        DrawingTool.RECTANGLE -> scope.drawRect(color, topLeft, size, alpha, Stroke(width))
        DrawingTool.OVAL -> scope.drawOval(color, topLeft, size, alpha, Stroke(width))
        DrawingTool.LINE -> scope.drawLine(color, start, end, width, StrokeCap.Round, alpha = alpha)
        else -> {}
    }
}

fun fdroidToolProxy(type: ShapeType): DrawingTool {
    return when(type) {
        ShapeType.RECTANGLE -> DrawingTool.RECTANGLE
        ShapeType.OVAL -> DrawingTool.OVAL
        ShapeType.LINE -> DrawingTool.LINE
    }
}

// --- Persistence ---

fun saveDrawingToStorage(
    context: android.content.Context,
    elements: List<DrawingElement>,
    backgroundBitmap: Bitmap?,
    width: Int,
    height: Int,
    existingFilename: String
): String {
    val filename = if (existingFilename.isNotEmpty()) existingFilename else "drawing_${System.currentTimeMillis()}.png"
    val file = File(context.filesDir, filename)
    
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    // Draw Background
    backgroundBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) } ?: canvas.drawColor(android.graphics.Color.WHITE)

    // Draw Elements
    val paint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
    }

    elements.forEach { el ->
        paint.color = android.graphics.Color.argb(
            (el.alpha * 255).toInt(),
            (el.color.red * 255).toInt(),
            (el.color.green * 255).toInt(),
            (el.color.blue * 255).toInt()
        )
        paint.strokeWidth = el.strokeWidth

        when(el) {
            is StrokeElement -> {
                canvas.drawPath(el.path.asAndroidPath(), paint)
            }
            is ShapeElement -> {
                val left = kotlin.math.min(el.start.x, el.end.x)
                val top = kotlin.math.min(el.start.y, el.end.y)
                val right = kotlin.math.max(el.start.x, el.end.x)
                val bottom = kotlin.math.max(el.start.y, el.end.y)

                when(el.type) {
                    ShapeType.RECTANGLE -> canvas.drawRect(left, top, right, bottom, paint)
                    ShapeType.OVAL -> canvas.drawOval(left, top, right, bottom, paint)
                    ShapeType.LINE -> canvas.drawLine(el.start.x, el.start.y, el.end.x, el.end.y, paint)
                }
            }
        }
    }
    
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    
    return filename
}
