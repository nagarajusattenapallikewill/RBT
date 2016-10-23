package com.onmobile.apps.ringbacktones.Gatherer;
//RBT-12820 Protocol change to upload the recharge response file
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;

public class SFTPHandler {
	private static Logger logger = Logger.getLogger(SFTPHandler.class);

	private SFTPConfig m_sftpconfig;

	public SFTPHandler(SFTPConfig config) {
		m_sftpconfig = config;
	}

	public void uploadFileBySFTP(String path) {
		File _file = null;
		if (null != path && !path.isEmpty()) {
			_file = new File(path);
		}
		if (_file == null || !_file.exists())
			return;

		String sftpServerIP = m_sftpconfig.get_host();

		int sftpServerPort = m_sftpconfig.get_port();
		String sftpServerUserName = m_sftpconfig.get_user();
		String sftpServerPassword = m_sftpconfig.get_pwd();
		String sftpUploadDir = m_sftpconfig.get_dir();

		int sftpTimeout = m_sftpconfig.get_timeout();// 7200000;
		boolean sftp_success = true;
		int sftp_retries = 0;
		SftpClient sftp = null;
		while (sftp_retries < m_sftpconfig.get_retries()
				&& sftp_success) {
			try {
				ConfigurationLoader.initialize(false);
				SshClient ssh = new SshClient();
				// Connect to the host
				SshConnectionProperties prop = new SshConnectionProperties();
				prop.setHost(sftpServerIP);
				prop.setPort(sftpServerPort);
				prop.setUsername(sftpServerUserName);

				ssh.setSocketTimeout(sftpTimeout);
				logger.info("Connecting to the SFTP server..");
				ssh.connect(prop);
				// Create a password authentication instance
				logger.info("Connected to the SFTP server..");
				PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
				pwd.setUsername(sftpServerUserName);
				// Get the password
				pwd.setPassword(sftpServerPassword);
				// Try the authentication
				logger.info("Authenticating password to the SFTP server..");
				int result = ssh.authenticate(pwd);
				// Evaluate the result
				if (result == AuthenticationProtocolState.COMPLETE) {
					logger.info(" password Authenticated:");
					// The connection is authenticated we can now do some real
					// work!
					logger.info("Opening SFTPClient");
					sftp = ssh.openSftpClient();
					logger.info("Opened SFTPClient");
					try {
						logger.info("Going to dest dir " + sftpUploadDir
								+ " in SFTP");
						sftp.cd(sftpUploadDir);
						logger.info("Entered into dest dir" + sftpUploadDir
								+ " in SFTP");
						sftp.put(_file.getPath());
					} catch (IOException ioe) {
						logger.info("Either the folder does not exist or its not a directory ");
						logger.info("Could not get into the remote folder due to "
								+ ioe.getMessage() + "\n");
					}
					logger.info("Existing sftp dir==" + sftp.pwd().toString());
					sftp_success = false;
					sftp.quit();
				}

			} catch (IOException ioe) {
				logger.info("Could not upload the file into the remote folder due to "
						+ ioe.getMessage() + "\n");
				sftp_success = true;
				logger.info("Sleeping for " + m_sftpconfig.get_waitperiod()
						+ " milliseconds!");
				try {
					Thread.sleep(m_sftpconfig.get_waitperiod());
					sftp.quit();
				} catch (Exception e) {
				}
			}
			sftp_retries++;
		} // end while

	}
}
