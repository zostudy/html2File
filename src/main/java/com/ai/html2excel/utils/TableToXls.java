package com.ai.html2excel.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ai.html2excel.utils.css.CssApplier;
import com.ai.html2excel.utils.css.bean.XlsCssStyle;
import com.ai.html2excel.utils.css.support.AlignApplier;
import com.ai.html2excel.utils.css.support.BackgroundApplier;
import com.ai.html2excel.utils.css.support.BorderApplier;
import com.ai.html2excel.utils.css.support.HeightApplier;
import com.ai.html2excel.utils.css.support.TextApplier;
import com.ai.html2excel.utils.css.support.WidthApplier;

public class TableToXls {
    private static final Logger log = LoggerFactory.getLogger(TableToXls.class);
    private static final List<CssApplier> STYLE_APPLIERS = new LinkedList<CssApplier>();

    // static init
    static {
        STYLE_APPLIERS.add(new AlignApplier());
        STYLE_APPLIERS.add(new BackgroundApplier());
        STYLE_APPLIERS.add(new WidthApplier());
        STYLE_APPLIERS.add(new HeightApplier());
        STYLE_APPLIERS.add(new BorderApplier());
        STYLE_APPLIERS.add(new TextApplier());
    }

    private HSSFWorkbook workBook = new HSSFWorkbook();
    private HSSFSheet sheet;
    private Map<String, Object> cellsOccupied = new HashMap<String, Object>();
    private Map<String, HSSFCellStyle> cellStyles = new HashMap<String, HSSFCellStyle>();
    private HSSFCellStyle defaultCellStyle;
    private int maxRow = 0;

    // init
    {
        sheet = workBook.createSheet();
        defaultCellStyle = workBook.createCellStyle();
        defaultCellStyle.setWrapText(true);
        defaultCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        // border
        short black = new HSSFColor.BLACK().getIndex();
        short thin = CellStyle.BORDER_THIN;
        // top
        defaultCellStyle.setBorderTop(thin);
        defaultCellStyle.setTopBorderColor(black);
        // right
        defaultCellStyle.setBorderRight(thin);
        defaultCellStyle.setRightBorderColor(black);
        // bottom
        defaultCellStyle.setBorderBottom(thin);
        defaultCellStyle.setBottomBorderColor(black);
        // left
        defaultCellStyle.setBorderLeft(thin);
        defaultCellStyle.setLeftBorderColor(black);
    }

    /**
     * process html to xls
     *
     * @param html html char sequence
     *
     * @return xls bytes
     */
    public static byte[] process(CharSequence html) {
        return process(html, new ArrayList<XlsCssStyle>(0));
    }
    /**
     * process html to xls
     *
     * @param html html char sequence
     *
     * @return xls bytes
     */
    public static byte[] process(CharSequence html, List<XlsCssStyle> defaultStyleList) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            process(html, baos, defaultStyleList);
            return baos.toByteArray();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    log.warn("Close Byte Array Inpout Stream Error Caused.", e);
                }
            }
        }
    }

    /**
     * process html to output stream
     *
     * @param html   html char sequence
     * @param output output stream
     */
    public static void process(CharSequence html, OutputStream output) {
        process(html, output, null);
    }
    /**
     * process html to output stream
     *
     * @param html   html char sequence
     * @param output output stream
     */
    public static void process(CharSequence html, OutputStream output, List<XlsCssStyle> defaultStyleList) {
        new TableToXls().doProcessFromHtml(html instanceof String ? (String) html : html.toString(), output, defaultStyleList);
    }

    /**
     * process html to output stream
     *
     * @param url    html url
     * @param output output stream
     */
    public static void process(URL url, int timeoutMillis, OutputStream output) throws IOException {
        process(url, timeoutMillis, output, null);
    }
    /**
     * process html to output stream
     *
     * @param url    html url
     * @param output output stream
     */
    public static void process(URL url, int timeoutMillis, OutputStream output, List<XlsCssStyle> defaultStyleList) throws IOException {
        new TableToXls().doProcessFromUrl(url, timeoutMillis, output, defaultStyleList);
    }

    // private methods 转化 数据准备
    private void processTable(Element table) {
        processTable(table, null);
    }
    // private methods 转化 数据准备
    private void processTable(Element table, List<XlsCssStyle> defaultStyleList) {
        int rowIndex = 0;
        if (maxRow > 0) {
            // blank row
            maxRow += 2;
            rowIndex = maxRow;
        }
        log.info("Interate Table Rows.");
        for (Element row : table.select("tr")) {
            log.info("Parse Table Row [{}]. Row Index [{}].", row, rowIndex);
            int colIndex = 0;
            log.info("Interate Cols.");
            for (Element td : row.select("td, th")) {
                // skip occupied cell
                while (cellsOccupied.get(rowIndex + "_" + colIndex) != null) {
                    log.info("Cell [{}][{}] Has Been Occupied, Skip.", rowIndex, colIndex);
                    ++colIndex;
                }
                log.info("Parse Col [{}], Col Index [{}].", td, colIndex);
                // 用户定义默认样式填充
                fillDefaultStyle(td, defaultStyleList, rowIndex, colIndex);
                int rowSpan = 0;
                String strRowSpan = td.attr("rowspan");
                if (StringUtils.isNotBlank(strRowSpan) &&
                        StringUtils.isNumeric(strRowSpan)) {
                    log.info("Found Row Span [{}].", strRowSpan);
                    rowSpan = Integer.parseInt(strRowSpan);
                }
                int colSpan = 0;
                String strColSpan = td.attr("colspan");
                if (StringUtils.isNotBlank(strColSpan) &&
                        StringUtils.isNumeric(strColSpan)) {
                    log.info("Found Col Span [{}].", strColSpan);
                    colSpan = Integer.parseInt(strColSpan);
                }
                // col span & row span
                if (colSpan > 1 && rowSpan > 1) {
                    spanRowAndCol(td, rowIndex, colIndex, rowSpan, colSpan);
                    colIndex += colSpan;
                }
                // col span only
                else if (colSpan > 1) {
                    spanCol(td, rowIndex, colIndex, colSpan);
                    colIndex += colSpan;
                }
                // row span only
                else if (rowSpan > 1) {
                    spanRow(td, rowIndex, colIndex, rowSpan);
                    ++colIndex;
                }
                // no span
                else {
                    createCell(td, getOrCreateRow(rowIndex), colIndex).setCellValue(td.text());
                    ++colIndex;
                }
            }
            ++rowIndex;
        }
    }

    // 默认样式填充
    private void fillDefaultStyle(Element td, List<XlsCssStyle> defaultStyleList, int rowIndex, int colIndex) {
        if (CollectionUtils.isEmpty(defaultStyleList)) {
            return;
        }
        for (XlsCssStyle style : defaultStyleList) {
            if (StringUtils.isBlank(style.getStyle())) {
                continue;
            }
            boolean forOneCell = Objects.equals(style.getCol(), colIndex)
                    && Objects.equals(style.getRow(), rowIndex);
            boolean forAll = null == style.getCol() && null == style.getRow();
            boolean forRow = Objects.equals(style.getRow(), rowIndex) && null == style.getCol();
            boolean forCol = Objects.equals(style.getCol(), colIndex) && null == style.getRow();
            if (forOneCell || forRow || forCol || forAll) {
                String beforeStyle = td.attr(CssApplier.STYLE);
                if (StringUtils.isBlank(beforeStyle)) {
                    td.attr(CssApplier.STYLE, style.getStyle());
                } else {
                    if (style.isOverEffect()) {
                        String split = beforeStyle.endsWith(";") ? "" : ";";
                        td.attr(CssApplier.STYLE, beforeStyle + split + style.getStyle());
                    } else {
                        String split = style.getStyle().endsWith(";") ? "" : ";";
                        td.attr(CssApplier.STYLE, style.getStyle() + split + beforeStyle);
                    }
                }
            }
        }
    }

    private void doProcessFromHtml(String html, OutputStream output, List<XlsCssStyle> defaultStyleList) {
        Document doc = Jsoup.parseBodyFragment(html);
        doProcess(doc, output, defaultStyleList);
    }

    private void doProcessFromUrl(URL url, int timeoutMillis, OutputStream output, List<XlsCssStyle> defaultStyleList) throws IOException {
        Document doc = Jsoup.parse(url, timeoutMillis);
        doProcess(doc, output, defaultStyleList);
    }

    private void doProcess(Document doc, OutputStream output, List<XlsCssStyle> defaultStyleList) {
        try {
            for (Element table : doc.select("table")) {
                processTable(table, defaultStyleList);
            }
            workBook.write(output);
        } catch (IOException e) {
            throw new IllegalStateException("Table To XLS, IO ERROR.", e);
        }
    }

    private void spanRow(Element td, int rowIndex, int colIndex, int rowSpan) {
        log.info("Span Row , From Row [{}], Span [{}].", rowIndex, rowSpan);
        mergeRegion(rowIndex, rowIndex + rowSpan - 1, colIndex, colIndex);
        for (int i = 0; i < rowSpan; ++i) {
            HSSFRow row = getOrCreateRow(rowIndex + i);
            createCell(td, row, colIndex);
            cellsOccupied.put((rowIndex + i) + "_" + colIndex, true);
        }
        getOrCreateRow(rowIndex).getCell(colIndex).setCellValue(td.text());
    }

    private void spanCol(Element td, int rowIndex, int colIndex, int colSpan) {
        log.info("Span Col, From Col [{}], Span [{}].", colIndex, colSpan);
        mergeRegion(rowIndex, rowIndex, colIndex, colIndex + colSpan - 1);
        HSSFRow row = getOrCreateRow(rowIndex);
        for (int i = 0; i < colSpan; ++i) {
            createCell(td, row, colIndex + i);
        }
        row.getCell(colIndex).setCellValue(td.text());
    }

    private void spanRowAndCol(Element td, int rowIndex, int colIndex,
                               int rowSpan, int colSpan) {
        log.info("Span Row And Col, From Row [{}], Span [{}].", rowIndex, rowSpan);
        log.info("From Col [{}], Span [{}].", colIndex, colSpan);
        mergeRegion(rowIndex, rowIndex + rowSpan - 1, colIndex, colIndex + colSpan - 1);
        for (int i = 0; i < rowSpan; ++i) {
            HSSFRow row = getOrCreateRow(rowIndex + i);
            for (int j = 0; j < colSpan; ++j) {
                createCell(td, row, colIndex + j);
                cellsOccupied.put((rowIndex + i) + "_" + (colIndex + j), true);
            }
        }
        getOrCreateRow(rowIndex).getCell(colIndex).setCellValue(td.text());
    }

    private HSSFCell createCell(Element td, HSSFRow row, int colIndex) {
        HSSFCell cell = row.getCell(colIndex);
        if (cell == null) {
            log.debug("Create Cell [{}][{}].", row.getRowNum(), colIndex);
            cell = row.createCell(colIndex);
        }
        return applyStyle(td, cell);
    }

    private HSSFCell applyStyle(Element td, HSSFCell cell) {
        String style = td.attr(CssApplier.STYLE);
        HSSFCellStyle cellStyle = null;
        if (StringUtils.isNotBlank(style)) {
            if (cellStyles.size() < 4000) {
                Map<String, String> mapStyle = parseStyle(style.trim());
                Map<String, String> mapStyleParsed = new HashMap<String, String>();
                for (CssApplier applier : STYLE_APPLIERS) {
                    mapStyleParsed.putAll(applier.parse(mapStyle));
                }
                cellStyle = cellStyles.get(styleStr(mapStyleParsed));
                if (cellStyle == null) {
                    log.debug("No Cell Style Found In Cache, Parse New Style.");
                    cellStyle = workBook.createCellStyle();
                    cellStyle.cloneStyleFrom(defaultCellStyle);
                    for (CssApplier applier : STYLE_APPLIERS) {
                        applier.apply(cell, cellStyle, mapStyleParsed);
                    }
                    // cache style
                    cellStyles.put(styleStr(mapStyleParsed), cellStyle);
                }
            } else {
                log.info("Custom Cell Style Exceeds 4000, Could Not Create New Style, Use Default Style.");
                cellStyle = defaultCellStyle;
            }
        } else {
            log.debug("Use Default Cell Style.");
            cellStyle = defaultCellStyle;
        }
        cell.setCellStyle(cellStyle);
        return cell;
    }

    private String styleStr(Map<String, String> style) {
        log.debug("Build Style String, Style [{}].", style);
        StringBuilder sbStyle = new StringBuilder();
        Object[] keys = style.keySet().toArray();
        Arrays.sort(keys);
        for (Object key : keys) {
            sbStyle.append(key)
                    .append(':')
                    .append(style.get(key))
                    .append(';');
        }
        log.debug("Style String Result [{}].", sbStyle);
        return sbStyle.toString();
    }

    private Map<String, String> parseStyle(String style) {
        log.debug("Parse Style String [{}] To Map.", style);
        Map<String, String> mapStyle = new HashMap<String, String>();
        for (String s : style.split("\\s*;\\s*")) {
            if (StringUtils.isNotBlank(s)) {
                String[] ss = s.split("\\s*\\:\\s*");
                if (ss.length == 2 &&
                        StringUtils.isNotBlank(ss[0]) &&
                        StringUtils.isNotBlank(ss[1])) {
                    String attrName = ss[0].toLowerCase();
                    String attrValue = ss[1];
                    // do not change font name
                    if (!CssApplier.FONT.equals(attrName) &&
                            !CssApplier.FONT_FAMILY.equals(attrName)) {
                        attrValue = attrValue.toLowerCase();
                    }
                    mapStyle.put(attrName, attrValue);
                }
            }
        }
        log.debug("Style Map Result [{}].", mapStyle);
        return mapStyle;
    }

    private HSSFRow getOrCreateRow(int rowIndex) {
        HSSFRow row = sheet.getRow(rowIndex);
        if (row == null) {
            log.info("Create New Row [{}].", rowIndex);
            row = sheet.createRow(rowIndex);
            if (rowIndex > maxRow) {
                maxRow = rowIndex;
            }
        }
        return row;
    }

    private void mergeRegion(int firstRow, int lastRow, int firstCol, int lastCol) {
        log.debug("Merge Region, From Row [{}], To [{}].", firstRow, lastRow);
        log.debug("From Col [{}], To [{}].", firstCol, lastCol);
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }
}