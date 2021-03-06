package com.imudges.web.CloudAddressBook.Util;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	public static String getMd5(String plainText) {
		StringBuffer buf = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;
			buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			// System.out.println("result: " + buf.toString());//32位的加密

			// System.out.println("result: " +
			// buf.toString().substring(8,24));//16位的加密

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buf.toString();
	}

	private static final String salt = "hupeng@imudges.com";
	/**
	 * 此方法用于密码的计算
	 * @param ts 加密前的明文
	 * @return 加密后的密文
	 * 采用了 两次MD5散列，在其中还加了盐
	 * 具体的计算方式见方法的内容
	 * */
	public static String encryptTimeStamp(String ts){
		return getMd5(getMd5(ts)+"&"+salt);
	}
}
