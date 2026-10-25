/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ken.mizoguch.ladders;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import ken.mizoguch.console.Console;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * draw the ladders of the editor in a pdf file : the ladders are taken from the json
 * representation saved by the editor, so the drawing does not need the nodes of the editor
 * nor the state of the ladder ; only the name of the file of the ladder is needed for the
 * header of the pages
 *
 * @author mizoguch-ken
 */
class LadderPdfExporter {

    private final LadderJson ladderJson_;
    private final String ladderFileName_;
    private final CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap_;

    LadderPdfExporter(LadderJson ladderJson, String ladderFileName,
            CopyOnWriteArrayList<ConcurrentHashMap<String, String>> commentMap) {
        ladderJson_ = ladderJson;
        ladderFileName_ = ladderFileName;
        commentMap_ = commentMap;
    }

    /**
     * comment of an address of a ladder : the comments of the editor are given to the
     * exporter, so the drawing does not need the editor of the ladders
     *
     * @param index of a ladder
     * @param address
     * @return the comment of the address, null when the address has no comment
     */
    private String getComment(int index, String address) {
        if ((index < 0) || (index >= commentMap_.size())) {
            return null;
        }
        return commentMap_.get(index).get(address);
    }

    private PDPage pdfNewPage(PDDocument doc, PDRectangle rectangle) {
        PDPage page = new PDPage(rectangle);
        doc.addPage(page);
        return page;
    }

    private PDPageContentStream pdfPageBeginGrid(PDDocument doc, PDPage page, PDFont font, float fontSize,
            float lineWidth, float marginTop, float marginLeft) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND,
                true, true);
        contentStream.setFont(font, fontSize);
        contentStream.setLeading(font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f * fontSize);
        contentStream.setLineWidth(lineWidth);
        return contentStream;
    }

    private void pdfPageEndGrid(PDPageContentStream contentStream) throws IOException {
        contentStream.close();
    }

    Path export(Path file) {
        if (file != null) {
            try (PDDocument doc = new PDDocument();
                    TrueTypeCollection ttc = new TrueTypeCollection(
                        this.getClass().getClassLoader().getResourceAsStream("font/MyricaM.TTC"))) {
                PDRectangle rectangle = PDRectangle.A4;
                PDPage page;
                PDPageContentStream contentStream;
                PDFont font = PDType0Font.load(doc, ttc.getFontByName("MyricaMM"), true);
                float margin = 20f * 25.4f / 72f;
                float fontSize = 7.54f;
                float textWidth, textHeight;
                float gridWidth, gridHeight;
                float fontWidth = font.getFontDescriptor().getFontBoundingBox().getWidth() / 1000f * fontSize;
                float fontHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f * fontSize;
                float pageShowMinWidth = margin;
                float pageShowMaxWidth = rectangle.getWidth() - margin;
                float pageShowMinHeight = margin;
                float pageShowMaxHeight = rectangle.getHeight() - margin;
                float lineWidth = 1f;
                float contentsSize = font.getFontDescriptor().getFontBoundingBox().getWidth() / 1000f * fontSize * 4f;
                float gridSize = (rectangle.getWidth() - (pageShowMinWidth * 2f) - contentsSize) / 10f;
                float gridSize2 = gridSize / 2f;
                float gridSize3 = gridSize / 3f;
                float gridSize4 = gridSize / 4f;
                float gridSize6 = gridSize / 6f;
                float gridSize12 = gridSize / 12f;

                // the json of the ladders is given to the constructor of the exporter
                List<LadderJsonLadder> jsonLadders = ladderJson_.getLadders();

                String name, address, comment, line, content, c;
                int index, size, index2, size2, column, row, i;

                size = (jsonLadders == null) ? 0 : jsonLadders.size();
                List<LadderJsonBlock> jsonBlocks;
                LadderJsonBlock jsonBlock;
                for (index = 0; index < size; index++) {
                    column = 0;
                    row = 0;
                    name = jsonLadders.get(index).getName();
                    page = pdfNewPage(doc, rectangle);
                    gridWidth = pageShowMinWidth + contentsSize;
                    gridHeight = rectangle.getHeight() - pageShowMinHeight - contentsSize;
                    contentStream = pdfPageBeginGrid(doc, page, font, fontSize, lineWidth, pageShowMinWidth,
                            pageShowMinHeight);
                    if (ladderFileName_ == null) {
                        // page header name & title
                        contentStream.beginText();
                        contentStream.newLineAtOffset(
                                ((pageShowMaxWidth - (font.getStringWidth(name) / 1000f * fontSize)) / 2f)
                                        + pageShowMinWidth + fontWidth,
                                pageShowMaxHeight - fontHeight);
                        contentStream.showText(name);
                        contentStream.endText();
                        gridHeight += fontHeight;
                    } else {
                        // page header name
                        contentStream.beginText();
                        contentStream.newLineAtOffset(((pageShowMaxWidth
                                - (font.getStringWidth(name + " [" + ladderFileName_ + "]") / 1000f * fontSize)) / 2f)
                                + pageShowMinWidth + fontWidth, pageShowMaxHeight - fontHeight);
                        contentStream.showText(name + " [" + ladderFileName_ + "]");
                        contentStream.endText();
                        gridHeight += fontHeight;
                    }

                    for (i = 1; i < LadderDefaults.LADDER_DEFAULT_GRID_COLUMN; i++) {
                        contentStream.beginText();
                        content = String.valueOf(i);
                        contentStream.newLineAtOffset(gridWidth + (gridSize * (i - 1)) + gridSize2
                                + ((font.getStringWidth(content) / 1000f * fontSize) / 2f), gridHeight);
                        contentStream.showText(content);
                        contentStream.endText();
                    }

                    jsonBlocks = jsonLadders.get(index).getBlocks();
                    size2 = (jsonBlocks == null) ? 0 : jsonBlocks.size();
                    for (index2 = 0; index2 < size2; index2++) {
                        jsonBlock = jsonBlocks.get(index2);
                        if (row < jsonBlock.getRowIndex()) {
                            gridWidth = pageShowMinWidth + contentsSize;
                            column = 0;
                            for (; row < jsonBlock.getRowIndex(); row++) {
                                if (row > 0) {
                                    gridHeight -= gridSize;
                                }
                                if (gridHeight < (pageShowMinHeight + gridSize)) {
                                    pdfPageEndGrid(contentStream);
                                    page = pdfNewPage(doc, rectangle);
                                    contentStream = pdfPageBeginGrid(doc, page, font, fontSize, lineWidth,
                                            pageShowMinWidth, pageShowMinHeight);
                                    gridHeight = rectangle.getHeight() - pageShowMinHeight - contentsSize;

                                    for (i = 1; i < LadderDefaults.LADDER_DEFAULT_GRID_COLUMN; i++) {
                                        contentStream.beginText();
                                        content = String.valueOf(i);
                                        contentStream.newLineAtOffset(
                                                gridWidth + (gridSize * (i - 1)) + gridSize2
                                                        + ((font.getStringWidth(content) / 1000f * fontSize) / 2f),
                                                gridHeight);
                                        contentStream.showText(content);
                                        contentStream.endText();
                                    }
                                }

                                contentStream.beginText();
                                content = String.valueOf(row + 1);
                                contentStream.newLineAtOffset(gridWidth
                                        + ((-contentsSize + (font.getStringWidth(content) / 1000f * fontSize)) / 2f),
                                        gridHeight - gridSize2);
                                contentStream.showText(content);
                                contentStream.endText();
                            }
                        }
                        if (column < jsonBlock.getColumnIndex()) {
                            if (column == 0) {
                                column = 1;
                            }
                            gridWidth += (jsonBlock.getColumnIndex() - column) * gridSize;
                            column = jsonBlock.getColumnIndex();
                        }

                        address = jsonBlock.getAddress();
                        if (address == null) {
                            comment = jsonBlock.getComment();
                        } else {
                            if (address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                comment = getComment(index + 1, jsonBlock.getAddress());
                            } else {
                                comment = getComment(LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX, jsonBlock.getAddress());
                            }
                        }
                        if (jsonBlock.isVertical()) {
                            contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                            contentStream.lineTo(gridWidth, gridHeight);
                        }
                        if (jsonBlock.isVerticalOr()) {
                            contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                            contentStream.lineTo(gridWidth, gridHeight - gridSize);
                        }
                        contentStream.stroke();

                        switch (LadderBlock.valueOf(jsonBlock.getBlock())) {
                            case BLOCK_COMMENT:
                                textWidth = gridWidth;
                                textHeight = gridHeight;

                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                for (i = 0; i < comment.length(); i++) {
                                    c = comment.substring(i, i + 1);
                                    textWidth += font.getStringWidth(c) / 1000 * fontSize;
                                    if (textWidth > (gridWidth + gridSize - fontWidth)) {
                                        textHeight += fontHeight;
                                        if (textHeight > (gridHeight + gridSize - fontHeight)) {
                                            break;
                                        }
                                        contentStream.newLine();
                                        textWidth = gridWidth;
                                    }
                                    contentStream.showText(c);
                                }
                                contentStream.endText();
                                break;
                            case CONNECT_LINE:
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                contentStream.stroke();
                                break;
                            case LOAD:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                contentStream.stroke();
                                break;
                            case LOAD_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                // -|/|-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                break;
                            case LOAD_RISING:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                // -|↑|-
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.stroke();
                                break;
                            case LOAD_RISING_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                // -|/|-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -|/↑|-
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.stroke();
                                break;
                            case LOAD_FALLING:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                // -|↓|-
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.stroke();
                                break;
                            case LOAD_FALLING_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                // -|/|-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -|/↓|-
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.stroke();
                                break;
                            case OUT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -(
                                contentStream.moveTo(gridWidth + gridSize3, gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth, gridHeight - gridSize2, gridWidth + gridSize3,
                                        gridHeight - gridSize2 - gridSize4);
                                // -( )
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth + gridSize, gridHeight - gridSize2,
                                        gridWidth + gridSize - gridSize3, gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                break;
                            case OUT_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -(
                                contentStream.moveTo(gridWidth + gridSize3, gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth, gridHeight - gridSize2, gridWidth + gridSize3,
                                        gridHeight - gridSize2 - gridSize4);
                                // -( )
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth + gridSize, gridHeight - gridSize2,
                                        gridWidth + gridSize - gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(/)
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize3, gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                break;
                            case OUT_RISING:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -(
                                contentStream.moveTo(gridWidth + gridSize3, gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth, gridHeight - gridSize2, gridWidth + gridSize3,
                                        gridHeight - gridSize2 - gridSize4);
                                // -( )
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth + gridSize, gridHeight - gridSize2,
                                        gridWidth + gridSize - gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(↑)
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.stroke();
                                break;
                            case OUT_RISING_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -(
                                contentStream.moveTo(gridWidth + gridSize3, gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth, gridHeight - gridSize2, gridWidth + gridSize3,
                                        gridHeight - gridSize2 - gridSize4);
                                // -( )
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth + gridSize, gridHeight - gridSize2,
                                        gridWidth + gridSize - gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(/)
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(/↑)
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 + gridSize4 - gridSize12);
                                contentStream.stroke();
                                break;
                            case OUT_FALLING:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -(
                                contentStream.moveTo(gridWidth + gridSize3, gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth, gridHeight - gridSize2, gridWidth + gridSize3,
                                        gridHeight - gridSize2 - gridSize4);
                                // -( )
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth + gridSize, gridHeight - gridSize2,
                                        gridWidth + gridSize - gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(↓)
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.stroke();
                                break;
                            case OUT_FALLING_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -(
                                contentStream.moveTo(gridWidth + gridSize3, gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth, gridHeight - gridSize2, gridWidth + gridSize3,
                                        gridHeight - gridSize2 - gridSize4);
                                // -( )
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.curveTo2(gridWidth + gridSize, gridHeight - gridSize2,
                                        gridWidth + gridSize - gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(/)
                                contentStream.moveTo(gridWidth + gridSize - gridSize3,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize3, gridHeight - gridSize2 - gridSize4);
                                // -(/↓)
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 - gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.moveTo(gridWidth + gridSize2, gridHeight - gridSize2 - gridSize4);
                                contentStream.lineTo(gridWidth + gridSize2 + gridSize12,
                                        gridHeight - gridSize2 - gridSize4 + gridSize12);
                                contentStream.stroke();
                                break;
                            case COMPARISON_EQUAL:
                            case COMPARISON_NOT_EQUAL:
                            case COMPARISON_LESS:
                            case COMPARISON_LESS_EQUAL:
                            case COMPARISON_GREATER:
                            case COMPARISON_GREATER_EQUAL:
                            case COMPARISON_AND_BITS:
                            case COMPARISON_OR_BITS:
                            case COMPARISON_XOR_BITS:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6, gridHeight - gridSize2);
                                if (jsonBlock.getBlockFunctions().get(0).address() != null) {
                                    contentStream.showText(jsonBlock.getBlockFunctions().get(0).address());
                                } else {
                                    switch (jsonBlock.getBlockFunctions().get(0).radix()) {
                                        case 10:
                                            contentStream.showText(
                                                    Double.toString(jsonBlock.getBlockFunctions().get(0).value()));
                                            break;
                                        case 16:
                                            contentStream.showText("0x" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value().longValue(), 16));
                                            break;
                                        case 2:
                                            contentStream.showText("0b" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value().longValue(), 2));
                                            break;
                                    }
                                }
                                contentStream.endText();
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -| |-
                                contentStream.moveTo(gridWidth + gridSize - gridSize6, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize, gridHeight - gridSize2);
                                contentStream.stroke();
                                // command
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 + gridSize6);
                                contentStream.showText(LadderBlock.valueOf(jsonBlock.getBlock()).toCommand());
                                contentStream.endText();
                                break;
                            case SET:
                            case RESET:
                            case RANDOM:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                // command
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 + gridSize6);
                                contentStream.showText(LadderBlock.valueOf(jsonBlock.getBlock()).toCommand());
                                contentStream.endText();
                                break;
                            case NOT_BITS:
                            case TIMER:
                            case COUNTER:
                            case MOVE:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6, gridHeight - gridSize2);
                                if (jsonBlock.getBlockFunctions().get(0).address() != null) {
                                    contentStream.showText(jsonBlock.getBlockFunctions().get(0).address());
                                } else {
                                    switch (jsonBlock.getBlockFunctions().get(0).radix()) {
                                        case 10:
                                            contentStream.showText(
                                                    Double.toString(jsonBlock.getBlockFunctions().get(0).value()));
                                            break;
                                        case 16:
                                            contentStream.showText("0x" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value().longValue(), 16));
                                            break;
                                        case 2:
                                            contentStream.showText("0b" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value().longValue(), 2));
                                            break;
                                    }
                                }
                                contentStream.endText();
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                // command
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 + gridSize6);
                                contentStream.showText(LadderBlock.valueOf(jsonBlock.getBlock()).toCommand());
                                contentStream.endText();
                                break;
                            case TIMER_NOT:
                            case COUNTER_NOT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6, gridHeight - gridSize2);
                                if (jsonBlock.getBlockFunctions().get(0).address() != null) {
                                    contentStream.showText(jsonBlock.getBlockFunctions().get(0).address());
                                } else {
                                    switch (jsonBlock.getBlockFunctions().get(0).radix()) {
                                        case 10:
                                            contentStream.showText(
                                                    Double.toString(jsonBlock.getBlockFunctions().get(0).value()));
                                            break;
                                        case 16:
                                            contentStream.showText("0x" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value().longValue(), 16));
                                            break;
                                        case 2:
                                            contentStream.showText("0b" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value().longValue(), 2));
                                            break;
                                    }
                                }
                                contentStream.endText();
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                // -|/|
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                // command
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 + gridSize6);
                                contentStream.showText(LadderBlock.valueOf(jsonBlock.getBlock()).toCommand());
                                contentStream.endText();
                                break;
                            case AND_BITS:
                            case OR_BITS:
                            case XOR_BITS:
                            case ADDITION:
                            case SUBTRACTION:
                            case MULTIPLICATION:
                            case DIVISION:
                            case AVERAGE:
                            case SHIFT_LEFT_BITS:
                            case SHIFT_RIGHT_BITS:
                            case SIGMOID:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6, gridHeight - gridSize2);
                                if (jsonBlock.getBlockFunctions().get(0).address() != null) {
                                    contentStream.showText(jsonBlock.getBlockFunctions().get(0).address());
                                } else {
                                    switch (jsonBlock.getBlockFunctions().get(0).radix()) {
                                        case 10:
                                            contentStream.showText(
                                                    Double.toString(jsonBlock.getBlockFunctions().get(0).value()));
                                            break;
                                        case 16:
                                            contentStream.showText("0x" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value().longValue(), 16));
                                            break;
                                        case 2:
                                            contentStream.showText("0b" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(0).value().longValue(), 2));
                                            break;
                                    }
                                }
                                contentStream.endText();
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 - gridSize6);
                                if (jsonBlock.getBlockFunctions().get(1).address() != null) {
                                    contentStream.showText(jsonBlock.getBlockFunctions().get(1).address());
                                } else {
                                    switch (jsonBlock.getBlockFunctions().get(1).radix()) {
                                        case 10:
                                            contentStream.showText(
                                                    Double.toString(jsonBlock.getBlockFunctions().get(1).value()));
                                            break;
                                        case 16:
                                            contentStream.showText("0x" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(1).value().longValue(), 16));
                                            break;
                                        case 2:
                                            contentStream.showText("0b" + Long.toString(
                                                    jsonBlock.getBlockFunctions().get(1).value().longValue(), 2));
                                            break;
                                    }
                                }
                                contentStream.endText();
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                // command
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 + gridSize6);
                                contentStream.showText(LadderBlock.valueOf(jsonBlock.getBlock()).toCommand());
                                contentStream.endText();
                                break;
                            case SCRIPT:
                                // address
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize6);
                                contentStream.showText(address);
                                contentStream.endText();
                                // comment
                                if (comment != null) {
                                    contentStream.beginText();
                                    contentStream.newLineAtOffset(gridWidth, gridHeight - gridSize + gridSize6);
                                    contentStream.showText(comment);
                                    contentStream.endText();
                                }
                                // script
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6, gridHeight - gridSize2);
                                contentStream.showText(jsonBlock.getBlockScript());
                                contentStream.endText();
                                // -
                                contentStream.moveTo(gridWidth, gridHeight - gridSize2);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2);
                                // -|
                                contentStream.moveTo(gridWidth + gridSize6, gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize6, gridHeight - gridSize2 - gridSize4);
                                // -| |
                                contentStream.moveTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 + gridSize4);
                                contentStream.lineTo(gridWidth + gridSize - gridSize6,
                                        gridHeight - gridSize2 - gridSize4);
                                contentStream.stroke();
                                // command
                                contentStream.beginText();
                                contentStream.newLineAtOffset(gridWidth + gridSize6,
                                        gridHeight - gridSize2 + gridSize6);
                                contentStream.showText(LadderBlock.valueOf(jsonBlock.getBlock()).toCommand());
                                contentStream.endText();
                                break;
                            default:
                                break;
                        }
                    }
                    pdfPageEndGrid(contentStream);
                }

                // page footer count
                for (index = 0; index < doc.getNumberOfPages(); index++) {
                    line = (index + 1) + " / " + doc.getNumberOfPages();
                    contentStream = pdfPageBeginGrid(doc, doc.getPage(index), font, fontSize, lineWidth,
                            pageShowMinWidth, pageShowMinHeight);
                    contentStream.beginText();
                    contentStream
                            .newLineAtOffset(((pageShowMaxWidth - (font.getStringWidth(line) / 1000f * fontSize)) / 2f)
                                    + pageShowMinWidth + fontWidth, pageShowMinHeight);
                    contentStream.showText(line);
                    contentStream.endText();
                    pdfPageEndGrid(contentStream);
                }

                doc.save(file.toFile());
                return file;
            } catch (IOException ex) {
                Console.writeStackTrace(LadderPdfExporter.class.getName(), ex);
            }
        }
        return null;
    }
}
