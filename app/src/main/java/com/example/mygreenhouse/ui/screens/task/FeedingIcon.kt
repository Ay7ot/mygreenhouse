package com.example.mygreenhouse.ui.screens.task

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom watering can icon for feeding tasks.
 * Larger, more substantial design for better visibility.
 */
object FeedingIcon {
    val WateringCan: ImageVector by lazy {
        ImageVector.Builder(
            name = "WateringCan",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Main body of the watering can (larger and chunkier)
            path(
                fill = SolidColor(Color(0xFF4CAF50)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(2f, 11f)
                lineTo(2f, 19f)
                lineTo(3f, 20f)
                lineTo(14f, 20f)
                lineTo(15f, 19f)
                lineTo(15f, 11f)
                lineTo(14f, 10f)
                lineTo(3f, 10f)
                close()
            }
            
            // Top rim of the watering can
            path(
                fill = SolidColor(Color(0xFF388E3C)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(2f, 10f)
                lineTo(2f, 11f)
                lineTo(15f, 11f)
                lineTo(15f, 10f)
                close()
            }
            
            // Handle (thicker)
            path(
                fill = null,
                fillAlpha = 1.0f,
                stroke = SolidColor(Color(0xFF2E7D32)),
                strokeAlpha = 1.0f,
                strokeLineWidth = 2.0f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(0.5f, 13f)
                lineTo(0.5f, 16f)
                lineTo(1f, 17f)
                lineTo(2f, 17f)
                lineTo(2.5f, 16f)
                lineTo(2.5f, 13f)
            }
            
            // Spout (wider)
            path(
                fill = SolidColor(Color(0xFF2E7D32)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(15f, 13f)
                lineTo(19f, 9f)
                lineTo(20f, 10f)
                lineTo(16f, 14f)
                close()
            }
            
            // Spout shower head (larger)
            path(
                fill = SolidColor(Color(0xFF1B5E20)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(19f, 8.5f)
                lineTo(20.5f, 8.5f)
                lineTo(20.5f, 10.5f)
                lineTo(19f, 10.5f)
                close()
            }
            
            // Water drops (larger and repositioned)
            path(
                fill = SolidColor(Color(0xFF2196F3)),
                fillAlpha = 0.9f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Larger drops
                moveTo(20.5f, 6f)
                lineTo(21.5f, 7f)
                lineTo(20.5f, 8f)
                lineTo(19.5f, 7f)
                close()
                moveTo(18f, 7f)
                lineTo(19f, 8f)
                lineTo(18f, 9f)
                lineTo(17f, 8f)
                close()
                moveTo(22f, 8f)
                lineTo(23f, 9f)
                lineTo(22f, 10f)
                lineTo(21f, 9f)
                close()
            }
            
            // Shower head holes (multiple small holes)
            path(
                fill = SolidColor(Color(0xFF81C784)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Row 1 of holes
                moveTo(17.3f, 10.8f)
                lineTo(17.6f, 10.8f)
                lineTo(17.6f, 11.1f)
                lineTo(17.3f, 11.1f)
                close()
                
                moveTo(17.9f, 10.8f)
                lineTo(18.2f, 10.8f)
                lineTo(18.2f, 11.1f)
                lineTo(17.9f, 11.1f)
                close()
                
                // Row 2 of holes
                moveTo(17.3f, 11.4f)
                lineTo(17.6f, 11.4f)
                lineTo(17.6f, 11.7f)
                lineTo(17.3f, 11.7f)
                close()
                
                moveTo(17.9f, 11.4f)
                lineTo(18.2f, 11.4f)
                lineTo(18.2f, 11.7f)
                lineTo(17.9f, 11.7f)
                close()
            }
            
            // Water stream lines (subtle effect)
            path(
                fill = null,
                fillAlpha = 1.0f,
                stroke = SolidColor(Color(0xFF64B5F6)),
                strokeAlpha = 0.4f,
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Subtle water stream lines
                moveTo(17.5f, 10.5f)
                lineTo(17.2f, 12f)
                
                moveTo(18f, 10.5f)
                lineTo(17.8f, 12f)
                
                moveTo(18.3f, 10.8f)
                lineTo(18.2f, 12f)
            }
            
        }.build()
    }
} 