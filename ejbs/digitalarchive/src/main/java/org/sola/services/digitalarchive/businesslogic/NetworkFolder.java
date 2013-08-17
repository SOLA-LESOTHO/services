/**
 * ******************************************************************************************
 * Copyright (c) 2013 Food and Agriculture Organization of the United Nations
 * (FAO) and the Lesotho Land Administration Authority (LAA). All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,this
 * list of conditions and the following disclaimer. 2. Redistributions in binary
 * form must reproduce the above copyright notice,this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. 3. Neither the names of FAO, the LAA nor the names of
 * its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.services.digitalarchive.businesslogic;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import org.sola.common.FileUtility;
import org.sola.common.SOLAException;
import org.sola.common.messaging.ServiceMessage;
import org.sola.services.common.logging.LogUtility;
import org.sola.services.digitalarchive.repository.entities.FileInfo;

/**
 * Provides a facade to the File and the SmbFile classes. Used so that SOLA can
 * manage a network scan folder on the local computer as well as authenticate to
 * a file share on a different computer. This class also ensures that the file
 * handle is only held for short periods so that other processes can read and
 * write to the folder or folder location.
 *
 * @author soladev
 */
public class NetworkFolder {

    private final static String SAMBA_PREFIX = "smb://";
    private String folder;
    private NtlmPasswordAuthentication networkAuth;
    boolean isNetworkFolder = false;

    /**
     * Use this constructor to represent a file on the local file system
     *
     * @param folderLocation
     */
    public NetworkFolder(String folderLocation) {
        if (folderLocation != null && !folderLocation.endsWith(File.separator)) {
            folderLocation = folderLocation + File.separator;
        }
        folder = folderLocation;
        LogUtility.log("Network Folder Location = " + folder);
    }

    /**
     * Use this constructor to present a file on a network file share. You
     * should use limited privilege user accounts to connect to the file share.
     * AVOID USING DOMAIN OR COMPUTER ADMINISTRATOR ACCOUNTS.
     *
     * @param folderLocation The network file share. Should be <Server>/<share>
     * @param domain The domain or computer name for the user account.
     * @param user The user account to connect to the file share with
     * @param pword The password to use to connect to the file share.
     */
    public NetworkFolder(String folderLocation, String domain, String user, String pword) {
        if (folderLocation != null && !folderLocation.startsWith(SAMBA_PREFIX)) {
            folderLocation = SAMBA_PREFIX + folderLocation;
        }
        if (folderLocation != null && !folderLocation.endsWith(File.separator)) {
            folderLocation = folderLocation + File.separator;
        }
        folder = folderLocation;
        LogUtility.log("Network Folder Location = " + folder);
        networkAuth = new NtlmPasswordAuthentication(domain, user, pword);
    }

    /**
     * Returns the path for the Network folder.
     */
    public String getPath() {
        return folder;
    }

    /**
     * Returns a network folder representing the specified subfolder. Also
     * creates the subfolder if it doesn't exist.
     *
     * @param subFolderName
     * @return
     */
    public NetworkFolder getSubFolder(String subFolderName) {
        NetworkFolder result;
        if (isNetworkFolder) {
            result = new NetworkFolder(getPath() + subFolderName,
                    networkAuth.getDomain(), networkAuth.getUsername(),
                    networkAuth.getPassword());
        } else {
            result = new NetworkFolder(getPath() + subFolderName);
        }
        result.createFolder();
        return result;
    }

    /**
     * Checks if the network folder exists at the specified location.
     *
     * @return
     */
    public boolean exists() {
        boolean result = false;
        if (isNetworkFolder) {
            try {
                SmbFile file = new SmbFile(folder, networkAuth);
                result = file.exists();
            } catch (Exception ex) {
                throw new SOLAException(ServiceMessage.EXCEPTION_NETWORK_SCAN_FOLDER, ex);
            }
        } else {
            File file = new File(folder);
            result = file.exists();
        }
        return result;
    }

    /**
     * Checks if the file exists within the Network File location
     *
     * @param fileName The file path and file name of the file to test within
     * the Network File location
     * @return
     */
    public boolean fileExists(String fileName) {
        boolean result = false;
        fileName = fileName.replaceAll(File.pathSeparator, "\\\\");
        if (isNetworkFolder) {
            try {
                SmbFile file = new SmbFile(folder + fileName, networkAuth);
                result = file.exists();
            } catch (Exception ex) {
                throw new SOLAException(ServiceMessage.EXCEPTION_NETWORK_SCAN_FOLDER, ex);
            }
        } else {
            File file = new File(folder + fileName);
            result = file.exists();
        }
        return result;
    }

    /**
     * Deletes a file within the Network Folder
     *
     * @param fileName Name of the file to delete
     */
    public void deleteFile(String fileName) {
        fileName = fileName.replaceAll(File.pathSeparator, "\\\\");
        if (fileExists(fileName)) {
            if (isNetworkFolder) {
                try {
                    SmbFile file = new SmbFile(folder + fileName, networkAuth);
                    file.delete();
                } catch (Exception ex) {
                    throw new SOLAException(ServiceMessage.EXCEPTION_NETWORK_SCAN_FOLDER, ex);
                }
            } else {
                File file = new File(folder + fileName);
                file.delete();
            }
        }
    }

    /**
     * Attempts to create the Network Folder if it doesn't exist.
     */
    public void createFolder() {
        if (!exists()) {
            if (isNetworkFolder) {
                try {
                    SmbFile file = new SmbFile(folder, networkAuth);
                    file.mkdirs();
                } catch (Exception ex) {
                    throw new SOLAException(ServiceMessage.EXCEPTION_NETWORK_SCAN_FOLDER, ex);
                }
            } else {
                File file = new File(folder);
                file.mkdirs();
            }
        }
    }

    /**
     * Moves a file from the NetworkFolder location to a local file location. If
     * the destination file already exists, the file will not be moved.
     *
     * @param fileName The name of the file to move
     * @param destination The local file location
     * @return
     */
    public boolean copyFileToLocal(String fileName, File destination) {
        boolean result = false;
        fileName = fileName.replaceAll(File.pathSeparator, "\\\\");
        if (fileExists(fileName) && !destination.exists()) {
            result = true;
            if (isNetworkFolder) {
                try {
                    SmbFile file = new SmbFile(folder + fileName, networkAuth);
                    FileUtility.writeFile(file.getInputStream(), destination);
                } catch (Exception ex) {
                    throw new SOLAException(ServiceMessage.EXCEPTION_NETWORK_SCAN_FOLDER, ex);
                }
            } else {
                try {
                    File file = new File(folder + fileName);
                    FileUtility.writeFile(new FileInputStream(file), destination);
                } catch (Exception ex) {
                    throw new SOLAException(ServiceMessage.EXCEPTION_NETWORK_SCAN_FOLDER, ex);
                }
            }
        }
        return result;
    }

    /**
     * Returns information on all files in the Network Folder as well as the
     * subfolders of the Network Folder. Can be restricted to only match
     * specific file types by providing a regex filter expression that is
     * resolved against the file name.
     *
     * @return
     */
    public List<FileInfo> getAllFiles(String fileNameFilter) {
        fileNameFilter = fileNameFilter == null ? "*" : fileNameFilter;
        List<FileInfo> result = new ArrayList<FileInfo>();
        if (isNetworkFolder) {
            try {
                SmbFile file = new SmbFile(folder, networkAuth);
                for (SmbFile f : file.listFiles()) {
                    if (f.isFile() && f.getName().toLowerCase().matches(fileNameFilter)) {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.setModificationDate(new Date(f.lastModified()));
                        fileInfo.setFileSize(f.length());
                        fileInfo.setName(f.getName());
                        result.add(fileInfo);
                    }
                    if (f.isDirectory()) {
                        // Show the files in the subdirectories as well and use a
                        // pathSeparator (;) in the file name to delimit the subdirectories. 
                        NetworkFolder subFolder = getSubFolder(f.getName());
                        List<FileInfo> temp = subFolder.getAllFiles(fileNameFilter);
                        for (FileInfo fi : temp) {
                            fi.setName(f.getName() + File.pathSeparator + fi.getName());
                        }
                        result.addAll(temp);
                    }
                }
            } catch (Exception ex) {
                throw new SOLAException(ServiceMessage.EXCEPTION_NETWORK_SCAN_FOLDER, ex);
            }
        } else {
            File file = new File(folder);
            for (File f : file.listFiles()) {
                if (f.isFile() && f.getName().toLowerCase().matches(fileNameFilter)) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setModificationDate(new Date(f.lastModified()));
                    fileInfo.setFileSize(f.length());
                    fileInfo.setName(f.getName());
                    result.add(fileInfo);
                }
                if (f.isDirectory()) {
                    // Show the files in the subdirectories as well and use a
                    // pathSeparator (;) in the file name to delimit the subdirectories. 
                    NetworkFolder subFolder = getSubFolder(f.getName());
                    List<FileInfo> temp = subFolder.getAllFiles(fileNameFilter);
                    for (FileInfo fi : temp) {
                        fi.setName(f.getName() + File.pathSeparator + fi.getName());
                    }
                    result.addAll(temp);
                }
            }
        }
        return result;
    }
}
