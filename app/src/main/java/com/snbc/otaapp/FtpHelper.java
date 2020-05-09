package com.snbc.otaapp;

import android.text.TextUtils;
import android.util.Log;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * author: zhougaoxiong
 * date: 2020/5/8,11:38
 * projectName:Tempftp
 * packageName:com.snbc.tempftp
 */
public class FtpHelper {

    private static final String TAG = FtpHelper.class.getName();

    private String mHost;
    private String mPort;
    private String mUserName;
    private String mPwd;
    private String mSrc;
    private String mDest;
    //default 160000
    private int mSessionTimeout;
    //default 5000
    private int mSessionConnectTimeout;
    //listener
    private SftpProgressMonitor mSftpProgressMonitor;

    public FtpHelper(String host, String port, String userName, String pwd, String src, String dest,
                     int session_timeout, int session_connect_timeout, SftpProgressMonitor sftpProgressMonitor) {
        mHost = host;
        mPort = port;
        mUserName = userName;
        mPwd = pwd;
        mSrc = src;
        mDest = dest;
        mSessionTimeout = session_timeout;
        mSessionConnectTimeout = session_connect_timeout;
        mSftpProgressMonitor = sftpProgressMonitor;
    }

    Session session = null;
    com.jcraft.jsch.Channel channel = null;

    private static Map<String, String> mConnectConfigHashMap = new HashMap<String, String>();
    public final String SFTP_REQ_HOST = "host";
    public final String SFTP_REQ_PORT = "port";
    public final String SFTP_REQ_USERNAME = "username";
    public final String SFTP_REQ_PASSWORD = "password";
    public final int SFTP_DEFAULT_PORT = 22;

    protected String privateKey;// 密钥文件路径
    protected String passphrase;// 密钥口令

    public void download() {
        // set sftp connect ip.port.username.pwd
        mConnectConfigHashMap.put(SFTP_REQ_HOST, mHost);
        mConnectConfigHashMap.put(SFTP_REQ_USERNAME, mUserName);
        mConnectConfigHashMap.put(SFTP_REQ_PASSWORD, mPwd);
        mConnectConfigHashMap.put(SFTP_REQ_PORT, mPort);

        try {
            downloadFile(mSrc, mDest, mConnectConfigHashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(String src, String dst, Map<String, String> sftpDetails) {

        ChannelSftp chSftp = getChannel(sftpDetails, mSessionTimeout);

        // Retrieves the file attributes of a file or directory
        //SftpATTRS attr = chSftp.stat(src);
        //long fileSize = attr.getSize();
        //代码段1/代码段2/代码段3:分别演示了如何使用JSch的各种put方法来进行文件下载
        try {
            // 代码段1:使用这个方法时，dst可以是目录，若dst为目录，则下载到本地的文件名将与src文件名相同
            //断点续传
            chSftp.get(src, dst, mSftpProgressMonitor, ChannelSftp.RESUME);
            /***
             OutputStream out = new FileOutputStream(dst);
             // 代码段2:将目标服务器上文件名为src的文件下载到本地的一个输出流对象，该输出流为一个文件输出流
             chSftp.get(src, out, new MyProgressMonitor());

             // 代码段3:采用读取get方法返回的输入流数据的方式来下载文件
             InputStream is = chSftp.get(src, new MyProgressMonitor(),ChannelSftp.RESUME);
             byte[] buff = new byte[1024 * 2];
             int read;
             if (is != null) {
             do {
             read = is.read(buff, 0, buff.length);
             if (read > 0) {
             out.write(buff, 0, read);
             }
             out.flush();
             } while (read >= 0);
             }
             **/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            chSftp.quit();
            closeChannel();
        }
    }

    public void closeChannel() {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    /**
     * 根据ip，用户名及密码得到一个SFTP, channel对象，即ChannelSftp的实例对象，在应用程序中就可以使用该对象来调用SFTP的各种操作方法
     */
    public ChannelSftp getChannel(Map<String, String> sftpDetails, int timeout) {

        String ftpHost = sftpDetails.get(SFTP_REQ_HOST);
        String port = sftpDetails.get(SFTP_REQ_PORT);
        String ftpUserName = sftpDetails.get(SFTP_REQ_USERNAME);
        String ftpPassword = sftpDetails.get(SFTP_REQ_PASSWORD);

        int ftpPort = SFTP_DEFAULT_PORT;

        if (port != null && !port.equals("")) {
            ftpPort = Integer.valueOf(port);
        }

        JSch jsch = new JSch(); // 创建JSch对象

        try {
            if (!TextUtils.isEmpty(privateKey)) {
                // 使用密钥验证方式，密钥可以使有口令的密钥，也可以是没有口令的密钥
                if (!TextUtils.isEmpty(passphrase)) {
                    jsch.addIdentity(privateKey, passphrase);
                } else {
                    jsch.addIdentity(privateKey);//设置私钥
                }
            }

            // 根据用户名，主机ip，端口获取一个Session对象
            session = jsch.getSession(ftpUserName, ftpHost, ftpPort);
            if (ftpPassword != null) {
                // 设置密码
                session.setPassword(ftpPassword);
            }

            Properties config = new Properties();
            // do not verify host
            config.put("StrictHostKeyChecking", "no");

            // 为Session对象设置properties
            session.setConfig(config);
            // 设置timeout时间
            session.setTimeout(timeout);
            // 通过Session建立链接
            session.connect(mSessionConnectTimeout);
            // 打开SFTP通道
            channel = session.openChannel("sftp");
            // 建立SFTP通道的连接
            channel.connect();
        } catch (JSchException e) {
            e.printStackTrace();
            Log.i(TAG, e.toString());
        }

        return (ChannelSftp) channel;
    }

}

