package com.example.happynews.tools;

/**
 *@author coolszy
 *@date 2012-3-26
 *@blog http://blog.92coding.com
 */
public class StringUtil
{
	/**
	 * string转化为int类型
	 * @param str 
	 * @return
	 */
	public static int String2Int(String str)
	{
		try
		{
			int value = Integer.valueOf(str);
			return value;
		} catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}
}
