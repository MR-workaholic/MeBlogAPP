package com.example.happynews.custom;

import java.util.List;
import java.util.Map;

import com.example.happynews.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;



/**
 *@author coolszy
 *@date 2012-2-27
 *@blog http://blog.92coding.com
 */

public class CustomSimpleAdapter extends SimpleAdapter
{

	public CustomSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to)
	{
		super(context, data, resource, from, to);//super是指用父类的方法实现
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View v = super.getView(position, convertView, parent);
		//更新第一个TextView的背�?
		if (position==0)
		{
			TextView categoryTitle = (TextView)v;
			categoryTitle.setBackgroundResource(R.drawable.categorybar_item_background);
			categoryTitle.setTextColor(0XFFFFFFFF);
		}
		return v;
	}
}
