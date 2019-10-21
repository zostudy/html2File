package com.ai.html2html.service;

import org.springframework.stereotype.Service;

import com.ai.util.BaseUtils;
import com.ai.util.FilesUtils;
import com.ai.util.PathUtils;

@Service
public class Html2HtmlService {
	/**
	 * 转储HTML字符串为HTML页面
	 *
	 * @param pageHtmlContent
	 * @return
	 */
	public String excute(String pageHtmlContent) throws Exception {
		String outputPath = new StringBuffer("/output/").append(BaseUtils.getDateStr("yyyyMMdd")).append("/html/")
				.append(BaseUtils.uuid2()).append(".html").toString();
		String absoultOutputPath = PathUtils.getClassRootPath(outputPath);
		FilesUtils.checkFolderAndCreate(absoultOutputPath);
		FilesUtils.newFile(absoultOutputPath, pageHtmlContent, "UTF-8");
		return outputPath;
	}
}
