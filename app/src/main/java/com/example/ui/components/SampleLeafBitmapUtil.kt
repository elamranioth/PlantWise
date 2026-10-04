package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

object SampleLeafBitmapUtil {

    fun createMonsteraSampleBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#0A1C10"))

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#1B5E20")
        }

        // Draw leaf heart shape
        val path = Path().apply {
            moveTo(200f, 50f)
            cubicTo(80f, 50f, 40f, 170f, 80f, 260f)
            cubicTo(120f, 330f, 200f, 370f, 200f, 370f)
            cubicTo(200f, 370f, 280f, 330f, 320f, 260f)
            cubicTo(360f, 170f, 320f, 50f, 200f, 50f)
            close()
        }
        canvas.drawPath(path, paint)

        // Fenestrations (holes) in dark background color
        val holePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#0A1C10")
        }
        canvas.drawOval(120f, 140f, 155f, 200f, holePaint)
        canvas.drawOval(245f, 140f, 280f, 200f, holePaint)
        canvas.drawOval(140f, 230f, 170f, 280f, holePaint)
        canvas.drawOval(230f, 230f, 260f, 280f, holePaint)

        // Leaf main vein
        val veinPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.parseColor("#4CAF50")
        }
        canvas.drawLine(200f, 60f, 200f, 360f, veinPaint)

        return bitmap
    }

    fun createEnglishOakSampleBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#0F1A12"))

        val leafPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#2E7D32")
        }

        // Distinctive lobed Quercus robur leaf
        val path = Path().apply {
            moveTo(200f, 40f)
            // Left lobes
            quadTo(140f, 60f, 130f, 100f)
            quadTo(160f, 120f, 120f, 160f)
            quadTo(160f, 180f, 110f, 230f)
            quadTo(150f, 260f, 130f, 310f)
            quadTo(170f, 340f, 200f, 370f)
            // Right lobes
            quadTo(230f, 340f, 270f, 310f)
            quadTo(250f, 260f, 290f, 230f)
            quadTo(240f, 180f, 280f, 160f)
            quadTo(240f, 120f, 270f, 100f)
            quadTo(260f, 60f, 200f, 40f)
            close()
        }
        canvas.drawPath(path, leafPaint)

        // Vein
        val veinPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 5f
            color = Color.parseColor("#81C784")
        }
        canvas.drawLine(200f, 40f, 200f, 370f, veinPaint)

        // Acorn cupule icon at bottom
        val cupulePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#8D6E63")
        }
        canvas.drawOval(180f, 350f, 220f, 385f, cupulePaint)

        return bitmap
    }

    fun createJapaneseMapleSampleBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#150F10"))

        val mapleRedPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#C62828") // Fiery Japanese Maple Autumn Red
        }

        // Draw 7-pointed palmate star leaf
        val path = Path().apply {
            moveTo(200f, 40f)
            lineTo(220f, 130f)
            lineTo(300f, 90f)
            lineTo(240f, 170f)
            lineTo(340f, 210f)
            lineTo(240f, 230f)
            lineTo(280f, 320f)
            lineTo(200f, 270f)
            lineTo(120f, 320f)
            lineTo(160f, 230f)
            lineTo(60f, 210f)
            lineTo(160f, 170f)
            lineTo(100f, 90f)
            lineTo(180f, 130f)
            close()
        }
        canvas.drawPath(path, mapleRedPaint)

        val veinPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = Color.parseColor("#FF8A80")
        }
        canvas.drawLine(200f, 270f, 200f, 50f, veinPaint)
        canvas.drawLine(200f, 270f, 300f, 100f, veinPaint)
        canvas.drawLine(200f, 270f, 100f, 100f, veinPaint)
        canvas.drawLine(200f, 270f, 330f, 210f, veinPaint)
        canvas.drawLine(200f, 270f, 70f, 210f, veinPaint)

        return bitmap
    }

    fun createCoastRedwoodSampleBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#09140E"))

        val woodPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 14f
            color = Color.parseColor("#795548")
        }
        canvas.drawLine(200f, 380f, 200f, 40f, woodPaint)

        val needlePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.parseColor("#2E7D32")
        }

        // Horizontal two-ranked conifer needles
        for (y in 70..340 step 25) {
            canvas.drawLine(200f, y.toFloat(), 120f, (y - 15).toFloat(), needlePaint)
            canvas.drawLine(200f, y.toFloat(), 280f, (y - 15).toFloat(), needlePaint)
            canvas.drawLine(200f, y.toFloat(), 90f, y.toFloat(), needlePaint)
            canvas.drawLine(200f, y.toFloat(), 310f, y.toFloat(), needlePaint)
        }

        return bitmap
    }

    fun createOliveSampleBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#0F1710"))

        val twigPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 7f
            color = Color.parseColor("#8D6E63")
        }
        canvas.drawLine(100f, 350f, 300f, 80f, twigPaint)

        val leafPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#556B2F") // Olive green
        }

        // Opposite silvery-green lanceolate leaves
        canvas.drawOval(80f, 240f, 170f, 290f, leafPaint)
        canvas.drawOval(190f, 200f, 280f, 250f, leafPaint)
        canvas.drawOval(130f, 150f, 220f, 200f, leafPaint)
        canvas.drawOval(230f, 100f, 320f, 150f, leafPaint)

        // Hanging ripe dark purple olive drupe
        val olivePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#2E1C2B")
        }
        canvas.drawOval(190f, 270f, 225f, 320f, olivePaint)

        return bitmap
    }

    fun createTulsiSampleBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#0D1A10"))

        val stemPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 10f
            color = Color.parseColor("#4E342E")
        }
        canvas.drawLine(200f, 380f, 200f, 100f, stemPaint)

        val leafPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#388E3C")
        }

        // Left & right pairs of aromatic leaves
        canvas.drawOval(90f, 200f, 200f, 260f, leafPaint)
        canvas.drawOval(200f, 200f, 310f, 260f, leafPaint)
        canvas.drawOval(110f, 140f, 200f, 190f, leafPaint)
        canvas.drawOval(200f, 140f, 290f, 190f, leafPaint)

        // Distinct purple flower spike (Lamiaceae marker)
        val flowerPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#9C27B0")
        }
        canvas.drawCircle(200f, 85f, 14f, flowerPaint)
        canvas.drawCircle(200f, 65f, 12f, flowerPaint)
        canvas.drawCircle(200f, 48f, 10f, flowerPaint)

        return bitmap
    }

    fun createSnakePlantSampleBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(250, 420, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#0B170F"))

        // Golden yellow margin border
        val goldPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#FBC02D")
        }
        val swordPath = Path().apply {
            moveTo(125f, 30f)
            quadTo(50f, 150f, 60f, 380f)
            lineTo(190f, 380f)
            quadTo(200f, 150f, 125f, 30f)
            close()
        }
        canvas.drawPath(swordPath, goldPaint)

        // Inner banded dark green
        val greenPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.parseColor("#1B5E20")
        }
        val innerPath = Path().apply {
            moveTo(125f, 50f)
            quadTo(70f, 160f, 80f, 380f)
            lineTo(170f, 380f)
            quadTo(180f, 160f, 125f, 50f)
            close()
        }
        canvas.drawPath(innerPath, greenPaint)

        // Horizontal banding
        val bandPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.parseColor("#81C784")
        }
        for (y in 100..340 step 40) {
            canvas.drawLine(85f, y.toFloat(), 165f, (y + 10).toFloat(), bandPaint)
        }

        return bitmap
    }

    fun generateSampleBitmap(name: String): Bitmap {
        return when {
            name.contains("Monstera", ignoreCase = true) -> createMonsteraSampleBitmap()
            name.contains("Snake", ignoreCase = true) -> createSnakePlantSampleBitmap()
            name.contains("Oak", ignoreCase = true) -> createEnglishOakSampleBitmap()
            name.contains("Redwood", ignoreCase = true) -> createCoastRedwoodSampleBitmap()
            name.contains("Maple", ignoreCase = true) -> createJapaneseMapleSampleBitmap()
            name.contains("Olive", ignoreCase = true) -> createOliveSampleBitmap()
            name.contains("Tulsi", ignoreCase = true) || name.contains("Basil", ignoreCase = true) -> createTulsiSampleBitmap()
            else -> createMonsteraSampleBitmap()
        }
    }
}
