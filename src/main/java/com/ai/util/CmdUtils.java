package com.ai.util;

import com.ai.util.thread.StreamGobbler;

public class CmdUtils {
	
	/**
     * 执行cmd命令
     *
     * @param cmdStr 命令字符串
     * @return 成功失败
     */
    public static boolean excute(String cmdStr) {
        // 利用Runtime输出流读取
        Runtime rt = Runtime.getRuntime();
        try {
            System.out.println("Command：" + cmdStr);
            Process p = rt.exec(cmdStr);
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),
                    "ERROR");
            // 开启屏幕标准错误流
            errorGobbler.start();
            StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(),
                    "STDOUT");
            // 开启屏幕标准输出流
            outGobbler.start();
            int w = p.waitFor();
            int v = p.exitValue();
            if (w == 0 && v == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
