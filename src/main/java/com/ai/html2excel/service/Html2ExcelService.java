package com.ai.html2excel.service;

import java.io.FileOutputStream;
import java.net.URL;

import org.springframework.stereotype.Service;

import com.ai.html2excel.utils.TableToXls;
import com.ai.util.BaseUtils;
import com.ai.util.FilesUtils;
import com.ai.util.PathUtils;

@Service
public class Html2ExcelService {
	/**
	 * 解析生成markdown
	 *
	 * @param pageUrl
	 * @return
	 */
	public String excute(String pageUrl) throws Exception {
		String outputPath = new StringBuffer("/output/").append(BaseUtils.getDateStr("yyyyMMdd")).append("/excel/")
				.append(BaseUtils.uuid2()).append(".xls").toString();
		URL url = new URL(pageUrl);
		String absoultOutputPath = PathUtils.getClassRootPath(outputPath);
		FilesUtils.checkFolderAndCreate(absoultOutputPath);
		FileOutputStream fout = new FileOutputStream(absoultOutputPath);
		TableToXls.process(url, 30000, fout);
		fout.close();
		return outputPath;
	}
}