package com.oyku.mezzemaster

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.cos
import kotlin.math.sin

@Composable fun Motif(modifier: Modifier) {
    Canvas(modifier) {
        val step = 22f
        var x = 0f
        while (x < size.width) {
            val p = Path().apply { moveTo(x, size.height/2); lineTo(x+step/2,0f); lineTo(x+step,size.height/2); lineTo(x+step/2,size.height); close() }
            drawPath(p, if ((x/step).toInt()%3 == 0) Wine else Olive.copy(alpha = .35f))
            x += step + 5f
        }
    }
}
@Composable fun FoodArt(id: Int, modifier: Modifier) {
    Canvas(modifier) {
        val s = size.minDimension / 200f
        withTransform({ translate((size.width-200*s)/2, (size.height-200*s)/2); scale(s,s,Offset.Zero) }) { dish(id) }
    }
}
private fun DrawScope.dish(id: Int) {
    drawOval(Color(0x2527352C), Offset(15f,23f), Size(176f,166f))
    drawCircle(Color(0xFFFFFDF6), 86f, Offset(100f,96f))
    drawCircle(Color(0xFF497470), 78f, Offset(100f,96f), style = Stroke(2f))
    for (i in 0..23) {
        val a = i * Math.PI / 12
        drawCircle(Color(0xFF497470).copy(alpha=.5f), 1.6f, Offset(100f+82*cos(a).toFloat(),96f+82*sin(a).toFloat()))
    }
    val base = when(id) {
        0 -> Color(0xFFE0C58C); 1 -> Color(0xFFB6A37F); 2 -> Color(0xFF537A3D); 3 -> Color(0xFF70984B)
        4 -> Color(0xFFECE1C5); 5 -> Color(0xFFFFF9E6); 6 -> Color(0xFFAC452B); 7 -> Color(0xFFE7D8AE)
        8 -> Color(0xFFEBDDC2); else -> Color(0xFFE9D09B)
    }
    drawCircle(base, 68f, Offset(100f,96f))
    when(id) {
        0,1,5,6 -> {
            for(i in 0..3) drawArc(if(id==6) Color(0xFFCA7139) else Gold.copy(alpha=.7f), 20f+i*18, 270f, false, Offset(54f+i*7,50f+i*7), Size(92f-i*14,92f-i*14), style=Stroke(3f))
            drawOval(Color(0xFFD4A536),Offset(76f,74f),Size(49f,35f))
            if(id==0) for(i in 0..6) {
                val a=i*Math.PI/3.5
                drawCircle(Color(0xFFECD7A5),6f,Offset(100f+26*cos(a).toFloat(),96f+24*sin(a).toFloat()))
            }
            if(id==6) for(i in 0..4) {
                val x=65f+i*15; drawOval(Color(0xFF74502E),Offset(x,83f+(i%2)*20),Size(15f,10f))
                drawLine(Color(0xFFC19B68),Offset(x+7,84f+(i%2)*20),Offset(x+7,91f+(i%2)*20),2f)
            }
            if(id==5) for(i in 0..15) drawCircle(Olive.copy(alpha=.7f),1.8f,Offset(72f+(i*17%60),70f+(i*23%55)))
        }
        2,3 -> {
            for(i in 0..42) {
                val x=49f+(i*31%101); val y=45f+(i*47%105)
                if ((x-100)*(x-100)+(y-96)*(y-96)<3600) {
                    drawOval(if(i%3==0) Color(0xFF8DBD60) else Color(0xFF365F31),Offset(x,y),Size(13f,8f))
                    if(i%5==0) drawRoundRect(Color(0xFFC65039),Offset(x,y),Size(12f,10f),androidx.compose.ui.geometry.CornerRadius(2f))
                    if(id==3 && i%7==0) rotate(30f,Offset(x,y)) { drawRect(Color(0xFFCBA265),Offset(x,y),Size(22f,15f)) }
                    if(id==2 && i%2==0) drawCircle(Color(0xFFE0CC8D),2f,Offset(x+4,y+3))
                }
            }
        }
        4 -> {
            listOf(Offset(70f,77f),Offset(128f,78f),Offset(100f,126f)).forEach { c ->
                drawCircle(Color(0xFF78512B),25f,c); drawCircle(Color(0xFFAD803A),22f,Offset(c.x-2,c.y-3))
                for(j in 0..12) drawCircle(if(j%3==0) Olive else Color(0xFFCCA75A),1.6f,Offset(c.x-15+(j*11%30),c.y-15+(j*17%30)))
            }
        }
        7 -> for(i in 0..3) {
            val x=54f+(i%2)*48; val y=53f+(i/2)*47
            rotate(-18f,Offset(x+20,y+17)) {
                drawRoundRect(Color(0xFF4E6332),Offset(x,y),Size(40f,30f),androidx.compose.ui.geometry.CornerRadius(12f))
                for(j in 0..3) drawLine(Color(0xFF81924D),Offset(x+8+j*7,y+4),Offset(x+8+j*7,y+26),1.5f)
            }
        }
        8 -> for(i in 0..1) {
            val x=78f+i*46; val y=88f+i*23
            rotate(if(i==0) -20f else 25f,Offset(x,y)) {
                val p=Path().apply { moveTo(x,y-43); cubicTo(x-40,y-10,x-30,y+27,x,y+43); cubicTo(x+30,y+27,x+40,y-10,x,y-43); close() }
                drawPath(p,Color(0xFF99613A))
                for(j in 0..8) drawCircle(Color(0xFFC08C46),1.5f,Offset(x-12+(j*7%24),y-20+(j*13%40)))
            }
        }
        else -> for(i in 0..13) {
            val x=51f+(i*31%87); val y=49f+(i*29%87)
            rotate((i*19).toFloat(), Offset(x+10,y+10)) {
                drawRoundRect(Color(0xFFB87E37),Offset(x,y),Size(23f,23f),androidx.compose.ui.geometry.CornerRadius(3f))
                drawRect(Color(0xFFE6B65F),Offset(x+1,y+1),Size(17f,16f))
                drawCircle(Color(0xFFB3392E),2f,Offset(x+8,y+7))
            }
        }
    }
    // Parsley sprig and a pinch of sumac.
    for(i in 0..3) drawOval(Olive,Offset(117f+i*4,121f-i*6),Size(12f,6f))
    drawLine(Olive,Offset(118f,139f),Offset(140f,112f),2f)
    for(i in 0..8) drawCircle(Wine.copy(alpha=.8f),1.4f,Offset(59f+i*4,115f+(i*7%18)))
}
@Composable fun TableArt(modifier: Modifier) {
    Canvas(modifier) {
        val scale = size.width/400f
        withTransform({ scale(scale,scale,Offset.Zero) }) {
            drawRect(Color(0xFFE9E6D7),size=Size(400f,400f))
            for(i in 0..14) drawLine(Color.White.copy(alpha=.22f),Offset(i*32f,0f),Offset(i*32f,400f),2f)
            // Linen table runner.
            drawRect(Color(0xFFDDD0AE),Offset(0f,90f),Size(400f,100f))
            for(i in 0..24) drawLine(Wine.copy(alpha=.25f),Offset(i*18f,95f),Offset(i*18f+12,102f),3f)
            withTransform({ translate(-25f,-26f); scale(.86f,.86f,Offset.Zero) }) { dish(0) }
            withTransform({ translate(170f,-17f); scale(.88f,.88f,Offset.Zero) }) { dish(4) }
            withTransform({ translate(77f,79f); scale(.95f,.95f,Offset.Zero) }) { dish(2) }
            drawOval(Color(0xFFDFC17B),Offset(304f,167f),Size(85f,59f))
            for(i in 0..8) drawCircle(Color(0xFFC49B54),2f,Offset(318f+i*7,180f+(i*13%32)))
            drawLine(Olive,Offset(20f,223f),Offset(60f,193f),3f)
            for(i in 0..5) drawOval(Olive,Offset(22f+i*6,206f-i*4),Size(18f,8f))
        }
    }
}
@Composable fun ChefArt(modifier: Modifier, guest: Int) {
    Canvas(modifier) {
        val s=size.minDimension/100f
        withTransform({ scale(s,s,Offset.Zero) }) {
            drawCircle(Color(0xFFE7E9D9),50f,Offset(50f,50f))
            drawOval(if(guest%2==0) Olive else Wine,Offset(18f,67f),Size(64f,48f))
            drawOval(Color(0xFF3F3028),Offset(25f,17f),Size(50f,66f))
            drawOval(Color(0xFFDCA87D),Offset(29f,29f),Size(42f,49f))
            drawCircle(Color(0xFF3F3028),3f,Offset(40f,51f)); drawCircle(Color(0xFF3F3028),3f,Offset(60f,51f))
            drawArc(Wine,10f,160f,false,Offset(42f,55f),Size(16f,10f),style=Stroke(2f))
            drawCircle(Color.White,14f,Offset(33f,23f)); drawCircle(Color.White,17f,Offset(50f,17f)); drawCircle(Color.White,14f,Offset(67f,23f))
            drawRoundRect(Color.White,Offset(29f,23f),Size(42f,13f),androidx.compose.ui.geometry.CornerRadius(3f))
            drawLine(Gold,Offset(33f,31f),Offset(67f,31f),1.5f)
            drawCircle(Gold,2f,Offset(50f,87f)); drawCircle(Gold,2f,Offset(50f,96f))
        }
    }
}

/** A warm Lebanese dining room, drawn locally with no network assets. */
@Composable fun RestaurantArt(modifier: Modifier, guest: Int = -1, servings: Int = 0) {
    Canvas(modifier) {
        val sx = size.width / 400f
        val sy = size.height / 240f
        withTransform({ scale(sx,sy,Offset.Zero) }) {
            drawRect(Color(0xFFECDCC2), size = Size(400f,240f))
            drawRect(Color(0xFFDFCCAC), Offset(0f,145f),Size(400f,95f))
            // Perspective floor tiles.
            for(i in 0..10) drawLine(Color(0xFFC5AC87),Offset(200f+(i-5)*18,145f),Offset((i-5)*85f+200,240f),1f)
            for(y in listOf(157f,175f,199f,229f)) drawLine(Color(0xFFC5AC87),Offset(0f,y),Offset(400f,y),1f)
            drawRect(Olive,Offset(0f,133f),Size(400f,8f))
            // Arched windows, evening sky and cedar silhouettes.
            for(x in listOf(35f,169f,303f)) {
                val arch=Path().apply { moveTo(x,128f); lineTo(x,61f); cubicTo(x,17f,x+62,17f,x+62,61f); lineTo(x+62,128f); close() }
                drawPath(arch,Color(0xFF89A8AA))
                drawCircle(Color(0xFFF4D98D),9f,Offset(x+44,54f))
                val cedar=Path().apply { moveTo(x+30,66f); lineTo(x+12,87f); lineTo(x+22,87f); lineTo(x+7,106f); lineTo(x+27,106f); lineTo(x+27,119f); lineTo(x+33,119f); lineTo(x+33,106f); lineTo(x+53,106f); lineTo(x+38,87f); lineTo(x+48,87f); close() }
                drawPath(cedar,Olive)
                drawPath(arch,Color(0xFFAF8053),style=Stroke(5f))
                drawLine(Color(0xFFAF8053),Offset(x+31,35f),Offset(x+31,128f),3f)
                drawLine(Color(0xFFAF8053),Offset(x,78f),Offset(x+62,78f),3f)
            }
            // Hanging brass lanterns.
            for(x in listOf(132f,268f)) {
                drawLine(Color(0xFF69583A),Offset(x,0f),Offset(x,38f),2f)
                drawCircle(Gold.copy(alpha=.12f),25f,Offset(x,53f))
                drawRoundRect(Gold,Offset(x-10,38f),Size(20f,28f),androidx.compose.ui.geometry.CornerRadius(5f))
                drawRect(Color(0xFFFFE1A1),Offset(x-5,43f),Size(10f,18f))
                drawLine(Olive,Offset(x-12,67f),Offset(x+12,67f),2f)
            }
            // Background bistro tables.
            for(x in listOf(40f,330f)) {
                drawRoundRect(Wine,Offset(x-19,148f),Size(18f,38f),androidx.compose.ui.geometry.CornerRadius(5f))
                drawRoundRect(Wine,Offset(x+41,148f),Size(18f,38f),androidx.compose.ui.geometry.CornerRadius(5f))
                drawLine(Color(0xFF71523A),Offset(x+20,165f),Offset(x+20,199f),5f)
                drawOval(Color(0xFFF9F1DF),Offset(x-10,153f),Size(80f,24f))
                drawOval(Gold,Offset(x+2,158f),Size(20f,9f))
                drawOval(Gold,Offset(x+29,158f),Size(20f,9f))
            }
            // Guest seated at the player's table; each order gets a different outfit.
            val person = if(guest < 0) 0 else guest
            drawRoundRect(Wine,Offset(164f,131f),Size(72f,70f),androidx.compose.ui.geometry.CornerRadius(12f))
            drawOval(if(person%2==0) Olive else Color(0xFF9D5260),Offset(171f,125f),Size(58f,52f))
            drawOval(Color(0xFF453129),Offset(181f,79f),Size(39f,48f))
            drawOval(if(person%2==0) Color(0xFFDCA87D) else Color(0xFFC68C63),Offset(184f,88f),Size(32f,39f))
            drawCircle(Ink,2f,Offset(194f,103f)); drawCircle(Ink,2f,Offset(207f,103f))
            drawArc(Wine,10f,160f,false,Offset(195f,109f),Size(11f,6f),style=Stroke(1.5f))
            // Foreground table, linen runner, ceramic plate, water and flowers.
            drawLine(Color(0xFF71523A),Offset(145f,201f),Offset(137f,240f),6f)
            drawLine(Color(0xFF71523A),Offset(254f,201f),Offset(263f,240f),6f)
            drawOval(Color(0xFFB78B58),Offset(105f,168f),Size(190f,57f))
            drawOval(Color(0xFFFAF1DE),Offset(105f,164f),Size(190f,53f))
            drawRect(Wine.copy(alpha=.8f),Offset(177f,166f),Size(45f,50f))
            drawOval(Color.White,Offset(151f,178f),Size(54f,25f))
            drawOval(Olive.copy(alpha=.35f),Offset(156f,181f),Size(44f,18f),style=Stroke(1.5f))
            for(i in 0 until servings.coerceAtMost(6)) drawCircle(if(i%2==0) Gold else Olive,4f,Offset(164f+(i%3)*12,186f+(i/3)*8))
            drawRoundRect(Color(0xFFB5D5D5),Offset(230f,175f),Size(13f,19f),androidx.compose.ui.geometry.CornerRadius(3f))
            drawLine(Olive,Offset(254f,184f),Offset(254f,162f),2f)
            drawCircle(Wine,5f,Offset(254f,161f))
            drawOval(Gold,Offset(247f,181f),Size(14f,10f))
            // Terracotta planter.
            drawRoundRect(Color(0xFFB96B46),Offset(9f,200f),Size(26f,32f),androidx.compose.ui.geometry.CornerRadius(4f))
            for(i in 0..4) {
                drawLine(Olive,Offset(22f,204f),Offset(10f+i*6,169f+(i%2)*9),2f)
                drawOval(Olive,Offset(4f+i*6,169f+(i%2)*9),Size(15f,8f))
            }
        }
    }
}
