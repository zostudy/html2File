package com.ai.html2excel.utils.css.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;

import com.ai.html2excel.utils.css.CssApplier;
import com.ai.html2excel.utils.css.CssUtils;

public class HeightApplier implements CssApplier {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> parse(Map<String, String> style) {
		Map<String, String> mapRtn = new HashMap<String, String>();
		String height = style.get(HEIGHT);
		if (CssUtils.isNum(height)) {
			mapRtn.put(HEIGHT, height);
		}
		return mapRtn;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void apply(HSSFCell cell, HSSFCellStyle cellStyle, Map<String, String> style) {
		int height = Math.round(CssUtils.getInt(style.get(HEIGHT)) * 255 / 12.75F);
		HSSFRow row = cell.getRow();
		if (height > row.getHeight()) {
			row.setHeight((short) height);
		}
	}
}
