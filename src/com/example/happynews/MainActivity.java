package com.example.happynews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.happynews.R.string;
import com.example.happynews.custom.*;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.happynews.tools.*;
import com.example.happynews.*;
import com.example.happynews.httpservices.*;


public class MainActivity extends Activity 
{
	private final int COLUMNWIDTHPX = 600;//在模拟器上为55，在手机上为200，若英文分类则为600
	private final int FLINGVELOCITYPX = 4000; //滚动距离
	private int mColumnWidthDip;
	private int mFlingVelocityDip;
	
	private final int NEWSCOUNT = 5; //返回新闻数目
	private final int SUCCESS = 0;//加载成功
	private final int NONEWS = 1;//该栏目下没有新闻
	private final int NOMORENEWS = 2;//该栏目下没有更多新闻
	private final int LOADERROR = 3;//加载失败
	
	private int mCid;
	private ArrayList<HashMap<String, Object>> mNewsData;
	private SimpleAdapter mNewsListAdapter;
	private ListView mNewsList;
	private LayoutInflater mInflater;
	private Button mTitlebarRefresh;
	private ProgressBar mLoadnewsProgress;
	private Button mLoadMoreBtn;
	private Button mGoDetailBtn;
	
	private String mCatName;
	private String service_name;
	
	private LoadNewsAsyncTask loadNewsAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
       
      
      //把px转换成dip
        mColumnWidthDip=DensityUtil.px2dip(this, COLUMNWIDTHPX);
        mFlingVelocityDip=DensityUtil.px2dip(this, FLINGVELOCITYPX);
        
        mNewsData = new ArrayList<HashMap<String,Object>>();  //记得初始化
        mInflater = getLayoutInflater();
        mNewsList=(ListView)findViewById(R.id.news_list);
        mTitlebarRefresh = (Button)findViewById(R.id.titlebar_refresh);//右上角更新按钮
        mLoadnewsProgress = (ProgressBar)findViewById(R.id.loadnews_progress);//进度条
		mTitlebarRefresh.setOnClickListener(loadMoreListener);
		
		service_name = getResources().getString(R.string.service_name);
		
		
		
		
		
		
	    /**①新闻分类放进categoryAdapter*******/
	    
		//获取笔记分类
	  		//String[] categoryArray = getResources().getStringArray(R.array.categories);
			String[] categoryArray = getCategories();
	  	//把笔记分类保存到List中
	  		final List<HashMap<String, Category>> categories = new ArrayList<HashMap<String, Category>>();
	  		//分隔笔记类型字符串
	  		for(int i = 0; i < categoryArray.length; i++)
	  		{
	  			String[] temp = categoryArray[i].split("[|]");
	  			if (temp.length == 2)
	  			{
	  				int cid = StringUtil.String2Int(temp[0]);
	  				if (i == 0) {
	  					//默认选中的新闻分类
	  					mCid = cid;
					}
	  				String title = temp[1];
	  				Category type = new Category(cid, title);
	  				HashMap<String, Category> hashMap = new HashMap<String, Category>();
	  				hashMap.put("category_title", type);
	  				categories.add(hashMap);
	  			}
	  		}
	  		
	  		//获取ArrayList<HashMap<String, Category>>中，第0个元素中的title
	  		mCatName = categories.get(0).get("category_title").getTitle();
	            
	      		
	     
	      	//创建Adapter，指明映射字段
	      	CustomSimpleAdapter categoryAdapter = new CustomSimpleAdapter(this, categories, 
	    			                                          R.layout.category_title, 
	    			                                          new String[]{"category_title"}, //文字来源，这个是对应上面的hashMap的category_title
	    			                                          new int[]{R.id.category_title});//文字去处，这里是填xml文件中的某个item的ID，这个ID
	      	                                                                                  //是layout文件夹下的category_title.xml文件中的需要放字体进去的textview的ID
	      		
	      	/**②创建GridView，并将categoryAdapter放进去********/
	        GridView categoryGridView=new GridView(this);
	        categoryGridView.setColumnWidth(mColumnWidthDip);//每个单元格宽度
	        categoryGridView.setNumColumns(GridView.AUTO_FIT);//单元格数目
	        categoryGridView.setGravity(Gravity.CENTER);//设置对其方式
	        categoryGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));//设置单元格选择时背景色为透明
	      	int width = mColumnWidthDip * categories.size();//根据单元格宽度和数目计算总宽度
	      	LayoutParams params = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
			//更新categoryGridView宽度和高度，使categoryGridView在一行显示
	      	categoryGridView.setLayoutParams(params);
			//设置适配器
	      	categoryGridView.setAdapter(categoryAdapter);
			//把category加入到容器中
	      	
	      	/**③将GridView放进去xml文件中***********/
	      	RelativeLayout categoryList=(RelativeLayout)findViewById(R.id.category_layout);
	      	categoryList.addView(categoryGridView);
	      	
	      	//箭头的作用，点击可以使水平滑动条滑动
	      	final HorizontalScrollView categoryScrollview 
	      	= (HorizontalScrollView) findViewById(R.id.category_scrollview);
			Button categoryArrowRight 
			=(Button)findViewById(R.id.category_arrow_right);
			categoryArrowRight.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					categoryScrollview.fling(mFlingVelocityDip);
				}
			});
			
		
		//点击那些分类信息后的回调函数
		categoryGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)                 //arg0是装载每一个item的丫
			{
				TextView categoryTitle;
				//恢复每个单元格背景色，将其附加的背景去掉，颜色恢复为原来的
				for (int i = 0; i < arg0.getChildCount(); i++)
				{
					categoryTitle = (TextView) (arg0.getChildAt(i));
					categoryTitle.setBackgroundDrawable(null);//背景设置为无
					categoryTitle.setTextColor(0XFF707070);//在category_title文件中的textcolor中可以见到这个没有被按下的时候的颜色
				}                                          //注意这个函数一定要这样写颜色，一定要多加前面的ff
				// TODO Auto-generated method stub
				TextView categoryTextView=(TextView)arg1;
				categoryTextView.setTextColor(0xffffffff);
				categoryTextView.setBackgroundColor(R.drawable.categorybar_item_background);
				
				//获取选中的笔记分类id
				mCid = categories.get(arg2).get("category_title").getCid();
				//获取选中的笔记分类名称
				mCatName = categories.get(arg2).get("category_title").getTitle();
				//获取该栏目下笔记
				//getSpeCateNews(mCid, mNewsData, 0, true);
				//通知ListView进行更新
				//mNewsListAdapter.notifyDataSetChanged();
				//现在凡是加载笔记的均采用异步方式后台运行
				loadNewsAsyncTask = new LoadNewsAsyncTask();
				loadNewsAsyncTask.execute(mCid, 0, true);
                  				
			}
		});
		
		//填充listview
		
		//List<HashMap<String, String>> newsData = new ArrayList<HashMap<String,String>>();
//		List<HashMap<String, Object>> newsData = getSpeCateNews(1);
		

		
		
//		for(int i=0;i<10;i++)
//		{
//			HashMap<String, String> hashMap = new HashMap<String, String>();
//			hashMap.put("newslist_item_title","正能量新闻客户端发布啦" );
//			hashMap.put("newslist_item_digest","正能量新闻客户端正式开始发布啦" );
//			hashMap.put("newslist_item_source", "来源：开心每一天");
//			hashMap.put("newslist_item_ptime", "2015-03-24 14:28:22");
//			newsData.add(hashMap);
//		}
		
		mNewsListAdapter = new SimpleAdapter(
				this, 
				mNewsData, 
				R.layout.newslist_item, 
				new String[]{"newslist_item_title", "newslist_item_digest", "newslist_item_source", "newslist_item_ptime"},
                //文字来源，这个是对应上面的hashMap的newslist_item_title等等
                new int[]{R.id.newslist_item_title, R.id.newslist_item_digest, R.id.newslist_item_source, R.id.newslist_item_ptime}
				//文字去处，这里是填xml文件中的某个item的ID，这个ID,是layout文件中需要放字体进去的textview的ID
			);
                
		
		
		
		
		
		//增加下拉到底部加载效果，//类似于讲述那个升级UI那一节一样，先view与xml关联，再加进去组件当中
		View loadMoreLayout = mInflater.inflate(R.layout.loadmore, null);
		mNewsList.addFooterView(loadMoreLayout);
		
		mNewsList.setAdapter(mNewsListAdapter);
		
		//必须先加载loadmore.xml进来后才能执行下面的代码
		mLoadMoreBtn = (Button)findViewById(R.id.loadmore_btn);
		mLoadMoreBtn.setOnClickListener(loadMoreListener);
		
		mNewsList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Intent intent = new Intent(MainActivity.this, NewDetailsActivity.class);
				//把需要的信息放到Intent中
				
				intent.putExtra("newsDate", mNewsData); //为了不报错，需要将mNewsData的类型改为arraylist
				intent.putExtra("position", position);
				intent.putExtra("categoryName", mCatName);
				startActivity(intent);
			}
		});
		
		
		
		
		
		
		//必须放在比较后面，可能也是因为未加载loadmore.xml的原因吧
		//获取指定栏目的笔记列表
		//getSpeCateNews(mCid,mNewsData,0,true);
		//现在凡是加载新闻的均采用异步
		loadNewsAsyncTask = new LoadNewsAsyncTask(); //注意每执行一次都需要new一次   	   
		loadNewsAsyncTask.execute(mCid, 0, true);  
    }
    
    /**
	 * 获取指定新闻类型的新闻列表
	 * @param cid 类型的ID
	 * @param newsList 保存新闻信息的集合
	 * @param startnid 分页，开始的id
	 * @param firstTimes	是否第一次加载
	 */
	private int getSpeCateNews(int cid, List<HashMap<String, Object>> newsList, int startnid, Boolean firstTimes)
	{
		if (firstTimes)
		{
			//如果是第一次，则清空集合里数据
			newsList.clear();
		}
		
		//新建http实例，传入url与参数
//		String url = "http://119.29.171.68/getSpecifyCategoryNews";
		String url = "http://"+service_name+"/getSpecifyCategoryNews";
		String params = "starttid="+startnid+"&count="+NEWSCOUNT+"&cid="+cid;
		SyncHttp syncHttp = new SyncHttp();
		try
		{
			//以get方式去请求
			String retStr = syncHttp.httpGet(url, params);
			System.out.println("##"+retStr);
			
			JSONObject jsonObject = new JSONObject(retStr);
			//解释json格式，获取返回码，0表示成功获取
			int retCode = jsonObject.getInt("ret");
			if (0 == retCode)
			{
				//在jsonObject中获取里面的json格式的对象——data
				JSONObject dataObject = jsonObject.getJSONObject("data");
				//获取返回的总条数
				int totalnum = dataObject.getInt("totalnum");
				if (totalnum>0)
				{
					//获取新闻集，是jsonObject中获取里面的json格式的数组——newslist
					JSONArray newslist = dataObject.getJSONArray("newslist");
					for(int i = 0; i < newslist.length(); i++)
					{
						//新闻数组newslist里面的是json格式的对象，放到哈希表中
						JSONObject newsObject = (JSONObject)newslist.opt(i); 
						HashMap<String, Object> hashMap = new HashMap<String, Object>();
						hashMap.put("nid", newsObject.getInt("nid"));
						hashMap.put("newslist_item_title", newsObject.getString("title")); //标题
						hashMap.put("newslist_item_digest", newsObject.getString("digest"));//摘要
						hashMap.put("newslist_item_source", newsObject.getString("source"));//来源
						hashMap.put("newslist_item_ptime", newsObject.getString("ptime"));//时间
						hashMap.put("newslist_item_comments", newsObject.getString("commentcount"));//跟帖数量
						newsList.add(hashMap);
					}
					return SUCCESS;
				}
				else
				{
					if (firstTimes)
					{

//						Toast.makeText(MainActivity.this, "该栏目下暂时没有新闻", Toast.LENGTH_LONG).show();
						return NONEWS;
					}
					else
					{

//						Toast.makeText(MainActivity.this, "该栏目下暂时没有更多新闻", Toast.LENGTH_LONG).show();
						return NOMORENEWS;
					}
					
				}
			}
			else
			{
//				Toast.makeText(MainActivity.this, "获取新闻列表失败", Toast.LENGTH_LONG).show();
				return LOADERROR;
			}
		} catch (Exception e)
		{
			
			e.printStackTrace();
//			Toast.makeText(MainActivity.this, "获取新闻列表失败", Toast.LENGTH_LONG).show();
			return LOADERROR;
		}
//		return newsList;
	}

    /**
	 * 获取笔记分类
	 */
	private String[] getCategories()
	{

		
		//新建http实例，传入url与参数
//		String url = "http://119.29.171.68/getCategories";
		String url = "http://"+service_name+"/getCategories";
		String params = null;
		SyncHttp syncHttp = new SyncHttp();

		try
		{
			//以get方式去请求
			String retStr = syncHttp.httpGet(url, params);
			System.out.println("##"+retStr);
			
			JSONObject jsonObject = new JSONObject(retStr);
			//解释json格式，获取返回码，0表示成功获取
			int retCode = jsonObject.getInt("ret");
			if (0 == retCode)
			{
				//在jsonObject中获取里面的json格式的对象——data
				JSONObject dataObject = jsonObject.getJSONObject("data");
				//获取返回的总条数
				int totalnum = dataObject.getInt("totalnum");
				if (totalnum > 0)
				{
					String[] result = new String[totalnum];
					//获取分类集，是jsonObject中获取里面的json格式的数组——info
					JSONArray info = dataObject.getJSONArray("info");
					for(int i = 0; i < info.length(); i++)
					{
						//分类数组newslist里面的是json格式的对象，放到string[]中
						JSONObject infoObject = (JSONObject)info.opt(i); 
						String temp = infoObject.getInt("cid") + "|" + infoObject.getString("title");

						result[i] = temp;
					}
					return result;
				}
			}

		} catch (Exception e)
		{
			
			e.printStackTrace();
		}
		return null;
	}	
	
	
	private OnClickListener loadMoreListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			loadNewsAsyncTask = new LoadNewsAsyncTask();
			switch (v.getId())
			{
			//加载更多按钮，在页面的底部
			case R.id.loadmore_btn:
				//获取该栏目下新闻
				//getSpeCateNews(mCid, mNewsData, mNewsData.size(), false);
				//通知ListView进行更新
				//mNewsListAdapter.notifyDataSetChanged();
				loadNewsAsyncTask.execute(mCid, mNewsData.size(), false);
				break;
				//更新按钮，在右上角
			case R.id.titlebar_refresh:
				loadNewsAsyncTask.execute(mCid, 0, true);
				
				break;
			}
		}
	};
	
	
	//开启异步任务
	private class LoadNewsAsyncTask extends AsyncTask<Object, Integer, Integer>
	{
		//准备运行函数
		@Override
		protected void onPreExecute()
		{
			//隐藏刷新按钮
			mTitlebarRefresh.setVisibility(View.GONE);
			//显示进度条
			mLoadnewsProgress.setVisibility(View.VISIBLE); 
			//设置LoadMore Button 显示文本
			mLoadMoreBtn.setText(R.string.loadmore_txt);
		}

		//正在后台运行函数，onPreExecute执行完毕之后立即调用，通常完成耗时的事件，结果返回到onPostExecute当中。另一种使用方法见笔记，通常用作更新进度条的。
		@Override
		protected Integer doInBackground(Object... params)
		{
			//parameter  0：笔记类型；1：查询的起始id；2：是否第一次加载
			return getSpeCateNews((Integer)params[0], mNewsData, (Integer)params[1], (Boolean)params[2]);
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			//根据返回值显示相关的Toast
			switch (result)
			{
				case NONEWS:
					Toast.makeText(MainActivity.this, R.string.no_news, Toast.LENGTH_LONG).show();
				break;
				case NOMORENEWS:
					Toast.makeText(MainActivity.this, R.string.no_more_news, Toast.LENGTH_LONG).show();
					break;
				case LOADERROR:
					Toast.makeText(MainActivity.this, R.string.load_news_failure, Toast.LENGTH_LONG).show();
					break;	
			}
			
			//更新listview
			mNewsListAdapter.notifyDataSetChanged();
			
			//下面的操作与onPreExecute的相反
			//显示刷新按钮
			mTitlebarRefresh.setVisibility(View.VISIBLE);
			//隐藏进度条
			mLoadnewsProgress.setVisibility(View.GONE); 
			//设置LoadMore Button 显示文本
			mLoadMoreBtn.setText(R.string.loadmore_btn);
		}
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
