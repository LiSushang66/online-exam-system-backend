package cn.org.alan.exam.utils;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class FileUtil {

    private static final String REMOTE_HOST = "192.168.49.130"; // 远程服务器地址
    private static final int REMOTE_PORT = 22; // 默认 SFTP 端口
    private static final String USERNAME = "root"; // 用户名
    private static final String PASSWORD = "123456Qq"; // 密码

    /**
     * 将数据写入远程服务器文件
     */
    public static void saveFile(String remoteFolderPath, String fileName, String content) {
        saveFile2(remoteFolderPath, fileName, content);
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            // 创建 SSH 会话
            session = jsch.getSession(USERNAME, REMOTE_HOST, REMOTE_PORT);
            session.setPassword(PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no"); // 跳过主机密钥检查
            session.connect();

            // 打开 SFTP 通道
            Channel channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;

            // 切换到目标目录，如果不存在则创建
            try {
                channelSftp.cd(remoteFolderPath);
            } catch (SftpException e) {
                channelSftp.mkdir(remoteFolderPath);
                channelSftp.cd(remoteFolderPath);
            }

            // 创建临时本地文件并写入内容
            File tempFile = File.createTempFile("temp", ".txt");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(content);
            }

            // 上传文件到远程服务器
            channelSftp.put(new FileInputStream(tempFile), fileName);

            System.out.println("文件上传成功！");
        } catch (Exception e) {
            System.out.println(remoteFolderPath);
            System.out.println("文件保存失败：" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    /**
     * 删除远程服务器上的文件或文件夹
     */
    public static void delete(String remotePath) {
        delete2(new File(remotePath));
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            // 创建 SSH 会话
            session = jsch.getSession(USERNAME, REMOTE_HOST, REMOTE_PORT);
            session.setPassword(PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no"); // 跳过主机密钥检查
            session.connect();

            // 打开 SFTP 通道
            Channel channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;

            // 检查路径是否存在
            try {
                channelSftp.stat(remotePath); // 如果路径不存在会抛出异常
            } catch (SftpException e) {
                System.out.println("路径不存在：" + remotePath);
                return;
            }

            // 删除文件或文件夹
            deleteRecursive(channelSftp, remotePath);

            System.out.println("文件/文件夹删除成功！");
        } catch (Exception e) {
            System.out.println("文件/文件夹删除失败：" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    /**
     * 递归删除远程服务器上的文件或文件夹
     */
    private static void deleteRecursive(ChannelSftp channelSftp, String path) throws SftpException {
        try {
            // 检查是否是目录
            if (isDirectory(channelSftp, path)) {
                Vector<ChannelSftp.LsEntry> entries = channelSftp.ls(path);
                for (ChannelSftp.LsEntry entry : entries) {
                    String name = entry.getFilename();
                    if (!name.equals(".") && !name.equals("..")) { // 忽略 "." 和 ".."
                        deleteRecursive(channelSftp, path + "/" + name);
                    }
                }
                channelSftp.rmdir(path); // 删除空目录
            } else {
                channelSftp.rm(path); // 删除文件
            }
        } catch (SftpException e) {
            System.out.println("删除失败：" + path);
            throw e;
        }
    }

    /**
     * 检查远程路径是否是目录
     */
    private static boolean isDirectory(ChannelSftp channelSftp, String path) throws SftpException {
        try {
            return channelSftp.stat(path).isDir();
        } catch (SftpException e) {
            throw e;
        }
    }


    /**
     * 将数据写入文件
     */
    public static File saveFile2(String folderPath, String fileName, String content) {
        File fileFolder = new File(folderPath);
        // 如果文件夹不存在则创建
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }
        // 创建文件
        File file = new File(fileFolder, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println("文件保存失败：" + e.getMessage());
            throw new RuntimeException();
        }
        return fileFolder;
    }


    /**
     * 删除文件/文件夹
     */
    public static void delete2(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    delete2(file);
                }
            }
        }
        folder.delete();
    }
}
