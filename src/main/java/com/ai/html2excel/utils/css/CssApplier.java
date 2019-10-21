package com.ai.html2excel.utils.css;

import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;

public interface CssApplier {
	// constants
	String PATTERN_LENGTH = "\\d*\\.?\\d+\\s*(?:em|ex|cm|mm|q|in|pt|pc|px)?";
	String STYLE = "style";
	// direction
	String TOP = "top";
	String RIGHT = "right";
	String BOTTOM = "bottom";
	String LEFT = "left";
	String WIDTH = "width";
	String HEIGHT = "height";
	String COLOR = "color";
	String BORDER = "border";
	String CENTER = "center";
	String JUSTIFY = "justify";
	String MIDDLE = "middle";
	String FONT = "font";
	String FONT_STYLE = "font-style";
	String FONT_WEIGHT = "font-weight";
	String FONT_SIZE = "font-size";
	String FONT_FAMILY = "font-family";
	String ITALIC = "italic";
	String BOLD = "bold";
	String NORMAL = "normal";
	String TEXT_ALIGN = "text-align";
	String VETICAL_ALIGN = "vertical-align";
	String BACKGROUND = "background";
	String BACKGROUND_COLOR = "background-color";

	// methods
	/**
	 * parse css styles
	 * @param style style to parse
	 * @return parse result
	 */
	Map<String, String> parse(Map<String, String> style);

	/**
	 * apply styles
	 * @param cell cell to apply style
	 * @param cellStyle cell style
	 * @param style style
	 */
	void apply(HSSFCell cell, HSSFCellStyle cellStyle, Map<String, String> style);
}
