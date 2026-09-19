package com.example.shiyu.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class MdSegment {
    data class Plain(val text: String) : MdSegment()
    data class Bold(val text: String) : MdSegment()
    data class Italic(val text: String) : MdSegment()
    data class InlineCode(val text: String) : MdSegment()
}

sealed class MdBlock {
    data class TextBlock(val segments: List<MdSegment>) : MdBlock()
    data class Header(val level: Int, val text: String) : MdBlock()
    data class CodeBlock(val language: String, val code: String) : MdBlock()
    data class BulletItem(val segments: List<MdSegment>, val indent: Int) : MdBlock()
    data class NumberedItem(val number: Int, val segments: List<MdSegment>, val indent: Int) : MdBlock()
    data class Quote(val segments: List<MdSegment>) : MdBlock()
    data class Divider(val char: Char) : MdBlock()
    object EmptyLine : MdBlock()
}

fun parseMarkdown(text: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val lines = text.split("\n")
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        if (line.isBlank()) {
            blocks.add(MdBlock.EmptyLine)
            i++
            continue
        }

        val codeBlockMatch = Regex("^```(\\w*)").find(line)
        if (codeBlockMatch != null) {
            val language = codeBlockMatch.groupValues[1]
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            if (i < lines.size) i++
            blocks.add(MdBlock.CodeBlock(language, codeLines.joinToString("\n")))
            continue
        }

        val dividerMatch = Regex("^([-*_])\\1{2,}$").matchEntire(line.trim())
        if (dividerMatch != null) {
            blocks.add(MdBlock.Divider(dividerMatch.groupValues[1][0]))
            i++
            continue
        }

        val headerMatch = Regex("^(#{1,3})\\s+(.+)").find(line)
        if (headerMatch != null) {
            val level = headerMatch.groupValues[1].length
            val headerText = headerMatch.groupValues[2]
            blocks.add(MdBlock.Header(level, headerText))
            i++
            continue
        }

        val quoteMatch = Regex("^>\\s?(.*)").find(line)
        if (quoteMatch != null) {
            val quoteText = quoteMatch.groupValues[1]
            blocks.add(MdBlock.Quote(parseInline(quoteText)))
            i++
            continue
        }

        val bulletMatch = Regex("^(\\s*)[-*+]\\s+(.+)").find(line)
        if (bulletMatch != null) {
            val indent = bulletMatch.groupValues[1].length / 2
            val bulletText = bulletMatch.groupValues[2]
            blocks.add(MdBlock.BulletItem(parseInline(bulletText), indent))
            i++
            continue
        }

        val numberedMatch = Regex("^(\\s*)(\\d+)\\.\\s+(.+)").find(line)
        if (numberedMatch != null) {
            val indent = numberedMatch.groupValues[1].length / 2
            val number = numberedMatch.groupValues[2].toIntOrNull() ?: 1
            val numberedText = numberedMatch.groupValues[3]
            blocks.add(MdBlock.NumberedItem(number, parseInline(numberedText), indent))
            i++
            continue
        }

        blocks.add(MdBlock.TextBlock(parseInline(line)))
        i++
    }

    return blocks
}

fun parseInline(text: String): List<MdSegment> {
    val segments = mutableListOf<MdSegment>()
    var pos = 0

    while (pos < text.length) {
        if (pos < text.length - 1 && text[pos] == '`') {
            val end = text.indexOf('`', pos + 1)
            if (end != -1) {
                if (pos + 1 < end) {
                    segments.add(MdSegment.InlineCode(text.substring(pos + 1, end)))
                }
                pos = end + 1
                continue
            }
        }

        if (pos < text.length - 1 && text[pos] == '*' && text[pos + 1] == '*') {
            val end = text.indexOf("**", pos + 2)
            if (end != -1) {
                if (pos + 2 < end) {
                    segments.add(MdSegment.Bold(text.substring(pos + 2, end)))
                }
                pos = end + 2
                continue
            }
        }

        if (text[pos] == '*' && (pos + 1 >= text.length || text[pos + 1] != '*')) {
            val end = text.indexOf('*', pos + 1)
            if (end != -1 && end > pos + 1 && (end + 1 >= text.length || text[end + 1] != '*')) {
                if (pos + 1 < end) {
                    segments.add(MdSegment.Italic(text.substring(pos + 1, end)))
                }
                pos = end + 1
                continue
            }
        }

        var nextSpecial = text.length
        for (ch in charArrayOf('`', '*')) {
            val idx = text.indexOf(ch, pos + 1)
            if (idx != -1 && idx < nextSpecial) {
                nextSpecial = idx
            }
        }
        if (nextSpecial == pos + 1 && pos < text.length) {
            nextSpecial = text.length
        }
        if (nextSpecial > pos) {
            segments.add(MdSegment.Plain(text.substring(pos, nextSpecial)))
            pos = nextSpecial
        } else {
            pos++
        }
    }

    return segments
}

private fun buildRichText(segments: List<MdSegment>, baseColor: Color): AnnotatedString {
    return buildAnnotatedString {
        for (segment in segments) {
            when (segment) {
                is MdSegment.Plain -> {
                    withStyle(SpanStyle(color = baseColor)) {
                        append(segment.text)
                    }
                }
                is MdSegment.Bold -> {
                    withStyle(SpanStyle(color = baseColor, fontWeight = FontWeight.Bold)) {
                        append(segment.text)
                    }
                }
                is MdSegment.Italic -> {
                    withStyle(SpanStyle(color = baseColor, fontStyle = FontStyle.Italic)) {
                        append(segment.text)
                    }
                }
                is MdSegment.InlineCode -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFFCDD6F4),
                            background = Color(0xFF313244)
                        )
                    ) {
                        append(segment.text)
                    }
                }
            }
        }
    }
}

@Composable
fun RichTextLine(
    segments: List<MdSegment>,
    textColor: Color,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp
) {
    val annotated = remember(segments, textColor) {
        buildRichText(segments, textColor)
    }
    Text(
        text = annotated,
        style = TextStyle(fontSize = fontSize, lineHeight = lineHeight)
    )
}

@Composable
fun HeaderBlock(
    level: Int,
    text: String,
    textColor: Color,
    fontSize: TextUnit = 14.sp
) {
    val headerFontSize = when (level) {
        1 -> 20.sp
        2 -> 17.sp
        3 -> 15.sp
        else -> 14.sp
    }
    val segments = remember(text) { parseInline(text) }
    val annotated = remember(segments, textColor) {
        buildAnnotatedString {
            for (segment in segments) {
                when (segment) {
                    is MdSegment.Plain -> {
                        withStyle(SpanStyle(color = textColor, fontWeight = FontWeight.Bold, fontSize = headerFontSize)) {
                            append(segment.text)
                        }
                    }
                    is MdSegment.Bold -> {
                        withStyle(SpanStyle(color = textColor, fontWeight = FontWeight.Bold, fontSize = headerFontSize)) {
                            append(segment.text)
                        }
                    }
                    is MdSegment.Italic -> {
                        withStyle(SpanStyle(color = textColor, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, fontSize = headerFontSize)) {
                            append(segment.text)
                        }
                    }
                    is MdSegment.InlineCode -> {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = (headerFontSize.value * 0.9).sp,
                                color = Color(0xFFCDD6F4),
                                background = Color(0xFF313244),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(segment.text)
                        }
                    }
                }
            }
        }
    }
    Text(
        text = annotated,
        modifier = Modifier.padding(vertical = 4.dp),
        style = TextStyle(fontSize = headerFontSize, lineHeight = (headerFontSize.value + 6).sp)
    )
}

@Composable
fun CodeBlockView(
    language: String,
    code: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E2E))
    ) {
        if (language.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF313244))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = language,
                    color = Color(0xFFA6ADC8),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                color = Color(0xFFCDD6F4),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun BulletItem(
    segments: List<MdSegment>,
    indent: Int,
    textColor: Color,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (16 + indent * 16).dp, top = 2.dp, bottom = 2.dp)
    ) {
        Text(
            text = "\u2022",
            color = textColor,
            fontSize = fontSize,
            lineHeight = lineHeight,
            modifier = Modifier.padding(end = 8.dp)
        )
        RichTextLine(
            segments = segments,
            textColor = textColor,
            fontSize = fontSize,
            lineHeight = lineHeight
        )
    }
}

@Composable
fun NumberedItem(
    number: Int,
    segments: List<MdSegment>,
    indent: Int,
    textColor: Color,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (16 + indent * 16).dp, top = 2.dp, bottom = 2.dp)
    ) {
        Text(
            text = "$number.",
            color = textColor,
            fontSize = fontSize,
            lineHeight = lineHeight,
            modifier = Modifier.padding(end = 8.dp)
        )
        RichTextLine(
            segments = segments,
            textColor = textColor,
            fontSize = fontSize,
            lineHeight = lineHeight
        )
    }
}

@Composable
fun QuoteBlock(
    segments: List<MdSegment>,
    textColor: Color,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF4A9EFF))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFF1E1E2E).copy(alpha = 0.5f))
                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
        ) {
            RichTextLine(
                segments = segments,
                textColor = textColor.copy(alpha = 0.9f),
                fontSize = fontSize,
                lineHeight = lineHeight
            )
        }
    }
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp
) {
    val blocks = remember(text) { parseMarkdown(text) }

    Column(modifier = modifier) {
        for (block in blocks) {
            when (block) {
                is MdBlock.EmptyLine -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                is MdBlock.Header -> {
                    HeaderBlock(
                        level = block.level,
                        text = block.text,
                        textColor = textColor,
                        fontSize = fontSize
                    )
                }
                is MdBlock.CodeBlock -> {
                    CodeBlockView(
                        language = block.language,
                        code = block.code
                    )
                }
                is MdBlock.BulletItem -> {
                    BulletItem(
                        segments = block.segments,
                        indent = block.indent,
                        textColor = textColor,
                        fontSize = fontSize,
                        lineHeight = lineHeight
                    )
                }
                is MdBlock.NumberedItem -> {
                    NumberedItem(
                        number = block.number,
                        segments = block.segments,
                        indent = block.indent,
                        textColor = textColor,
                        fontSize = fontSize,
                        lineHeight = lineHeight
                    )
                }
                is MdBlock.Quote -> {
                    QuoteBlock(
                        segments = block.segments,
                        textColor = textColor,
                        fontSize = fontSize,
                        lineHeight = lineHeight
                    )
                }
                is MdBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFF585B70),
                        thickness = 1.dp
                    )
                }
                is MdBlock.TextBlock -> {
                    RichTextLine(
                        segments = block.segments,
                        textColor = textColor,
                        fontSize = fontSize,
                        lineHeight = lineHeight
                    )
                }
            }
        }
    }
}
