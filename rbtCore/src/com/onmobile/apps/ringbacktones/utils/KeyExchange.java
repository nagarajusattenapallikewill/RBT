package com.onmobile.apps.ringbacktones.utils;

import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

public class KeyExchange implements HostKeyVerification {

	public boolean verifyHost(String arg0, SshPublicKey arg1)
			throws TransportProtocolException {
		return true;
	}

}
