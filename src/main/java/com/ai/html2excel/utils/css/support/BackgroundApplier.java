package com.ai.html2excel.utils.css.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.CellStyle;

import com.ai.html2excel.utils.css.CssApplier;
import com.ai.html2excel.utils.css.CssUtils;

public class BackgroundApplier implements CssApplier {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> parse(Map<String, String> style) {
		Map<String, String> mapRtn = new HashMap<String, String>();
		String bg = style.get(BACKGROUND);
		String bgColor = null;
		if (StringUtils.isNotBlank(bg)) {
			for (String bgAttr : bg.split("(?<=\\)|\\w|%)\\s+(?=\\w)")) {
				if ((bgColor = CssUtils.processColor(bgAttr)) != null) {
					mapRtn.put(BACKGROUND_COLOR, bgColor);
					break;
				}
			}
		}
		bg = style.get(BACKGROUND_COLOR);
		if (StringUtils.isNotBlank(bg) && (bgColor = CssUtils.processColor(bg)) != null) {
			mapRtn.put(BACKGROUND_COLOR, bgColor);

		}
		if (bgColor != null) {
			bgColor = mapRtn.get(BACKGROUND_COLOR);
			if ("#ffffff".equals(bgColor)) {
				mapRtn.remove(BACKGROUND_COLOR);
			}
		}
		return mapRtn;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void apply(HSSFCell cell, HSSFCellStyle cellStyle, Map<String, String> style) {
		String bgColor = style.get(BACKGROUND_COLOR);
		if (StringUtils.isNotBlank(bgColor)) {
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellStyle.setFillForegroundColor(CssUtils.parseColor(cell.getSheet().getWorkbook(), bgColor).getIndex());
		}
	}
}
