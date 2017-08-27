package com.example.happynews;





import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.example.happynews.httpservices.SyncHttp;
import com.example.happynews.model.Parameter;

import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class NewDetailsActivity extends Activity {
	
	private ViewFlipper mNewsBodyFlipper;
	private LayoutInflater mNewsBodyInflater;
	private int mCount=0;
	private int cur=0;
	private int max=0;
	private float mStartX;
	private String service_name;
	
	private Button newsdetailsTitlebarComm;
	private Button newsDetailsTitlebarPre;
	private Button newsDetailsTitlebarNext;
	
//	String[] titlesarr = new String[10];
	
	private ArrayList<HashMap<String, Object>> mNewsData; //�ʼǵļ���
	private int mPosition = 0;  //�ڼ�ƪ�ʼ�
	private int mNid;
	private int mCursor;
	private String mTitle;
	
	private TextView mNewsDetails;
	
	
	private final int FINISH = 0;
	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.arg1)
			{
			case FINISH:
				// �ѻ�ȡ���ıʼ���ʾ��������
//				String replaycountString = msg.obj.toString().substring(0, 1);
//				newsdetailsTitlebarComm.setText(replaycountString + "����");
//				
//				String contentString = msg.obj.toString().substring(1, msg.obj.toString().length());
				mNewsDetails.setText(Html.fromHtml(msg.obj.toString()));
				break;
			}
		}
	};
	
	private ImageButton mNewsReplyImgBtn;// �������Żظ�ͼƬ
	private LinearLayout mNewsReplyImgLayout;// ��������ͼƬLayout
	private LinearLayout mNewsReplyEditLayout;// ��������Layout
	private TextView mNewsReplyContent;// ���Żظ�����

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newsdetails);
		

		service_name = getResources().getString(R.string.service_name);
		
		
		//��ȡ���ݵ�����
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		// ���ñ���������
		String categoryName = bundle.getString("categoryName");
		TextView titleBarTitle = (TextView) findViewById(R.id.newsdetails_titlebar_title);
		titleBarTitle.setText(categoryName);
		//��ȡ���ż���
	    Serializable s  = bundle.getSerializable("newsDate");
		mNewsData = (ArrayList<HashMap<String, Object>>) s;
		//��ȡ���λ��
		mCursor = mPosition = bundle.getInt("position");
		//��ȡ����ʼǻ�����Ϣ
		HashMap<String, Object> hashMap = mNewsData.get(mPosition);
		
		// ���ұʼ�����ͼƬLayout
		mNewsReplyImgLayout = (LinearLayout) findViewById(R.id.news_reply_img_layout);
		// ���ұʼ�����Layout
		mNewsReplyEditLayout = (LinearLayout) findViewById(R.id.news_reply_edit_layout);
		// �ʼ���������
		mNewsReplyContent = (TextView) findViewById(R.id.news_reply_edittext);
		
		//Ϊ�����л���ť����¼�
		newsDetailsOnClickListener newsDetailsOnClickListener = new newsDetailsOnClickListener();
		
		
		 newsdetailsTitlebarComm = (Button)findViewById(R.id.newsdetails_titlebar_comments);//�鿴���۰�ť
		 newsDetailsTitlebarPre = (Button)findViewById(R.id.newsdetails_titlebar_previous);//ǰһƪ��ť
		 newsDetailsTitlebarNext = (Button)findViewById(R.id.newsdetails_titlebar_next);//��һƪ��ť
		
		
		newsDetailsTitlebarPre.setOnClickListener(newsDetailsOnClickListener);
		newsDetailsTitlebarNext.setOnClickListener(newsDetailsOnClickListener);
		newsdetailsTitlebarComm.setOnClickListener(newsDetailsOnClickListener);
		
		// ��ʼ�������Żظ�ͼƬButton
		mNewsReplyImgBtn = (ImageButton) findViewById(R.id.news_reply_img_btn);
		mNewsReplyImgBtn.setOnClickListener(newsDetailsOnClickListener);
		// д��ظ���ķ���ť
		Button newsReplyPost = (Button) findViewById(R.id.news_reply_post);
		newsReplyPost.setOnClickListener(newsDetailsOnClickListener);
				
				
		//��̬����������ͼ������ֵ
		mNewsBodyInflater = getLayoutInflater();
		inflateView(0);
		
		/*
		 * 	��������ݷŵ�inflateView()��������
		 */
		
		/*
		
		//LayoutInflater mNewsBodyInflater��xml�ļ������������ٷ���View newsBodyLayout����
		View newsBodyLayout = mNewsBodyInflater.inflate(R.layout.news_body, null);
		
		//��View newsBodyLayout������
//		TextView newsTitle = (TextView)newsBodyLayout.findViewById(R.id.news_body_title);
//		newsTitle.setText("���������ſͻ��˽̷̳�����");
//		TextView newsPtimeAndSource = (TextView)newsBodyLayout.findViewById(R.id.news_body_ptime_source);
//		newsPtimeAndSource.setText("��Դ�����Ĺ�����      2015-12-17 10:21:22");
//		TextView newsDetails = (TextView)newsBodyLayout.findViewById(R.id.news_body_details);
//		newsDetails.setText(Html.fromHtml(NEWS));
//		
//		titlesarr[0] = "���������ſͻ��˽̷̳�����";
		
		//�������滻���������ʽ
		//���ű���
		TextView newsTitle = (TextView)newsBodyLayout.findViewById(R.id.news_body_title);
		newsTitle.setText(hashMap.get("newslist_item_title").toString());
		//����ʱ��ͳ���
		TextView newsPtimeAndSource = (TextView)newsBodyLayout.findViewById(R.id.news_body_ptime_source);
		newsPtimeAndSource.setText("����ʱ��:"+hashMap.get("newslist_item_ptime").toString() + "      ������Դ:" + hashMap.get("newslist_item_source").toString());
		//���ű��
		mNid = (Integer)hashMap.get("nid");
		
		//���Żظ���
//		Button newsdetailsTitlebarComm = (Button)findViewById(R.id.newsdetails_titlebar_comments);
		newsdetailsTitlebarComm.setOnClickListener(newsDetailsOnClickListener);
		
		
		newsdetailsTitlebarComm.setText(hashMap.get("newslist_item_comments")+"����");
		//������ϸ��Ϣ
		TextView newsDetails = (TextView)newsBodyLayout.findViewById(R.id.news_body_details);
		newsDetails.setText(Html.fromHtml(getNewsBody()));
		
//		//�����ڶ���view�������л�
//		View newsBodyLayout1 = mNewsBodyInflater.inflate(R.layout.news_body, null);
//		//��View newsBodyLayout������
//		TextView newsTitle1 = (TextView)newsBodyLayout1.findViewById(R.id.news_body_title);
//		newsTitle1.setText("�����µ�������������");
//		TextView newsPtimeAndSource1 = (TextView)newsBodyLayout1.findViewById(R.id.news_body_ptime_source);
//		newsPtimeAndSource1.setText("��Դ�����Ĺ�����      2015-12-17 10:23:22");
//		TextView newsDetails1 = (TextView)newsBodyLayout1.findViewById(R.id.news_body_details);
//		newsDetails1.setText(Html.fromHtml(NEWS));
		
		
		//viewFlipper����ʾ�����������,��ViewFlipper mNewsBodyFlipper�����View newsBodyLayout
		mNewsBodyFlipper = (ViewFlipper)findViewById(R.id.news_body_flipper);
		mNewsBodyFlipper.addView(newsBodyLayout);
//		mNewsBodyFlipper.addView(newsBodyLayout1);
		
		
		
		
		
		newsDetails.setOnTouchListener(new NewsBodyOnTouchListener());
		*/
	}
	
	
	
	/*
	 * ����һ����ť�������ڲ���
	 * */
	class newsDetailsOnClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			//�۽�Ч���ģ����ǵ���ظ���ʱ�򣬵����ظ�������ͬʱ���������뷨
			InputMethodManager m = (InputMethodManager) mNewsReplyContent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			
			switch (v.getId())
			{
			//�����һ����ť�Ļ�
			case R.id.newsdetails_titlebar_previous:
				showPrevious();
				break;
			//�����һ����ť�Ļ�
			case R.id.newsdetails_titlebar_next:
				showNext();
				break;
			//��ʾ����	
			case R.id.newsdetails_titlebar_comments:
				Intent intent = new Intent(NewDetailsActivity.this, CommentsActivity.class);
				intent.putExtra("nid", mNid);
				intent.putExtra("title", mTitle);
				startActivity(intent);
				break;
			// ��ʼ�ʼǻظ�ͼƬ
			case R.id.news_reply_img_btn:
				//����ͼƬ���Ǹ�view�������༭��view
				mNewsReplyImgLayout.setVisibility(View.GONE);
				mNewsReplyEditLayout.setVisibility(View.VISIBLE);
				mNewsReplyContent.requestFocus();
				m.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
				break;
			// ����ظ�
			case R.id.news_reply_post:
				//��post������̼߳��ص����̵߳��У�����ͬ������
				mNewsReplyEditLayout.post(new PostCommentThread());
				//����ͼƬ���Ǹ�view�����ر༭��view
				mNewsReplyImgLayout.setVisibility(View.VISIBLE);
				mNewsReplyEditLayout.setVisibility(View.GONE);
				m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				break;	
				
				
			default:
				break;
			}
			
		}

		

		
	}
	
	/**
	 * ����ظ�
	 * 
	 */
	private class PostCommentThread extends Thread
	{
		@Override
		public void run()
		{
			
			//ע�⣬����������ͨ��post��ʽ��
			SyncHttp syncHttp = new SyncHttp();
//			String url = "http://10.0.2.2:8080/web/postComment";
			String url = "http://"+service_name+"/postComment";
			List<Parameter> params = new ArrayList<Parameter>();
			params.add(new Parameter("nid", mNid + ""));
			params.add(new Parameter("region", "�㶫ʡ������"));
			params.add(new Parameter("content", mNewsReplyContent.getText().toString()));
			try
			{
				String retStr = syncHttp.httpPost(url, params);
				JSONObject jsonObject = new JSONObject(retStr);
				int retCode = jsonObject.getInt("ret");
				if (0 == retCode)
				{
					Toast.makeText(NewDetailsActivity.this, R.string.post_success, Toast.LENGTH_SHORT).show();
					mNewsReplyImgLayout.setVisibility(View.VISIBLE);
					mNewsReplyEditLayout.setVisibility(View.GONE);
					return;
				}

			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			Toast.makeText(NewDetailsActivity.this, R.string.post_failure, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void showPrevious() {
		// TODO Auto-generated method stub
		
		if (mPosition > 0)
		{
			mPosition--;
			//��¼��ǰ���ű�ţ��ظ���ʱ����Ҫ�õ�
			HashMap<String, Object> hashMap = mNewsData.get(mPosition);
			mNid = (Integer)hashMap.get("nid");
			mTitle = (String)hashMap.get("newslist_item_title").toString();
			if (mCursor > mPosition)
			{
				mCursor = mPosition;
				inflateView(0);//��0λ������view
				System.out.println(mNewsBodyFlipper.getChildCount());
				mNewsBodyFlipper.showNext();// ��ʾ��һҳ
			}
			mNewsBodyFlipper.setInAnimation(this, R.anim.push_right_in);// ������һҳ����ʱ�Ķ���
			mNewsBodyFlipper.setOutAnimation(this, R.anim.push_right_out);// ���嵱ǰҳ��ȥ�Ķ���
			mNewsBodyFlipper.showPrevious();// ��ʾ��һҳ
		}
		else
		{
			Toast.makeText(this, R.string.no_pre_news, Toast.LENGTH_SHORT).show();
		}
		System.out.println(mCursor + ";" + mPosition + ";" + mNid);
	}
	
	private void showNext() {
		// TODO Auto-generated method stub
		//�ж��Ƿ������һƪwin��
				if (mPosition<mNewsData.size()-1)
				{
					//��ӻ����Ķ���Ч��
					mNewsBodyFlipper.setInAnimation(this, R.anim.push_left_in);
					mNewsBodyFlipper.setOutAnimation(this, R.anim.push_left_out);
					
					mPosition++;
					HashMap<String, Object> hashMap = mNewsData.get(mPosition);
					mNid = (Integer) hashMap.get("nid");
					mTitle = (String)hashMap.get("newslist_item_title").toString();
					//�ж���һ���Ƿ���Ҫ����
					if (mPosition >= mNewsBodyFlipper.getChildCount())
					{
						inflateView(mNewsBodyFlipper.getChildCount());//���������view
					}
					// ��ʾ��һ��
					mNewsBodyFlipper.showNext();
				}
				else
				{
					Toast.makeText(this, R.string.no_next_news, Toast.LENGTH_SHORT).show();
				}
				System.out.println(mCursor +";"+mPosition);
		
	}
	
	private void inflateView(int index)
	{
		// ��̬����������ͼ������ֵ
		View newsBodyLayout = mNewsBodyInflater.inflate(R.layout.news_body, null);
		// ��ȡ������Ż�����Ϣ
		HashMap<String, Object> hashMap = mNewsData.get(mPosition);
		// ���ű���
		TextView newsTitle = (TextView) newsBodyLayout.findViewById(R.id.news_body_title);
		newsTitle.setText(hashMap.get("newslist_item_title").toString());
		mTitle = (String)hashMap.get("newslist_item_title").toString();
		// ����ʱ��ͳ���
		TextView newsPtimeAndSource = (TextView) newsBodyLayout.findViewById(R.id.news_body_ptime_source);
		newsPtimeAndSource.setText("����ʱ��:"+hashMap.get("newslist_item_ptime").toString() + "      ������Դ:" + hashMap.get("newslist_item_source").toString());
		// ���ű��
		mNid = (Integer) hashMap.get("nid");
		// ���Żظ���
		
		
		newsdetailsTitlebarComm.setText(hashMap.get("newslist_item_comments") + "����");
	
		// ��������ͼ��ӵ�Flipper��
		mNewsBodyFlipper = (ViewFlipper) findViewById(R.id.news_body_flipper);
		mNewsBodyFlipper.addView(newsBodyLayout,index);
	
		// ������Body��Ӵ����¼�
		mNewsDetails = (TextView) newsBodyLayout.findViewById(R.id.news_body_details);
		mNewsDetails.setOnTouchListener(new NewsBodyOnTouchListener());
	
		// �����̣߳��������ŵ����岿��
		new UpdateNewsThread().start();
	}
	
	private class UpdateNewsThread extends Thread
	{
		@Override
		public void run()
		{
			// �������ϻ�ȡ����
			String newsBody = getNewsBody();
			
			//������Ϣ��handle�У����ؽ������UI
			Message msg = mHandler.obtainMessage();
			msg.arg1 = FINISH;
			msg.obj = newsBody;
			mHandler.sendMessage(msg);
		}
	}
	
	
//	public final String NEWS = "<p>��������ȫ��ͣ���״γ���</p>\r\n<p>����������ʾ�����½���Ʒסլ���棬2012��1��ȫ��70�����г��У��۸񻷱��½��ĳ�����48������ƽ�ĳ�����22����û��һ�����г������ǡ��ӷ��ۼ۸�ָ�������������״γ������½���Ʒסլ����ȫ��ͣ�ǵ�����</p>\r\n<p>������ͬ�ȿ���70�����г����У��۸��½��ĳ�����15������ȥ��12�·�����6����1�·ݣ�ͬ���Ƿ�����ĳ�����50�����Ƿ���δ����3.9%��</p>\r\n<p>��������סլ</p>\r\n<p>��������5�����л�������</p>\r\n<p>�����Ӷ��ַ���,��������ȣ�70�����г����У��۸��½��ĳ�����54������ƽ�ĳ�����11������ȥ��12�·���ȣ�1�·ݻ��ȼ۸��½��ĳ���������3�����������ǵĽ�5�����У��ֱ�Ϊ�������������������عء����壬��������0.1%��ͬ�ȿ���70�����г����У��۸��½��ĳ�����37������ȥ��12�·�������8����1�·ݣ�ͬ���Ƿ�����ĳ�����29�����Ƿ���δ����3.5%��</p>\r\n<p>�����������</p>\r\n<p>�������ַ�����ͬ�Ⱦ��µ�</p>\r\n<p>����������ʾ���������ַ��۸񲻹��ǻ��Ȼ���ͬ�ȣ������µ������µ����Ⱦ������Ӵ�</p>\r\n<p>�����ڻ��ȷ��棬��ȥ��8�·ݳ���ͣ�Ǻ󣬶��ַ��۸񻷱ȿ�ʼ�µ����Ҵ˺�ÿ���µ��µ������ڲ��ϼӴ󣬵�2012��1�·ݣ��价���µ������Ѵﵽ0.9%������ͬ�ȷ��棬2012��1�·��µ�3.1%��������������</p>\r\n<p>���������½�סլ�۸�ָ��2010��5��ʱͬ���Ƿ�Ϊ22%��������2012��1��ͬ���Ƿ���Ϊ0.1%���½���Ʒ���۸�ָ��ͬ���Ƿ�Ҳ�������µ����½�����Ҳ�Ƚϴ�ȥ�����ͬ���Ƿ�Ϊ1.3%�������˽���1�·ݣ���ͬ���Ƿ��µ�Ϊ0.1%���ڻ��ȷ��棬��2011��10���״γ����µ��󣬻��ȼ����µ�Ϊ0.1%��</p>\r\n<p>������ ����</p>\r\n<p>�����������۵��س�Ч����</p>\r\n<p>����������ԭ�ز��г��ܼ��Ŵ�ΰ��Ϊ��������Ϊ�޹�ִ�����ϸ�ĳ��У����۵����Ѿ��������Գ�Ч���޹����µ�ֱ��������٣��޹�����Ͷ�ʡ�Ͷ�����ڱ�����̨�����ϸ���޹��£�5����ػ����޹�����ʹ�ù����߻ع���ס����������ռ��9�ɣ���ס����ռ��9�ɣ�ȫ�����һ��ɽ�����Ͷ�ʼ�Ͷ������������</p>\r\n<p>����ͬʱ������˫�����ļӾ磬�޹��޴�ʹ�ù������ڴ��۸��µ�������Ͷ���ֶ��ѷ���ͨ��������Ȼ���谭�۸��»��Ĺؼ����ء��ر��ǳ������ֶ��ַ�������Ȼ��ͦ�۸�ϧ�ۣ���ʹ���ڼ۸���Ȼ�Ӹ߲��µ�����£����������н�����</p>\r\n<p>������ Ԥ��</p>\r\n<p>�������귿�ۻ��µ�10%-20%</p>\r\n<p>�����Ŵ�ΰ��Ϊ��2012���޹����߲�����ɣ�һ�߳���¥�йյ��Ѿ���ȷ��Ԥ����6-12�����ڷ��ۿ��ܻ���10%-20%���µ�������һ�߳��ж�ȫ����ʾ�����÷ǳ��󣬲���������ִ�������ϣ��ڷ����µ�������Ҳ������Ӱ��ȫ����</p>\r\n<p>�������ҵز��г��о�����������Ϊ����¥�е���Ч���������̵ı����£����������Ի������1�·�ȫ����������·��г��ɽ�����ȵף�������������Ũ�أ�Ϊ�������ۻؿ�������п����̼��뽵�ۻ�����Ӫ���·��۸�Ԥ�ڻ��һ���µ���</p>";

	/**
	 * ��ȡ������ϸ��Ϣ
	 * @return
	 */
	private String getNewsBody()
	{
		String retStr = "��������ʧ�ܣ����Ժ�����";
		SyncHttp syncHttp = new SyncHttp();
//		String url = "http://10.0.2.2:8080/web/getNews";
		String url = "http://"+service_name+"/getNews";
		String params = "nid=" + mNid;
		try
		{
			String retString = syncHttp.httpGet(url, params);
			JSONObject jsonObject = new JSONObject(retString);
			//��ȡ�����룬0��ʾ�ɹ�
			int retCode = jsonObject.getInt("ret");
			if (0 == retCode)
			{
				JSONObject dataObject = jsonObject.getJSONObject("data");
				JSONObject newsObject = dataObject.getJSONObject("news");
				retStr = newsObject.getString("body");
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return retStr;
	}
	
	/**
	 * ����һ�������������ڲ���
	 */
	class NewsBodyOnTouchListener implements OnTouchListener
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			switch (event.getAction())
			{
			//��ָ����
			case MotionEvent.ACTION_DOWN:
				//��¼��ʼ����
				mNewsReplyImgLayout.setVisibility(View.VISIBLE);
				mNewsReplyEditLayout.setVisibility(View.GONE);
//				InputMethodManager m = (InputMethodManager) mNewsReplyContent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//				m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				
//				InputMethodManager m = (InputMethodManager) mNewsReplyContent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//				m.toggleSoftInput(0, InputMethodManager.RESULT_UNCHANGED_HIDDEN);
				
				mStartX = event.getX();
				break;
			//��ָ̧��
			case MotionEvent.ACTION_UP:
				//���󻬶�
				if (event.getX() < mStartX)
				{
					showPrevious();
				}
				//���һ���
				else if (event.getX() > mStartX)
				{
					showNext();
				}
				break;
			}
			return true;
		}
	}
	
	

}
