package com.onmobile.apps.ringbacktones.Gatherer;
//RBT-12820 Protocol change to upload the recharge response file
public class SFTPConfig {
	private String _host = "";
	private int _waitperiod = 1800000;
	private int _port = 22;
	private String _user = "";
	private String _pwd = "";
	private int _retries = 3;
	private String _dir = "";
	private int _timeout = 7200000;

	public SFTPConfig(String server, int port, String user, String pwd,
			String dir, int wait, int retry, int timeout) {
		if (server != null)
			_host = server;
		if (user != null)
			_user = user;
		if (pwd != null)
			_pwd = pwd;
		if (dir != null)
			_dir = dir;
		if (port != 0)
			_port = port;
		_waitperiod = wait;
		_retries = retry;
		_timeout = timeout;
	}

	public String get_dir() {
		return _dir;
	}

	public void set_dir(String dir) {
		_dir = dir;
	}

	public String get_host() {
		return _host;
	}

	public int get_port() {
		return _port;
	}

	public String get_pwd() {
		return _pwd;
	}

	public int get_retries() {
		return _retries;
	}

	public int get_timeout() {
		return _timeout;
	}

	public String get_user() {
		return _user;
	}

	public int get_waitperiod() {
		return _waitperiod;
	}

	public String toString() {
		return super.toString() + " values: host=" + _host + ", port=" + _port
				+ ", user=" + _user + ", pwd=" + _pwd + ", dir=" + _dir
				+ ", timeout=" + _timeout + ", wait=" + _waitperiod
				+ ", reties=" + _retries;
	}
}
