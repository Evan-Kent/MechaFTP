package edu.pdx.cs;

import org.apache.commons.net.ftp.*;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.nio.file.Paths;

import org.apache.commons.net.io.Util;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Client {

    private static Logger logger;
    public ClientState state;
    Path logpath;
    FTPClient ftp;

    Client(){
        logger =  LogManager.getLogger(Client.class);
        ftp = new FTPClient();
        state = new ClientState();
    }

    Client(Logger logger, Path logpath, FTPClient ftp, ClientState state){
        this.logger = logger;
        this.logpath = logpath;
        this.ftp = ftp;
        this.state = state;
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

    /**
     * This function retrieves a file from the server.
     * @param file file name in the remote server
     * @return
     * @throws IOException
     */
    public boolean retrieveFile(String file)throws IOException{
        FileOutputStream output = new FileOutputStream(file);

        if(!ftp.retrieveFile(file, output))
        {
            logger.error("Can't download file!");
            return false;
        }

        output.close();
        logger.info("File" + file + " retrieved from the server!");
        return true;
    }

    /**
     * This function uploads a files to the server
     * @param file
     * @return
     * @throws IOException
     */
    public boolean uploadFile(File file) throws IOException {

        if(!file.exists()){
            logger.error("Passed File not created on local machine. It can't be upload to sever");
            return false;
        }

        FileInputStream input = new FileInputStream(file);

        if((!ftp.storeFile(file.getName(), input))){
            logger.error("File  " + file.getName() + " can't upload ");
            return false;
        }

        input.close();
        logger.info("File " + file.getName() + " uploaded ");
        return  true;
    }


    /**
     * Logs the username off and outputs logging out message
     * @param username
     * @return
     * @throws IOException
     */
    public boolean logout(String username)throws IOException{
        logger.info("User " + username +" is logging out!");
        return ftp.logout();

    }


}
