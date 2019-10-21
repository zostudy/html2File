package com.ai.html2markdown.service;

import java.net.URL;

import org.springframework.stereotype.Service;

import com.ai.html2markdown.utils.HTML2Md;
import com.ai.util.BaseUtils;
import com.ai.util.FilesUtils;
import com.ai.util.PathUtils;

@Service
public class Html2MarkdownService {
	/**
	 * 解析生成markdown
	 *
	 * @param pageUrl
	 * @return
	 */
	public String excute(String pageUrl) throws Exception {
		String outputPath = new StringBuffer("/output/").append(BaseUtils.getDateStr("yyyyMMdd")).append("/markdown/")
				.append(BaseUtils.uuid2()).append(".md").toString();
		URL url = new URL(pageUrl);
		String markdownStr = HTML2Md.convert(url, 30000);
		String absoultOutputPath = PathUtils.getClassRootPath(outputPath);
		FilesUtils.checkFolderAndCreate(absoultOutputPath);
		FilesUtils.newFile(absoultOutputPath, markdownStr, "UTF-8");
		return outputPath;
	}
}
