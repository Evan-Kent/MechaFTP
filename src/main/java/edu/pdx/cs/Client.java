package edu.pdx.cs;
import org.apache.commons.net.ftp.*;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Client {

    private static Logger logger;
    public ClientState state;
    Path logpath;
    FTPClient ftp;

    Client(){
        logger =  LogManager.getLogger(Log4jExample.class);
        ftp = new FTPClient();
        state = new ClientState();
    }

    Client(Logger logger, Path logpath, FTPClient ftp){
        this.logger = logger;
        this.logpath = logpath;
        this.ftp = ftp;
        this.state = new ClientState();
    }

    public void connect(String server, int port)
    {
        connect(server, Integer.valueOf(port));
    }

    public void connect(String server, Integer port)
    {
        try
        {
            if (port != null)
                ftp.connect(server, port);
            else
                ftp.connect(server);
            // TODO: state.setRemoteCwd to the current working directory on server
            state.setRemoteCwd(Paths.get("/data/aang"));
        }
        catch (IOException e)
        {
            logger.error("Failed to connect to server " + server + " with error:\n" + e.getLocalizedMessage());
            state.setRemoteCwd(Path.of(""));
        }

    }

    /**
     * Logs users into the FTP server
     *
     * @param username the username
     * @param password the user's password
     * @return true if login is successful, false otherwise
     * @throws IOException
     */
    public boolean login (String username, String password) throws IOException {
        boolean status = ftp.login(username, password);
        if (status) {
            logger.info("Logged in as: ", username);
        } else {
            logger.error("Failed login for: ", username);
        }
        return status;
    }

    void setLogfile(Path logpath)
    {
        this.logpath = logpath;
    }

    public boolean uploadFile(Path toUpload) {
        System.out.println("toUpload: " + toUpload);

        boolean done = false;

        try {
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            String filepath1 = String.valueOf(toUpload);
            File firstLocalFile = new File(filepath1);

            String firstRemoteFile = String.valueOf(toUpload);
            InputStream inputStream = new FileInputStream(firstLocalFile);

            System.out.println("Start uploading first file - " + firstRemoteFile);
            done = ftp.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
            if (done) {
                System.out.println("The first file is uploaded successfully.");
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ftp.isConnected()) {
                    ftp.logout();
                    ftp.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return done;
    }

    public boolean downloadFile() {
        boolean success = false;

        try {
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            String filepath1 = "1MB.zip";
            String remoteFile1 = "//" + filepath1;
            File downloadFile1 = new File(filepath1);
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
            success = ftp.retrieveFile(remoteFile1, outputStream1);
            outputStream1.close();

            if (success) {
                System.out.println("File #1 has been downloaded successfully.");
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ftp.isConnected()) {
                    ftp.logout();
                    ftp.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return success;
    }

    private static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }

    public boolean createDirectory() throws IOException {
        boolean success = false;

        try {
            ftp.enterLocalPassiveMode();

            String dirToCreate = "/upload123";
            success = ftp.makeDirectory(dirToCreate);
            showServerReply(ftp);
            if (success) {
                System.out.println("Successfully created directory: " + dirToCreate);
            } else {
                System.out.println("Failed to create directory. See server's reply.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ftp.isConnected()) {
                    ftp.logout();
                    ftp.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return success;
    }
    /**
     * Lists directories w/in the current working directory on remote server
     * @return an array of <code>FTPFile</code> objects
     */
    protected FTPFile[] listRemoteDirectories() throws IOException {
        String path = ftp.printWorkingDirectory();
        return ftp.listDirectories(path);
    }

    /**
     * Lists files w/in the current directory on remote server
     * @return an array of <code>FTPFile</code> objects
     */
    protected FTPFile[] listRemoteFiles() throws IOException{
        String path = ftp.printWorkingDirectory();

        FTPFile[] files =  ftp.listFiles(path, new FTPFileFilter() {
            @Override
            public boolean accept(FTPFile ftpFile) {
                return !ftpFile.isDirectory();
            }
        });

        return files;
    }

    /**
     * Converts names of <code>FTPFile</code> objects to strings
     * @param files array of <code>FTPFiles</code> objects
     * @return <code>ArrayList</code> of names in <code>String</code> format
     */
    protected ArrayList<String> fileDirectoryListStrings(FTPFile[] files){
       ArrayList<String> names = null;

       for(FTPFile file:files)
           names.add(file.getName());

       return names;
    }

    /**
     * Returns <code>String</code> of the current working directory
     * @return current working directory
     * @throws IOException
     */
    protected String printWorkingDirectory() throws IOException{
        return ftp.printWorkingDirectory();
    }

    /**
     * Changes the working directory...
     * @param dir ...to the given directory relative to the current working directory
     * @return true if the path change was successful, false otherwise
     */
    protected boolean changeDirectory(String dir){
        boolean success = false;

        try {
            success = ftp.changeWorkingDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }

    public boolean logout(String username)throws IOException{
        logger.info("User " + username +" is logging out!");
        return ftp.logout();
        //ftp.disconnect();

    }

    public void run() {

    }


}
