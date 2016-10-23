/**
 * 
 */
package com.onmobile.apps.ringbacktones.common;

/**
 * @author Mohsin
 *
 */
public class RBTEncryption {

	private static final String m_strUpperCase = "QNFWKLTCRSUEVZAXYOPIBJMHGD";
	private static final String m_strLowerCase = "dqfgxzrnvwyemaupbijcslkoth";
	private static final String m_strNumbers = "7925813640";
	
	
	public static String decrypt(String input)
	{
		String output = "";
		if(input != null)
		{
			for(int i=0; i<input.length(); i++)
			{
				char c = input.charAt(i);
				if(Character.isLowerCase(c))
					output += (char)(m_strLowerCase.indexOf(c) + 97);
				else if(Character.isUpperCase(c))
					output += (char)(m_strUpperCase.indexOf(c) + 65);
				else if(Character.isDigit(c))
					output += (char)(m_strNumbers.indexOf(c) + 48);
				else
					output += c;
				
			}
		}
		return output;
	}
	
	public static String encrypt(String input)
	{
		String output = "";
		if(input != null)
		{
			for(int i=0; i<input.length(); i++)
			{
				char c = input.charAt(i);
				int n = c;
				if(Character.isLowerCase(c))
					output += m_strLowerCase.charAt(n-97);
				else if(Character.isUpperCase(c))
					output += m_strUpperCase.charAt(n-65);
				else if(Character.isDigit(c))
					output += m_strNumbers.charAt(n-48);
				else
					output += c;
				
			}
		}
		return output;
	}

	public static void main(String[] args)
	{
		RBTEncryption encrypt = new RBTEncryption();
		encrypt.decrypt("munjva");
	}
	
}
