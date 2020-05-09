package com.snbc.otaapp;

import com.jcraft.jsch.SftpProgressMonitor;

/**
 * author: zhougaoxiong
 * date: 2020/5/8,14:41
 * projectName:Tempftp
 * packageName:com.snbc.tempftp
 */

/**
 * 进度监控器-JSch每次传输一个数据块，就会调用count方法来实现主动进度通知
 */
public class MyProgressMonitor implements SftpProgressMonitor {

    private long count = 0;     //当前接收的总字节数
    private long max = 0;       //最终文件大小
    private long percent = -1;  //进度

    /**
     * 当每次传输了一个数据块后，调用count方法，count方法的参数为这一次传输的数据块大小
     */
    @Override
    public boolean count(long count) {
        this.count += count;
        if (percent >= this.count * 100 / max) {
            return true;
        }
        percent = this.count * 100 / max;
        System.out.println("Completed " + this.count + "(" + percent + "%) out of " + max + ".");
        return true;
    }

    /**
     * 当传输结束时，调用end方法
     */
    @Override
    public void end() {
        System.out.println("Transferring done.");
    }

    /**
     * 当文件开始传输时，调用init方法
     */
    @Override
    public void init(int op, String src, String dest, long max) {
        if (op == SftpProgressMonitor.PUT) {
            System.out.println("Upload file begin.");
        } else {
            System.out.println("Download file begin.");
        }

        this.max = max;
        this.count = 0;
        this.percent = -1;
    }
}
