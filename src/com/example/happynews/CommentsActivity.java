package com.example.happynews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.happynews.httpservices.SyncHttp;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class CommentsActivity extends Activity {
	
	private int mcount = 0 ;
	public TextView mTextView;
	private List<HashMap<String, Object>> mCommsData;
	private String service_name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comments);
		

		service_name = getResources().getString(R.string.service_name);
		
		
		//获取新闻编号与标题
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		
		int nid = bundle.getInt("nid");
		String titleString = bundle.getString("title");
		if(titleString.length() > 12)
		{
			titleString = titleString.substring(0, 12);
			titleString += "…的评论:";
		}else {
			titleString += "的评论:";
		}
		System.out.println(titleString.length());
		mCommsData = new ArrayList<HashMap<String, Object>>();
		//获取笔记评论内容
		getComments(nid);
        
        mTextView = (TextView)findViewById(R.id.comments_titlebar_title);
        mTextView.setText(titleString);

		
//		List<HashMap<String, String>> comments = new ArrayList<HashMap<String, String>>();
//		for (int i = 0; i < 10; i++)
//		{
//			mcount++;
//			HashMap<String, String> hashMap = new HashMap<String, String>();
//			hashMap.put("commentator_from", "广州");
//			hashMap.put("comment_ptime", "2015-03-22 20:21:22");
//			hashMap.put("comment_content", "赞加"+mcount);
//			comments.add(hashMap);
//		}
        
		SimpleAdapter commentsAdapter = new SimpleAdapter(this, mCommsData, R.layout.comments_list_item, new String[]
		{ "commentator_from", "comment_ptime", "comment_content" }, new int[]
		{ R.id.commentator_from, R.id.comment_ptime, R.id.comment_content });

		ListView commentsList = (ListView) findViewById(R.id.comments_list);
		commentsList.setAdapter(commentsAdapter);
		
		//返回按钮
		Button commsTitlebarNews =(Button)findViewById(R.id.comments_titlebar_news);
		commsTitlebarNews.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		
		
		
	}
	
	/**
	 * 获取新闻回复内容
	 * @param nid 新闻编号
	 */
	private void getComments(int nid)
	{
//		String url = "http://10.0.2.2:8080/web/getComments";
		String url = "http://"+service_name+"/getComments";
		String params = "nid=" + nid + "&startnid=0&count=10";
		SyncHttp http = new SyncHttp();
		try
		{
			String retStr = http.httpGet(url, params);
			JSONObject jsonObject = new JSONObject(retStr);
			int retCode = jsonObject.getInt("ret");
			if (retCode == 0)
			{
				JSONObject dataObject = jsonObject.getJSONObject("data");
				// 获取返回数目
				int totalnum = dataObject.getInt("totalnum");
				if (totalnum > 0)
				{
					// 获取返回新闻集合
					JSONArray commsList = dataObject.getJSONArray("commentslist");
					for (int i = 0; i < commsList.length(); i++)
					{
						JSONObject commsObject = (JSONObject) commsList.opt(i);
						HashMap<String, Object> hashMap = new HashMap<String, Object>();
						hashMap.put("cid", commsObject.getInt("cid"));
						hashMap.put("commentator_from", commsObject.getString("region"));
						hashMap.put("comment_content", commsObject.getString("content"));
						hashMap.put("comment_ptime", commsObject.getString("ptime"));
						mCommsData.add(hashMap);
					}
				}
				else
				{
					Toast.makeText(CommentsActivity.this, R.string.no_comments, Toast.LENGTH_LONG).show();
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(CommentsActivity.this, R.string.get_comms_failure, Toast.LENGTH_LONG).show();
		}
	}
	
	

}
