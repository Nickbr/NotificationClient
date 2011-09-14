package com.cog.lab;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NotificationClientActivity extends ListActivity {
	private ProgressDialog m_ProgressDialog = null; 
	private ArrayList<Order> m_orders = null;
	private OrderAdapter m_adapter;
	private Runnable viewOrders;
	private UITimer timer;
	public Handler handler = new Handler();

	private static AmazonSQS simpleQueue = null;
	private static List<com.amazonaws.services.sqs.model.Message> lastRecievedMessages = null;
	public static final String QUEUE_URL = "https://queue.amazonaws.com/484583698755/testqueue"; 
	public static final String MESSAGE_INDEX = "_message_index";
	public static final String MESSAGE_ID = "_message_id";
	public static BasicAWSCredentials credentials = null;	
	private boolean credentials_found;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try
    	{   
    		Log.v("onCreate", "into");
	        super.onCreate(savedInstanceState);
	        Log.v("onCreate", "called base");
	        setContentView(R.layout.main);
	        Log.v("onCreate", "set content view from R.layout.main");
	        startGetCredentials();
	        Log.v("onCreate", "got credentials");
	        initialise();
	        Log.v("onCreate", "initialised");
    		Log.v("onCreate", "out");
    	}
    	catch( Exception ex)
    	{
    		String err = (ex.getMessage()==null)?"error in onCreate":ex.getMessage();
    		Log.e("onCreate:",err);  
    		ex.printStackTrace();
    	}
    }
    
    public void initialise()
    {
    	try
    	{
	        Log.v("initialise", "into");
	        m_orders = new ArrayList<Order>();
	        Log.v("initialise", "initialised m_orders");
	        
	        this.m_adapter = new OrderAdapter(this, R.layout.row, m_orders);
	        Log.v("initialise", "initialised M_adapter")
	        ;
	        setListAdapter(this.m_adapter);
	        Log.v("initialise", "called setListAdapter");
	       
	        viewOrders = new Runnable(){
	            @Override
	            public void run() {
	                getOrders();
	            }
	        };    
	        
	        timer = new UITimer(handler, viewOrders, 10000);       
	        timer.start();
		    Thread thread =  new Thread(null, viewOrders, "MagentoBackground");
	        Log.v("initialise", "initialised MagentoBackground thread");
		       
	        thread.start();
		    Log.v("initialise", "started MagentoBackground thread");
		        
		    m_ProgressDialog = ProgressDialog.show(NotificationClientActivity.this, "Please wait...", "Retrieving data ...", true);
		    Log.v("initialise", "show ProgressDialog");
		    
	        Log.v("initialise", "out");
    	}
    	catch( Exception ex)
    	{
    		String err = (ex.getMessage()==null)?"error in initialise":ex.getMessage();
    		Log.e("initialise:",err);  
    		ex.printStackTrace();
    	}
    }
    
    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
        }
      };

  	@SuppressWarnings("unchecked")
    private void getOrders(){
        try
        {
        	
	        Log.v("getOrders", "into");
        	ReceiveMessageRequest req = new ReceiveMessageRequest(QUEUE_URL);
	        Log.v("getOrders", "initialised req");

    		String creds = (credentials.getAWSAccessKeyId()==null)?"credentials":credentials.getAWSAccessKeyId();
    		Log.v("getOrders:",creds);  

			req.setRequestCredentials(credentials);
	        Log.v("getOrders", "res.setrRequestCredentials");
	        
			lastRecievedMessages =  getInstance().receiveMessage(req).getMessages();
    		Log.v("getOrders:", "lastRecievedMessages.size:" + Integer.toString(lastRecievedMessages.size())); 
    		
			for(com.amazonaws.services.sqs.model.Message m : lastRecievedMessages){  

				Order mb = null;
		        Log.v("getOrders", "initialised req");

				GsonBuilder gsonb = new GsonBuilder();
		        Log.v("getOrders", "initialised gsonb");
		        
				Gson gson = gsonb.create();
		        Log.v("getOrders", "created gson");
		        
				String jsonData = jsonEscape(m.getBody());
		        Log.v("getOrders", "jsonData: " + jsonData);
				 
				try
				{
				    mb = gson.fromJson(jsonData, Order.class);
			        Log.v("getOrders", "mb created");
		    	}
		    	catch( Exception ex)
		    	{
		    		String err = (ex.getMessage()==null)?"error in getOrders.setMb":ex.getMessage();
		    		Log.e("getOrders.setMb:",err);
		    		ex.printStackTrace();  
		    	}
				
				m_orders.add(mb);
		        Log.v("getOrders", "mb added to m_orders");
	    		Log.v("getOrders:", "m_orders.size in loop:" + Integer.toString(m_orders.size())); 
		        
				deleteMessageFromQueue(m.getReceiptHandle());
		        Log.v("getOrders", "message deleted from queue");
			}

    		Log.v("getOrders:", "m_orders.size on exit:" + Integer.toString(m_orders.size())); 
    		
	        Log.v("getOrders", "out");
    	}
    	catch( Exception ex)
    	{
    		String err = (ex.getMessage()==null)?"error in getOrders":ex.getMessage();
    		Log.e("getOrders:",err);  
    		ex.printStackTrace();
    	}
          runOnUiThread(returnRes);
      }
    
    public void deleteMessageFromQueue(String recieptHandle)
    {
    	try
    	{
	        Log.v("deleteMessageFromQueue", "into");
	        Log.v("deleteMessageFromQueue", "recieptHandle:" + recieptHandle);
	        
			DeleteMessageRequest dmr = new DeleteMessageRequest();
	        Log.v("deleteMessageFromQueue", "initialised dmr");
	        
			dmr.setReceiptHandle(recieptHandle);
	        Log.v("deleteMessageFromQueue", "dmr.setReceiptHandle");
	        
			dmr.setQueueUrl(QUEUE_URL);
	        Log.v("deleteMessageFromQueue", "dmr.setQueueUrl");
	        
			dmr.setRequestCredentials(credentials);
	        Log.v("deleteMessageFromQueue", "dmr.setRequestCredentials");
	        Log.v("deleteMessageFromQueue", "dmr.setRequestCredentials:" + credentials.getAWSAccessKeyId());
			getInstance().deleteMessage(dmr);

	        Log.v("deleteMessageFromQueue", "out");
    	}
    	catch( Exception ex)
    	{
    		String err = (ex.getMessage()==null)?"error in deleteMessageFromQueue":ex.getMessage();
    		Log.e("deleteMessageFromQueue:",err);  
    		ex.printStackTrace();
    	}
    	
    }
	

    public static String testJsonString(){
    	String x = "{  \"Type\" : \"Notification\",  \"MessageId\" : \"24964a7a-b92a-4b90-9f9b-ed6a80241eee\",  \"TopicArn\" : \"arn:aws:sns:us-east-1:484583698755:TestTopic\",  \"Subject\" : \"Test JSON\",  \"Message\" : \"Known good format not from aws\",  \"Timestamp\" : \"2011-09-07T23:17:53.127Z\",  \"SignatureVersion\" : \"1\",  \"Signature\" : \"e1dCO8WtVnYC+P26erAhhZrS1CVDx1sPi0NikDOIA02mRKpEjuKdxXnOKXsoILeE5U8IgFTOYJxngxFafxQkWScz6c8QAFJpQkcCNBsSI4gpnNrUCpoaLjiaPtjoQhRhGcG3PAT6zj+fO1oh/XZoRqoxAjS8F5xO9ZK9sZhgYp0=\",  \"SigningCertURL\" : \"https://sns.us-east-1.amazonaws.com/SimpleNotificationService-f3ecfb7224c7233fe7bb5f59f96de52f.pem\",  \"UnsubscribeURL\" : \"https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:484583698755:TestTopic:0c97fbe6-e486-445d-8606-e866ff43cb78\"}";
        return x;
    }
	
	public static String jsonEscape(String str)  {
	    String intString = str.replace("\n", "").replace("\r", "").replace("\t", "");
	    String intString4 = intString + "&SubscriptionArn=arn:aws:sns:us-east-1:484583698755:TestTopic:0c97fbe6-e486-445d-8606-e866ff43cb78" + "\"" + "}";
	    return intString4;
	}

	public static AmazonSQS getInstance() {
        if ( simpleQueue == null ) {
		    simpleQueue = new AmazonSQSClient(credentials);
        }
        return simpleQueue;
	}

    private void startGetCredentials() {
    	Thread t = new Thread() {
    		@Override
    		public void run(){
    	        try {            
    	            Properties properties = new Properties();
    	            properties.load( getClass().getResourceAsStream( "AwsCredentials.properties" ) );
    	            
    	            String accessKeyId = properties.getProperty( "accessKey" );
    	            String secretKey = properties.getProperty( "secretKey" );
    	            
    	            if ( ( accessKeyId == null ) || ( accessKeyId.equals( "" ) ) ||
    	            	 ( accessKeyId.equals( "CHANGEME" ) ) ||( secretKey == null )   || 
    	                 ( secretKey.equals( "" ) ) || ( secretKey.equals( "CHANGEME" ) ) ) {
    	                Log.e( "AWS", "Aws Credentials not configured correctly." );                                    
        	            credentials_found = false;
    	            } else {
    	            credentials = new BasicAWSCredentials( properties.getProperty( "accessKey" ), properties.getProperty( "secretKey" ) );
        	        credentials_found = true;
    	            }

    	        }
    	        catch ( Exception exception ) {
    	            Log.e( "Loading AWS Credentials", exception.getMessage() );
    	            credentials_found = false;
    	        }
    			//HelloWorldActivity.this.mHandler.post(postResults);
    		}
    	};
    	t.start();
    }
    
    protected void displayCredentialsIssueAndExit() {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        confirm.setTitle("Credential Problem!");
        confirm.setMessage( "AWS Credentials not configured correctly.  Please review the README file." );
        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int which ) {
                	NotificationClientActivity.this.finish();
                }
        } );
        confirm.show().show();                
    }
}