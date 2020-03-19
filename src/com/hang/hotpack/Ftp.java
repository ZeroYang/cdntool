package com.hang.hotpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Ftp {
    //打印log日志
    private static final Log logger = LogFactory.getLog(Ftp.class);
 
    private Session sshSession;
 
    private ChannelSftp channel;
 
    private static ThreadLocal<Ftp> sftpLocal = new ThreadLocal<Ftp>();
 
    private Ftp(String host, int port, String username, String password) throws Exception {
        JSch jsch = new JSch();
        jsch.getSession(username, host, port);
        //根据用户名，密码，端口号获取session
        sshSession = jsch.getSession(username, host, port);
        sshSession.setPassword(password);
        //修改服务器/etc/ssh/sshd_config 中 GSSAPIAuthentication的值yes为no，解决用户不能远程登录
        sshSession.setConfig("userauth.gssapi-with-mic", "no");
 
        //为session对象设置properties,第一次访问服务器时不用输入yes
        sshSession.setConfig("StrictHostKeyChecking", "no");
        sshSession.connect();
        //获取sftp通道
        channel = (ChannelSftp)sshSession.openChannel("sftp");
        channel.connect();
        logger.info("连接ftp成功!" + sshSession);
    }
 
    /**
     * 是否已连接
     *
     * @return
     */
    private boolean isConnected() {
        return null != channel && channel.isConnected();
    }
 
    /**
     * 获取本地线程存储的sftp客户端
     *
     * @return
     * @throws Exception
     */
    public static Ftp getSftpUtil(String host, int port, String username, String password) throws Exception {
        //获取本地线程
        Ftp sftpUtil = sftpLocal.get();
        if (null == sftpUtil || !sftpUtil.isConnected()) {
            //将新连接防止本地线程，实现并发处理
            sftpLocal.set(new Ftp(host, port, username, password));
        }
        return sftpLocal.get();
    }
 
    /**
     * 释放本地线程存储的sftp客户端
     */
    public static void release() {
        if (null != sftpLocal.get()) {
            sftpLocal.get().closeChannel();
            logger.info("关闭连接" + sftpLocal.get().sshSession);
            sftpLocal.set(null);
 
        }
    }

	public String exceCommond(String com) {
		// sshSession.openChannel("exec");
		String result="";
		ChannelExec openChannel = null;
		try {
			openChannel =(ChannelExec)sshSession.openChannel("exec");
            openChannel.setCommand(com);
            openChannel.connect();
            InputStream in = openChannel.getInputStream();  
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));  
            String buf = null;
            //buf = reader.readLine();
            while ((buf = reader.readLine()) != null && !buf.isEmpty()) {
                result = new String(buf.getBytes("gbk"),"UTF-8");
            }  
            
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
            if(openChannel!=null&&!openChannel.isClosed()){
                openChannel.disconnect();
            }
		}
		return result;
	}
    /**
     * 关闭通道
     *
     * @throws Exception
     */
    public void closeChannel() {
        if (null != channel) {
            try {
                channel.disconnect();
            } catch (Exception e) {
                logger.error("关闭SFTP通道发生异常:", e);
            }
        }
        if (null != sshSession) {
            try {
                sshSession.disconnect();
            } catch (Exception e) {
                logger.error("SFTP关闭 session异常:", e);
            }
        }
    }
 

    public  void upLoadFile(String sPath, String dPath) {
        try {
            //channel.connect(10000000);
            ChannelSftp sftp = (ChannelSftp) channel;
            try {
            	//上传
                sftp.cd(dPath);
 
            } catch (SftpException e) {
 
                sftp.mkdir(dPath);
                sftp.cd(dPath);
 
            }
            File file = new File(sPath);
            copyFile(sftp, file, sftp.pwd());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void copyFile(ChannelSftp sftp, File file, String pwd) {
 
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            try {
                try {
                    String fileName = file.getName();
                    sftp.cd(pwd);
                    System.out.println("正在创建目录:" + sftp.pwd() + "/" + fileName);
                    sftp.mkdir(fileName);
                    System.out.println("目录创建成功:" + sftp.pwd() + "/" + fileName);
                } catch (Exception e) {
                }
                pwd = pwd + "/" + file.getName();
                try {
 
                    sftp.cd(file.getName());
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < list.length; i++) {
                copyFile(sftp, list[i], pwd);
            }
        } else {
 
            try {
                sftp.cd(pwd);
 
            } catch (SftpException e1) {
                e1.printStackTrace();
            }
            System.out.println("正在上传文件:" + file.getAbsolutePath());
            InputStream instream = null;
            OutputStream outstream = null;
            try {
                outstream = sftp.put(file.getName());
                instream = new FileInputStream(file);
 
                byte b[] = new byte[1024];
                int n;
                try {
                    while ((n = instream.read(b)) != -1) {
                        outstream.write(b, 0, n);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
 
            } catch (SftpException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    outstream.flush();
                    outstream.close();
                    instream.close();
 
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
}
